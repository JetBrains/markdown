package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl

public class LinkReferenceDefinitionMarkerBlock(myConstraints: MarkdownConstraints,
                                                marker: ProductionHolder.Marker,
                                                private val endPosition: Int)
: MarkerBlockImpl(myConstraints, marker) {
    override fun allowsSubBlocks(): Boolean = false

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(pos: LookaheadText.Position, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (pos.offset < endPosition) {
            return MarkerBlock.ProcessingResult.CANCEL
        }
        return MarkerBlock.ProcessingResult.DEFAULT
    }

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
        return pos.offset + 1
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownElementTypes.LINK_DEFINITION
    }

    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean {
        return true
    }

}