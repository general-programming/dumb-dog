package ch.offbeatwit.dumbdog.game.net

import ch.offbeatwit.dumbdog.game.Player
import ch.offbeatwit.dumbdog.game.net.packets.PacketChangeState
import ch.offbeatwit.dumbdog.game.net.packets.PacketOk
import ch.offbeatwit.dumbdog.game.net.packets.PacketSubmitAnswer
import ch.offbeatwit.dumbdog.game.net.packets.PacketWrapper

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class NetHandlerGame(val conn: UserConnection, val player: Player): NetHandler(conn.socket, UserConnection.State.GAME) {
    init {
        player.netHandler = this
    }

    override suspend fun processPacket(packet: PacketWrapper) {
        when (packet.t) {
            "SUBMIT" -> {
                val pkt: PacketSubmitAnswer = gson.fromJson(packet.d, PacketSubmitAnswer::class.java)
                this.handleSubmitAnswer(pkt)
            }

            "START_GAME" -> {
                if (player.room.owner.id == player.id) {
                    player.room.controller(conn.gameState).nextRound()
                    respond(PacketOk("Started next round."))
                }
            }

            "LEAVE_ROOM" -> {
                conn.handler = NetHandlerLobby(conn, player)
                respond(PacketChangeState(conn.handler.state))
            }
        }
    }

    suspend fun handleSubmitAnswer(pkt: PacketSubmitAnswer) {
        player.answer = pkt.answerKey
        player.room.controller(conn.gameState).answerSubmitted(false)
    }

    override fun processDisconnect() {
        player.leaveRoom()
    }
}