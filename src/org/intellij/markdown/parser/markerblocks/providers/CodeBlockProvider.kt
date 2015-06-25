package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.CodeBlockMarkerBlock

public class CodeBlockProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlock(pos: LookaheadText.Position,
                                   productionHolder: ProductionHolder,
                                   stateInfo: MarkerProcessor.StateInfo): MarkerBlock? {
        if (stateInfo.paragraphBlock != null) {
            return null
        }

        if (MarkdownParserUtil.hasCodeBlockIndent(pos, stateInfo.currentConstraints)) {
            return CodeBlockMarkerBlock(stateInfo.currentConstraints, productionHolder.mark())
        } else {
            return null
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position): Boolean {
        return false
    }

}