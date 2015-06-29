package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl
import kotlin.text.Regex

public class CodeFenceMarkerBlock(myConstraints: MarkdownConstraints,
                                  marker: ProductionHolder.Marker,
                                  private val fenceStart: String) : MarkerBlockImpl(myConstraints, marker) {
    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = pos.char == '\n'

    private val endLineRegex = Regex("^ {0,3}${fenceStart}+ *$")

    private var realInterestingOffset = -1

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int? {
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

        if (!nextLineConstraints.upstreamWith(constraints)) {
            return MarkerBlock.ProcessingResult.CANCEL
        }

        val currentLine = pos.currentLine.subSequence(nextLineConstraints.getIndent(), pos.currentLine.length())
        if (endsThisFence(currentLine)) {
            scheduleProcessingResult(nextLineOffset, MarkerBlock.ProcessingResult.DEFAULT)
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
