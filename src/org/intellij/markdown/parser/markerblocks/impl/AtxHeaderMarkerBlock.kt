package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl

public class AtxHeaderMarkerBlock(myConstraints: MarkdownConstraints,
                                  productionHolder: ProductionHolder,
                                  headerSize: Int)
        : MarkerBlockImpl(myConstraints, productionHolder.mark()) {
    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = true

    private val nodeType = calcNodeType(headerSize)

    private val startPosition = productionHolder.currentPosition + headerSize

    private fun calcNodeType(headerSize: Int): IElementType {
        when (headerSize) {
            1 -> return MarkdownElementTypes.ATX_1
            2 -> return MarkdownElementTypes.ATX_2
            3 -> return MarkdownElementTypes.ATX_3
            4 -> return MarkdownElementTypes.ATX_4
            5 -> return MarkdownElementTypes.ATX_5
            6 -> return MarkdownElementTypes.ATX_6
            else -> return MarkdownElementTypes.ATX_6
        }
    }

    override fun getDefaultNodeType(): IElementType {
        return nodeType
    }

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int? {
        return pos.offset + 1
    }

    override fun doProcessToken(pos: LookaheadText.Position,
                                currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (pos.char == '\n') {
            return MarkerBlock.ProcessingResult(MarkerBlock.ClosingAction.DROP, MarkerBlock.ClosingAction.DONE, MarkerBlock.EventAction.PROPAGATE)
        }
        return MarkerBlock.ProcessingResult.CANCEL;
    }

}