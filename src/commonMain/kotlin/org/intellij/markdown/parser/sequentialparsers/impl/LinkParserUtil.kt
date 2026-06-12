package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.*

class LinkParserUtil {
    companion object {
        fun parseLinkDestination(iterator: TokensCache.Iterator): LocalParsingResult? {
            var it = iterator
            if (it.type == MarkdownTokenTypes.EOL || it.type == MarkdownTokenTypes.RPAREN) {
                return null
            }

            val startIndex = it.index
            val withBraces = it.type == MarkdownTokenTypes.LT
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

        fun parseLinkTitle(iterator: TokensCache.Iterator): LocalParsingResult? {
            var it = iterator
            if (it.type == MarkdownTokenTypes.EOL) {
                return null
            }

            val startIndex = it.index
            val closingType: IElementType?

            if (it.type == MarkdownTokenTypes.SINGLE_QUOTE || it.type == MarkdownTokenTypes.DOUBLE_QUOTE) {
                closingType = it.type
            } else if (it.type == MarkdownTokenTypes.LPAREN) {
                closingType = MarkdownTokenTypes.RPAREN
            } else {
                return null
            }

            it = it.advance()
            while (it.type != null && it.type != closingType) {
                it = it.advance()
            }

            if (it.type != null) {
                return LocalParsingResult(it, 
                        listOf(SequentialParser.Node(startIndex..it.index + 1, MarkdownElementTypes.LINK_TITLE)))
            }
            return null
        }
    }
}
