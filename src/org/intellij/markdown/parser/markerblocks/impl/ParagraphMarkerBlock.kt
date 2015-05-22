package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.MarkdownConstraints
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.TokensCache
import org.intellij.markdown.parser.markerblocks.InlineStructureHoldingMarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil

public class ParagraphMarkerBlock(myConstraints: MarkdownConstraints,
                                  productionHolder: ProductionHolder,
                                  tokensCache: TokensCache)
        : InlineStructureHoldingMarkerBlock(myConstraints, tokensCache, productionHolder, null) {
    private val startPosition = productionHolder.currentPosition

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(tokenType: IElementType, iterator: TokensCache.Iterator, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (tokenType == MarkdownTokenTypes.SETEXT_1 || tokenType == MarkdownTokenTypes.SETEXT_2) {
            return MarkerBlock.ProcessingResult.PASS;
        }
        else if (tokenType != MarkdownTokenTypes.EOL) {
            return MarkerBlock.ProcessingResult.CANCEL;
        }

        assert(tokenType == MarkdownTokenTypes.EOL)

        if (MarkdownParserUtil.calcNumberOfConsequentEols(iterator) >= 2) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        var afterEol: IElementType? = iterator.advance().type
        if (afterEol == MarkdownTokenTypes.BLOCK_QUOTE) {
            if (!MarkdownConstraints.fromBase(iterator, 1, constraints).upstreamWith(constraints)) {
                return MarkerBlock.ProcessingResult.DEFAULT
            }

            afterEol = iterator.rawLookup(MarkdownParserUtil.getFirstNextLineNonBlockquoteRawIndex(iterator))
        }

        if (afterEol == MarkdownTokenTypes.SETEXT_1 || afterEol == MarkdownTokenTypes.SETEXT_2) {
            return MarkerBlock.ProcessingResult(MarkerBlock.ClosingAction.NOTHING, MarkerBlock.ClosingAction.DROP, MarkerBlock.EventAction.PROPAGATE)
        }

        // Something breaks paragraph

        if (nextTokenTypeBreaksParagraph(afterEol)) {
            // RUBY-16750 Let's check this is valid start for a new block
            val nextLineConstraints = MarkdownConstraints.fromBase(iterator, 1, constraints)
            if (!MarkdownParserUtil.hasCodeBlockIndent(iterator.advance(), 0, nextLineConstraints)) {
                return MarkerBlock.ProcessingResult.DEFAULT
            }

        }

        return MarkerBlock.ProcessingResult.CANCEL
    }

    protected fun nextTokenTypeBreaksParagraph(afterEol: IElementType?): Boolean {
        return afterEol == MarkdownTokenTypes.EOL
                || afterEol == MarkdownTokenTypes.HORIZONTAL_RULE
                || afterEol == MarkdownTokenTypes.CODE_FENCE_START
                || afterEol == MarkdownTokenTypes.LIST_BULLET
                || afterEol == MarkdownTokenTypes.LIST_NUMBER
                || afterEol == MarkdownTokenTypes.ATX_HEADER
                || afterEol == MarkdownTokenTypes.BLOCK_QUOTE
                || afterEol == MarkdownTokenTypes.HTML_BLOCK
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownElementTypes.PARAGRAPH
    }

    override fun getRangesContainingInlineStructure(): Collection<Range<Int>> {
        val endPosition = productionHolder.currentPosition
        return SequentialParserUtil.filterBlockquotes(tokensCache, startPosition..endPosition)
    }
}
