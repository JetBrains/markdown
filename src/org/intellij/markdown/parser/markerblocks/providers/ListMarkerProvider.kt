package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.ListMarkerBlock

public class ListMarkerProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlock(pos: LookaheadText.Position,
                                   productionHolder: ProductionHolder,
                                   stateInfo: MarkerProcessor.StateInfo): MarkerBlock? {
        if (stateInfo.lastBlock is ListMarkerBlock) {
            return null
        }

        val currentConstraints = stateInfo.currentConstraints
        val nextConstraints = stateInfo.newConstraints
        if (nextConstraints != currentConstraints
                && nextConstraints.getLastType() != '>' && nextConstraints.getLastExplicit() == true) {
            return ListMarkerBlock(nextConstraints, productionHolder.mark(), nextConstraints.getLastType()!!)
        } else {
            return null
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position): Boolean {
        // Actually, list item interrupts a paragraph, but we have MarkdownConstraints for these cases
        return false
    }
}