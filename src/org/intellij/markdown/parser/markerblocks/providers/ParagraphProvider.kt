package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.ParagraphMarkerBlock

public class ParagraphProvider : MarkerBlockProvider<MarkerProcessor.PositionInfo> {
    override fun createMarkerBlock(pos: LookaheadText.Position,
                                   positionInfo: MarkerProcessor.PositionInfo): MarkerBlock? {
        if (Character.isWhitespace(pos.char)) {
            return null
        }
        if (positionInfo.getParagraphBlock() != null) {
            return null
        }
        return ParagraphMarkerBlock(positionInfo.currentConstraints, positionInfo.productionHolder.mark())
    }

    override fun interruptsParagraph(pos: LookaheadText.Position): Boolean {
        return false
    }
}