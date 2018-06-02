package gq.genprog.dumbdog.game

import gq.genprog.dumbdog.game.net.packets.PacketBase
import kotlinx.coroutines.experimental.Job

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class Room(val id: String, var owner: User) {
    val players: HashSet<Player> = hashSetOf()
    var state: RoomState = RoomState.WAITING
    var scoreThreshold = 10
    var roomTimeout = 80L
    var partyMode = false

    @Transient var partyHost: Player? = null
    @Transient internal var current: Question? = null
    @Transient internal var timeoutJob: Job? = null

    fun controller(gameState: GameState): RoomController {
        return RoomController(gameState, this)
    }

    fun isOwner(player: Player): Boolean {
        return player.id == owner.id
    }

    fun broadcast(packet: PacketBase) {
        for (player in players) {
            player.netHandler?.respond(packet)
        }

        if (partyMode && partyHost != null) {
            partyHost?.netHandler?.respond(packet)
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