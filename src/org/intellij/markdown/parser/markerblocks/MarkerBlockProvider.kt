package org.intellij.markdown.parser.markerblocks

import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

public interface MarkerBlockProvider<T : MarkerProcessor.StateInfo> {
    fun createMarkerBlocks(pos: LookaheadText.Position,
                           productionHolder: ProductionHolder,
                           stateInfo: T): List<MarkerBlock>

    fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean

    companion object {
        public fun addTokenFromConstraints(proH: ProductionHolder,
                                           pos: LookaheadText.Position,
                                           oldC: MarkdownConstraints,
                                           newC: MarkdownConstraints) {
            val startOffset = pos.offset - pos.offsetInCurrentLine + oldC.getCharsEaten(pos.currentLine)
            val endOffset = Math.min(pos.offset - pos.offsetInCurrentLine + newC.getCharsEaten(pos.currentLine), pos.nextLineOrEofOffset)

            val type = when (newC.getLastType()) {
                '>' ->
                    MarkdownTokenTypes.BLOCK_QUOTE
                '.', ')' ->
                    MarkdownTokenTypes.LIST_NUMBER
                else ->
                    MarkdownTokenTypes.LIST_BULLET
            }
            proH.addProduction(listOf(SequentialParser.Node(startOffset..endOffset, type)))
        }

        public fun isStartOfLineWithConstraints(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
            return pos.offsetInCurrentLine == constraints.getCharsEaten(pos.currentLine)
        }
    }
}