package ch.offbeatwit.dumbdog.game.net

import ch.offbeatwit.dumbdog.game.Player
import ch.offbeatwit.dumbdog.game.net.packets.PacketBase
import ch.offbeatwit.dumbdog.game.net.packets.PacketOk
import ch.offbeatwit.dumbdog.game.net.packets.PacketSetUsername
import ch.offbeatwit.dumbdog.game.net.packets.PacketWrapper
import com.google.gson.Gson
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.experimental.async

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class NetHandlerPlayer(val player: Player, val socket: WebSocketSession) {
    val gson = Gson()

    private fun respond(packet: PacketBase) {
        val payload = gson.toJsonTree(packet)
        val res = gson.toJson(PacketWrapper(packet.typeName, payload))

        async {
            socket.outgoing.send(Frame.Text(res))
        }
    }

    fun processFrame(frame: Frame.Text) {
        val text = frame.readText()
        val packet: PacketWrapper = gson.fromJson(text, PacketWrapper::class.java)

        when (packet.t) {
            "SET_USERNAME" -> {
                val data = gson.fromJson<PacketSetUsername>(packet.d, PacketSetUsername::class.java)
                this.handleSetUsername(data)
            }

            "JOIN_ROOM" -> {

            }
        }
    }

    fun handleSetUsername(pkt: PacketSetUsername) {
        player.username = pkt.username

        respond(PacketOk("Username set successfully."))
    }
}