package org.intellij.markdown.flavours.glfm

import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl

/**
 * Marker block for GitLab multiline blockquote syntax.
 *
 * A `>>>` on its own line opens the block; the next `>>>` on its own line closes it.
 * Content between the fence lines is emitted as BLOCK_QUOTE content, allowing
 * the normal paragraph / inline parsers to process it.
 */
class MultilineBlockQuoteMarkerBlock(
    myConstraints: MarkdownConstraints,
    productionHolder: ProductionHolder,
) : MarkerBlockImpl(myConstraints, productionHolder.mark()) {

    override fun allowsSubBlocks(): Boolean = true

    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = true

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int =
        pos.nextLineOrEofOffset

    override fun getDefaultAction(): MarkerBlock.ClosingAction =
        MarkerBlock.ClosingAction.DONE

    override fun doProcessToken(
        pos: LookaheadText.Position,
        currentConstraints: MarkdownConstraints,
    ): MarkerBlock.ProcessingResult {
        if (pos.offsetInCurrentLine != -1) {
            return MarkerBlock.ProcessingResult.CANCEL
        }

        if (isClosingFence(pos.currentLine)) {
            scheduleProcessingResult(pos.nextLineOrEofOffset, MarkerBlock.ProcessingResult.DEFAULT)
        }

        return MarkerBlock.ProcessingResult.CANCEL
    }

    override fun getDefaultNodeType(): IElementType = GLFMElementTypes.MULTILINE_BLOCK_QUOTE

    companion object {
        const val FENCE = ">>>"

        fun isClosingFence(line: CharSequence): Boolean =
            line.trim() == FENCE
    }
}
