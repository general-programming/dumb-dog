package ch.offbeatwit.dumbdog.game

import ch.offbeatwit.dumbdog.game.net.packets.PacketBase
import ch.offbeatwit.dumbdog.game.net.packets.PacketNewRound
import ch.offbeatwit.dumbdog.game.net.packets.PacketRoomUpdate
import ch.offbeatwit.dumbdog.game.net.packets.PacketRoundEnd
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.time.delay
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class Room(val id: String, val owner: User, @Transient val gameState: GameState) {
    val players: ArrayList<Player> = arrayListOf()
    var state: RoomState = RoomState.WAITING
    var scoreThreshold = 10
    var roomTimeout = 80L
    @Transient private var current: Question? = null
    @Transient private var timeoutJob: Job? = null

    suspend fun answerSubmitted(override: Boolean) {
        if (this.current == null) {
            return // if there's no question (either because we haven't started or we're in post-round) don't run
        }

        if (players.all { it.hasAnswered() } || override) {
            // All players have answered, calculate scores
            this.current = null

            if (this.timeoutJob != null)
                this.timeoutJob!!.cancel()

            val correct = arrayListOf<Player>()
            var hasPlayerWon = false

            players.forEach {
                if (it.answer == current!!.text) {
                    it.correct++
                    correct.add(it)
                } else {
                    it.incorrect++
                }

                it.cleanup()

                if (it.score >= it.room.scoreThreshold) {
                    // Player wins
                    hasPlayerWon = true
                }
            }

            this.broadcast(PacketRoundEnd(correct, hasPlayerWon))
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

        this.timeoutJob = launch {
            delay(Duration.ofSeconds(this@Room.roomTimeout))
            this@Room.answerSubmitted(true)
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