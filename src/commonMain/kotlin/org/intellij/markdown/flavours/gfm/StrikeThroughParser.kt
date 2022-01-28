package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache

class StrikeThroughParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResultBuilder()
        val outerDelegateIndices = RangesListBuilder()
        var innerDelegateIndices: RangesListBuilder? = null
        var lastOpenedPos: Int? = null
        var iterator = tokens.RangesListIterator(rangesToGlue)

        while (iterator.type != null) {
            if (iterator.type != GFMTokenTypes.TILDE) {
                (innerDelegateIndices ?: outerDelegateIndices).put(iterator.index)
                iterator = iterator.advance()
                continue
            }

            if (lastOpenedPos != null
                    && isGoodType(iterator.rawLookup(-1))
                    && iterator.rawLookup(1) == GFMTokenTypes.TILDE) {
                iterator = iterator.advance()
                result.withNode(SequentialParser.Node(lastOpenedPos..iterator.index + 1, GFMElementTypes.STRIKETHROUGH))
                    .withFurtherProcessing(requireNotNull(innerDelegateIndices).get())
                iterator = iterator.advance()
                lastOpenedPos = null
                innerDelegateIndices = null

                continue
            }
            if (lastOpenedPos == null
                    && iterator.rawLookup(1) == GFMTokenTypes.TILDE
                    && isGoodType(iterator.rawLookup(2))) {
                lastOpenedPos = iterator.index
                innerDelegateIndices = RangesListBuilder()
                iterator = iterator.advance().advance()
                continue
            }

            (innerDelegateIndices ?: outerDelegateIndices).put(iterator.index)
            iterator = iterator.advance()
        }

        if (lastOpenedPos != null) {
            outerDelegateIndices.put(lastOpenedPos)
            outerDelegateIndices.put(lastOpenedPos + 1)
            outerDelegateIndices.put(requireNotNull(innerDelegateIndices))
        }

        return result.withFurtherProcessing(outerDelegateIndices.get())
    }

    private fun isGoodType(type: IElementType?): Boolean {
        return type == null ||
                (type != MarkdownTokenTypes.WHITE_SPACE
                && type != MarkdownTokenTypes.EOL
                && type != GFMTokenTypes.TILDE
                && type != MarkdownTokenTypes.HTML_TAG
                && type != MarkdownTokenTypes.HTML_BLOCK_CONTENT)
    }
}