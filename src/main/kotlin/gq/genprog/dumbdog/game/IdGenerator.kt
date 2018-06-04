package gq.genprog.dumbdog.game

import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class IdGenerator {
    private val r = Random()
    private val adjectives = javaClass.getResource("/pools/adjectives.txt").readText().lines()
    private val animals = javaClass.getResource("/pools/animals.txt").readText().lines()
    private val letters = "abcdefghijklmnopqrstuvwxyz0123456789"

    init {
        println("Loaded ${adjectives.size} adjectives.")
        println("Loaded ${animals.size} animals.")
    }

    fun <T> List<T>.randomFrom(): T {
        return this[r.nextInt(this.size)]
    }

    fun String.randomSelection(n: Int): String {
        var i = 0

        return StringBuilder().also {
            while (i < n) {
                it.append(this@randomSelection[r.nextInt(this.length)])
                i++
            }
        }.toString()
    }

    fun generateHumanId(): String {
        return adjectives.randomFrom() + "-" + animals.randomFrom()
    }

    fun generateShortId(): String {
        return letters.randomSelection(8)
    }
}