package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.*

class LinkParserUtil {
    /**
     * Where the link parts starting at the tokens of [rangesToGlue] end, each kind of part found in one pass.
     *
     * Every `[` is a link candidate, and a failed one used to scan its text, destination, and title
     * up to their ends only to throw the result away, so text full of such candidates made inline
     * parsing quadratic (IJPL-96392). Given this index, the scans look the ends up instead.
     * It must be used only with iterators over the same [rangesToGlue].
     */
    class ScanIndex(tokens: TokensCache, rangesToGlue: List<IntRange>) {
        private val iterators = ArrayList<TokensCache.Iterator>().apply {
            var iterator: TokensCache.Iterator = tokens.RangesListIterator(rangesToGlue)
            while (iterator.type != null) {
                add(iterator)
                iterator = iterator.advance()
            }
        }
        private val matchingBracket = matchPairs(MarkdownTokenTypes.LBRACKET, MarkdownTokenTypes.RBRACKET)

        // Mirrors the loop of parseLinkDestination without braces: destinationEnd[i] is where
        // the loop entered at i with zero depth stops, or -1 if the parentheses are unbalanced there.
        private val destinationEnd by lazy {
            val matchingParen = matchPairs(MarkdownTokenTypes.LPAREN, MarkdownTokenTypes.RPAREN)
            val result = IntArray(iterators.size)
            var wordEnd = -1
            for (i in iterators.lastIndex downTo 0) {
                val token = iterators[i]
                val next = token.rawLookup(1)
                val isWordEnd = SequentialParserUtil.isWhitespace(token, 1) || next == null
                if (isWordEnd) {
                    wordEnd = i
                }
                val closingParen = matchingParen[i]
                result[i] = when {
                    token.type == MarkdownTokenTypes.LPAREN ->
                        if (closingParen != -1 && closingParen <= wordEnd) result[closingParen] else -1
                    isWordEnd || next == MarkdownTokenTypes.RPAREN -> i
                    else -> result[i + 1]
                }
            }
            result
        }

        // For a closing token type, the first position of such a token from each position on, or -1.
        private val nextOfType = listOf(
            MarkdownTokenTypes.GT,
            MarkdownTokenTypes.RPAREN,
            MarkdownTokenTypes.SINGLE_QUOTE,
            MarkdownTokenTypes.DOUBLE_QUOTE,
        ).associateWith { lazy { nextPositions(it) } }

        private fun matchPairs(open: IElementType, close: IElementType): IntArray {
            val result = IntArray(iterators.size) { -1 }
            val stack = ArrayDeque<Int>()
            for (i in iterators.indices) {
                when (iterators[i].type) {
                    open -> stack.addLast(i)
                    close -> if (stack.isNotEmpty()) result[stack.removeLast()] = i
                }
            }
            return result
        }

        private fun nextPositions(type: IElementType): IntArray {
            val result = IntArray(iterators.size)
            var next = -1
            for (i in iterators.lastIndex downTo 0) {
                if (iterators[i].type == type) {
                    next = i
                }
                result[i] = next
            }
            return result
        }

        private fun find(iterator: TokensCache.Iterator, positions: IntArray, offset: Int = 0): TokensCache.Iterator? {
            val i = iterators.binarySearch { it.index.compareTo(iterator.index) }
            if (i < 0) {
                return null
            }
            return iterators.getOrNull(positions.getOrElse(i + offset) { -1 })
        }

        /** The `]` matching the `[` at [iterator], as found by [parseLinkText]. */
        internal fun matchingBracket(iterator: TokensCache.Iterator) = find(iterator, matchingBracket)

        internal fun destinationEnd(iterator: TokensCache.Iterator) = find(iterator, destinationEnd)

        /** The first token of [type] after [iterator]; [type] closes a `<...>` destination or a title. */
        internal fun nextAfter(iterator: TokensCache.Iterator, type: IElementType) =
            find(iterator, nextOfType.getValue(type).value, offset = 1)
    }

