package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.SetextHeaderMarkerBlock
import kotlin.text.Regex

public class SetextHeaderProvider : MarkerBlockProvider<MarkerProcessor.PositionInfo> {
    override fun createMarkerBlock(pos: LookaheadText.Position, positionInfo: MarkerProcessor.PositionInfo): MarkerBlock? {
        if (positionInfo.getParagraphBlock() != null) {
            return null
        }
        if (pos.offsetInCurrentLine == 0 && pos.nextLine?.matches(REGEX) == true) {
            return SetextHeaderMarkerBlock(positionInfo.currentConstraints, positionInfo.productionHolder)
        } else {
            return null
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position): Boolean {
        return false
    }

    companion object {
        val REGEX: Regex = Regex("^(\\-+|=+) *$")
    }
}