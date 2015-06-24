package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.ListItemMarkerBlock

public class ListItemMarkerProvider : MarkerBlockProvider<MarkerProcessor.PositionInfo> {
    override fun createMarkerBlock(pos: LookaheadText.Position, positionInfo: MarkerProcessor.PositionInfo): MarkerBlock? {
        val currentConstraints = positionInfo.currentConstraints
        val nextConstraints = currentConstraints.addModifierIfNeeded(pos)
        if (nextConstraints != null && nextConstraints.getLastType() != '>' && nextConstraints.getLastExplicit() == true) {
            return ListItemMarkerBlock(nextConstraints, positionInfo.productionHolder.mark())
        } else {
            return null
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position): Boolean {
        // Actually, list item interrupts a paragraph, but we have MarkdownConstraints for these cases
        return false
    }

}