package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.ListMarkerBlock

public class ListMarkerProvider : MarkerBlockProvider<MarkerProcessor.PositionInfo> {
    override fun createMarkerBlock(pos: LookaheadText.Position, positionInfo: MarkerProcessor.PositionInfo): MarkerBlock? {
        if (positionInfo.getLastBlock() is ListMarkerBlock) {
            return null
        }

        val currentConstraints = positionInfo.currentConstraints
        val nextConstraints = positionInfo.newConstraints
        if (nextConstraints != currentConstraints
                && nextConstraints.getLastType() != '>' && nextConstraints.getLastExplicit() == true) {
            return ListMarkerBlock(nextConstraints, positionInfo.productionHolder.mark(), nextConstraints.getLastType())
        } else {
            return null
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position): Boolean {
        // Actually, list item interrupts a paragraph, but we have MarkdownConstraints for these cases
        return false
    }
}