(function () {
    // Model
    var DumbDog = {
        auth: {
            user: null,
            username: null,
            setUsername: function(username) {
                DumbDog.auth.username = username;
            },
            isLoggedIn: function() {
                return this.user != null;
            },
            login: function() {
                m.request({
                    method: "POST",
                    url: "/api/login",
                    data: {
                        username: DumbDog.auth.username
                    }
                }).then(res => {
                    this.user = {
                        id: res.id,
                        username: res.username
                    };

                    m.route.set("/lobby");
                }).catch(err => {
                    console.error(err)
                });
            },
            doesOwnCurrentRoom: function() {
                if (DumbDog.rooms.room != null) {
                    return DumbDog.rooms.room.owner.id === this.user.id;
                }

                return false;
            },
            checkState: function() {
                if (this.isLoggedIn())
                    return Promise.resolve(true);

                return m.request({
                    method: "GET",
                    url: "/api/users/@me"
                }).then(user => {
                    this.user = user;
                }).catch(err => {
                    m.route.set("/splash")
                });
            }
        },
        rooms: {
            room: null,
            roomName: null,
            setName: function(name) {
                DumbDog.rooms.roomName = name;
            },
            canJoin: function() {
                return this.roomName != null;
            },
            join: function() {
                if (!DumbDog.rooms.canJoin())
                    return;

                m.request({
                    method: "GET",
                    url: "/api/room/" + DumbDog.rooms.roomName
                }).then(room => {
                    this.room = room;

                    m.route.set("/room/:id", { id: room.id });
                }).catch(err => {
                    console.error(err)
                });
            },
            leave: function() {
                DumbDog.socket.send("LEAVE_ROOM");

                m.route.set("/lobby");
            },
            create: function() {
                m.request({
                    method: "POST",
                    url: "/api/rooms/create"
                }).then(room => {
                    this.room = room;

                    m.route.set("/room/:id", { id: room.id });
                })
            },
            startGame: function () {
                if (DumbDog.auth.doesOwnCurrentRoom()) {
                    DumbDog.socket.send("START_GAME")
                }
            },
            skip: function() {
                if (DumbDog.auth.doesOwnCurrentRoom()) {
                    DumbDog.socket.send("SKIP_ROUND")
                }
            },
            getPlayers: function() {
                if (this.room == null) return [];

                return this.room.players;
            }
        },
        socket: {
            ws: null,
            connected: false,
            state: "login",
            isConnected: function() {
                return this.ws != null && this.connected;
            },
            connect: function() {
                this.ws = new WebSocket("ws://" + window.location.host);

                this.ws.onmessage = (d) => {
                    var packet = JSON.parse(d.data);

                    console.log(packet);

                    if (packet.t === "CHANGE_STATE") {
                        this.state = packet.d.newState;
                    } else {
                        var handler = this.handlers[packet.t];

                        if (handler != null)
                            handler(packet.d);
                    }
                };

                this.ws.onclose = (frame) => {
                    console.log("Socket closed");
                    console.log(frame);

                    this.ws = null;
                    this.connected = false;
                };

                return new Promise((resolve, reject) => {
                    this.ws.onopen = () => {
                        this.connected = true;
                        resolve();
                    };

                    this.ws.onerror = (err) => {
                        reject(err);
                    };
                });
            },
            send: function(type, packet) {
                var data = JSON.stringify({ t: type, d: packet || {} });

                this.ws.send(data);
            },
            handlers: {
                "ROOM_UPDATE": (pkt) => {
                    DumbDog.rooms.room = pkt.room;

                    m.redraw(); // TODO: this shouldn't really be in model
                },
                "NEW_ROUND": (round) => {
                    DumbDog.round.current = round;
                    DumbDog.round.postRoundInfo = null;

                    m.redraw();
                },
                "END_ROUND": (info) => {
                    DumbDog.round.current = null;
                    DumbDog.round.answer = null;

                    if (info.isGameEnd) {
                        // handle this in some way
                    }

                    DumbDog.round.postRoundInfo = info;
                }
            }
        },
        round: {
            current: null,
            hasStarted: function() {
                return this.current != null
            },
            getImageUrl: function() {
                return "/images/" + this.current.key
            },
            getOptions: function() {
                return this.current.options.sort((a, b) => Math.random() > 0.5);
            },
            answer: null,
            setAnswer: function(val) {
                this.answer = val;

                DumbDog.socket.send("SUBMIT", { answerKey: val });
            },
            postRoundInfo: null,
            isPostRound: function() {
                return this.postRoundInfo != null;
            }
        },
        util: {
            capitalize: (str) => {
                return str.split(" ").map((part) => part[0].toUpperCase() + part.substr(1)).join(" ")
            }
        }
    };

    window.dd = DumbDog;

    // Components
    var SubmittableInput = {
        onkeypress: function(ev) {
            if (ev.keyCode === 13) {
                ev.preventDefault();
                this.attrs.onsubmit();
            }
        },
        view: (vnode) => {
            vnode.attrs.onkeypress = vnode.state.onkeypress.bind(vnode);

            return m("div", [
                m("input[type=text]", vnode.attrs),
                m("button", { onclick: vnode.attrs.onsubmit }, vnode.children)
            ]);
        }
    };

    var ClipboardTextZone = {
        onfocus: function(ev) {
            ev.target.select();
        },
        view: (vnode) => {
            vnode.attrs.onfocus = vnode.state.onfocus.bind(vnode);

            return m("input#clipzone[type=text][readonly]", vnode.attrs)
        }
    };

    var LoginZone = {
        view: () => {
            return m(SubmittableInput, {
                oninput: m.withAttr("value", DumbDog.auth.setUsername),
                onsubmit: DumbDog.auth.login,
                placeholder: "Username"
            }, "Let's go!")
        }
    };

    var LobbyCreateRoom = {
        view: () => {
            return m("div.box", [
                m("h3", "Create a Room"),
                m("p", "Create a new room and invite your friends!"),
                m("button", { onclick: DumbDog.rooms.create }, "New Room")
            ])
        }
    };

    var LobbyJoinRoom = {
        view: () => {
            return m("div.box", [
                m("h3", "Join a Room"),
                m("p", "Join a friend's room!"),
                m(SubmittableInput, {
                    oninput: m.withAttr("value", DumbDog.rooms.setName),
                    onsubmit: DumbDog.rooms.join,
                    placeholder: "Room slug"
                }, "Join >>")
            ])
        }
    };

    var HostUtilities = {
        view: (vnode) => {
            return m("div.host", [
                m("button", { onclick: DumbDog.rooms.startGame }, "Start Game"),
                m("button", { onclick: DumbDog.rooms.skip }, "Skip Round")
            ]);
        }
    };

    var LoadingScreen = {
        oninit: () => {
            DumbDog.auth.checkState().then(() => {
                m.route.set("/lobby");
            })
        },
        view: () => {
            return m("div.loading", [
                m("h1", "Loading...")
            ])
        }
    };

    var Splash = {
        oninit: async () => {
            await DumbDog.auth.checkState();
        },
        view: () => {
            return m("div", [
                m("div.header", [
                    m("h1", "Dumb Dog"),
                    m("p", "A game about WikiHow illustrations")
                ]),
                m(LoginZone)
            ])
        }
    };

    var Lobby = {
        oninit: async () => {
            await DumbDog.auth.checkState();

            if (DumbDog.auth.isLoggedIn()) {
                DumbDog.socket.connect().then(() => {
                    DumbDog.socket.send("HELLO", {});
                }).catch(err => {
                    console.error(err)
                });
            }
        },
        view: () => {
            return m("div", [
                m(LobbyCreateRoom),
                m(LobbyJoinRoom)
            ])
        }
    };

    var Room = {
        oninit: async (vnode) => {
            if (!DumbDog.auth.isLoggedIn()) {
                await DumbDog.auth.checkState();
            }

            if (!DumbDog.socket.connected) {
                DumbDog.socket.connect().then(() => {
                    DumbDog.socket.send("HELLO", {});
                    DumbDog.socket.send("JOIN_ROOM", { slug: vnode.attrs.id });

                    m.redraw();
                }).catch(err => {
                    console.error(err);
                    m.route.set("/lobby")
                })
            } else {
                DumbDog.socket.send("JOIN_ROOM", { slug: vnode.attrs.id });
            }
        },
        view: (vnode) => {
            return m("div.game", [
                m("div.menu", [
                    m("span", "Room slug:"),
                    m(ClipboardTextZone, { value: vnode.attrs.id }),
                    m("button", { onclick: DumbDog.rooms.leave }, "Leave Room"),
                    m("p", "You can also give the URL directly to your friends!"),
                    DumbDog.auth.doesOwnCurrentRoom() ? [
                        m(HostUtilities)
                    ] : []
                ]),
                m("ul.players", DumbDog.rooms.getPlayers().map(
                    (player) => {
                        return m("li.player", player.username + ` (${player.correct - player.incorrect} points)`)
                    })
                ),
                m("div.game", DumbDog.round.hasStarted() ? [
                    m("img", { src: DumbDog.round.getImageUrl() }),
                    m("select", { oninput: m.withAttr("value", DumbDog.round.setAnswer) }, DumbDog.round.getOptions().map(
                        (item) => {
                            return m("option", { value: item }, "How To " + DumbDog.util.capitalize(item))
                        })
                    )
                ] : [
                    m("h1.faded", "Waiting for round to start...")
                ])
            ]);
        }
    };

    var root = document.getElementById("app");
    m.route(root, "/", {
        "/": LoadingScreen,
        "/splash": Splash,
        "/lobby": Lobby,
        "/room/:id": Room
    });
})();