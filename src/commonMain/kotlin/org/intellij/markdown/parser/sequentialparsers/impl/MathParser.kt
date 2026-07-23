package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.html.isPunctuation
import org.intellij.markdown.html.isWhitespace
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache

class MathParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = RangesListBuilder()
        var linkRanges: List<IntRange>? = null
        fun getLinkRanges(): List<IntRange> {
            return linkRanges ?: collectLinkRanges(tokens, rangesToGlue).also { linkRanges = it }
        }
        var iterator: TokensCache.Iterator = tokens.RangesListIterator(rangesToGlue)

        while (iterator.type != null) {
            if (iterator.type == GFMTokenTypes.DOLLAR && canOpenMath(iterator)) {
                val ranges = getLinkRanges()
                if (isInsideLink(iterator, ranges)) {
                    delegateIndices.put(iterator.index)
                    iterator = iterator.advance()
                    continue
                }

                val endIterator = findOfSize(iterator.advance(), iterator.length, ranges)

                if (endIterator != null) {
                    if (iterator.length == 1) {
                        result.withNode(SequentialParser.Node(iterator.index..endIterator.index + 1, GFMElementTypes.INLINE_MATH))
                    } else {
                        result.withNode(SequentialParser.Node(iterator.index..endIterator.index + 1, GFMElementTypes.BLOCK_MATH))
                    }
                    iterator = endIterator.advance()
                    continue
                }
            }
            delegateIndices.put(iterator.index)
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(delegateIndices.get())
    }

    private fun findOfSize(it: TokensCache.Iterator, length: Int, linkRanges: List<IntRange>): TokensCache.Iterator? {
        var iterator = it
        while (iterator.type != null) {
            if (iterator.type == GFMTokenTypes.DOLLAR) {
                if (iterator.length == length && canCloseMath(iterator) && !isInsideLink(iterator, linkRanges)) {
                    return iterator
                }
            }

            iterator = iterator.advance()
        }
        return null
    }

    private fun canOpenMath(iterator: TokensCache.Iterator): Boolean {
        val previous = iterator.charLookup(-1)
        return !isWhitespace(iterator.charLookup(1)) && !previous.isWordCharacter() && !isPunctuation(previous)
    }

    private fun canCloseMath(iterator: TokensCache.Iterator): Boolean {
        return !isWhitespace(iterator.charLookup(-1)) && !iterator.charLookup(1).isWordCharacter()
    }

    private fun collectLinkRanges(tokens: TokensCache, rangesToGlue: List<IntRange>): List<IntRange> {
        val result = ArrayList<IntRange>()
        val inlineLinkStarts = LinkParserUtil.buildBracketStarts(tokens, rangesToGlue) {
            it.rawLookup(1) == MarkdownTokenTypes.LPAREN
        }
        val referenceLinkStarts = LinkParserUtil.buildBracketStarts(tokens, rangesToGlue)
        var iterator: TokensCache.Iterator = tokens.RangesListIterator(rangesToGlue)

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.LBRACKET) {
                val link = if (iterator.index in inlineLinkStarts) {
                    InlineLinkParser.parseInlineLink(iterator, inlineLinkStarts)
                } else {
                    null
                } ?: if (iterator.index in referenceLinkStarts) {
                    ReferenceLinkParser.parseReferenceLink(iterator)
                } else {
                    null
                }
                if (link != null) {
                    result.add(iterator.index..link.iteratorPosition.index)
                    iterator = link.iteratorPosition.advance()
                    continue
                }
            }
            iterator = iterator.advance()
        }
        return result
    }

    private fun isInsideLink(iterator: TokensCache.Iterator, linkRanges: List<IntRange>): Boolean {
        return linkRanges.any { iterator.index in it }
    }

    private fun Char.isWordCharacter(): Boolean {
        return isLetterOrDigit() || this == '_'
    }
}
