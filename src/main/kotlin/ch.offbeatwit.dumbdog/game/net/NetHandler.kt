package ch.offbeatwit.dumbdog.game.net

import ch.offbeatwit.dumbdog.game.net.packets.PacketBase
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
abstract class NetHandler(val socket: WebSocketSession, val state: UserConnection.State) {
    val gson = Gson()

    fun respond(packet: PacketBase) {
        try {
            val payload = gson.toJsonTree(packet)
            val res = gson.toJson(PacketWrapper(packet.typeName, payload))

            async {
                socket.outgoing.send(Frame.Text(res))
            }
        } catch (err: Throwable) {
            err.printStackTrace()
        }
    }

    suspend fun processFrame(frame: Frame.Text) {
        try {
            val text = frame.readText()
            val packet: PacketWrapper = gson.fromJson(text, PacketWrapper::class.java)

            this.processPacket(packet)
        } catch (err: Throwable) {
            err.printStackTrace()
        }
    }

    open fun processDisconnect() {

    }

    abstract suspend fun processPacket(packet: PacketWrapper)
}