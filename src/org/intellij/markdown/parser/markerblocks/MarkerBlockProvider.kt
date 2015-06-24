package org.intellij.markdown.parser.markerblocks

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor

public interface MarkerBlockProvider<T : MarkerProcessor.PositionInfo> {
    fun createMarkerBlock(pos: LookaheadText.Position,
                          positionInfo: T): MarkerBlock?

    fun interruptsParagraph(pos: LookaheadText.Position): Boolean
}