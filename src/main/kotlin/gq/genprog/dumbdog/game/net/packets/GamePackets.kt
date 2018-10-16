package gq.genprog.dumbdog.game.net.packets

import gq.genprog.dumbdog.game.Player
import gq.genprog.dumbdog.game.Room

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
data class PacketSubmitAnswer(val answerKey: String): PacketBase("SUBMIT")
data class PacketRoomUpdate(val room: Room): PacketBase("ROOM_UPDATE")
data class PacketNewRound(val options: Array<String>, val key: String): PacketBase("NEW_ROUND")
data class PacketRoundEnd(val correct: List<Player>, val answer: String, val isGameEnd: Boolean): PacketBase("END_ROUND")
data class PacketPartyMode(val enabled: Boolean): PacketBase("PARTY_MODE_SET")