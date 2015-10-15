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
        val matchingGroup = matches(pos, stateInfo.currentConstraints)
        if (matchingGroup != -1) {
            return listOf(HtmlBlockMarkerBlock(stateInfo.currentConstraints, productionHolder, OPEN_CLOSE_REGEXES[matchingGroup].second, pos))
        }
        return emptyList()
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        return matches(pos, constraints) in 0..5
    }

    private fun matches(pos: LookaheadText.Position, constraints: MarkdownConstraints): Int {
        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, constraints)) {
            return -1
        }
        val matchResult = FIND_START_REGEX.match(pos.currentLineFromPosition)
                ?: return -1
        assert(matchResult.groups.size == OPEN_CLOSE_REGEXES.size + 1) { "There are some excess capturing groups probably!" }
        for (i in 1..OPEN_CLOSE_REGEXES.size) {
            if (matchResult.groups[i] != null) {
                return i - 1
            }
        }
        assert(false) { "Match found but all groups are empty!" }
        return -1
    }

    companion object {
        val TAG_NAMES =
                "address, article, aside, base, basefont, blockquote, body, caption, center, col, colgroup, dd, details, " +
                        "dialog, dir, div, dl, dt, fieldset, figcaption, figure, footer, form, frame, frameset, h1, " +
                        "head, header, hr, html, legend, li, link, main, menu, menuitem, meta, nav, noframes, ol, " +
                        "optgroup, option, p, param, pre, section, source, title, summary, table, tbody, td, tfoot, " +
                        "th, thead, title, tr, track, ul"

        val TAG_NAME = "[a-zA-Z][a-zA-Z0-9-]*"

        val ATTR_NAME = "[A-Za-z:_][A-Za-z0-9_.:-]*"

        val ATTR_VALUE = "\\s*=\\s*(?:[^ \"'=<>`]+|'[^']*'|\"[^\"]*\")"

        val ATTRIBUTE = "\\s+${ATTR_NAME}(?:${ATTR_VALUE})?"

        val OPEN_TAG = "<${TAG_NAME}(?:${ATTRIBUTE})*\\s*/?>"

        /**
         * Closing tag allowance is not in public spec version yet
         */
        val CLOSE_TAG = "</${TAG_NAME}\\s*>"

        /** see {@link http://spec.commonmark.org/0.21/#html-blocks}
         *
         * nulls mean "Next line should be blank"
         * */
        val OPEN_CLOSE_REGEXES: List<Pair<Regex, Regex?>> = listOf(
                Pair(Regex("<(?i:script|pre|style)(?: |>|$)"), Regex("</(?i:script|style|pre)>")),
                Pair(Regex("<!--"), Regex("-->")),
                Pair(Regex("<\\?"), Regex("\\?>")),
                Pair(Regex("<![A-Z]"), Regex(">")),
                Pair(Regex("<!\\[CDATA\\["), Regex("\\]\\]>")),
                Pair(Regex("</?(?i:${TAG_NAMES.replace(", ", "|")})(?: |/?>|$)"), null),
                Pair(Regex("(?:${OPEN_TAG}|${CLOSE_TAG})(?: |$)"), null)
        )

        val FIND_START_REGEX = Regex(
                OPEN_CLOSE_REGEXES.joinToString(separator = "|", transform = { "^ {0,3}(${it.first.pattern})" })
        )

    }
}