package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.HorizontalRuleMarkerBlock
import java.util.ArrayList
import kotlin.text.Regex

public class HorizontalRuleProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlock(pos: LookaheadText.Position,
                                   productionHolder: ProductionHolder,
                                   stateInfo: MarkerProcessor.StateInfo): MarkerBlock? {
        if (matches(pos)) {
            return HorizontalRuleMarkerBlock(stateInfo.currentConstraints, productionHolder.mark())
        } else {
            return null
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position): Boolean {
        return matches(pos)
    }

    fun matches(pos: LookaheadText.Position): Boolean {
        if (pos.offsetInCurrentLine != 0) {
            return false
        }
        return REGEX.matches(pos.currentLine)
    }

    companion object {
        val REGEX: Regex = {
            var variants = ArrayList<String>()
            for (c in arrayOf('-', '_', '*')) {
                variants.add("(${c} *)(${c} *)(${c} *)+")
            }
            Regex("^ {0,3}(" + variants.join("|") + ")$")
        }.invoke()
    }
}