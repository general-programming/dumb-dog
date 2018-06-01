package ch.offbeatwit.dumbdog.game.net

import ch.offbeatwit.dumbdog.game.net.packets.PacketWrapper

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class NetHandlerLogin(val conn: UserConnection): NetHandler(conn.socket) {
    override fun processPacket(packet: PacketWrapper) {
        if (packet.t == "HELLO") {
            
        }
    }
}