package org.intellij.markdown

import kotlin.test.assertTrue

private val fsSeparator = lazy {
    (js("require('path').sep") as String)[0]
}

private val intellijMarkdownHome: Lazy<String> = lazy {
    val sep = fsSeparator.value
    var dir = (js("process.cwd()") as String)
    while (!js("require('fs').existsSync")("$dir${sep}README.md") as Boolean) {
        dir = dir.substringBeforeLast(sep, "")
        if (dir.isEmpty()) {
            error("could not find repo root. cwd=${js("process.cwd()")}")
        }
    }
    dir
}

actual fun readFromFile(path: String): String {
    return js("require('fs').readFileSync")(path, "utf-8") as String
}

actual fun assertSameLinesWithFile(path: String, result: String) {
    assertEqualsIdeaFriendly(readFromFile(path), result)
}

actual fun getIntellijMarkdownHome(): String {
    return intellijMarkdownHome.value
}

private fun assertEqualsIdeaFriendly(expected: String, actual: String) {
    if (actual != expected) {
        @Suppress("ReplaceAssertBooleanWithAssertEquality")
        assertTrue(actual == expected, "expected:<$expected> but was:<$actual>")
    }
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
                    .map {
                        // Kotlin-JS compiler might add _<number> to method names
                        val trimMatch = it.match("^(\\S+)_\\d+$")
                        trimMatch?.get(1) ?: it
                    }
                    .first()
        }
    }
}