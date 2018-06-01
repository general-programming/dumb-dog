package ch.offbeatwit.dumbdog.game

import ch.offbeatwit.dumbdog.getResourceList
import java.net.URL
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class QuestionManager(private val state: GameState) {
    private val r = Random()
    private val keys: Array<String>
    val questions: HashMap<String, QuestionEntry> = hashMapOf()

    init {
        javaClass.getResourceList("/questions").forEach {
            var key = state.generator.generateShortId()
            val text = it.substringAfter('/').substringBeforeLast('.')
                    .replace('-', ' ')

            while (questions.containsKey(key)) {
                key = state.generator.generateShortId()
            }

            questions[key] = QuestionEntry(javaClass.getResource("/questions/$it"), text)
        }

        keys = questions.keys.toTypedArray()

//        println(keys.joinToString(", ", "[", "]"))
    }

    fun randomQuestion(): Question {
        val key = keys[r.nextInt(keys.size)]

        return Question(key, questions[key]!!.text)
    }

    fun randomQuestionText(): String {
        val key = keys[r.nextInt(keys.size)]

        return questions[key]!!.text
    }

    data class QuestionEntry(val path: URL, val text: String)
}