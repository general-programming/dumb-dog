package gq.genprog.dumbdog.error

import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class UnauthorizedException(msg: String): Exception(msg)
class NotFoundException(msg: String): Exception(msg)

fun StatusPages.Configuration.handlers() {
    exception<UnauthorizedException> {
        call.respond(HttpStatusCode.Unauthorized, it.message!!)
    }

    exception<NotFoundException> {
        call.respond(HttpStatusCode.NotFound, it.message!!)
    }

    exception<Exception> {
        call.respond(HttpStatusCode.InternalServerError, it.message ?: "Unknown Error")
    }
}
