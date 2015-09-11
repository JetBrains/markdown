package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.ListItemMarkerBlock
import org.intellij.markdown.parser.markerblocks.impl.ListMarkerBlock
import java.util.ArrayList

public class ListMarkerProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(pos: LookaheadText.Position,
                                   productionHolder: ProductionHolder,
                                   stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {

//        if (Character.isWhitespace(pos.char)) {
//            return emptyList()
//        }
//        if (pos.offsetInCurrentLine != 0 && !Character.isWhitespace(pos.currentLine[pos.offsetInCurrentLine - 1])) {
//            return emptyList()
//        }

        val currentConstraints = stateInfo.currentConstraints
        val nextConstraints = stateInfo.nextConstraints

        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, currentConstraints)) {
            return emptyList()
        }
        if (nextConstraints != currentConstraints
                && nextConstraints.getLastType() != '>' && nextConstraints.getLastExplicit() == true) {

            val result = ArrayList<MarkerBlock>()
            if (stateInfo.lastBlock !is ListMarkerBlock) {
                result.add(ListMarkerBlock(nextConstraints, productionHolder.mark(), nextConstraints.getLastType()!!))
            }
            result.add(ListItemMarkerBlock(nextConstraints, productionHolder.mark()))
            return result
        } else {
            return emptyList()
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        // Actually, list item interrupts a paragraph, but we have MarkdownConstraints for these cases
        return false
    }
}