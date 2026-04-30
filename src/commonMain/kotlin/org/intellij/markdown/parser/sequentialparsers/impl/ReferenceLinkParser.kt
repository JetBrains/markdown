package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.LocalParsingResult
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache

class ReferenceLinkParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
        var result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = RangesListBuilder()
        var iterator: TokensCache.Iterator = tokens.RangesListIterator(rangesToGlue)

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.LBRACKET) {
                val referenceLink = parseReferenceLink(iterator)
                if (referenceLink != null) {
                    iterator = referenceLink.iteratorPosition.advance()
                    result = result.withOtherParsingResult(referenceLink)
                    continue
                }
            }

            delegateIndices.put(iterator.index)
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(delegateIndices.get())
    }

    companion object {
        fun parseReferenceLink(iterator: TokensCache.Iterator): LocalParsingResult? {
            return parseFullReferenceLink(iterator) ?: parseShortReferenceLink(iterator)
        }

        private fun parseFullReferenceLink(iterator: TokensCache.Iterator): LocalParsingResult? {
            val startIndex = iterator.index

            val linkText = LinkParserUtil.parseLinkText(iterator)
                    ?: return null
            var it = linkText.iteratorPosition.advance()

            if (it.start != linkText.iteratorPosition.end) {
                return null
            }

            val linkLabel = LinkParserUtil.parseLinkLabel(it)
                    ?: return null

            it = linkLabel.iteratorPosition
            return LocalParsingResult(it,
                    linkText.parsedNodes
                            + linkLabel.parsedNodes
                            + SequentialParser.Node(startIndex..it.index + 1, MarkdownElementTypes.FULL_REFERENCE_LINK),
                    linkText.rangesToProcessFurther + linkLabel.rangesToProcessFurther)
        }

        private fun parseShortReferenceLink(iterator: TokensCache.Iterator): LocalParsingResult? {
            val startIndex = iterator.index

            val linkLabel = LinkParserUtil.parseLinkLabel(iterator)
                    ?: return null

            var it = linkLabel.iteratorPosition
            val shortcutLinkEnd = it

            it = it.advance()

            if (it.start == shortcutLinkEnd.end && it.type == MarkdownTokenTypes.LBRACKET && it.rawLookup(1) == MarkdownTokenTypes.RBRACKET) {
                it = it.advance()
            } else {
                it = shortcutLinkEnd
            }

            return LocalParsingResult(it,
                    linkLabel.parsedNodes
                            + SequentialParser.Node(startIndex..it.index + 1, MarkdownElementTypes.SHORT_REFERENCE_LINK),
                    linkLabel.rangesToProcessFurther)
        }

    }

}
