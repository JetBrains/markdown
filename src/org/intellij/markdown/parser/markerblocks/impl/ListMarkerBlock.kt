package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl

public class ListMarkerBlock(myConstraints: MarkdownConstraints,
                             marker: ProductionHolder.Marker,
                             private val listType: Char)
    : MarkerBlockImpl(myConstraints, marker) {

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int? {
        return pos.nextLineOffset
    }

    override fun doProcessToken(pos: LookaheadText.Position,
                                currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        assert(pos.char == '\n')

        val eolN = MarkdownParserUtil.calcNumberOfConsequentEols(pos)
        if (eolN >= 3) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        val nonemptyPos = MarkdownParserUtil.getFirstNonWhitespaceLinePos(pos)
                ?: return MarkerBlock.ProcessingResult.DEFAULT
        val nextLineConstraints = MarkdownConstraints.fromBase(nonemptyPos, constraints)
        if (!nextLineConstraints.extendsList(constraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        return MarkerBlock.ProcessingResult.PASS
    }

    override fun getDefaultNodeType(): IElementType {
        return if (listType == '-' || listType == '*')
            MarkdownElementTypes.UNORDERED_LIST
        else
            MarkdownElementTypes.ORDERED_LIST
    }
}
