package ch.offbeatwit.dumbdog.game

import ch.offbeatwit.dumbdog.game.net.NetHandlerGame
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class Player(uuid: UUID, val room: Room): User(uuid) {
    @Transient var netHandler: NetHandlerGame? = null
    var correct = 0
    var incorrect = 0
    val score get() = correct - incorrect

    @Transient var answer: String? = null

    fun hasAnswered(): Boolean {
        return answer != null
    }

    fun cleanup() {
        answer = null
    }

    fun leaveRoom() {
        room.players.remove(this)
        room.syncPlayers()
    }
}