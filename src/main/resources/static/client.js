(function () {
    // Model
    var DumbDog = {
        auth: {
            user: null,
            username: null,
            setUsername: (username) => {
                this.username = username;
            },
            isLoggedIn: () => this.user != null,
            login: () => {
                m.request({
                    method: "POST",
                    url: "/api/login",
                    data: {
                        username: this.username
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
            doesOwnCurrentRoom: () => {
                if (DumbDog.rooms.room != null) {
                    return DumbDog.rooms.room.owner.id === this.user.id;
                }

                return false;
            },
            checkState: () => {
                m.request({
                    method: "GET",
                    url: "/api/users/@me",
                    async: false
                }).then(user => {
                    this.user = user;

                    m.route.set("/lobby")
                }).catch(err => {
                    m.route.set("/splash")
                });
            }
        },
        rooms: {
            room: null,
            roomName: null,
            setName: (name) => {
                this.roomName = name;
            },
            canJoin: () => this.roomName != null,
            join: () => {
                if (!DumbDog.rooms.canJoin())
                    return;

                m.request({
                    method: "GET",
                    url: "/api/room/" + this.roomName
                }).then(room => {
                    this.room = room;

                    m.route.set("/room", { id: room.id });
                }).catch(err => {
                    console.error(err)
                });
            },
            create: () => {
                m.request({
                    method: "POST",
                    url: "/api/rooms/create"
                }).then(room => {
                    this.room = room;

                    m.route.set("/room", { id: room.id });
                })
            }
        },
        socket: {
            ws: null,
            connected: false,
            state: "login",
            isConnected: () => this.ws != null && this.connected,
            connect: () => {
                this.ws = new WebSocket("ws://" + window.location.host);

                this.ws.onmessage = (d) => {
                    var packet = JSON.parse(d.data);

                    if (packet.t === "CHANGE_STATE") {
                        this.state = packet.d.newState;
                    } else {
                        var handler = this.handlers[packet.t];

                        if (handler != null)
                            handler(packet.d);
                    }
                };

                this.ws.onclose = () => {
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
            send: (type, packet) => {
                var data = JSON.stringify({ t: type, d: packet });

                this.ws.send(data);
            },
            handlers: {
                "ROOM_UPDATE": (room) => {
                    DumbDog.rooms.room = room;
                },
                "NEW_ROUND": (round) => {
                    DumbDog.round.current = round;
                }
            }
        },
        round: {
            current: null,
            hasStarted: () => this.current != null,
            getImageUrl: () => {
                return "/images/" + this.current.key
            },
            getOptions: () => {
                return this.current.options.sort((a, b) => Math.random() > 0.5);
            },
            answer: null,
            setAnswer: (val) => {
                this.answer = val;

                DumbDog.socket.send("SUBMIT", { answerKey: val });
            }
        },
        util: {
            capitalize: (str) => {
                return str.split(" ").map((part) => part[0].toUpperCase() + part.substr(1)).join(" ")
            }
        }
    };

    // Components
    var SubmittableInput = {
        oninit: (vnode) => {
            vnode.attrs.onkeypress = (ev) => {
                if (ev.which === 13) {
                    ev.preventDefault();
                    vnode.attrs.onsubmit(ev);
                }
            };
        },
        view: (vnode) => {
            return m("div", [
                m("input[type=text]", vnode.attrs),
                m("button", { onclick: vnode.attrs.onsubmit }, vnode.children)
            ]);
        }
    };

    var ClipboardTextZone = {
        oninit: (vnode) => {
            vnode.attrs.onfocus = (ev) => {
                ev.target.select();
            };
        },
        view: (vnode) => {
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

    var LoadingScreen = {
        oninit: () => {
            DumbDog.auth.checkState()
        },
        view: () => {
            return m("div.loading", [
                m("h1", "Loading...")
            ])
        }
    };

    var Splash = {
        oninit: () => {
            DumbDog.auth.checkState();
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
        oninit: () => {
            DumbDog.auth.checkState();

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
        oninit: (vnode) => {
            if (!DumbDog.socket.connected) {
                DumbDog.socket.connect().then(() => {
                    DumbDog.socket.send("JOIN_ROOM", { slug: vnode.attrs.id })
                }).catch(err => {
                    console.error(err);
                    m.route.set("/lobby")
                })
            }
        },
        view: (vnode) => {
            return m("div.game", [
                m("div.menu", [
                    m("span", "Room slug:"),
                    m(ClipboardTextZone, { value: vnode.attrs.id }),
                    m("p", "You can also give the URL directly to your friends!")
                ]),
                m("div.game", DumbDog.round.hasStarted() ? [
                    m("img", { src: DumbDog.round.getImageUrl() }),
                    m("select", { oninput: m.withAttr("value", DumbDog.round.setAnswer) }, DumbDog.round.current.getOptions().map(
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

    m.route(document.body, "/", {
        "/": LoadingScreen,
        "/splash": Splash,
        "/lobby": Lobby,
        "/room/:id": Room
    });
})();