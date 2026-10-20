package org.intellij.markdown.html.entities

import org.intellij.markdown.lexer.Compat
import kotlin.text.Regex

object EntityConverter {
    private const val escapeAllowedString = """!"#\$%&'\(\)\*\+,\-.\/:;<=>\?@\[\\\]\^_`{\|}~"""
    private val replacements: Map<Char, String> = mapOf(
        '"' to "&quot;",
        '&' to "&amp;",
        '<' to "&lt;",
        '>' to "&gt;"
    )

    private val REGEX = Regex("""&(?:([a-zA-Z0-9]+)|#([0-9]{1,7})|#[xX]([a-fA-F0-9]{1,6}));|(["&<>])""")
    private val REGEX_ESCAPES = Regex("${REGEX.pattern}|\\\\([${escapeAllowedString}])")

    fun replaceEntities(text: CharSequence, processEntities: Boolean, processEscapes: Boolean): String {
        return (if (processEscapes)
            REGEX_ESCAPES
        else
            REGEX).replace(text) { match ->
            val g = match.groups
            if (g.size > 5 && g[5] != null) {
                val char = g[5]!!.value[0]
                replacements[char] ?: char.toString()
            } else
                if (g[4] != null) {
                    replacements[g[4]!!.value[0]] ?: match.value
                } else {
                    val code = if (!processEntities) {
                        null
                    } else if (g[1] != null) {
                        Entities.map[match.value]
                    } else if (g[2] != null) {
                        g[2]!!.value.toInt()
                    } else if (g[3] != null) {
                        g[3]!!.value.toInt(16)
                    } else {
                        null
                    }

                    if (code != null) {
                        val string = Compat.codePointToString(if (isValidCodePoint(code)) code else REPLACEMENT_CHARACTER)
                        replacements[string[0]] ?: string
                    } else {
                        "&amp;${match.value.substring(1)}"
                    }

                }
        }
    }

    private const val REPLACEMENT_CHARACTER = 0xFFFD

    private fun isValidCodePoint(code: Int): Boolean =
        code in 1..0x10FFFF && code !in 0xD800..0xDFFF
}
