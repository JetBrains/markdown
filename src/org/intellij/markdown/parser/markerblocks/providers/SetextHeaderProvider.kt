package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.SetextHeaderMarkerBlock
import kotlin.text.Regex

public class SetextHeaderProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(pos: LookaheadText.Position,
                                   productionHolder: ProductionHolder,
                                   stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {
        if (stateInfo.paragraphBlock != null) {
            return emptyList()
        }
        val currentConstaints = stateInfo.currentConstraints
        if (stateInfo.nextConstraints != currentConstaints) {
            return emptyList()
        }
        if (MarkerBlockProvider.isStartOfLineWithConstraints(pos, currentConstaints)
                && getNextLineFromConstraints(pos, currentConstaints)?.matches(REGEX) == true) {
            return listOf(SetextHeaderMarkerBlock(currentConstaints, productionHolder))
        } else {
            return emptyList()
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        return false
    }

    private fun getNextLineFromConstraints(pos: LookaheadText.Position, constraints: MarkdownConstraints): String? {
        val line = pos.nextLine ?: return null
        val nextLineConstraints = MarkdownConstraints.fillFromPrevious(line, 0, constraints, MarkdownConstraints.BASE)
        if (nextLineConstraints.extendsPrev(nextLineConstraints)) {
            return line.substring(nextLineConstraints.getIndent())
        } else {
            return null
        }
    }

    companion object {
        val REGEX: Regex = Regex("^ {0,3}(\\-+|=+) *$")
    }
}