package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.HorizontalRuleMarkerBlock
import java.util.ArrayList
import kotlin.text.Regex

public class HorizontalRuleProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(pos: LookaheadText.Position,
                                   productionHolder: ProductionHolder,
                                   stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {
        if (matches(pos)) {
            return listOf(HorizontalRuleMarkerBlock(stateInfo.currentConstraints, productionHolder.mark()))
        } else {
            return emptyList()
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        return matches(pos)
    }

    fun matches(pos: LookaheadText.Position): Boolean {
        if (pos.offsetInCurrentLine != 0) {
            return false
        }
        return REGEX.matches(pos.currentLine)
    }

    companion object {
        val REGEX: Regex = run {
            var variants = ArrayList<String>()
            for (c in arrayOf("-", "_", "\\*")) {
                variants.add("(${c} *)(${c} *)(${c} *)+")
            }
            Regex("^ {0,3}(" + variants.join("|") + ")$")
        }
    }
}