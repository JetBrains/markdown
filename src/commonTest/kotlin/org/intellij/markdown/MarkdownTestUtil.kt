package org.intellij.markdown

import org.intellij.markdown.lexer.Compat.assert

const val INTELLIJ_MARKDOWN_TEST_KEY = "Intellij.markdown.home"

const val MARKDOWN_TEST_DATA_PATH = "src/commonTest/resources/data"

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