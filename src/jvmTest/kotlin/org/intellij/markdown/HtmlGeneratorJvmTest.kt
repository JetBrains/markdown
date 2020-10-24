package org.intellij.markdown

import org.intellij.markdown.html.URI
import org.junit.Test

class HtmlGeneratorJvmTest : HtmlGeneratorTestBase() {
    override fun getTestDataPath(): String {
        return getIntellijMarkdownHome() + "/src/jvmTest/resources/data/html"
    }
    @Test
    fun testBaseUriFile() {
        defaultTest(baseURI = URI("file:///c:/foo/bar.html"))
    }
}