package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.lexer.Compat.assert
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.constraints.applyToNextLineAndAddModifiers
import org.intellij.markdown.parser.constraints.getCharsEaten
import org.intellij.markdown.parser.constraints.upstreamWith
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl

class ParagraphMarkerBlock(constraints: MarkdownConstraints,
                           marker: ProductionHolder.Marker,
                           val interruptsParagraph: (LookaheadText.Position, MarkdownConstraints) -> Boolean)
        : MarkerBlockImpl(constraints, marker) {
    override fun allowsSubBlocks(): Boolean = false

    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = true

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
        return pos.nextLineOrEofOffset
    }

    override fun doProcessToken(pos: LookaheadText.Position,
                                currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {

        if (pos.offsetInCurrentLine != -1) {
            return MarkerBlock.ProcessingResult.CANCEL
        }

        assert(pos.offsetInCurrentLine == -1)

        if (MarkdownParserUtil.calcNumberOfConsequentEols(pos, constraints) >= 2) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        val nextLineConstraints = constraints.applyToNextLineAndAddModifiers(pos)
        if (!nextLineConstraints.upstreamWith(constraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        val posToCheck = pos.nextPosition(1 + nextLineConstraints.getCharsEaten(pos.currentLine))
        if (posToCheck == null || interruptsParagraph(posToCheck, nextLineConstraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        return MarkerBlock.ProcessingResult.CANCEL
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownElementTypes.PARAGRAPH
    }

}
