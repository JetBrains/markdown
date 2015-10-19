package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil
import org.intellij.markdown.parser.sequentialparsers.TokensCache
import java.util.*

public class EmphStrongParser : SequentialParser {

    override fun parse(tokens: TokensCache, rangesToGlue: Collection<Range<Int>>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResult()

        val indices = SequentialParserUtil.textRangesToIndices(rangesToGlue)

        var myType: Char = 0.toChar()
        val openingOnes = Stack<Pair<Int, Int>>()

        var i = 0
        while (i < indices.size()) {
            val iterator = tokens.ListIterator(indices, i)
            if (iterator.type != MarkdownTokenTypes.EMPH) {
                i++
                continue
            }

            var numCanEnd = canEndNumber(iterator)
            if (numCanEnd != 0 && myType == getType(iterator) && !openingOnes.isEmpty()) {
                while (numCanEnd > 0 && !openingOnes.isEmpty()) {
                    val lastOpening = openingOnes.pop()
                    val toMakeMax = Math.min(lastOpening.second, numCanEnd)
                    val toMake = if (toMakeMax % 2 == 0) 2 else 1
                    val from = lastOpening.first + (lastOpening.second - toMake)
                    val to = i + toMake - 1

                    val nodeType = if (toMake == 2) MarkdownElementTypes.STRONG else MarkdownElementTypes.EMPH
                    result.withNode(SequentialParser.Node(indices.get(from)..indices.get(to) + 1, nodeType))

                    i += toMake
                    numCanEnd -= toMake
                    if (lastOpening.second > toMake) {
                        openingOnes.push(Pair(lastOpening.first, lastOpening.second - toMake))
                    }
                }
                continue
            }

            val numCanStart = canStartNumber(iterator)
            if (numCanStart != 0) {
                if (myType.toInt() == 0) {
                    myType = getType(iterator)
                } else if (myType != getType(iterator)) {
                    i++
                    continue
                }

                openingOnes.push(Pair(i, numCanStart))
                i += numCanStart
            }
            i++
        }

        return result
    }

    private fun canStartNumber(iterator: TokensCache.Iterator): Int {
        var leftIt = iterator
        while (leftIt.rawLookup(-1) == MarkdownTokenTypes.EMPH && getType(leftIt) == leftIt.charLookup(-1)) {
            leftIt = leftIt.rollback()
        }

        var it = iterator
        for (i in 0..50 - 1) {
            if (it.rawLookup(1) != MarkdownTokenTypes.EMPH || getType(it) != getType(it.advance())) {
                if (!isLeftFlankingRun(leftIt, it)) {
                    return 0
                }
                if (getType(it) == ITALIC && isRightFlankingRun(leftIt, it) && !SequentialParserUtil.isPunctuation(leftIt, -1)) {
                    return 0
                }

                return i + 1

            }
            it = it.advance()
        }

        return 50
    }

    private fun canEndNumber(iterator: TokensCache.Iterator): Int {
        var it = iterator
        if (SequentialParserUtil.isWhitespace(it, -1)) {
            return 0
        }

        for (i in 0..50 - 1) {
            if (it.rawLookup(1) != MarkdownTokenTypes.EMPH || getType(it) != getType(it.advance())) {
                if (!isRightFlankingRun(iterator, it)) {
                    return 0
                }

                if (getType(it) == ITALIC && isLeftFlankingRun(iterator, it) && !SequentialParserUtil.isPunctuation(it, 1)) {
                    return 0
                }

                return i + 1
            }
            it = it.advance()
        }

        return 50
    }

    /**
     * see <http://spec.commonmark.org/0.22/#left-flanking-delimiter-run>
     */
    private fun isLeftFlankingRun(leftIt: TokensCache.Iterator, rightIt: TokensCache.Iterator): Boolean {
        return !SequentialParserUtil.isWhitespace(rightIt, 1) &&
                (!SequentialParserUtil.isPunctuation(rightIt, 1)
                        || SequentialParserUtil.isWhitespace(leftIt, -1)
                        || SequentialParserUtil.isPunctuation(leftIt, -1))
    }

    private fun isRightFlankingRun(leftIt: TokensCache.Iterator, rightIt: TokensCache.Iterator): Boolean {
        return leftIt.charLookup(-1) != getType(leftIt) &&
                !SequentialParserUtil.isWhitespace(leftIt, -1) &&
                (!SequentialParserUtil.isPunctuation(leftIt, -1)
                        || SequentialParserUtil.isWhitespace(rightIt, 1)
                        || SequentialParserUtil.isPunctuation(rightIt, 1))
    }

    private fun getType(info: TokensCache.Iterator): Char {
        return info.text.charAt(0)
    }

    companion object {

        val ITALIC: Char = '_'

        val BOLD: Char = '*'
    }
}
