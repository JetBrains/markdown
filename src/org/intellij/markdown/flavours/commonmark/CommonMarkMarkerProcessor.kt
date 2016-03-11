package org.intellij.markdown.flavours.commonmark

import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.MarkerProcessorFactory
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.providers.*
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

public open class CommonMarkMarkerProcessor(productionHolder: ProductionHolder, constraintsBase: MarkdownConstraints)
: MarkerProcessor<MarkerProcessor.StateInfo>(productionHolder, constraintsBase) {
    override var stateInfo: MarkerProcessor.StateInfo = MarkerProcessor.StateInfo(startConstraints,
            startConstraints,
            markersStack)

    private val markerBlockProviders = listOf(
            CodeBlockProvider(),
            HorizontalRuleProvider(),
            SetextHeaderProvider(),
            BlockQuoteProvider(),
            ListMarkerProvider(),
            AtxHeaderProvider(true),
            CodeFenceProvider(),
            HtmlBlockProvider(),
            LinkReferenceDefinitionProvider()
    )

    override fun getMarkerBlockProviders(): List<MarkerBlockProvider<MarkerProcessor.StateInfo>> {
        return markerBlockProviders
    }

    override fun updateStateInfo(pos: LookaheadText.Position) {
        if (pos.char == '\n') {
            stateInfo = MarkerProcessor.StateInfo(startConstraints,
                    MarkdownConstraints.fillFromPrevious(pos.currentLine, 0, topBlockConstraints),
                    markersStack)
        } else if (MarkerBlockProvider.isStartOfLineWithConstraints(pos, stateInfo.nextConstraints)) {
            stateInfo = MarkerProcessor.StateInfo(stateInfo.nextConstraints,
                    stateInfo.nextConstraints.addModifierIfNeeded(pos) ?: stateInfo.nextConstraints,
                    markersStack)
        }
    }

    override fun populateConstraintsTokens(pos: LookaheadText.Position,
                                           constraints: MarkdownConstraints,
                                           productionHolder: ProductionHolder) {
        if (constraints.getIndent() == 0) {
            return
        }

        val startOffset = pos.offset
        val endOffset = Math.min(pos.offset - pos.offsetInCurrentLine + constraints.getCharsEaten(pos.currentLine),
                pos.nextLineOrEofOffset)

        val type = when (constraints.getLastType()) {
            '>' ->
                MarkdownTokenTypes.BLOCK_QUOTE
            '.', ')' ->
                MarkdownTokenTypes.LIST_NUMBER
            else ->
                MarkdownTokenTypes.LIST_BULLET
        }
        productionHolder.addProduction(listOf(SequentialParser.Node(startOffset..endOffset, type)))
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
            return CommonMarkMarkerProcessor(productionHolder, MarkdownConstraints.BASE)
        }
    }
}
