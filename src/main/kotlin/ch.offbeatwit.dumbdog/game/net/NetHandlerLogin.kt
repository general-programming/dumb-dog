package ch.offbeatwit.dumbdog.game.net

import ch.offbeatwit.dumbdog.game.net.packets.PacketFail
import ch.offbeatwit.dumbdog.game.net.packets.PacketOk
import ch.offbeatwit.dumbdog.game.net.packets.PacketWrapper
import ch.offbeatwit.dumbdog.session.UserSession
import io.ktor.sessions.get
import io.ktor.sessions.sessions

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class NetHandlerLogin(val conn: UserConnection): NetHandler(conn.socket) {
    override fun processPacket(packet: PacketWrapper) {
        if (packet.t == "HELLO") {
            val session: UserSession? = conn.socket.call.sessions.get()
            if (session == null) {
                respond(PacketFail(401, "Not logged in!"))
                return
            }

            conn.state = UserConnection.State.LOBBY
            conn.handler = NetHandlerLobby(conn, session.user)

            respond(PacketOk("Logged in successfully!"))
        }
    }
}