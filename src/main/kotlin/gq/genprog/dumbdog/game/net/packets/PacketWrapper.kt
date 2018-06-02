package gq.genprog.dumbdog.game.net.packets

import com.google.gson.JsonElement

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
data class PacketWrapper(val t: String, val d: JsonElement)