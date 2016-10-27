package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.BlockQuoteMarkerBlock

class BlockQuoteProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(pos: LookaheadText.Position,
                                   productionHolder: ProductionHolder,
                                   stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {
//        if (Character.isWhitespace(pos.char)) {
//            return emptyList()
//        }

        val currentConstraints = stateInfo.currentConstraints
        val nextConstraints = stateInfo.nextConstraints
        if (pos.offsetInCurrentLine != currentConstraints.getCharsEaten(pos.currentLine)) {
            return emptyList()
        }
        if (nextConstraints != currentConstraints && nextConstraints.getLastType() == '>') {
            return listOf(BlockQuoteMarkerBlock(nextConstraints, productionHolder.mark()))
        } else {
            return emptyList()
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        // Actually, blockquote may interrupt a paragraph, but we have MarkdownConstraints for these cases
        return false
    }
}