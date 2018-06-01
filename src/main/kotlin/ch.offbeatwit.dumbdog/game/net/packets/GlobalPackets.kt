package ch.offbeatwit.dumbdog.game.net.packets

import ch.offbeatwit.dumbdog.game.net.UserConnection

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
data class PacketOk(val msg: String): PacketBase("OK")
data class PacketFail(val status: Int, val msg: String): PacketBase("ERROR")
data class PacketChangeState(val newState: UserConnection.State): PacketBase("CHANGE_STATE")