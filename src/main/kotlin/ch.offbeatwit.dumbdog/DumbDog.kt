package ch.offbeatwit.dumbdog

import ch.offbeatwit.dumbdog.error.handlers
import ch.offbeatwit.dumbdog.game.GameState
import ch.offbeatwit.dumbdog.routes.Failure
import ch.offbeatwit.dumbdog.routes.rooms
import ch.offbeatwit.dumbdog.routes.users
import ch.offbeatwit.dumbdog.routes.ws
import ch.offbeatwit.dumbdog.session.UserSession
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.content.defaultResource
import io.ktor.content.resources
import io.ktor.content.static
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.sessions.SessionStorageMemory
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
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
        cookie("dumb_user", UserSession::class, SessionStorageMemory())
    }

    install(StatusPages) {
        handlers()
    }

    val gameState = GameState()

    routing {
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

        static {
            resources("static")
            defaultResource("index.html", "static")
        }

        rooms(gameState)
        users(gameState)
        ws(gameState)
    }
}