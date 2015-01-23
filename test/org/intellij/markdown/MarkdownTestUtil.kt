package org.intellij.markdown

import java.io.File
import kotlin.test.fail
import com.intellij.rt.execution.junit.FileComparisonFailure
import junit.framework.TestCase

public fun TestCase.assertSameLinesWithFile(filePath: String, text: String) {
    val file = File(filePath)

    if (!file.exists()) {
        file.writeText(text)
        fail("File not found. Created $filePath.")
    }

    val fileText = file.readText()
    if (!fileText.equals(text)) {
        throw FileComparisonFailure("File contents differ from the answer", fileText, text, filePath)
    }
}

val TestCase.testName : String
    get() {
        val name = getName()
        assert(name.startsWith("test"))
        return name.substring("test".length()).decapitalize()
    }