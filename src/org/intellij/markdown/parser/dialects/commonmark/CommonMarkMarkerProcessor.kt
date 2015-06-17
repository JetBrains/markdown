package org.intellij.markdown.parser.dialects.commonmark

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.MarkerProcessorFactory
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.dialects.FixedPriorityListMarkerProcessor
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.impl.*
import java.util.ArrayList

public class CommonMarkMarkerProcessor(productionHolder: ProductionHolder)
        : FixedPriorityListMarkerProcessor(productionHolder, MarkdownConstraints.BASE) {

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

    override fun createNewMarkerBlocks(tokenType: IElementType, iterator: TokensCache.Iterator, productionHolder: ProductionHolder): Array<MarkerBlock> {
        if (tokenType == MarkdownTokenTypes.EOL) {
            return NO_BLOCKS
        }
        if (tokenType == MarkdownTokenTypes.HORIZONTAL_RULE
                || tokenType == MarkdownTokenTypes.SETEXT_1
                || tokenType == MarkdownTokenTypes.SETEXT_2
                || tokenType == MarkdownTokenTypes.HTML_BLOCK) {
            return NO_BLOCKS
        }

        val result = ArrayList<MarkerBlock>(1)

        val newConstraints = currentConstraints.addModifierIfNeeded(tokenType, iterator)
        val paragraph = getParagraphBlock()


        if (MarkdownParserUtil.hasCodeBlockIndent(iterator, 0, newConstraints) && paragraph == null) {
            result.add(CodeBlockMarkerBlock(newConstraints, productionHolder.mark()))
        } else if (tokenType == MarkdownTokenTypes.BLOCK_QUOTE) {
            result.add(BlockQuoteMarkerBlock(newConstraints, productionHolder.mark()))
        } else if (tokenType == MarkdownTokenTypes.LIST_NUMBER || tokenType == MarkdownTokenTypes.LIST_BULLET) {
            if (getLastBlock() is ListMarkerBlock) {
                result.add(ListItemMarkerBlock(newConstraints, productionHolder.mark()))
            } else {
                result.add(ListMarkerBlock(newConstraints, productionHolder.mark(), tokenType))
                result.add(ListItemMarkerBlock(newConstraints, productionHolder.mark()))
            }
        } else if (tokenType == MarkdownTokenTypes.ATX_HEADER && paragraph == null) {
            val tokenText = iterator.text
            result.add(AtxHeaderMarkerBlock(newConstraints, tokensCache, productionHolder, tokenText.length()))
        } else if (tokenType == MarkdownTokenTypes.CODE_FENCE_START) {
            result.add(CodeFenceMarkerBlock(newConstraints, productionHolder.mark()))
        } else {
            assert(tokenType != MarkdownTokenTypes.EOL)
            val paragraphToUse: ParagraphMarkerBlock

            if (paragraph == null) {
                paragraphToUse = ParagraphMarkerBlock(newConstraints, productionHolder, tokensCache)
                result.add(paragraphToUse)

                if (isAtLineStart(iterator)) {
                    result.add(SetextHeaderMarkerBlock(newConstraints, tokensCache, productionHolder))

                }
            }

        }

        return result.toArray(arrayOfNulls<MarkerBlock>(result.size()))
    }

    private fun getParagraphBlock(): ParagraphMarkerBlock? {
        return markersStack.firstOrNull { block -> block is ParagraphMarkerBlock } as ParagraphMarkerBlock?
    }

    private fun getLastBlock(): MarkerBlock? {
        return markersStack.lastOrNull()
    }

    public object Factory : MarkerProcessorFactory {
        override fun createMarkerProcessor(productionHolder: ProductionHolder): MarkerProcessor {
            return CommonMarkMarkerProcessor(productionHolder)
        }
    }

    companion object {

        private fun isAtLineStart(iterator: TokensCache.Iterator): Boolean {
            var index = -1
            while (true) {
                val `type` = iterator.rawLookup(index)
                if (`type` == null || `type` == MarkdownTokenTypes.EOL) {
                    return true
                }
                if (`type` != MarkdownTokenTypes.WHITE_SPACE) {
                    return false
                }
                --index
            }
        }
    }

}
