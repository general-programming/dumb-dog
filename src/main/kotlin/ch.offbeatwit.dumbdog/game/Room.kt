package ch.offbeatwit.dumbdog.game

import ch.offbeatwit.dumbdog.game.net.packets.PacketBase
import ch.offbeatwit.dumbdog.game.net.packets.PacketNewRound
import ch.offbeatwit.dumbdog.game.net.packets.PacketRoomUpdate
import kotlinx.coroutines.experimental.delay
import java.util.concurrent.TimeUnit

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class Room(val id: String, val owner: User, val gameState: GameState) {
    val players: ArrayList<Player> = arrayListOf()
    var state: RoomState = RoomState.WAITING
    var scoreThreshold = 10
    @Transient private var current: Question? = null

    suspend fun answerSubmitted() {
        if (players.all { it.hasAnswered() }) {
            // All players have answered, calculate scores
            players.forEach {
                if (it.answer == current!!.text) {
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
            delay(5, TimeUnit.SECONDS)
            this.nextRound()
        }
    }

    fun nextRound() {
        this.current = gameState.questions.randomQuestion()
        val choices = mutableListOf(
                current!!.text,
                gameState.questions.randomQuestionText(),
                gameState.questions.randomQuestionText(),
                gameState.questions.randomQuestionText()
        )
        choices.sortBy { Math.random() }

        this.broadcast(PacketNewRound(choices.toTypedArray(), current!!.imageKey))
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
        var gameState: GameState? = null

        fun build(): Room {
            return Room(id!!, owner!!, gameState!!)
        }
    }

    enum class RoomState {
        WAITING,
        STARTED,
        END
    }
}