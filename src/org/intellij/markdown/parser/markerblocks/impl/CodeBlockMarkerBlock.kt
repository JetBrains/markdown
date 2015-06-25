package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl

public class CodeBlockMarkerBlock(myConstraints: MarkdownConstraints, marker: ProductionHolder.Marker) : MarkerBlockImpl(myConstraints, marker) {
    private var realInterestingOffset = -1

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int? {
        return pos.offset + 1
    }

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(pos: LookaheadText.Position, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (pos.offset < realInterestingOffset) {
            return MarkerBlock.ProcessingResult.CANCEL
        }

        // Eat everything if we're on code line
        if (pos.char != '\n') {
            return MarkerBlock.ProcessingResult.CANCEL
        }

        assert(pos.char == '\n')

        val nonemptyPos = MarkdownParserUtil.findNonEmptyLineWithSameConstraints(constraints, pos)
            ?: return MarkerBlock.ProcessingResult.DEFAULT

        val nextConstraints = MarkdownConstraints.fromBase(nonemptyPos, constraints)
        val shifted = nonemptyPos.nextPosition(1 + nextConstraints.getIndent())
        val nonWhitespace = shifted?.nextPosition(shifted.charsToNonWhitespace() ?: 0)

        if (!MarkdownParserUtil.hasCodeBlockIndent(nonemptyPos, nextConstraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        } else {
            realInterestingOffset = nonemptyPos.offset
            return MarkerBlock.ProcessingResult.CANCEL
        }
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownElementTypes.CODE_BLOCK
    }
}
