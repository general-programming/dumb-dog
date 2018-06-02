package gq.genprog.dumbdog.game.net

import gq.genprog.dumbdog.game.GameState
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class UserConnection(val socket: WebSocketSession, val gameState: GameState) {
    var handler: NetHandler = NetHandlerLogin(this)

    suspend fun processFrame(frame: Frame.Text) {
        this.handler.processFrame(frame)
    }

    fun handleDisconnect(isCloseFrame: Boolean, frame: Frame.Close?) {
        this.handler.processDisconnect()
    }

    enum class State {
        LOGIN,
        LOBBY,
        GAME
    }
}