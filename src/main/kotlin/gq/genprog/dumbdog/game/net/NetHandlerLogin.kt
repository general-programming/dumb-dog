package gq.genprog.dumbdog.game.net

import gq.genprog.dumbdog.game.net.packets.PacketChangeState
import gq.genprog.dumbdog.game.net.packets.PacketFail
import gq.genprog.dumbdog.game.net.packets.PacketWrapper
import gq.genprog.dumbdog.session.UserSession
import io.ktor.sessions.get
import io.ktor.sessions.sessions

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class NetHandlerLogin(val conn: UserConnection): NetHandler(conn.socket, UserConnection.State.LOGIN) {
    override suspend fun processPacket(packet: PacketWrapper) {
        if (packet.t == "HELLO") {
            val session: UserSession? = conn.socket.call.sessions.get()
            if (session == null) {
                respond(PacketFail(401, "Not logged in!"))
                return
            }

            session.apply {
                conn.handler = NetHandlerLobby(conn, conn.gameState.getSessionUser()!!)
            }

            respond(PacketChangeState(conn.handler.state))
        }
    }
}