    companion object {
        fun parseLinkDestination(iterator: TokensCache.Iterator, scanIndex: ScanIndex? = null): LocalParsingResult? {
            var it = iterator
            if (it.type == MarkdownTokenTypes.EOL || it.type == MarkdownTokenTypes.RPAREN) {
                return null
            }

            val startIndex = it.index
            val withBraces = it.type == MarkdownTokenTypes.LT
            if (scanIndex != null) {
                it = (if (withBraces) scanIndex.nextAfter(it, MarkdownTokenTypes.GT) else scanIndex.destinationEnd(it))
                    ?: return null
                return LocalParsingResult(it,
                        listOf(SequentialParser.Node(startIndex..it.index + 1, MarkdownElementTypes.LINK_DESTINATION)))
            }
            if (withBraces) {
                it = it.advance()
            }

            var openParenthesesDepth = 0
            while (it.type != null) {
                if (withBraces && it.type == MarkdownTokenTypes.GT) {
                    break
                } else if (!withBraces) {
                    if (it.type == MarkdownTokenTypes.LPAREN) {
                        openParenthesesDepth++
                    }

                    val next = it.rawLookup(1)
                    if (SequentialParserUtil.isWhitespace(it, 1) || next == null) {
                        break
                    } else if (next == MarkdownTokenTypes.RPAREN) {
                        if (openParenthesesDepth == 0) {
                            break
                        }
                        openParenthesesDepth--
                    }
                }

                it = it.advance()
            }

            if (it.type != null && openParenthesesDepth == 0) {
                return LocalParsingResult(it, 
                        listOf(SequentialParser.Node(startIndex..it.index + 1, MarkdownElementTypes.LINK_DESTINATION)))
            }
            return null
        }

        fun parseLinkLabel(iterator: TokensCache.Iterator): LocalParsingResult? {
            var it = iterator

            if (it.type != MarkdownTokenTypes.LBRACKET) {
                return null
            }

            val startIndex = it.index

            val delegate = RangesListBuilder()

            it = it.advance()
            while (it.type != MarkdownTokenTypes.RBRACKET && it.type != null) {
                delegate.put(it.index)
                if (it.type == MarkdownTokenTypes.LBRACKET) {
                    break
                }
                it = it.advance()
            }

            if (it.type == MarkdownTokenTypes.RBRACKET) {
                val endIndex = it.index
                if (endIndex == startIndex + 1) {
                    return null
                }

                return LocalParsingResult(it,
                        listOf(SequentialParser.Node(startIndex..endIndex + 1, MarkdownElementTypes.LINK_LABEL)),
                        delegate.get())
            }
            return null
        }

        fun parseLinkText(iterator: TokensCache.Iterator): LocalParsingResult? {
            var it = iterator

            if (it.type != MarkdownTokenTypes.LBRACKET) {
                return null
            }

            val startIndex = it.index
            val delegate = RangesListBuilder()

            var bracketDepth = 1

            it = it.advance()
            while (it.type != null) {
                if (it.type == MarkdownTokenTypes.RBRACKET) {
                    if (--bracketDepth == 0) {
                        break
                    }
                }

                delegate.put(it.index)
                if (it.type == MarkdownTokenTypes.LBRACKET) {
                    bracketDepth++
                }
                it = it.advance()
            }

            if (it.type == MarkdownTokenTypes.RBRACKET) {
                return LocalParsingResult(it,
                        listOf(SequentialParser.Node(startIndex..it.index + 1, MarkdownElementTypes.LINK_TEXT)),
                        delegate.get())
            }
            return null
        }

        fun buildBracketStarts(
            tokens: TokensCache,
            rangesToGlue: List<IntRange>,
            closeFilter: (TokensCache.Iterator) -> Boolean = { true }
        ): Set<Int> {
            val result = HashSet<Int>()
            val stack = ArrayDeque<Int>()
            var it = tokens.RangesListIterator(rangesToGlue)
            while (it.type != null) {
                when (it.type) {
                    MarkdownTokenTypes.LBRACKET -> stack.addLast(it.index)
                    MarkdownTokenTypes.RBRACKET -> if (stack.isNotEmpty()) {
                        val openIdx = stack.removeLast()
                        if (closeFilter(it)) result.add(openIdx)
                    }
                }
                it = it.advance()
            }
            return result
        }

        fun parseLinkTitle(iterator: TokensCache.Iterator, scanIndex: ScanIndex? = null): LocalParsingResult? {
            var it = iterator
            if (it.type == MarkdownTokenTypes.EOL) {
                return null
            }

            val startIndex = it.index
            val closingType = when (it.type) {
                MarkdownTokenTypes.SINGLE_QUOTE -> MarkdownTokenTypes.SINGLE_QUOTE
                MarkdownTokenTypes.DOUBLE_QUOTE -> MarkdownTokenTypes.DOUBLE_QUOTE
                MarkdownTokenTypes.LPAREN -> MarkdownTokenTypes.RPAREN
                else -> return null
            }

            if (scanIndex != null) {
                it = scanIndex.nextAfter(it, closingType) ?: return null
            } else {
                it = it.advance()
                while (it.type != null && it.type != closingType) {
                    it = it.advance()
                }
            }

            if (it.type != null) {
                return LocalParsingResult(it, 
                        listOf(SequentialParser.Node(startIndex..it.index + 1, MarkdownElementTypes.LINK_TITLE)))
            }
            return null
        }
    }
}
