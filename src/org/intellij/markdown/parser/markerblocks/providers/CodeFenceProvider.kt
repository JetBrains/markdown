package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.CodeFenceMarkerBlock
import kotlin.text.Regex

public class CodeFenceProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlock(pos: LookaheadText.Position,
                                   productionHolder: ProductionHolder,
                                   stateInfo: MarkerProcessor.StateInfo): MarkerBlock? {
        val fenceStart = getFenceStart(pos)
        if (fenceStart != null) {
            return CodeFenceMarkerBlock(stateInfo.currentConstraints, productionHolder.mark(), fenceStart)
        } else {
            return null
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position): Boolean {
        return getFenceStart(pos) != null
    }

    private fun getFenceStart(pos: LookaheadText.Position): String? {
        val matchResult = REGEX.match(pos.currentLine.subSequence(pos.offsetInCurrentLine, pos.currentLine.length()))
            ?: return null
        return matchResult.groups[1]?.value
    }

    companion object {
        val REGEX: Regex = Regex("^ {0,3}(~~~+|```+)")
    }
}