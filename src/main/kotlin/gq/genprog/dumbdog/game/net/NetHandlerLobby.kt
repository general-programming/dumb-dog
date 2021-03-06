package gq.genprog.dumbdog.game.net

import gq.genprog.dumbdog.game.Player
import gq.genprog.dumbdog.game.User
import gq.genprog.dumbdog.game.net.packets.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class NetHandlerLobby(val conn: UserConnection, val user: User): NetHandler(conn.socket, UserConnection.State.LOBBY) {
    override suspend fun processPacket(packet: PacketWrapper) {
        when (packet.t) {
            "SET_USERNAME" -> {
                val pkt: PacketSetUsername = gson.fromJson(packet.d, PacketSetUsername::class.java)
                this.handleSetUsername(pkt)
            }

            "JOIN_ROOM" -> {
                val pkt: PacketJoinRoom = gson.fromJson(packet.d, PacketJoinRoom::class.java)
                this.handleJoinRoom(pkt)
            }
        }
    }

    fun handleSetUsername(pkt: PacketSetUsername) {
        user.username = pkt.username

        respond(PacketOk("Username set successfully."))
    }

    fun handleJoinRoom(pkt: PacketJoinRoom) {
        if (conn.gameState.rooms.containsKey(pkt.slug)) {
            val room = conn.gameState.rooms[pkt.slug]!!
            val player = Player(user.id, room)
            player.username = user.username

            room.players.add(player)

            conn.handler = NetHandlerGame(conn, player)
            respond(PacketChangeState(conn.handler.state))
            room.broadcast(PacketRoomUpdate(room))
        } else {
            respond(PacketFail(404, "That room doesn't exist!"))
        }
    }

    override fun processDisconnect() {
//        conn.gameState.users.remove(this.user.id)
    }
}