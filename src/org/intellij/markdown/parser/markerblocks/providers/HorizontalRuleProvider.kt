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
        if (matches(pos, stateInfo.currentConstraints)) {
            return listOf(HorizontalRuleMarkerBlock(stateInfo.currentConstraints, productionHolder.mark()))
        } else {
            return emptyList()
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        return matches(pos, constraints)
    }

    fun matches(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, constraints)) {
            return false
        }
        return isHorizontalRule(pos.currentLine, pos.offsetInCurrentLine)
    }

    companion object {
        public val REGEX: Regex = run {
            var variants = ArrayList<String>()
            for (c in arrayOf("-", "_", "\\*")) {
                variants.add("(${c} *){3,}")
            }
            Regex("^ {0,3}(" + variants.join("|") + ")$")
        }

        public fun isHorizontalRule(line: CharSequence, offset: Int): Boolean {
//                        return HorizontalRuleProvider.REGEX.matches(line.subSequence(offset, line.length()))
            var hrChar: Char? = null
            var startSpace = 0
            var charCount = 1
            for (i in offset..line.length() - 1) {
                val c = line[i]
                if (hrChar == null) {
                    if (c == '*' || c == '-' || c == '_') {
                        hrChar = c
                    } else if (startSpace < 3 && c == ' ') {
                        startSpace++
                    } else {
                        return false
                    }
                } else {
                    if (c == hrChar) {
                        charCount++
                    } else if (c != ' ' && c != '\t') {
                        return false
                    }
                }
            }
            return charCount >= 3
        }

    }
}