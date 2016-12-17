package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil
import org.intellij.markdown.parser.sequentialparsers.TokensCache
import java.util.*

class AutolinkParser(private val typesAfterLT: List<IElementType>) : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = ArrayList<Int>()
        val indices = SequentialParserUtil.textRangesToIndices(rangesToGlue)

        var i = 0
        while (i < indices.size) {
            var iterator: TokensCache.Iterator = tokens.ListIterator(indices, i)

            if (iterator.type == MarkdownTokenTypes.LT && iterator.rawLookup(1).let { it != null && it in typesAfterLT }) {
                val start = i
                while (iterator.type != MarkdownTokenTypes.GT && iterator.type != null) {
                    iterator = iterator.advance()
                    i++
                }
                if (iterator.type == null) {
                    i--
                }
                result.withNode(SequentialParser.Node(indices.get(start)..indices.get(i) + 1, MarkdownElementTypes.AUTOLINK))
            } else {
                delegateIndices.add(indices.get(i))
            }
            ++i
        }

        return result.withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(delegateIndices))
    }
}
