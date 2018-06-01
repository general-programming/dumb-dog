package ch.offbeatwit.dumbdog.game

import ch.offbeatwit.dumbdog.game.net.packets.PacketBase
import ch.offbeatwit.dumbdog.game.net.packets.PacketRoomUpdate

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class Room(val id: String, val owner: User) {
    val players: ArrayList<Player> = arrayListOf()
    var state: RoomState = RoomState.WAITING
    var scoreThreshold = 10
    @Transient var current: Question? = null

    fun answerSubmitted() {
        if (players.all { it.hasAnswered() }) {
            // All players have answered, calculate scores
            players.forEach {
                if (it.answer == current!!.imageKey) {
                    it.correct++
                } else {
                    it.incorrect++
                }

                it.cleanup()

                if (it.score >= it.room.scoreThreshold) {
                    // Player wins
                }
            }

            this.syncPlayers()
        }
    }

    fun syncPlayers() {
        this.broadcast(PacketRoomUpdate(this))
    }

    fun broadcast(packet: PacketBase) {
        for (player in players) {
            player.netHandler?.respond(packet)
        }
    }

    class Builder {
        var id: String? = null
        var owner: User? = null

        fun build(): Room {
            return Room(id!!, owner!!)
        }
    }

    enum class RoomState {
        WAITING,
        STARTED,
        END
    }
}