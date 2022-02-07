package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache

@Deprecated(
    message = "Use EmphasisLikeParser with StrikeThroughDelimiterParser instead",
    replaceWith = ReplaceWith(
        expression = "EmphasisLikeParser(StrikeThroughDelimiterParser())",
        imports = arrayOf(
            "org.intellij.markdown.parser.sequentialparsers.EmphasisLikeParser",
            "org.intellij.markdown.flavours.gfm.StrikeThroughDelimiterParser"
        )
    )
)
class StrikeThroughParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = RangesListBuilder()
        var lastOpenedPos: Int? = null
        var iterator = tokens.RangesListIterator(rangesToGlue)

        while (iterator.type != null) {
            if (iterator.type != GFMTokenTypes.TILDE) {
                delegateIndices.put(iterator.index)
                iterator = iterator.advance()
                continue
            }

            if (lastOpenedPos != null
                    && isGoodType(iterator.rawLookup(-1))
                    && iterator.rawLookup(1) == GFMTokenTypes.TILDE) {
                iterator = iterator.advance()
                result.withNode(SequentialParser.Node(lastOpenedPos..iterator.index + 1, GFMElementTypes.STRIKETHROUGH))
                iterator = iterator.advance()
                lastOpenedPos = null
                continue
            }
            if (lastOpenedPos == null
                    && iterator.rawLookup(1) == GFMTokenTypes.TILDE
                    && isGoodType(iterator.rawLookup(2))) {
                lastOpenedPos = iterator.index
                iterator = iterator.advance().advance()
                continue
            }

            delegateIndices.put(iterator.index)
            iterator = iterator.advance()
        }

        if (lastOpenedPos != null) {
            for (delta in 0..1) {
                delegateIndices.put(lastOpenedPos + delta)
            }
        }

        return result.withFurtherProcessing(delegateIndices.get())
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
