package org.intellij.markdown.flavours.glfm

import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache

/**
 * Parser for GitLab emoji shortcodes.
 *
 * Recognizes emoji in the format `:emoji_name:` where emoji_name consists of
 * alphanumeric characters, underscores, hyphens, and plus signs.
 * The name must not contain whitespace and the two colons must be adjacent
 * to the name (`: smile :` is NOT an emoji).
 *
 * Examples: `:smile:`, `:thumbsup:`, `:+1:`
 */
class EmojiParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = RangesListBuilder()
        var iterator: TokensCache.Iterator = tokens.RangesListIterator(rangesToGlue)

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.COLON) {
                val match = matchEmoji(tokens, iterator)
                if (match != null) {
                    result.withNode(
                        SequentialParser.Node(
                            iterator.index..match.index + 1,
                            GLFMElementTypes.EMOJI
                        )
                    )
                    iterator = match.advance()
                    continue
                }
            }
            delegateIndices.put(iterator.index)
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(delegateIndices.get())
    }

    /**
     * Attempts to match `:name:` starting at the opening COLON [it].
     * Returns the iterator positioned at the closing COLON, or null.
     */
    private fun matchEmoji(tokens: TokensCache, it: TokensCache.Iterator): TokensCache.Iterator? {
        var current = it.advance()
        var nameLength = 0
        var prevEnd = it.end

        while (current.type != null && current.type != MarkdownTokenTypes.COLON) {
            // Tokens must be adjacent — a gap means whitespace was filtered out,
            // which is not allowed inside an emoji name.
            if (current.start != prevEnd) return null

            // Every char of the token must be a valid emoji-name char
            for (offset in current.start until current.end) {
                if (!isValidEmojiChar(tokens.getRawCharAt(offset))) return null
            }

            nameLength += current.length
            if (nameLength > 50) return null

            prevEnd = current.end
            current = current.advance()
        }

        if (current.type != MarkdownTokenTypes.COLON) return null
        // Closing colon must also be adjacent to the name
        if (current.start != prevEnd) return null
        if (nameLength == 0) return null

        return current
    }

    private fun isValidEmojiChar(char: Char): Boolean {
        return char.isLetterOrDigit() || char == '_' || char == '-' || char == '+'
    }
}
