package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil
import org.intellij.markdown.parser.sequentialparsers.TokensCache

class AutolinkParser(private val typesAfterLT: List<IElementType>) : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = RangesListBuilder()
        var iterator = tokens.RangesListIterator(rangesToGlue)

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.LT && iterator.rawLookup(1).let { it != null && it in typesAfterLT }) {
                val start = iterator.index
                while (iterator.type != MarkdownTokenTypes.GT && iterator.type != null) {
                    iterator = iterator.advance()
                }
                if (iterator.type == MarkdownTokenTypes.GT) {
                    result.withNode(SequentialParser.Node(start..iterator.index + 1, MarkdownElementTypes.AUTOLINK))
                }
            } else {
                delegateIndices.put(iterator.index)
            }
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(delegateIndices.get())
    }
}
