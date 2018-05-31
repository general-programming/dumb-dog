package ch.offbeatwit.dumbdog

import ch.offbeatwit.dumbdog.game.GameState
import ch.offbeatwit.dumbdog.routes.rooms
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
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

    val gameState = GameState()

    routing {
        get("/") {
            call.respondText("Hi, world!", ContentType.Text.Plain)
        }

        rooms(gameState)
    }
}