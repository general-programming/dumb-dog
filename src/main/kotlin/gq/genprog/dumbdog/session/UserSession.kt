package gq.genprog.dumbdog.session

import gq.genprog.dumbdog.game.GameState
import gq.genprog.dumbdog.game.User
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