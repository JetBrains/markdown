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

public class SetextHeaderMarkerBlock(myConstraints: MarkdownConstraints,
                                     private val productionHolder: ProductionHolder)
        : MarkerBlockImpl(myConstraints, productionHolder.mark()) {
    override fun allowsSubBlocks(): Boolean = false

    private val contentMarker = productionHolder.mark()

    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = pos.char == '\n'

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
        return pos.nextLineOrEofOffset
    }

    private var nodeType: IElementType = MarkdownElementTypes.SETEXT_1

    override fun getDefaultNodeType(): IElementType {
        return nodeType
    }

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(pos: LookaheadText.Position,
                                currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (pos.offsetInCurrentLine != -1) {
            return MarkerBlock.ProcessingResult.CANCEL
        }

        val startSpaces = pos.charsToNonWhitespace()
                ?: return MarkerBlock.ProcessingResult(MarkerBlock.ClosingAction.DROP, MarkerBlock.ClosingAction.DROP, MarkerBlock.EventAction.PROPAGATE)

        val setextMarkerStart = pos.nextPosition(startSpaces)
        if (setextMarkerStart?.char == '-') {
            nodeType = MarkdownElementTypes.SETEXT_2
        }

        val setextMarkerStartOffset = setextMarkerStart?.offset ?: pos.offset
        val markerNodeType = if (nodeType == MarkdownElementTypes.SETEXT_2)
            MarkdownTokenTypes.SETEXT_2
        else
            MarkdownTokenTypes.SETEXT_1

        contentMarker.done(MarkdownTokenTypes.SETEXT_CONTENT)
        productionHolder.addProduction(listOf(SequentialParser.Node(
                setextMarkerStartOffset..pos.nextLineOrEofOffset, markerNodeType)))
        scheduleProcessingResult(pos.nextLineOrEofOffset, MarkerBlock.ProcessingResult.DEFAULT)
        return MarkerBlock.ProcessingResult.CANCEL
    }
}
