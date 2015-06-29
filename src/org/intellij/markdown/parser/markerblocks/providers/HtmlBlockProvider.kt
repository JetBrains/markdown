package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.HtmlBlockMarkerBlock
import kotlin.text.Regex

public class HtmlBlockProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(pos: LookaheadText.Position, productionHolder: ProductionHolder, stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {
        if (!Character.isWhitespace(pos.char) && matches(pos)) {
            return listOf(HtmlBlockMarkerBlock(stateInfo.currentConstraints, productionHolder.mark()))
        }
        return emptyList()
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        return matches(pos)
    }

    private fun matches(pos: LookaheadText.Position): Boolean {
        return REGEX.hasMatch(pos.textFromPosition)
    }

    companion object {
        val TAG_NAMES =
                "article, header, aside, hgroup, blockquote, hr, iframe, body, li, map, button, object, canvas, " +
                        "ol, caption, output, col, p, colgroup, pre, dd, progress, div, section, dl, table, td, dt, " +
                        "tbody, embed, textarea, fieldset, tfoot, figcaption, th, figure, thead, footer, tr, form, " +
                        "ul, h1, h2, h3, h4, h5, h6, video, script, style"

        val HTML_BLOCK_TAG = Regex("</?(?i:${TAG_NAMES.replace(", ", "|")})")

        val HTML_COMMENT = Regex("<!--([^-]|-[^-])*-->")

        val PROCESSING_INSTRUCTION = Regex("<\\?([^?]|\\?[^>])*\\?>")

        val DECLARATION = Regex("<![A-Z]+ + [^>]>")

        val CDATA = Regex("<!\\[CDATA\\[([^\\]]|\\][^\\]]|\\]\\][^>])*\\]\\]>")

        val REGEX = Regex("^ *(${HTML_BLOCK_TAG}|${HTML_COMMENT}|${PROCESSING_INSTRUCTION}|${DECLARATION}|${CDATA})")
    }
}