package org.intellij.markdown.flavours.glfm

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache

/**
 * Parser for GitLab inline diff syntax.
 *
 * Supports:
 * - Additions: `{+ added text +}` or `{+added text+}`
 * - Deletions: `[- deleted text -]` or `[-deleted text-]`
 *
 * Restrictions (matching GitLab behaviour):
 * - The diff must open and close on the same line (no EOL inside).
 *
 * Token shapes from the GFM lexer:
 * - `{+`  → TEXT token starting with those chars (may be longer, e.g. `{+nospace+}` is one token)
 * - `[-`  → LBRACKET + TEXT starting with `-`
 * - `+}`  → TEXT token whose first two chars are `+}` (may include trailing punctuation)
 * - `-]`  → TEXT ending with `-` + RBRACKET
 */
class InlineDiffParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = RangesListBuilder()
        var iterator: TokensCache.Iterator = tokens.RangesListIterator(rangesToGlue)

        while (iterator.type != null) {
            val node = detectDiff(tokens, iterator)
            if (node != null) {
                result.withNode(node)
                // advance past the node's last token
                var cur = iterator
                while (cur.index < node.range.last) cur = cur.advance()
                iterator = cur
                continue
            }
            delegateIndices.put(iterator.index)
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(delegateIndices.get())
    }

    private fun detectDiff(tokens: TokensCache, it: TokensCache.Iterator): SequentialParser.Node? {
        // ── Addition ────────────────────────────────────────────────────────
        // Opening: TEXT token starting with "{+"
        if (it.type == MarkdownTokenTypes.TEXT && it.length >= 2 &&
            tokens.getRawCharAt(it.start) == '{' && tokens.getRawCharAt(it.start + 1) == '+'
        ) {
            // Single-token form: "{+text+}" — one TEXT token that also ends with "+}"
            if (it.length >= 4 &&
                tokens.getRawCharAt(it.end - 2) == '+' && tokens.getRawCharAt(it.end - 1) == '}'
            ) {
                return SequentialParser.Node(it.index..it.index + 1, GLFMElementTypes.INLINE_DIFF_ADDITION)
            }
            // Multi-token form: "{+" … "+}"
            val close = findAdditionClose(tokens, it.advance())
            if (close != null) {
                return SequentialParser.Node(it.index..close.index + 1, GLFMElementTypes.INLINE_DIFF_ADDITION)
            }
        }

        // ── Deletion ────────────────────────────────────────────────────────
        // Opening: LBRACKET + TEXT starting with "-"
        if (it.type == MarkdownTokenTypes.LBRACKET) {
            val next = it.advance()
            if (next.type == MarkdownTokenTypes.TEXT && next.length >= 1 &&
                tokens.getRawCharAt(next.start) == '-'
            ) {
                // Single-token content form: "[-text-]" → LBRACKET + TEXT("-text-") + RBRACKET
                if (next.length >= 2 && tokens.getRawCharAt(next.end - 1) == '-') {
                    val after = next.advance()
                    if (after.type == MarkdownTokenTypes.RBRACKET) {
                        return SequentialParser.Node(it.index..after.index + 1, GLFMElementTypes.INLINE_DIFF_DELETION)
                    }
                }
                // Multi-token form: LBRACKET TEXT("-") … TEXT("-") RBRACKET
                if (next.length == 1) {
                    val close = findDeletionClose(tokens, next.advance())
                    if (close != null) {
                        return SequentialParser.Node(it.index..close.index + 1, GLFMElementTypes.INLINE_DIFF_DELETION)
                    }
                }
            }
        }

        return null
    }

    /** Finds a TEXT token whose first two chars are `+}` on the same line. */
    private fun findAdditionClose(tokens: TokensCache, from: TokensCache.Iterator): TokensCache.Iterator? {
        var cur = from
        while (cur.type != null) {
            if (cur.type == MarkdownTokenTypes.EOL) return null // must not span lines
            if (cur.type == MarkdownTokenTypes.TEXT && cur.length >= 2 &&
                tokens.getRawCharAt(cur.start) == '+' && tokens.getRawCharAt(cur.start + 1) == '}'
            ) {
                return cur
            }
            cur = cur.advance()
        }
        return null
    }

    /** Finds TEXT("-") followed by RBRACKET on the same line; returns the RBRACKET iterator. */
    private fun findDeletionClose(tokens: TokensCache, from: TokensCache.Iterator): TokensCache.Iterator? {
        var cur = from
        while (cur.type != null) {
            if (cur.type == MarkdownTokenTypes.EOL) return null // must not span lines
            if (cur.type == MarkdownTokenTypes.TEXT && cur.length == 1 &&
                tokens.getRawCharAt(cur.start) == '-'
            ) {
                val next = cur.advance()
                if (next.type == MarkdownTokenTypes.RBRACKET) {
                    return next
                }
            }
            cur = cur.advance()
        }
        return null
    }
}
