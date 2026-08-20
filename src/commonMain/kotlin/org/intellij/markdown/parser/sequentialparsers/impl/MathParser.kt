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
        var linkRangeIndex = 0
        var skipUntil = -1
        var iterator = tokens.RangesListIterator(rangesToGlue)

        val ranges = collectLinkRanges(tokens, rangesToGlue)
        val nextClosers = collectNextClosingIndices(tokens, rangesToGlue, ranges)

        while (iterator.type != null) {
            if (iterator.index <= skipUntil) {
                iterator = iterator.advance()
                continue
            }

            if (iterator.type == GFMTokenTypes.DOLLAR && canOpenMath(iterator)) {
                while (linkRangeIndex < ranges.size && iterator.index > ranges[linkRangeIndex].last) {
                    linkRangeIndex++
                }

                val isInsideLink = linkRangeIndex < ranges.size && iterator.index >= ranges[linkRangeIndex].first
                if (!isInsideLink) {
                    val endIndex = nextClosers[iterator.index]
                    if (endIndex == -1) {
                        delegateIndices.put(iterator.index)
                        iterator = iterator.advance()
                        continue
                    }
                    if (iterator.length == 1) {
                        result.withNode(SequentialParser.Node(iterator.index..endIndex + 1, GFMElementTypes.INLINE_MATH))
                    } else {
                        result.withNode(SequentialParser.Node(iterator.index..endIndex + 1, GFMElementTypes.BLOCK_MATH))
                    }
                    skipUntil = endIndex
                    iterator = iterator.advance()
                    continue
                }
            }
            delegateIndices.put(iterator.index)
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(delegateIndices.get())
    }

    private fun collectNextClosingIndices(
        tokens: TokensCache,
        rangesToGlue: List<IntRange>,
        linkRanges: List<IntRange>,
    ): IntArray {
        val result = IntArray(tokens.filteredTokens.size) { -1 }
        val nextClosingByLength = HashMap<Int, Int>()
        var linkRangeIndex = linkRanges.lastIndex

        for (range in rangesToGlue.asReversed()) {
            for (index in range.last downTo range.first) {
                val iterator = tokens.Iterator(index)
                if (iterator.type != GFMTokenTypes.DOLLAR) continue

                while (linkRangeIndex >= 0 && index < linkRanges[linkRangeIndex].first) {
                    linkRangeIndex--
                }
                val isInsideLink = linkRangeIndex >= 0 && index <= linkRanges[linkRangeIndex].last

                result[index] = nextClosingByLength[iterator.length] ?: -1
                if (!isInsideLink && canCloseMath(iterator)) {
                    nextClosingByLength[iterator.length] = index
                }
            }
        }
        return result
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

    private fun Char.isWordCharacter(): Boolean {
        return isLetterOrDigit() || this == '_'
    }
}
