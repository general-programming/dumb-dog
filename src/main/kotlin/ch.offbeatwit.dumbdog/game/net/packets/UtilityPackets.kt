package ch.offbeatwit.dumbdog.game.net.packets

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
data class PacketSetUsername(val username: String) : PacketBase("SET_USERNAME")

data class PacketOk(val msg: String): PacketBase("OK")