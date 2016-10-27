package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl

class BlockQuoteMarkerBlock(myConstraints: MarkdownConstraints, marker: ProductionHolder.Marker) : MarkerBlockImpl(myConstraints, marker) {
    override fun allowsSubBlocks(): Boolean = true

    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = pos.char == '\n'

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
        return pos.nextLineOffset ?: -1
    }

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(pos: LookaheadText.Position, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        assert(pos.char == '\n')

        val nextLineConstraints = MarkdownConstraints.fromBase(pos, constraints)
        // That means nextLineConstraints are "shorter" so our blockquote char is absent
        if (!nextLineConstraints.extendsPrev(constraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        return MarkerBlock.ProcessingResult.PASS
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownElementTypes.BLOCK_QUOTE
    }
}
