package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.constraints.getCharsEaten
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.CodeBlockMarkerBlock

class CodeBlockProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(pos: LookaheadText.Position,
                                    productionHolder: ProductionHolder,
                                    stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {
        if (stateInfo.nextConstraints.getCharsEaten(pos.currentLine) > pos.offsetInCurrentLine) {
            return emptyList()
        }

        val charsToNonWhitespace = pos.charsToNonWhitespace()
                ?: return emptyList()
        val blockStart = pos.nextPosition(charsToNonWhitespace)
                ?: return emptyList()

        if (MarkdownParserUtil.hasCodeBlockIndent(blockStart, stateInfo.currentConstraints)) {
            return listOf(CodeBlockMarkerBlock(stateInfo.currentConstraints, productionHolder, pos))
        } else {
            return emptyList()
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        return false
    }

}