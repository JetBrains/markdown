package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil
import org.intellij.markdown.parser.sequentialparsers.TokensCache
import java.util.*

public class StrikeThroughParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: Collection<Range<Int>>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResult()
        val delegateIndices = ArrayList<Int>()
        val indices = SequentialParserUtil.textRangesToIndices(rangesToGlue)
        var lastOpenedPos: Int? = null

        var i = 0
        while (i < indices.size) {
            val iterator = tokens.ListIterator(indices, i)
            if (iterator.type != GFMTokenTypes.TILDE) {
                delegateIndices.add(indices.get(i))
                i++
                continue
            }

            if (lastOpenedPos != null
                    && isGoodType(iterator.rawLookup(-1))
                    && iterator.rawLookup(1) == GFMTokenTypes.TILDE) {
                result.withNode(SequentialParser.Node(indices.get(lastOpenedPos)..indices.get(i + 1) + 1,
                        GFMElementTypes.STRIKETHROUGH));
                i += 2
                lastOpenedPos = null
                continue
            }
            if (lastOpenedPos == null
                    && iterator.rawLookup(1) == GFMTokenTypes.TILDE
                    && isGoodType(iterator.rawLookup(2))) {
                lastOpenedPos = i
                i += 2
                continue
            }

            delegateIndices.add(indices.get(i))

            i++
        }

        if (lastOpenedPos != null) {
            for (delta in 0..1) {
                delegateIndices.add(indices.get(lastOpenedPos + delta))
            }
            Collections.sort(delegateIndices)
        }

        return result.withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(delegateIndices))
    }

    private fun isGoodType(type: IElementType?): Boolean {
        return type != null
                && type != MarkdownTokenTypes.WHITE_SPACE
                && type != MarkdownTokenTypes.EOL
                && type != GFMTokenTypes.TILDE
                && type != MarkdownTokenTypes.HTML_TAG
                && type != MarkdownTokenTypes.HTML_BLOCK_CONTENT;
    }
}