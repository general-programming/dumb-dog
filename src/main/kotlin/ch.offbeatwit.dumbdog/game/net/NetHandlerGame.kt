package ch.offbeatwit.dumbdog.game.net

import ch.offbeatwit.dumbdog.game.Player
import ch.offbeatwit.dumbdog.game.net.packets.PacketSubmitAnswer
import ch.offbeatwit.dumbdog.game.net.packets.PacketWrapper

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class NetHandlerGame(val conn: UserConnection, val player: Player): NetHandler(conn.socket) {
    init {
        player.netHandler = this
    }

    override fun processPacket(packet: PacketWrapper) {
        when (packet.t) {
            "SUBMIT" -> {
                val pkt: PacketSubmitAnswer = gson.fromJson(packet.d, PacketSubmitAnswer::class.java)
                this.handleSubmitAnswer(pkt)
            }

            "LEAVE_ROOM" -> {
                this.processDisconnect()
            }
        }
    }

    fun handleSubmitAnswer(pkt: PacketSubmitAnswer) {
        player.answer = pkt.answerKey
        player.room.answerSubmitted()
    }

    override fun processDisconnect() {
        player.leaveRoom()
    }
}