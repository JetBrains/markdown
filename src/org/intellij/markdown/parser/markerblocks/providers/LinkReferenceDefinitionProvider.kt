package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.LinkReferenceDefinitionMarkerBlock
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import kotlin.text.Regex

public class LinkReferenceDefinitionProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(pos: LookaheadText.Position,
                                    productionHolder: ProductionHolder,
                                    stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {

        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, stateInfo.currentConstraints)) {
            return emptyList()
        }

        val matchResult = LINK_DEFINITION_REGEX.find(pos.textFromPosition) ?: return emptyList()
        var furthestOffset = 0
        for (i in 1..matchResult.groups.size - 1) {
            matchResult.groups[i]?.let { group ->
                productionHolder.addProduction(listOf(SequentialParser.Node(
                        addToRangeAndWiden(group.range, pos.offset), when (i) {
                    1 -> MarkdownElementTypes.LINK_LABEL
                    2 -> MarkdownElementTypes.LINK_DESTINATION
                    3 -> MarkdownElementTypes.LINK_TITLE
                    else -> throw AssertionError("There are no more than three groups in this regex")
                })))
                furthestOffset = group.range.end
            }
        }

        val matchLength = furthestOffset - matchResult.range.start + 1
        val endPosition = pos.nextPosition(matchLength)

        if (endPosition != null && !isEndOfLine(endPosition)) {
            return emptyList()
        }
        return listOf(LinkReferenceDefinitionMarkerBlock(stateInfo.currentConstraints, productionHolder.mark(),
                pos.offset + matchLength))
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        return false
    }

    companion object {
        val WHSP = "[ \\t]*"

        val NOT_CHARS = { c: String ->
            val nonWhitespace = "(?:\\\\[$c]|[^ \\t\\n$c])"
            val anyChar = "(?:\\\\[$c]|[^$c\\n])"
            "$anyChar*(?:\\n$anyChar*$nonWhitespace$WHSP)*(?:\\n$WHSP)?"
        }

        val LINK_LABEL = "\\[${NOT_CHARS("\\[\\]")}\\]"

        val NONCONTROL = "(?:\\\\[\\(\\)]|[^ \\n\\t\\(\\)])"

        val LINK_DESTINATION = "(?:<(?:\\\\[<>]|[^<>])*>|${NONCONTROL}*\\(${NONCONTROL}*\\)${NONCONTROL}*|${NONCONTROL}+)"

        val LINK_TITLE = "(?:\"${NOT_CHARS("\"")}\"|'${NOT_CHARS("'")}'|\\(${NOT_CHARS("\\)")}\\))"

        val NO_MORE_ONE_NEWLINE = "[ \\t]*(?:\\n[ \\t]*)?"

        val LINK_DEFINITION_REGEX = Regex(
                "^ {0,3}(${LINK_LABEL}):" +
                        "${NO_MORE_ONE_NEWLINE}(${LINK_DESTINATION})" +
                        "(?:${NO_MORE_ONE_NEWLINE}(${LINK_TITLE})$WHSP(?:\\n|$))?"
        )

        fun addToRangeAndWiden(range: IntRange, t: Int): IntRange {
            return IntRange(range.start + t, range.end + t + 1)
        }

        fun isEndOfLine(pos: LookaheadText.Position): Boolean {
            return pos.offsetInCurrentLine == -1 || pos.charsToNonWhitespace() == null
        }
    }
}
