package org.intellij.markdown

import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.DUMMY_ATTRIBUTES_CUSTOMIZER
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.html.URI
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkdownParser

abstract class HtmlGeneratorTestBase : TestCase() {
    protected abstract fun getTestDataPath(): String

    protected fun defaultTest(flavour: MarkdownFlavourDescriptor = CommonMarkFlavourDescriptor(),
                              baseURI: URI? = null,
                              tagRenderer: HtmlGenerator.TagRenderer = HtmlGenerator.DefaultTagRenderer(
                                  DUMMY_ATTRIBUTES_CUSTOMIZER, false)) {

        val result = generateHtmlFromFile(flavour, baseURI, tagRenderer)
        assertSameLinesWithFile(getTestDataPath() + "/" + testName + ".txt", result)
    }

    protected fun generateHtmlFromFile(
        flavour: MarkdownFlavourDescriptor = CommonMarkFlavourDescriptor(),
        baseURI: URI? = null,
        tagRenderer: HtmlGenerator.TagRenderer = HtmlGenerator.DefaultTagRenderer(DUMMY_ATTRIBUTES_CUSTOMIZER, false)
    ): String {
        val src = readFromFile(getTestDataPath() + "/" + testName + ".md")
        val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(src)
        val htmlGeneratingProviders = flavour.createHtmlGeneratingProviders(LinkMap.buildLinkMap(tree, src), baseURI)
        val html =
            HtmlGenerator(src, tree, htmlGeneratingProviders, includeSrcPositions = false).generateHtml(tagRenderer)

        return formatHtmlForTests(html)
    }

    companion object {
        fun formatHtmlForTests(html: String): String {
            val tags = Regex("</?[^>]+>")

            val split = tags.replace(html as CharSequence, { matchResult ->
                val next = matchResult.next()
                if (html[matchResult.range.start + 1] != '/'
                    && next != null
                    && html[next.range.start + 1] == '/') {
                    matchResult.value
                } else if (matchResult.value.contains("pre") && next?.value?.contains("code") == true
                    || matchResult.value.contains("/code") && next?.value?.contains("/pre") == true) {
                    matchResult.value
                } else {
                    matchResult.value + "\n"
                }
            })
            return split
        }

    }
}