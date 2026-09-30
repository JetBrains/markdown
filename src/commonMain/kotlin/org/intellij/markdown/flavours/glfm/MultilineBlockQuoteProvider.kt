package org.intellij.markdown.flavours.glfm

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider

/**
 * Provides [MultilineBlockQuoteMarkerBlock] when a line consisting solely of
 * `>>>` is encountered.
 */
class MultilineBlockQuoteProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {

    override fun createMarkerBlocks(
        pos: LookaheadText.Position,
        productionHolder: ProductionHolder,
        stateInfo: MarkerProcessor.StateInfo,
    ): List<MarkerBlock> {
        if (!isFenceLine(pos)) return emptyList()

        return listOf(
            MultilineBlockQuoteMarkerBlock(stateInfo.currentConstraints, productionHolder)
        )
    }

    override fun interruptsParagraph(
        pos: LookaheadText.Position,
        constraints: MarkdownConstraints,
    ): Boolean = isFenceLine(pos)

    companion object {
        fun isFenceLine(pos: LookaheadText.Position): Boolean =
            pos.currentLine.trim() == MultilineBlockQuoteMarkerBlock.FENCE &&
            pos.currentLineFromPosition.trimEnd() == MultilineBlockQuoteMarkerBlock.FENCE
    }
}
