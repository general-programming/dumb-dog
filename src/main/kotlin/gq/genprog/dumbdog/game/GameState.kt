package gq.genprog.dumbdog.game

import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class GameState {
    val rooms: HashMap<String, Room> = hashMapOf()
    val users: HashMap<UUID, User> = hashMapOf()
    val generator = IdGenerator()
    val questions = QuestionManager(this)
}