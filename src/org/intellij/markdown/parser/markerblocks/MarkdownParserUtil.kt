package org.intellij.markdown.parser.markerblocks

import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.constraints.MarkdownConstraints

public class MarkdownParserUtil private constructor() {
    companion object {

        public fun calcNumberOfConsequentEols(iterator: TokensCache.Iterator): Int {
            var it = iterator
            var answer = 0
            while (true) {
                val `type` = it.type
                if (`type` != MarkdownTokenTypes.EOL) {
                    return answer
                }
                it = it.advance()
                answer++
            }
        }

        public fun getFirstNextLineNonBlockquoteRawIndex(iterator: TokensCache.Iterator): Int {
            assert(iterator.type == MarkdownTokenTypes.EOL)

            var answer = 1
            while (true) {
                val `type` = iterator.rawLookup(answer)
                if (`type` != MarkdownTokenTypes.WHITE_SPACE && `type` != MarkdownTokenTypes.BLOCK_QUOTE) {
                    return answer
                }
                answer++
            }
        }

        public fun getFirstNonWhiteSpaceRawIndex(iterator: TokensCache.Iterator): Int {
            var answer = 0
            while (true) {
                val `type` = iterator.rawLookup(answer)
                if (`type` != MarkdownTokenTypes.WHITE_SPACE && `type` != MarkdownTokenTypes.EOL) {
                    return answer
                }
                answer++
            }
        }

        public fun getFirstNonWhitespaceLineEolRawIndex(iterator: TokensCache.Iterator): Int {
            assert(iterator.type == MarkdownTokenTypes.EOL)

            val lastIndex = getFirstNonWhiteSpaceRawIndex(iterator)
            var index = lastIndex - 1
            while (index >= 0) {
                if (iterator.rawLookup(index) == MarkdownTokenTypes.EOL) {
                    return index
                }
                --index
            }
            throw AssertionError("Could not be here: 0 is EOL")
        }

        public fun getIndentBeforeRawToken(pos: LookaheadText.Position): Int {
            return pos.offsetInCurrentLine
        }

        public fun hasCodeBlockIndent(pos: LookaheadText.Position,
                                      constraints: MarkdownConstraints): Boolean {
            return getIndentBeforeRawToken(pos) >= constraints.getIndent() + 4
        }
    }

}
