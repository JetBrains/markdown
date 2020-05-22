package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.IElementType
import org.intellij.markdown.flavours.gfm.lexer._GFMLexer
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache

class GfmAutolinkParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = RangesListBuilder()

        for (range in rangesToGlue) {
            val textRange = tokens.Iterator(range.first).start until tokens.Iterator(range.last).end
            val lexer = _GFMLexer(null)
            lexer.reset(tokens.originalText, textRange.first, textRange.last + 1, _GFMLexer.AUTOLINK_EXT)

            var iterator = tokens.Iterator(range.first)
            do {
                val tokenType: IElementType? = lexer.advance()
                if (tokenType == GFMElementTypes.GFM_AUTOLINK) {
                    while (iterator.start < lexer.tokenStart) {
                        delegateIndices.put(iterator.index)
                        iterator = iterator.advance()
                    }
                    val startIndex = iterator.index
                    while (iterator.start < lexer.tokenEnd) iterator = iterator.advance()
                    val endIndex = iterator.index

                    result.withNode(SequentialParser.Node(startIndex .. endIndex, GFMElementTypes.GFM_AUTOLINK))
                }
            } while (tokenType != null)
        }

        return result.withFurtherProcessing(delegateIndices.get())
    }

}
