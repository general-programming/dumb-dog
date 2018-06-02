package gq.genprog.dumbdog.game.net

import gq.genprog.dumbdog.game.Player
import gq.genprog.dumbdog.game.net.packets.PacketChangeState
import gq.genprog.dumbdog.game.net.packets.PacketPartyMode
import gq.genprog.dumbdog.game.net.packets.PacketSubmitAnswer
import gq.genprog.dumbdog.game.net.packets.PacketWrapper

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

            "LEAVE_ROOM" -> {
                player.leaveRoom()
                player.room.controller(conn.gameState).checkRoomState()
                conn.handler = NetHandlerLobby(conn, player)
                respond(PacketChangeState(conn.handler.state))
            }

            "START_GAME" -> {
                if (player.room.isOwner(player)) {
                    player.room.controller(conn.gameState).nextRound()
                }
            }

            "SKIP_ROUND" -> {
                if (player.room.isOwner(player)) {
                    player.room.controller(conn.gameState).answerSubmitted(true)
                }
            }

            "PARTY_MODE_SET" -> {
                val pkt: PacketPartyMode = gson.fromJson(packet.d, PacketPartyMode::class.java)
                this.handlePartyMode(pkt)
            }
        }
    }

    suspend fun handleSubmitAnswer(pkt: PacketSubmitAnswer) {
        player.answer = pkt.answerKey
        player.room.controller(conn.gameState).answerSubmitted(false)
    }

    suspend fun handlePartyMode(pkt: PacketPartyMode) {
        if (player.room.isOwner(player)) {
            if (pkt.enabled) {
                player.room.partyMode = true
                player.room.players.remove(player)
                player.room.partyHost = player
            } else {
                player.room.partyHost = null
                player.room.players.add(player)
                player.room.partyMode = false
            }

            player.room.controller(conn.gameState).syncPlayers()
        }
    }

    override fun processDisconnect() {
        player.leaveRoom()
    }
}