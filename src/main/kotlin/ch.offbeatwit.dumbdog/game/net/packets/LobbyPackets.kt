package ch.offbeatwit.dumbdog.game.net.packets

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
data class PacketOk(val msg: String): PacketBase("OK")
data class PacketFail(val status: Int, val msg: String): PacketBase("ERROR")
data class PacketSetUsername(val username: String) : PacketBase("SET_USERNAME")
data class PacketJoinRoom(val slug: String) : PacketBase("JOIN_ROOM")
