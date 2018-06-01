package ch.offbeatwit.dumbdog.game

import ch.offbeatwit.dumbdog.game.net.NetHandlerGame
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class Player(uuid: UUID, val room: Room): User(uuid) {
    var netHandler: NetHandlerGame? = null
    var correct = 0
    var incorrect = 0
    val score get() = correct - incorrect

    var answer: String? = null

    fun hasAnswered(): Boolean {
        return answer != null
    }

    fun cleanup() {
        answer = null
    }
}