package org.intellij.markdown

import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import kotlin.test.assertEquals


abstract class SpecTest(private val flavour: MarkdownFlavourDescriptor) : TestCase() {
    fun doTest(markdown: String, html: String) {
        val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)
        val generated = HtmlGenerator(markdown, tree, flavour, false).generateHtml()
        assertEquals(
                normalizeHtml(html),
                normalizeHtml(generated).removeSurrounding("<body>", "</body>")
        )
    }
}

private val HTML_TAG_REGEX = Regex("<[^>]+>")

/** Remove newlines from [html], unless they occur in a tag or within text enclosed by `<pre>` tags. */
private fun normalizeHtml(html: String): String = buildString {
    val tags = HTML_TAG_REGEX.findAll(html)
    var insidePre = false
    var lastAppended = 0

    fun String.normalizeWhitespace() = if (insidePre) this else replace("\n", "")

    for (tag in tags) {
        if (tag.range.first > lastAppended) {
            append(html.substring(lastAppended, tag.range.first).normalizeWhitespace())
        }

        if (tag.value == "<pre>") insidePre = true
        else if (tag.value == "</pre>") insidePre = false

        append(tag.value)
        lastAppended = tag.range.last + 1
    }

    if (lastAppended < html.length) {
        append(html.substring(lastAppended).normalizeWhitespace())
    }
}
