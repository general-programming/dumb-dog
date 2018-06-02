(function () {
    var messageMount = document.getElementById("msg-mount");

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
                    return true;
                }).catch(err => {
                    m.route.set("/splash");
                    return false;
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

                DumbDog.rooms.room = null;
                DumbDog.round.current = null;
                DumbDog.round.postRoundInfo = null;

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
            },
            setParty: function() {
                DumbDog.socket.send("PARTY_MODE_SET", { enabled: !DumbDog.rooms.isPartyMode() })
            },
            isPartyMode: function () {
                if (this.room == null) return false;

                return this.room.partyMode;
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

                    if (frame.code !== 403) {
                        setTimeout(DumbDog.socket.connect.bind(DumbDog.socket), 3000)
                    }
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

                console.log("SEND: " + type);

                this.ws.send(data);
            },
            handlers: {
                "ERROR": (err) => {
                    DumbDog.socket.ws.close(err.status, err.message)
                },
                "OK": () => {
                    // m.mount(messageMount, null);
                },
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
                if (!this.hasStarted()) return [];

                return this.current.options;
            },
            answer: null,
            setAnswer: function(val) {
                if (val === "") return;

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
                m("button.attached", { onclick: vnode.attrs.onsubmit }, vnode.children)
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

    var Message = {
        view: (vnode) => {
            return m("div.message", vnode.attrs, [
                m("p", vnode.children)
            ]);
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

    var UserDisplay = {
        view: () => {
            if (!DumbDog.auth.isLoggedIn()) {
                return m(".user")
            }

            return m(".user", [
                m("span", "Logged in as "),
                DumbDog.auth.isLoggedIn() ? m("b", DumbDog.auth.user.username) : []
            ]);
        }
    };

    var LobbyCreateRoom = {
        view: () => {
            return m("div.box.left", [
                m("h3", "Create a Room"),
                m("p", "Create a new room and invite your friends!"),
                m("button", { onclick: DumbDog.rooms.create }, "New Room")
            ])
        }
    };

    var LobbyJoinRoom = {
        view: () => {
            return m("div.box.right", [
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
                m("button", { onclick: DumbDog.rooms.skip }, "Skip Round"),
                m("button", { onclick: DumbDog.rooms.setParty }, DumbDog.rooms.isPartyMode() ? "Disable Party Mode" : "Enable Party Mode")
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
            var result = await DumbDog.auth.checkState();

            if (result) {
                m.route.set("/lobby");
            }
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

            if (DumbDog.auth.isLoggedIn() && !DumbDog.socket.connected) {
                DumbDog.socket.connect().then(() => {
                    DumbDog.socket.send("HELLO", {});

                    if (DumbDog.rooms.canJoin()) {
                        m.route.set("/room/:id", { id: DumbDog.rooms.roomName })
                    }
                }).catch(err => {
                    console.error(err)
                });
            } else if (!DumbDog.auth.isLoggedIn()) {
                m.route.set("/splash")
            }
        },
        view: () => {
            return m("div.lobby", [
                m(LobbyCreateRoom),
                m(LobbyJoinRoom)
            ])
        }
    };

    var Room = {
        oninit: async (vnode) => {
            if (!DumbDog.auth.isLoggedIn()) {
                if (!await DumbDog.auth.checkState()) {
                    DumbDog.rooms.setName(vnode.attrs.id);
                    m.route.set("/splash");
                }
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
            var gameClass = [];
            if (DumbDog.rooms.isPartyMode()) gameClass.push("party");
            if (DumbDog.auth.doesOwnCurrentRoom()) gameClass.push("host");

            return m("div.gamebox", [
                m("div.menu", [
                    m(".info", [
                        m("span", "Room slug: ", m(ClipboardTextZone, { value: vnode.attrs.id })),
                        m("p", "You can also give the URL directly to your friends!")
                    ]),
                    DumbDog.auth.doesOwnCurrentRoom() ? [
                        m(HostUtilities)
                    ] : [],
                    m("button.t", { onclick: DumbDog.rooms.leave }, "Leave Room"),
                    m("h4", "Players"),
                    m("ul.players", DumbDog.rooms.getPlayers().map(
                        (player) => {
                            return m("li.player", player.username + ` (${player.correct} points)`)
                        })
                    )
                ]),
                m("div.game", { className: gameClass.join(" ") }, DumbDog.round.hasStarted() ? [
                    m("img", { src: DumbDog.round.getImageUrl() }),
                    m("select[autocomplete=off]", { oninput: m.withAttr("value", DumbDog.round.setAnswer), selectedIndex: 0 },
                        m("option[selected]", { value: "" }, "Select answer..."),
                        DumbDog.round.getOptions().map((item) => {
                            return m("option", { value: item }, "How To " + DumbDog.util.capitalize(item))
                        })
                    )
                ] : [
                    DumbDog.round.isPostRound() ? m("div.postround", [
                        m("h2", "Round over!"),
                        m("p", "The correct answer was ", m("b", "How To ", DumbDog.util.capitalize(DumbDog.round.postRoundInfo.answer))),
                        m("div.correct", DumbDog.round.postRoundInfo.correct.map(player => {
                            return m("div.entry", m("b", player.username), " +1")
                        }))
                    ]) : m("h1.faded", "Waiting for round to start...")
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

    m.mount(document.getElementById("user-hook"), UserDisplay);
})();