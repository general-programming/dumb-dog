package ch.offbeatwit.dumbdog.routes

import ch.offbeatwit.dumbdog.game.GameState
import ch.offbeatwit.dumbdog.game.Room
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
fun Routing.rooms(state: GameState) {
    get("/room/{id}") {
        val roomId = call.parameters["id"]

        call.respondText("id: $roomId", ContentType.Text.Plain)
    }

    post("/rooms/create") {
        val room = Room.Builder().also {
            it.id = "goose"
        }.build()

        state.rooms[room.id] = room
    }
}