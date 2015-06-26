package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl

public class SetextHeaderMarkerBlock(myConstraints: MarkdownConstraints,
                                     productionHolder: ProductionHolder)
        : MarkerBlockImpl(myConstraints, productionHolder.mark()) {

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int? {
        return pos.nextLineOffset
    }

    private var nodeType: IElementType = MarkdownElementTypes.SETEXT_1

    override fun getDefaultNodeType(): IElementType {
        return nodeType
    }

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DROP
    }

    override fun doProcessToken(pos: LookaheadText.Position,
                                currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        val startSpaces = pos.charsToNonWhitespace()
                ?: return MarkerBlock.ProcessingResult(MarkerBlock.ClosingAction.DROP, MarkerBlock.ClosingAction.DROP, MarkerBlock.EventAction.PROPAGATE)

        if (pos.nextPosition(startSpaces)?.char == '-') {
            nodeType = MarkdownElementTypes.SETEXT_2
        }

        scheduleProcessingResult(pos.nextLineOrEofOffset, MarkerBlock.ProcessingResult.DEFAULT)
        return MarkerBlock.ProcessingResult.CANCEL
    }
}
