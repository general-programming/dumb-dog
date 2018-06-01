package ch.offbeatwit.dumbdog.routes

import ch.offbeatwit.dumbdog.error.UnauthorizedException
import ch.offbeatwit.dumbdog.game.GameState
import ch.offbeatwit.dumbdog.game.User
import ch.offbeatwit.dumbdog.session.UserSession
import io.ktor.application.call
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
fun Routing.users(state: GameState) {
    post("/api/login") {
        val req: UserLoginRequest? = call.receiveOrNull()

        val user = User.Builder().also {
            it.id = UUID.randomUUID()

            if (req?.username != null) {
                it.username = req.username
            }
        }.build()

        state.users[user.id] = user
        call.sessions.set(UserSession(user.id))
        call.respond(UserLoginResponse(user.id, user.username))
    }

    get("/api/users/@me") {
        val session: UserSession = call.sessions.get() ?: throw UnauthorizedException("Not logged in!")

        session.apply {
            val user = state.getSessionUser()!!

            call.respond(user)
        }
    }
}

data class UserLoginRequest(val username: String?)
data class UserLoginResponse(val id: UUID, val username: String) {
    val t = "user_login_success"
}