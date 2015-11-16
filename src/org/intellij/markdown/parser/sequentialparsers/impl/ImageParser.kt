package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil
import org.intellij.markdown.parser.sequentialparsers.TokensCache
import java.util.*

class ImageParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: Collection<IntRange>): SequentialParser.ParsingResult {
        var result = SequentialParser.ParsingResult()
        val delegateIndices = ArrayList<Int>()
        val indices = SequentialParserUtil.textRangesToIndices(rangesToGlue)

        var iterator: TokensCache.Iterator = tokens.ListIterator(indices, 0)

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.EXCLAMATION_MARK
                    && iterator.rawLookup(1) == MarkdownTokenTypes.LBRACKET) {
                val localDelegates = ArrayList<Int>()
                val resultNodes = ArrayList<SequentialParser.Node>()
                val afterLink = InlineLinkParser.parseInlineLink(resultNodes, localDelegates, iterator.advance())
                        ?: run {
                    resultNodes.clear()
                    localDelegates.clear()
                    ReferenceLinkParser.parseReferenceLink(resultNodes, localDelegates, iterator.advance())
                }
                if (afterLink != null) {
                    resultNodes.add(SequentialParser.Node(iterator.index..afterLink.index + 1, MarkdownElementTypes.IMAGE))
                    iterator = afterLink.advance()
                    result = result.withNodes(resultNodes).withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(localDelegates))
                    continue
                }
            }

            delegateIndices.add(iterator.index)
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(delegateIndices))

    }
}