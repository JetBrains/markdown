package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import kotlin.text.Regex

public class CodeFenceMarkerBlock(myConstraints: MarkdownConstraints,
                                  private val productionHolder: ProductionHolder,
                                  private val fenceStart: String) : MarkerBlockImpl(myConstraints, productionHolder.mark()) {
    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = true //pos.char == '\n'

    private val endLineRegex = Regex("^ {0,3}${fenceStart}+ *$")

    private var realInterestingOffset = -1

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
        return pos.offset + 1
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
        if (pos.char != '\n') {
            return MarkerBlock.ProcessingResult.CANCEL
        }

        assert(pos.char == '\n')

        val nextLineConstraints = MarkdownConstraints.fromBase(pos, constraints)
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
            val contentRange = Math.min(pos.offset + 1 + constraints.getIndent(), nextLineOffset)..nextLineOffset
            if (contentRange.start < contentRange.end) {
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
