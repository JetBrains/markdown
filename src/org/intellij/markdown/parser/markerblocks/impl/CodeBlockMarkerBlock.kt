package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.MarkdownConstraints
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.TokensCache
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl

public class CodeBlockMarkerBlock(myConstraints: MarkdownConstraints, marker: ProductionHolder.Marker) : MarkerBlockImpl(myConstraints, marker) {

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(tokenType: IElementType, iterator: TokensCache.Iterator, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        // Eat everything if we're on code line
        if (tokenType != MarkdownTokenTypes.EOL) {
            return MarkerBlock.ProcessingResult.CANCEL
        }

        assert(tokenType == MarkdownTokenTypes.EOL)

        var afterEol: IElementType? = iterator.advance().type
        val nonWhitespaceOffset: Int
        if (afterEol == MarkdownTokenTypes.BLOCK_QUOTE) {
            val nextLineConstraints = MarkdownConstraints.fromBase(iterator, 1, constraints)
            // kinda equals
            if (!(nextLineConstraints.upstreamWith(constraints) && nextLineConstraints.extendsPrev(constraints))) {
                return MarkerBlock.ProcessingResult.DEFAULT
            }

            afterEol = iterator.rawLookup(MarkdownParserUtil.getFirstNextLineNonBlockquoteRawIndex(iterator))
            nonWhitespaceOffset = MarkdownParserUtil.getFirstNextLineNonBlockquoteRawIndex(iterator)
        } else {
            nonWhitespaceOffset = MarkdownParserUtil.getFirstNonWhiteSpaceRawIndex(iterator)
        }

        if (afterEol == MarkdownTokenTypes.EOL) {
            return MarkerBlock.ProcessingResult.CANCEL
        }

        if (!MarkdownParserUtil.hasCodeBlockIndent(iterator, nonWhitespaceOffset, constraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        } else {
            return MarkerBlock.ProcessingResult.CANCEL
        }
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownElementTypes.CODE_BLOCK
    }
}
