package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.TokensCache
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.InlineStructureHoldingMarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlock

public class SetextHeaderMarkerBlock(myConstraints: MarkdownConstraints,
                                     productionHolder: ProductionHolder)
        : InlineStructureHoldingMarkerBlock(myConstraints, tokensCache, productionHolder, setOf(MarkdownTokenTypes.SETEXT_1,
                                                                                                MarkdownTokenTypes.SETEXT_2)) {

    private var nodeType: IElementType = MarkdownElementTypes.SETEXT_1

    private val startPosition: Int = productionHolder.currentPosition

    override fun getRangesContainingInlineStructure(): Collection<Range<Int>> {
        val endPosition = productionHolder.currentPosition
        return listOf(startPosition..endPosition - 2)
    }

    override fun getDefaultNodeType(): IElementType {
        return nodeType
    }

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DROP
    }

    override fun doProcessToken(tokenType: IElementType, iterator: TokensCache.Iterator, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (tokenType == MarkdownTokenTypes.SETEXT_1) {
            nodeType = MarkdownElementTypes.SETEXT_1
        } else {
            nodeType = MarkdownElementTypes.SETEXT_2
        }

        return MarkerBlock.ProcessingResult.DEFAULT.postpone()
    }
}
