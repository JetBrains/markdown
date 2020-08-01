package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.constraints.applyToNextLineAndAddModifiers
import org.intellij.markdown.parser.constraints.getCharsEaten
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

class CodeBlockMarkerBlock(myConstraints: MarkdownConstraints,
                           private val productionHolder: ProductionHolder,
                           startPosition: LookaheadText.Position)
: MarkerBlockImpl(myConstraints, productionHolder.mark()) {
    
    init {
        productionHolder.addProduction(listOf(SequentialParser.Node(
                startPosition.offset..startPosition.nextLineOrEofOffset, MarkdownTokenTypes.CODE_LINE)))
    }
    
    override fun allowsSubBlocks(): Boolean = false

    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = true

    private var realInterestingOffset = -1

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
        return pos.nextLineOrEofOffset
    }

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(pos: LookaheadText.Position, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (pos.offset < realInterestingOffset) {
            return MarkerBlock.ProcessingResult.CANCEL
        }

        // Eat everything if we're on code line
        if (pos.offsetInCurrentLine != -1) {
            return MarkerBlock.ProcessingResult.CANCEL
        }

        assert(pos.offsetInCurrentLine == -1)

        val nonemptyPos = MarkdownParserUtil.findNonEmptyLineWithSameConstraints(constraints, pos)
            ?: return MarkerBlock.ProcessingResult.DEFAULT

        val nextConstraints = constraints.applyToNextLineAndAddModifiers(nonemptyPos)
        val shifted = nonemptyPos.nextPosition(1 + nextConstraints.getCharsEaten(nonemptyPos.currentLine))
        val nonWhitespace = shifted?.nextPosition(shifted.charsToNonWhitespace() ?: 0)
            ?: return MarkerBlock.ProcessingResult.DEFAULT

        if (!MarkdownParserUtil.hasCodeBlockIndent(nonWhitespace, nextConstraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        } else {
            // We'll add the current line anyway
            val nextLineConstraints = constraints.applyToNextLineAndAddModifiers(pos)
            val nodeRange = pos.offset + 1 + nextLineConstraints.getCharsEaten(pos.currentLine)..pos.nextLineOrEofOffset
            if (nodeRange.last - nodeRange.first > 0) {
                productionHolder.addProduction(listOf(SequentialParser.Node(
                        nodeRange, MarkdownTokenTypes.CODE_LINE)))
            }
            
            realInterestingOffset = pos.nextLineOrEofOffset
            return MarkerBlock.ProcessingResult.CANCEL
        }
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownElementTypes.CODE_BLOCK
    }
}
