package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.CommonMarkdownConstraints
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.constraints.getCharsEaten
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.BlockQuoteMarkerBlock

/**
 * @param lazyContinuation `true` if a paragraph inside a block quote may continue on a line without the `>` marker
 * (a [lazy continuation line](https://spec.commonmark.org/0.31.2/#lazy-continuation-line)), as the CommonMark spec
 * requires, and `false` if such a line ends the block quote instead. Lazy continuation of list items is not affected,
 * and a link reference definition inside a block quote may still span such a line.
 */
class BlockQuoteProvider(private val lazyContinuation: Boolean = true) : MarkerBlockProvider<MarkerProcessor.StateInfo> {
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
        if (nextConstraints != currentConstraints && nextConstraints.types.lastOrNull() == '>') {
            return listOf(BlockQuoteMarkerBlock(nextConstraints, productionHolder.mark()))
        } else {
            return emptyList()
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        // Actually, blockquote may interrupt a paragraph, but we have MarkdownConstraints for these cases
        return false
    }

    /**
     * Whether a line with [constraints], which would continue a paragraph with [paragraphConstraints] otherwise,
     * ends that paragraph instead because it is a lazy continuation line.
     */
    internal fun endsParagraph(constraints: MarkdownConstraints, paragraphConstraints: MarkdownConstraints): Boolean {
        // The line constraints are a prefix of the paragraph ones, so shorter ones lack some block quote marker
        return !lazyContinuation
                && constraints.types.size <= paragraphConstraints.types.lastIndexOf(CommonMarkdownConstraints.BQ_CHAR)
    }
}