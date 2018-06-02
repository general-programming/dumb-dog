package ch.offbeatwit.dumbdog.game

import ch.offbeatwit.dumbdog.game.net.packets.PacketBase
import kotlinx.coroutines.experimental.Job
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class Room(val id: String, val owner: User) {
    val players: ArrayList<Player> = arrayListOf()
    var state: RoomState = RoomState.WAITING
    var scoreThreshold = 10
    var roomTimeout = 80L

    @Transient internal var current: Question? = null
    @Transient internal var timeoutJob: Job? = null

    fun controller(gameState: GameState): RoomController {
        return RoomController(gameState, this)
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