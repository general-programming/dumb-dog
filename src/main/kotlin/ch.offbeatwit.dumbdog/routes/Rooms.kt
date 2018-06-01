package ch.offbeatwit.dumbdog.routes

import ch.offbeatwit.dumbdog.game.GameState
import ch.offbeatwit.dumbdog.game.Room
import ch.offbeatwit.dumbdog.session.UserSession
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.sessions.get
import io.ktor.sessions.sessions

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
fun Routing.rooms(state: GameState) {
    get("/api/room/{id}") {
        val roomId = call.parameters["id"]

        if (state.rooms.containsKey(roomId))
            call.respond(state.rooms[roomId]!!)
        else
            call.respond(Failure(404, "Room does not exist."))
    }

    post("/api/rooms/create") {
        val session: UserSession? = call.sessions.get()
        if (session == null) {
            call.respond(Failure(401, "Not logged in!"))
            return@post
        }

        val room = Room.Builder().also {
            it.id = state.generator.generateHumanId()
            it.owner = session.user
            it.gameState = state
        }.build()

        state.rooms[room.id] = room

        call.respond(RoomCreateResponse(room.id))
    }
}

data class Failure(val status: Int, val msg: String)
data class RoomCreateResponse(val id: String) {
    val t = "room_created"
}
