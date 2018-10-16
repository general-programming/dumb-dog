package gq.genprog.dumbdog.game

import gq.genprog.dumbdog.game.net.packets.PacketNewRound
import gq.genprog.dumbdog.game.net.packets.PacketRoomUpdate
import gq.genprog.dumbdog.game.net.packets.PacketRoundEnd
import gq.genprog.dumbdog.game.net.packets.PacketWinAnnouncement
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.time.delay
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class RoomController(val gameState: GameState, val room: Room) {
    suspend fun answerSubmitted(override: Boolean = false) {
        if (room.current == null) {
            return // if there's no question (either because we haven't started or we're in post-round) don't run
        }

        if (room.players.all { it.hasAnswered() } || override) {
            // All players have answered, calculate scores
            val question = room.current!!
            room.current = null

            if (room.timeoutJob != null)
                room.timeoutJob!!.cancel()

            val correct = arrayListOf<Player>()
            var hasPlayerWon = false

            room.players.forEach {
                if (it.answer == question.text) {
                    it.correct++
                    correct.add(it)
                } else {
                    it.incorrect++
                }

                it.cleanup()

                if (it.correct >= it.room.scoreThreshold) {
                    // Player wins
                    hasPlayerWon = true
                    room.state = Room.RoomState.END

                    room.broadcast(PacketWinAnnouncement(it))
                }
            }

            room.broadcast(PacketRoundEnd(correct, question.text, hasPlayerWon))
            this.syncPlayers()

            async {
                delay(5, TimeUnit.SECONDS)
                this@RoomController.nextRound(false)
            }
        }
    }

    fun nextRound(newGame: Boolean) {
        if (room.state == Room.RoomState.END && !newGame) {
            return
        }

        room.state = Room.RoomState.STARTED
        room.current = gameState.questions.randomQuestion()
        val choices = mutableListOf(
                room.current!!.text,
                gameState.questions.randomQuestionText(),
                gameState.questions.randomQuestionText(),
                gameState.questions.randomQuestionText()
        )
        choices.sortBy { Math.random() }

        room.broadcast(PacketNewRound(choices.toTypedArray(), room.current!!.imageKey))

        room.timeoutJob = launch {
            delay(Duration.ofSeconds(room.roomTimeout))
            room.timeoutJob = null
            this@RoomController.answerSubmitted(true)
        }
    }

    fun checkRoomState() {
        if (room.partyMode && gameState.users.containsKey(room.owner.id) && room.state != Room.RoomState.END) {
            return
        }

        if (room.players.isEmpty()) {
            gameState.rooms.remove(room.id)
        } else if (!room.players.any { it.id == room.owner.id }) {
            room.owner = room.players.first()
            this.syncPlayers()
        }
    }

    fun syncPlayers() {
        this.room.broadcast(PacketRoomUpdate(room))
    }
}