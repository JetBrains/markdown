package org.intellij.markdown.parser.markerblocks

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder

public interface MarkerBlockProvider<T : MarkerProcessor.StateInfo> {
    fun createMarkerBlock(pos: LookaheadText.Position,
                          productionHolder: ProductionHolder,
                          stateInfo: T): MarkerBlock?

    fun interruptsParagraph(pos: LookaheadText.Position): Boolean
}