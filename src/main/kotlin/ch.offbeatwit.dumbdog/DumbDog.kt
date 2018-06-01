package ch.offbeatwit.dumbdog

import ch.offbeatwit.dumbdog.game.GameState
import ch.offbeatwit.dumbdog.routes.Failure
import ch.offbeatwit.dumbdog.routes.rooms
import ch.offbeatwit.dumbdog.routes.users
import ch.offbeatwit.dumbdog.routes.ws
import ch.offbeatwit.dumbdog.session.UserSession
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.sessions.SessionStorageMemory
import io.ktor.sessions.Sessions
import io.ktor.sessions.header
import io.ktor.websocket.WebSockets
import java.time.Duration

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
fun Application.main() {
    install(ContentNegotiation) {
        gson {}
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(120)
    }

    install(Sessions) {
        header("X-User", UserSession::class, SessionStorageMemory()) {

        }
    }

    install(StatusPages)

    val gameState = GameState()

    routing {
        get("/") {
            call.respondText("Hi, world!", ContentType.Text.Plain)
        }

        get("/images/{key}") {
            val key = call.parameters["key"]!!
            val question = gameState.questions.questions[key]

            if (question == null) {
                call.respond(Failure(404, "Image not found."))
            } else {
                val imageBytes = question.path.readBytes()

                call.respond(imageBytes)
            }
        }

        rooms(gameState)
        users(gameState)
        ws(gameState)
    }
}