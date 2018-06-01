package ch.offbeatwit.dumbdog.game

import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
open class User(val id: UUID) {
    var username = "Guest"

    class Builder {
        var id: UUID? = null
        var username: String? = null

        fun build(): User {
            val player = User(id!!)
            if (username != null)
                player.username = username!!

            return player
        }
    }
}