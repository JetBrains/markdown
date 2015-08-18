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
import java.util.ArrayList
import kotlin.text.Regex

public class LinkReferenceDefinitionProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(pos: LookaheadText.Position,
                                    productionHolder: ProductionHolder,
                                    stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {

        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, stateInfo.currentConstraints)) {
            return emptyList()
        }

        val nodesForProduction = ArrayList<SequentialParser.Node>()

        val prepareAnswer = { endOffset: Int ->
            productionHolder.addProduction(nodesForProduction)
            listOf(LinkReferenceDefinitionMarkerBlock(stateInfo.currentConstraints, productionHolder.mark(), endOffset))
        }

        var currentPos = pos

        var matchResult = FIRST_LINE.match(currentPos.currentLineFromPosition) ?: return emptyList()
        matchResult.groups[1]?.range?.let { nodesForProduction.add(SequentialParser.Node(
                addToRangeAndWiden(it, currentPos.offset), MarkdownElementTypes.LINK_LABEL)) }

        currentPos = currentPos.nextPosition(matchResult.range.end - matchResult.range.start + 1) ?: return emptyList()
        currentPos = travelToNextLineOrNonWhitespace(currentPos, stateInfo.currentConstraints)

        matchResult = SECOND_LINE.match(currentPos.currentLineFromPosition) ?: return emptyList()
        nodesForProduction.add(SequentialParser.Node(
                addToRangeAndWiden(matchResult.range, currentPos.offset), MarkdownElementTypes.LINK_DESTINATION))

        currentPos = currentPos.nextPosition(matchResult.range.end - matchResult.range.start + 1)
                ?: return prepareAnswer(currentPos.nextLineOrEofOffset)
        val endPosition = currentPos

        currentPos = travelToNextLineOrNonWhitespace(currentPos, stateInfo.currentConstraints)

        THIRD_LINE.match(currentPos.currentLineFromPosition)?.let { match ->
            val afterThis = currentPos.nextPosition(match.range.end - match.range.start + 1)
            if (afterThis == null || isEndOfLine(afterThis)) {
                nodesForProduction.add(SequentialParser.Node(
                        addToRangeAndWiden(match.range, currentPos.offset), MarkdownElementTypes.LINK_TITLE))
                return prepareAnswer(currentPos.offset + match.range.end - match.range.start + 1)
            }
        }

        if (!isEndOfLine(endPosition)) {
            return emptyList()
        }

        return prepareAnswer(endPosition.offset)
    }

    private fun travelToNextLineOrNonWhitespace(currentPos: LookaheadText.Position, constraints: MarkdownConstraints): LookaheadText.Position {
        val charsToNonWhitespace = currentPos.charsToNonWhitespace()
        if (charsToNonWhitespace != null) {
            return currentPos.nextPosition(charsToNonWhitespace) ?: currentPos
        }

        val nextLinePosition = currentPos.nextLinePosition()?.nextPosition(constraints.getIndent()) ?: return currentPos
        return nextLinePosition.charsToNonWhitespace()?.let { num -> nextLinePosition.nextPosition(num) }
                ?: currentPos
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        return false
    }

    companion object {
        val LINK_LABEL = "\\[(?:\\\\[\\[\\]]|[^\\[\\]])*[^ \\t](?:\\\\[\\[\\]]|[^\\[\\]])*\\]"

        val NONCONTROL = "(?:[^ \t\\(\\)]|\\\\[\\(\\)])"

        val LINK_DESTINATION = "(?:<(?:\\\\[<>]|[^<>])*>|${NONCONTROL}*\\(${NONCONTROL}*\\)${NONCONTROL}*|${NONCONTROL}+)"

        val LINK_TITLE = "(?:\"[^\"]*\"|'[^']*'|\\((?:\\\\\\)|[^\\)])*\\))"

        val FIRST_LINE = Regex("^ {0,3}(${LINK_LABEL}):")

        val SECOND_LINE = Regex(LINK_DESTINATION)

        val THIRD_LINE = Regex(LINK_TITLE)

        fun addToRangeAndWiden(range: IntRange, t: Int): IntRange {
            return IntRange(range.start + t, range.end + t + 1)
        }

        fun isEndOfLine(pos: LookaheadText.Position): Boolean {
            return pos.offsetInCurrentLine == -1 || pos.charsToNonWhitespace() == null
        }
    }
}
