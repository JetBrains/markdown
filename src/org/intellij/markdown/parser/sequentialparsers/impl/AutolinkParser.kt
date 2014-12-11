package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.TokensCache
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil
import java.util.ArrayList

public class AutolinkParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: Collection<Range<Int>>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResult()
        val delegateIndices = ArrayList<Int>()
        val indices = SequentialParserUtil.textRangesToIndices(rangesToGlue)

        var i = 0
        while (i < indices.size()) {
            var iterator: TokensCache.Iterator = tokens.ListIterator(indices, i)

            if (iterator.type == MarkdownTokenTypes.LT && iterator.rawLookup(1) == MarkdownTokenTypes.AUTOLINK) {
                val start = i
                while (iterator.type != MarkdownTokenTypes.GT) {
                    iterator = iterator.advance()
                    i++
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
