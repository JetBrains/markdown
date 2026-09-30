package org.intellij.markdown.flavours.glfm

import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache

/**
 * Parser for GitLab-specific references.
 *
 * Token shapes from GFM lexer (whitespace is filtered by sequential parser):
 *   @username  → single TEXT token (may include trailing punctuation)
 *   #123       → single TEXT token starting with '#'
 *   !456       → EXCLAMATION_MARK token + TEXT token "456"
 *   group/project#123 → single TEXT token
 *   ~label     → GFMTokenTypes.TILDE token + TEXT token
 *   %milestone → TEXT token starting with '%'
 *   &123       → TEXT token starting with '&'
 *
 * We only emit a GITLAB_REFERENCE when we consume complete token(s).
 */
class GitLabReferenceParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = RangesListBuilder()
        var iterator: TokensCache.Iterator = tokens.RangesListIterator(rangesToGlue)

        while (iterator.type != null) {
            val consumed = detectReference(tokens, iterator)
            if (consumed != null) {
                result.withNode(
                    SequentialParser.Node(
                        iterator.index..consumed + 1,
                        GLFMElementTypes.GITLAB_REFERENCE
                    )
                )
                var cur = iterator
                while (cur.index <= consumed) cur = cur.advance()
                iterator = cur
            } else {
                delegateIndices.put(iterator.index)
                iterator = iterator.advance()
            }
        }

        return result.withFurtherProcessing(delegateIndices.get())
    }

    /**
     * Returns the last consumed token index, or null if no reference detected.
     */
    private fun detectReference(tokens: TokensCache, it: TokensCache.Iterator): Int? {
        // !NNN  — EXCLAMATION_MARK + TEXT starting with digits
        if (it.type == MarkdownTokenTypes.EXCLAMATION_MARK) {
            val next = it.advance()
            if (next.type == MarkdownTokenTypes.TEXT) {
                val t = tokenText(tokens, next)
                // First char must be a digit; digits must end at a word boundary
                // (trailing punctuation is fine — the generator trims it)
                if (t.isNotEmpty() && t[0].isDigit() && hasDigitBoundary(t, 0)) {
                    return next.index
                }
            }
            return null
        }

        // $NNN  — DOLLAR token (GFM math token) + TEXT starting with digits
        if (it.type == GFMTokenTypes.DOLLAR && it.length == 1) {
            val next = it.advance()
            if (next.type == MarkdownTokenTypes.TEXT && next.length > 0) {
                val t = tokenText(tokens, next)
                if (t[0].isDigit() && hasDigitBoundary(t, 0)) {
                    return next.index
                }
            }
            return null
        }

        // TILDE → ~label or ~"multi word label" (but NOT ~~strikethrough~~)
        if (it.type == GFMTokenTypes.TILDE) {
            val next = it.advance()
            // If next token is also TILDE → ~~text~~ strikethrough opener, skip
            if (next.type == GFMTokenTypes.TILDE) return null
            // If the raw token immediately before this TILDE is also TILDE → we are
            // the second ~ of ~~, which means we're inside strikethrough context
            if (it.rawLookup(-1) == GFMTokenTypes.TILDE) return null

            // Quoted label: ~"multi word" → TILDE, DOUBLE_QUOTE, TEXT…, DOUBLE_QUOTE
            if (next.type == MarkdownTokenTypes.DOUBLE_QUOTE) {
                var cur = next.advance()
                var sawContent = false
                while (cur.type != null &&
                    cur.type != MarkdownTokenTypes.DOUBLE_QUOTE &&
                    cur.type != MarkdownTokenTypes.EOL
                ) {
                    sawContent = true
                    cur = cur.advance()
                }
                if (sawContent && cur.type == MarkdownTokenTypes.DOUBLE_QUOTE) {
                    return cur.index
                }
                return null
            }

            if (next.type == MarkdownTokenTypes.TEXT) {
                val t = tokenText(tokens, next)
                // Accept if starts with at least one valid label char — generator trims trailing
                val label = t.takeWhile { c -> c.isLetterOrDigit() || c == '_' || c == '-' }
                if (label.isNotEmpty()) {
                    return next.index
                }
            }
            return null
        }

        // TEXT token with embedded reference marker
        if (it.type == MarkdownTokenTypes.TEXT) {
            val t = tokenText(tokens, it)
            if (t.isEmpty()) return null
            return when (t[0]) {
                '@' -> detectUserMention(it, t)
                '#' -> detectNumericRef(it, t, 1)
                '%' -> detectAlphanumRef(it, t, 1)
                '&' -> detectNumericRef(it, t, 1)
                '$' -> detectNumericRef(it, t, 1)
                else -> detectCrossProjectRef(it, t)
            }
        }

        return null
    }

    private fun detectUserMention(it: TokensCache.Iterator, t: String): Int? {
        if (t.length <= 1) return null
        var pos = 1
        while (pos < t.length && isValidUsernameChar(t[pos])) pos++
        // require at least 1 valid username char after '@'
        if (pos <= 1) return null
        // Accept even if token has trailing punctuation — generator will trim
        return it.index
    }

    private fun detectNumericRef(it: TokensCache.Iterator, t: String, startPos: Int): Int? {
        if (t.length <= startPos) return null
        var pos = startPos
        while (pos < t.length && t[pos].isDigit()) pos++
        if (pos == startPos) return null // no digits
        // Require a word boundary after the digits: `#123abc` is NOT a reference
        if (pos < t.length && t[pos].isLetter()) return null
        return it.index
    }

    /** Like detectNumericRef but also allows letters (for milestone names like %v1.0). */
    private fun detectAlphanumRef(it: TokensCache.Iterator, t: String, startPos: Int): Int? {
        if (t.length <= startPos) return null
        var pos = startPos
        while (pos < t.length && (t[pos].isLetterOrDigit() || t[pos] == '.' || t[pos] == '_' || t[pos] == '-')) pos++
        if (pos == startPos) return null
        return it.index
    }

    private fun detectCrossProjectRef(it: TokensCache.Iterator, t: String): Int? {
        val hashIdx = t.indexOf('#')
        if (hashIdx <= 0) return null
        val prefix = t.substring(0, hashIdx)
        if (!isNamespacePath(prefix)) return null
        val id = t.substring(hashIdx + 1)
        if (id.isEmpty() || !id.all { it2 -> it2.isDigit() }) return null
        return it.index
    }

    private fun tokenText(tokens: TokensCache, it: TokensCache.Iterator): String {
        val sb = StringBuilder(it.length)
        for (i in it.start until it.end) sb.append(tokens.getRawCharAt(i))
        return sb.toString()
    }

    private fun isNamespacePath(s: String): Boolean {
        if (s.isEmpty()) return false
        return s.all { c -> c.isLetterOrDigit() || c == '_' || c == '-' || c == '.' || c == '/' }
    }

    private fun isValidUsernameChar(c: Char) = c.isLetterOrDigit() || c == '_' || c == '-' || c == '.'

    /** True when the digit run starting at [startPos] ends at a word boundary (not a letter). */
    private fun hasDigitBoundary(t: String, startPos: Int): Boolean {
        var pos = startPos
        while (pos < t.length && t[pos].isDigit()) pos++
        return pos >= t.length || !t[pos].isLetter()
    }
}
