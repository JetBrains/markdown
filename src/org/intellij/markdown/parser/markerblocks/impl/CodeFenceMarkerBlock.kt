package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.lexer.Compat.assert
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.*
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import kotlin.math.min
import kotlin.text.Regex

class CodeFenceMarkerBlock(myConstraints: MarkdownConstraints,
                           private val productionHolder: ProductionHolder,
                           private val fenceStart: String) : MarkerBlockImpl(myConstraints, productionHolder.mark()) {
    override fun allowsSubBlocks(): Boolean = false

    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = true //pos.offsetInCurrentLine == -1

    private val endLineRegex = Regex("^ {0,3}${fenceStart}+ *$")

    private var realInterestingOffset = -1

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
        return pos.nextLineOrEofOffset
    }

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(pos: LookaheadText.Position,
                                currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (pos.offset < realInterestingOffset) {
            return MarkerBlock.ProcessingResult.CANCEL
        }

        // Eat everything if we're on code line
        if (pos.offsetInCurrentLine != -1) {
            return MarkerBlock.ProcessingResult.CANCEL
        }

        assert(pos.offsetInCurrentLine == -1)

        val nextLineConstraints = constraints.applyToNextLineAndAddModifiers(pos)
        if (!nextLineConstraints.extendsPrev(constraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        val nextLineOffset = pos.nextLineOrEofOffset
        realInterestingOffset = nextLineOffset

        val currentLine = nextLineConstraints.eatItselfFromString(pos.currentLine)
        if (endsThisFence(currentLine)) {
            productionHolder.addProduction(listOf(SequentialParser.Node(pos.offset + 1..pos.nextLineOrEofOffset,
                    MarkdownTokenTypes.CODE_FENCE_END)))
            scheduleProcessingResult(nextLineOffset, MarkerBlock.ProcessingResult.DEFAULT)
        } else {
            val contentRange = min(pos.offset + 1 + constraints.getCharsEaten(pos.currentLine), nextLineOffset)..nextLineOffset
            if (contentRange.first < contentRange.last) {
                productionHolder.addProduction(listOf(SequentialParser.Node(
                        contentRange, MarkdownTokenTypes.CODE_FENCE_CONTENT)))
            }
        }

        return MarkerBlock.ProcessingResult.CANCEL
    }

    private fun endsThisFence(line: CharSequence): Boolean {
        return endLineRegex.matches(line)
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownElementTypes.CODE_FENCE
    }
}
