package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.CodeBlockMarkerBlock

public class CodeBlockProvider : MarkerBlockProvider<MarkerProcessor.PositionInfo> {
    override fun createMarkerBlock(pos: LookaheadText.Position,
                                   positionInfo: MarkerProcessor.PositionInfo): MarkerBlock? {
        if (positionInfo.getParagraphBlock() != null) {
            return null
        }

        if (MarkdownParserUtil.hasCodeBlockIndent(pos, positionInfo.currentConstraints)) {
            return CodeBlockMarkerBlock(positionInfo.currentConstraints, positionInfo.productionHolder.mark())
        } else {
            return null
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position): Boolean {
        return false
    }

}