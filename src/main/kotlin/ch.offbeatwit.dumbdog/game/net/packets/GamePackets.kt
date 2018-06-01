package ch.offbeatwit.dumbdog.game.net.packets

import ch.offbeatwit.dumbdog.game.Room

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
data class PacketSubmitAnswer(val answerKey: String): PacketBase("SUBMIT")
data class PacketRoomUpdate(val room: Room): PacketBase("ROOM_UPDATE")