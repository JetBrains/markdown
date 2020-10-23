package org.intellij.markdown

import org.intellij.markdown.lexer.Compat.assert

val INTELLIJ_MARKDOWN_TEST_KEY = "Intellij.markdown.home"

expect abstract class TestCase() {
    fun getName(): String
}

expect fun readFromFile(path: String): String

expect fun assertSameLinesWithFile(path: String, result: String)

expect fun getIntellijMarkdownHome(): String

val TestCase.testName: String
    get() {
        val name = getName()
        assert(name.startsWith("test"))
        return name.substring("test".length).decapitalize()
    }