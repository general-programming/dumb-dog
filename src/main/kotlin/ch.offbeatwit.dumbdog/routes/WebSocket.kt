package ch.offbeatwit.dumbdog.routes

import ch.offbeatwit.dumbdog.game.GameState
import ch.offbeatwit.dumbdog.game.net.UserConnection
import io.ktor.routing.Routing
import io.ktor.websocket.Frame
import io.ktor.websocket.webSocket

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
fun Routing.ws(state: GameState) {
    webSocket("/") {
        val connection = UserConnection(this, state)

        while (true) {
            val frame = incoming.receive()
            if (frame is Frame.Text)
                connection.processFrame(frame)
        }
    }
}