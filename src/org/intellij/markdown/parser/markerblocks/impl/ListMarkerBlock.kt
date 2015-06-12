package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.TokensCache
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl

public class ListMarkerBlock(myConstraints: MarkdownConstraints,
                             marker: ProductionHolder.Marker,
                             private val listType: IElementType)
    : MarkerBlockImpl(myConstraints, marker, setOf(MarkdownTokenTypes.EOL)) {

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(tokenType: IElementType, iterator: TokensCache.Iterator, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        assert(tokenType == MarkdownTokenTypes.EOL)

        val eolN = MarkdownParserUtil.calcNumberOfConsequentEols(iterator)
        if (eolN >= 3) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        val eolIndex = MarkdownParserUtil.getFirstNonWhitespaceLineEolRawIndex(iterator)
        val nextLineConstraints = MarkdownConstraints.fromBase(iterator, eolIndex + 1, constraints)

        if (!nextLineConstraints.extendsList(constraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        return MarkerBlock.ProcessingResult.PASS
    }

    override fun getDefaultNodeType(): IElementType {
        return if (listType == MarkdownTokenTypes.LIST_BULLET)
            MarkdownElementTypes.UNORDERED_LIST
        else
            MarkdownElementTypes.ORDERED_LIST
    }
}
