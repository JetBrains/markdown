package org.intellij.markdown

import com.intellij.rt.execution.junit.FileComparisonFailure
import junit.framework.AssertionFailedError
import junit.framework.TestCase
import java.io.File

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
    if (fileText != result) {
        throw FileComparisonFailure("File contents differ from the answer", fileText, result, path)
    }
}

actual fun getIntellijMarkdownHome(): String {
    return System.getProperty(INTELLIJ_MARKDOWN_TEST_KEY) ?: System.getProperty("user.dir") + "/.."
}

actual typealias TestCase = junit.framework.TestCase