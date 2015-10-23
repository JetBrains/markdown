package org.intellij.markdown.flavours.gfm.table

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import kotlin.text.Regex

class GitHubTableMarkerProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(pos: LookaheadText.Position, productionHolder: ProductionHolder, stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {
        val currentConstraints = stateInfo.currentConstraints
        if (stateInfo.nextConstraints != currentConstraints) {
            return emptyList()
        }

        if (!pos.currentLineFromPosition.contains('|')) {
            return emptyList()
        }
        if (getNextLineFromConstraints(pos, currentConstraints)?.let {
            SECOND_LINE_REGEX.find(it)?.value?.length?.let { it > 0 }
        } == true) {
            return listOf(GitHubTableMarkerBlock(pos, currentConstraints, productionHolder))
        } else {
            return emptyList()
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        return false
    }

    private fun getNextLineFromConstraints(pos: LookaheadText.Position, constraints: MarkdownConstraints): CharSequence? {
        val line = pos.nextLine ?: return null
        val nextLineConstraints = MarkdownConstraints.fillFromPrevious(line, 0, constraints)
        if (nextLineConstraints.extendsPrev(constraints)) {
            return nextLineConstraints.eatItselfFromString(line)
        } else {
            return null
        }
    }

    companion object {
        val WHSP = "[ \t]*(?:\\:[ \t]*)?"
        val SECOND_LINE_REGEX = Regex("^(?:$WHSP\\|?$WHSP---+)*$WHSP\\|$WHSP(?:---+$WHSP\\|?$WHSP)*$")

        fun CharSequence.contains(char: Char): Boolean {
            for (c in this) {
                if (c == char) {
                    return true
                }
            }
            return false
        }
    }
}