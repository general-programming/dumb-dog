package gq.genprog.dumbdog.game

import java.io.File
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
        val dir = File("questions/")
        if (dir.isDirectory) {
            dir.listFiles().forEach {
                var key = state.generator.generateShortId()
                val path = it.path
                val text = path.substringAfter('/').substringBeforeLast('.')
                        .replace('-', ' ')

                while (questions.containsKey(key)) {
                    key = state.generator.generateShortId()
                }

                questions[key] = QuestionEntry(it.toURI().toURL(), text)
            }

            println("Loaded ${questions.size} questions.")
        } else {
            println("Missing questions directory. You won't be able to start a game without it!")
        }

        keys = questions.keys.toTypedArray()
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