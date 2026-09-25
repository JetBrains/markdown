package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.parser.sequentialparsers.DelimiterParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache
import org.intellij.markdown.parser.sequentialparsers.impl.EmphStrongDelimiterParser

/**
 * Parses `==highlighted==` text (rendered as the HTML `<mark>` element).
 *
 * Unlike strikethrough, a highlight requires exactly two `=` on each side,
 * so `=text=` and `===text===` are left as plain text.
 *
 * See [Highlight (extended syntax)](https://www.markdownguide.org/extended-syntax/#highlight)
 */
class HighlightDelimiterParser : DelimiterParser() {
    override fun scan(tokens: TokensCache, iterator: TokensCache.Iterator, delimiters: MutableList<Info>): Int {
        if (iterator.type != GFMTokenTypes.EQUALS) {
            return 0
        }
        var stepsToAdvance = 1
        var rightIterator = iterator
        for (index in 0 until maxAdvance) {
            if (rightIterator.rawLookup(1) != GFMTokenTypes.EQUALS) {
                break
            }
            rightIterator = rightIterator.advance()
            stepsToAdvance += 1
        }
        val (canOpen, canClose) = canOpenClose(tokens, iterator, rightIterator, canSplitText = true)
        for (index in 0 until stepsToAdvance) {
            val info = Info(
                tokenType = GFMTokenTypes.EQUALS,
                position = iterator.index + index,
                length = 0,
                canOpen = canOpen,
                canClose = canClose,
                marker = '='
            )
            delimiters.add(info)
        }
        return stepsToAdvance
    }

    override fun process(
        tokens: TokensCache,
        iterator: TokensCache.Iterator,
        delimiters: MutableList<Info>,
        result: SequentialParser.ParsingResultBuilder
    ) {
        // Start at the end and move backward, matching tokens
        var index = delimiters.size - 1

        while (index > 0) {
            // Find opening equals sign
            if (!delimiters[index].isOpeningEquals()) {
                index -= 1
                continue
            }
            var openerIndex = index
            var closerIndex = delimiters[index].closerIndex

            // Attempt to widen the matched delimiters
            var delimitersMatched = 1
            while (EmphStrongDelimiterParser.areAdjacentSameMarkers(delimiters, openerIndex, closerIndex)) {
                openerIndex -= 1
                closerIndex += 1
                delimitersMatched += 1
            }

            // A highlight requires exactly two `=` on each side
            if (delimitersMatched == 2) {
                val opener = delimiters[openerIndex]
                val closer = delimiters[closerIndex]

                result.withNode(SequentialParser.Node(opener.position..closer.position + 1, GFMElementTypes.HIGHLIGHT))
            }

            // Update index
            index = openerIndex - 1
        }
    }
}

private fun DelimiterParser.Info.isOpeningEquals(): Boolean =
    tokenType == GFMTokenTypes.EQUALS && closerIndex != -1
