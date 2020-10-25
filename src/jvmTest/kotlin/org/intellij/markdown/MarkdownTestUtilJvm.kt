package org.intellij.markdown

import junit.framework.AssertionFailedError
import java.io.File
import kotlin.test.assertEquals

actual fun readFromFile(path: String): String {
    return File(path).readText()
}

actual fun assertSameLinesWithFile(path: String, result: String) {
    val file = File(path)

    if (!file.exists()) {
        file.writeText(result)
        throw AssertionFailedError("File not found. Created $path.")
    }

    val fileText = file.readText()
    assertEquals(fileText, result)
}

actual fun getIntellijMarkdownHome(): String {
    return System.getProperty(INTELLIJ_MARKDOWN_TEST_KEY) ?: System.getProperty("user.dir")
}

actual typealias TestCase = junit.framework.TestCase