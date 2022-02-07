package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.parser.sequentialparsers.DelimiterParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache
import org.intellij.markdown.parser.sequentialparsers.impl.EmphStrongDelimiterParser

class StrikeThroughDelimiterParser: DelimiterParser() {
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
        val (canOpen, canClose) = canOpenClose(tokens, iterator, rightIterator, canSplitText = true)
        for (index in 0 until stepsToAdvance) {
            val info = Info(
                tokenType = GFMTokenTypes.TILDE,
                position = iterator.index + index,
                length = 0,
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
        var shouldSkipNext = false
        for (index in delimiters.indices.reversed()) {
            if (shouldSkipNext) {
                shouldSkipNext = false
                continue
            }
            val opener = delimiters[index]
            if (opener.tokenType != GFMTokenTypes.TILDE || opener.closerIndex == -1) {
                continue
            }
            shouldSkipNext = EmphStrongDelimiterParser.areAdjacentSameMarkers(delimiters, index, opener.closerIndex)
            val closer = delimiters[opener.closerIndex]
            if (shouldSkipNext) {
                val node = SequentialParser.Node(
                    range = opener.position - 1..closer.position + 2,
                    type = GFMElementTypes.STRIKETHROUGH
                )
                result.withNode(node)
            }
        }
    }
}
