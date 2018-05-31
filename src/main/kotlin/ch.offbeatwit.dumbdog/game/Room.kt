package ch.offbeatwit.dumbdog.game

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class Room(val id: String) {
    val players: ArrayList<Player> = arrayListOf()

    class Builder {
        var id: String? = null

        fun build(): Room {
            return Room(id!!)
        }
    }
}