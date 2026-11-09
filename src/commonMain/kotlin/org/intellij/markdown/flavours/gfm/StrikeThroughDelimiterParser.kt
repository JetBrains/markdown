package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.parser.sequentialparsers.DelimiterParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache
import org.intellij.markdown.parser.sequentialparsers.impl.EmphStrongDelimiterParser

class StrikeThroughDelimiterParser : DelimiterParser() {
    override fun scan(tokens: TokensCache, iterator: TokensCache.Iterator, delimiters: MutableList<Info>): Int {
        if (iterator.type != GFMTokenTypes.TILDE) {
            return 0
        }
        var stepsToAdvance = 1
        var rightIterator = iterator
        for (index in 0 until maxAdvance) {
            if (rightIterator.rawLookup(1) != GFMTokenTypes.TILDE) {
                break
            }
            rightIterator = rightIterator.advance()
            stepsToAdvance += 1
        }
        // Runs of three or more tildes never open or close a strikethrough
        val (canOpen, canClose) = when {
            stepsToAdvance > 2 -> false to false
            else -> canOpenClose(tokens, iterator, rightIterator, canSplitText = true)
        }
        for (index in 0 until stepsToAdvance) {
            val info = Info(
                tokenType = GFMTokenTypes.TILDE,
                position = iterator.index + index,
                length = stepsToAdvance,
                canOpen = canOpen,
                canClose = canClose,
                marker = '~'
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

        while (index >= 0) {
            // Find opening tilde
            if (!delimiters[index].isOpeningTilde()) {
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

            val opener = delimiters[openerIndex]
            val closer = delimiters[closerIndex]
            // The opening and closing runs must be fully matched with each other
            if (delimitersMatched == opener.length && delimitersMatched == closer.length) {
                result.withNode(SequentialParser.Node(opener.position..closer.position + 1, GFMElementTypes.STRIKETHROUGH))
            }

            // Update index
            index = openerIndex - 1
        }
    }

    /**
     * A strikethrough is wrapped in a matching pair of one or two tildes,
     * so the opening and closing runs must have the same length.
     *
     * See [GFM spec](https://github.github.com/gfm/#strikethrough-extension-).
     */
    override fun canMatch(opener: Info, closer: Info): Boolean =
        opener.tokenType != GFMTokenTypes.TILDE || opener.length == closer.length
}

private fun DelimiterParser.Info.isOpeningTilde(): Boolean =
    tokenType == GFMTokenTypes.TILDE && closerIndex != -1
