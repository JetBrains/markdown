package org.intellij.markdown.flavours.gfm.alert

import org.intellij.markdown.IElementType
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.lexer.Compat.assert
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.constraints.applyToNextLineAndAddModifiers
import org.intellij.markdown.parser.constraints.extendsPrev
import org.intellij.markdown.parser.constraints.getCharsEaten
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

class GitHubAlertMarkerBlock(
    pos: LookaheadText.Position,
    myConstraints: MarkdownConstraints,
    productionHolder: ProductionHolder
) : MarkerBlockImpl(myConstraints, productionHolder.mark()) {

    private var firstLineSeen = false

    init {
        val lineStartOffset = pos.offset - pos.offsetInCurrentLine
        val charsEaten = constraints.getCharsEaten(pos.currentLine)
        val titleStart = lineStartOffset + charsEaten
        // The provider has already validated that the line content after the marker is `[!TYPE]`,
        // so the closing bracket is guaranteed to be present.
        val titleEnd = lineStartOffset + pos.currentLine.indexOf(']', charsEaten) + 1
        productionHolder.addProduction(listOf(
            SequentialParser.Node(titleStart..titleEnd, GFMTokenTypes.ALERT_TITLE)
        ))
    }

    override fun allowsSubBlocks(): Boolean = firstLineSeen

    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = pos.offsetInCurrentLine == -1

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
        return pos.nextLineOffset ?: -1
    }

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(pos: LookaheadText.Position, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        assert(pos.offsetInCurrentLine == -1)

        // The title line has now ended; let the rest of the alert parse as ordinary block content.
        firstLineSeen = true

        val nextLineConstraints = constraints.applyToNextLineAndAddModifiers(pos)
        // That means nextLineConstraints are "shorter" so our blockquote char is absent
        if (!nextLineConstraints.extendsPrev(constraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        return MarkerBlock.ProcessingResult.PASS
    }

    override fun getDefaultNodeType(): IElementType {
        return GFMElementTypes.ALERT
    }
}
