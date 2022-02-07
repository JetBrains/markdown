package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.DelimiterParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache

class EmphStrongDelimiterParser: DelimiterParser() {
    override fun scan(tokens: TokensCache, iterator: TokensCache.Iterator, delimiters: MutableList<Info>): Int {
        if (iterator.type != MarkdownTokenTypes.EMPH) {
            return 0
        }
        var stepsToAdvance = 1
        var rightIterator = iterator
        val marker = getType(rightIterator)
        for (index in 0 until maxAdvance) {
            if (rightIterator.rawLookup(1) != MarkdownTokenTypes.EMPH || getType(rightIterator.advance()) != marker) {
                break
            }
            rightIterator = rightIterator.advance()
            stepsToAdvance += 1
        }
        val (canOpen, canClose) = canOpenClose(tokens, iterator, rightIterator, canSplitText = marker == '*')
        for (index in 0 until stepsToAdvance) {
            val info = Info(
                tokenType = MarkdownTokenTypes.EMPH,
                position = iterator.index + index,
                length = stepsToAdvance,
                canOpen = canOpen,
                canClose = canClose,
                marker = marker
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
        var isStrong = false
        for (index in delimiters.indices.reversed()) {
            if (isStrong) {
                isStrong = false
                continue
            }
            val opener = delimiters[index]
            if (opener.tokenType != MarkdownTokenTypes.EMPH || opener.closerIndex == -1) {
                continue
            }
            isStrong = areAdjacentSameMarkers(delimiters, index, opener.closerIndex)
            val closer = delimiters[opener.closerIndex]
            val node = when {
                isStrong -> SequentialParser.Node(
                    range = opener.position - 1..closer.position + 2,
                    type = MarkdownElementTypes.STRONG
                )
                else -> SequentialParser.Node(
                    range = opener.position..closer.position + 1,
                    type = MarkdownElementTypes.EMPH
                )
            }
            result.withNode(node)
        }
    }

    companion object {
        fun areAdjacentSameMarkers(delimiters: List<Info>, openerIndex: Int, closerIndex: Int): Boolean {
            val opener = delimiters[openerIndex]
            val closer = delimiters[closerIndex]
            return openerIndex > 0 &&
                delimiters[openerIndex - 1].closerIndex == opener.closerIndex + 1 &&
                delimiters[openerIndex - 1].marker == opener.marker &&
                delimiters[openerIndex - 1].position == opener.position - 1 &&
                delimiters[opener.closerIndex + 1].position == closer.position + 1
        }
    }
}
