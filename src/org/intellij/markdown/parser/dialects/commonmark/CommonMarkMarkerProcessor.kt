package org.intellij.markdown.parser.dialects.commonmark

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.MarkerProcessorFactory
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.dialects.FixedPriorityListMarkerProcessor
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.providers.*
import java.util.ArrayList

public class CommonMarkMarkerProcessor(productionHolder: ProductionHolder)
: FixedPriorityListMarkerProcessor<MarkerProcessor.StateInfo>(productionHolder, MarkdownConstraints.BASE) {
    override var stateInfo: MarkerProcessor.StateInfo = MarkerProcessor.StateInfo(MarkdownConstraints.BASE,
            MarkdownConstraints.BASE,
            markersStack)

    private val markerBlockProviders = listOf(
            CodeBlockProvider(),
            BlockQuoteProvider(),
            ListMarkerProvider(),
            AtxHeaderProvider(),
            CodeFenceProvider(),
            SetextHeaderProvider(),
            HorizontalRuleProvider(),
            HtmlBlockProvider()
    )

    override fun getPriorityList(): List<Pair<IElementType, Int>> {
        val result = ArrayList<Pair<IElementType, Int>>()
        val itemsByPriority = ArrayList<List<IElementType>>()

        itemsByPriority.add(listOf(
                MarkdownElementTypes.ATX_1,
                MarkdownElementTypes.ATX_2,
                MarkdownElementTypes.ATX_3,
                MarkdownElementTypes.ATX_4,
                MarkdownElementTypes.ATX_5,
                MarkdownElementTypes.ATX_6))

        for (i in itemsByPriority.indices) {
            val types = itemsByPriority.get(i)
            for (`type` in types) {
                result.add(Pair(`type`, i + 1))
            }
        }

        return result
    }

    override fun getMarkerBlockProviders(): List<MarkerBlockProvider<MarkerProcessor.StateInfo>> {
        return markerBlockProviders
    }

    override fun updateStateInfo(pos: LookaheadText.Position) {
        if (pos.char == '\n') {
            stateInfo = MarkerProcessor.StateInfo(MarkdownConstraints.BASE,
                    MarkdownConstraints.fillFromPrevious(pos.currentLine, 0, topBlockConstraints, MarkdownConstraints.BASE),
                    markersStack)
        } else if (pos.offsetInCurrentLine == stateInfo.nextConstraints.getIndent()) {
            stateInfo = MarkerProcessor.StateInfo(stateInfo.nextConstraints,
                    stateInfo.nextConstraints.addModifierIfNeeded(pos) ?: stateInfo.nextConstraints,
                    markersStack)
        }
    }

    override fun createNewMarkerBlocks(pos: LookaheadText.Position,
                                       productionHolder: ProductionHolder): List<MarkerBlock> {
        if (pos.char == '\n') {
            return NO_BLOCKS
        }

        return super.createNewMarkerBlocks(pos, productionHolder)
    }

    public object Factory : MarkerProcessorFactory {
        override fun createMarkerProcessor(productionHolder: ProductionHolder): MarkerProcessor<*> {
            return CommonMarkMarkerProcessor(productionHolder)
        }
    }
}
