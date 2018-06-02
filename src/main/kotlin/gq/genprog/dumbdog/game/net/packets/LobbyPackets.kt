package gq.genprog.dumbdog.game.net.packets

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
data class PacketSetUsername(val username: String) : PacketBase("SET_USERNAME")
data class PacketJoinRoom(val slug: String) : PacketBase("JOIN_ROOM")
