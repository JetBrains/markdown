package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil
import org.intellij.markdown.parser.sequentialparsers.TokensCache

class ImageParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
        var result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = RangesListBuilder()
        var iterator: TokensCache.Iterator = tokens.RangesListIterator(rangesToGlue)

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.EXCLAMATION_MARK
                    && iterator.rawLookup(1) == MarkdownTokenTypes.LBRACKET) {
                val link = InlineLinkParser.parseInlineLink(iterator.advance())
                        ?: ReferenceLinkParser.parseReferenceLink(iterator.advance())

                if (link != null) {
                    result = result
                            .withNode(SequentialParser.Node(iterator.index..link.iteratorPosition.index + 1, MarkdownElementTypes.IMAGE))
                            .withOtherParsingResult(link)
                    iterator = link.iteratorPosition.advance()
                    continue
                }
            }

            delegateIndices.put(iterator.index)
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(delegateIndices.get())

    }
}