package ch.offbeatwit.dumbdog.game.net.packets

import ch.offbeatwit.dumbdog.game.Player
import ch.offbeatwit.dumbdog.game.Room

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
data class PacketSubmitAnswer(val answerKey: String): PacketBase("SUBMIT")
data class PacketRoomUpdate(val room: Room): PacketBase("ROOM_UPDATE")
data class PacketNewRound(val options: Array<String>, val key: String): PacketBase("NEW_ROUND")
data class PacketRoundEnd(val correct: List<Player>, val isGameEnd: Boolean): PacketBase("END_ROUND")