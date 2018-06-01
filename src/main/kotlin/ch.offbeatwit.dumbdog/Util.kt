package ch.offbeatwit.dumbdog

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
fun <T> Class<T>.getResourceList(path: String): List<String> {
    val stream = this.getResourceAsStream(path)
    val reader = BufferedReader(InputStreamReader(stream))
    val filenames = arrayListOf<String>()

    var resource: String? = reader.readLine()
    while (resource != null) {
        filenames.add(resource)

        resource = reader.readLine()
    }

    return filenames
}