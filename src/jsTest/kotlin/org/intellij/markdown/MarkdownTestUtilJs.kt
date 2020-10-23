package org.intellij.markdown

import kotlin.test.assertEquals

actual fun readFromFile(path: String): String {
    return js("require('fs').readFileSync")(path).toString()
}

actual fun assertSameLinesWithFile(path: String, result: String) {
    assertEquals(readFromFile(path), result)
}

actual fun getIntellijMarkdownHome(): String {
    return (js("process.cwd()") as String) + "/.."
}

actual abstract class TestCase {
    actual fun getName(): String {
        try {
            throw Exception()
        }
        catch (e: Exception) {
            val stack = e.stackTraceToString()
            val matches = Regex("\\s+at (\\S+) ").findAll(stack)
            return matches
                    .map { it.groupValues[1] }
                    .filter { it.contains('.') }
                    .map { it.split('.').last() }
                    .filter { it.startsWith("test") }
                    .first()
        }
    }
}