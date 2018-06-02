package ch.offbeatwit.dumbdog.session

import ch.offbeatwit.dumbdog.game.GameState
import ch.offbeatwit.dumbdog.game.User
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
data class UserSession(val id: UUID) {
    fun GameState.getSessionUser(): User? {
        return this.users[this@UserSession.id]
    }
}