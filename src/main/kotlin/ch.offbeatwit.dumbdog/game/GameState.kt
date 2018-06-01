package ch.offbeatwit.dumbdog.game

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class GameState {
    val rooms: HashMap<String, Room> = hashMapOf()
    val generator = IdGenerator()
    val questions = QuestionManager(this)
}