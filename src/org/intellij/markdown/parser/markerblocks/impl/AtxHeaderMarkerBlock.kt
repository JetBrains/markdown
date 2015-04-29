package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.MarkdownConstraints
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.TokensCache
import org.intellij.markdown.parser.markerblocks.InlineStructureHoldingMarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlock

public class AtxHeaderMarkerBlock(myConstraints: MarkdownConstraints,
                                  tokensCache: TokensCache,
                                  productionHolder: ProductionHolder,
                                  headerSize: Int)
        : InlineStructureHoldingMarkerBlock(myConstraints, tokensCache, productionHolder, null) {

    private val nodeType: IElementType

    private val startPosition: Int

    init {
        nodeType = calcNodeType(headerSize)
        startPosition = productionHolder.currentPosition
    }

    private fun calcNodeType(headerSize: Int): IElementType {
        when (headerSize) {
            1 -> return MarkdownElementTypes.ATX_1
            2 -> return MarkdownElementTypes.ATX_2
            3 -> return MarkdownElementTypes.ATX_3
            4 -> return MarkdownElementTypes.ATX_4
            5 -> return MarkdownElementTypes.ATX_5
            6 -> return MarkdownElementTypes.ATX_6
            else -> return MarkdownElementTypes.ATX_6
        }
    }

    override fun getDefaultNodeType(): IElementType {
        return nodeType
    }

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(tokenType: IElementType, iterator: TokensCache.Iterator, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (tokenType == MarkdownTokenTypes.EOL) {
            return MarkerBlock.ProcessingResult(MarkerBlock.ClosingAction.DROP, MarkerBlock.ClosingAction.DONE, MarkerBlock.EventAction.PROPAGATE)
        }
        return MarkerBlock.ProcessingResult.CANCEL;
    }

    override fun getRangesContainingInlineStructure(): Collection<Range<Int>> {
        val endPosition = productionHolder.currentPosition
        return listOf(startPosition + 1..endPosition)
    }
}
