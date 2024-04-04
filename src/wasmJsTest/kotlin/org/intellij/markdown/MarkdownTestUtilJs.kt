package org.intellij.markdown

import org.intellij.markdown.nodejs.FileSystem
import org.intellij.markdown.nodejs.Path
import org.intellij.markdown.nodejs.process
import kotlin.test.assertTrue

private val fsSeparator = lazy { Path.sep[0] }

private val intellijMarkdownHome: Lazy<String> = lazy {
    val sep = fsSeparator.value
    var dir = process.cwd()
    while (!FileSystem.existsSync("$dir${sep}README.md")) {
        dir = dir.substringBeforeLast(sep, "")
        if (dir.isEmpty()) {
            error("could not find repo root. cwd=${process.cwd()}")
        }
    }
    dir
}

actual fun readFromFile(path: String): String =
    FileSystem.readFileSync(path, "utf-8")

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
    actual inline fun getName(): String {
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
                .first { it.startsWith("test") }
        }
    }
}
