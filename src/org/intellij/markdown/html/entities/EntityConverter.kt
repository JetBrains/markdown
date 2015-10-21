package org.intellij.markdown.html.entities

import kotlin.text.Regex

public object EntityConverter {
    private val escapeAllowedString = "\\!\"#\\$%&'\\(\\)\\*\\+,\\-.\\/:;<=>\\?@\\[\\\\\\]\\^_`{\\|}\\~"
    private val replacements: Map<Char, String> = mapOf(
            '"' to "&quot;",
            '&' to "&amp;",
            '<' to "&lt;",
            '>' to "&gt;"
    )

    private val REGEX = Regex("&(?:([a-zA-Z0-9]+)|#([0-9]{1,8})|#[xX]([a-fA-F0-9]{1,8}));|([\"&<>])")
    private val REGEX_ESCAPES = Regex("${REGEX.pattern}|\\\\([${escapeAllowedString}])")

    public fun replaceEntities(text: CharSequence, processEntities: Boolean, processEscapes: Boolean): String {
        return (if (processEscapes)
            REGEX_ESCAPES
        else
            REGEX).replace(text, { match ->
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
                        Integer.parseInt(g[2]!!.value)
                    } else if (g[3] != null) {
                        Integer.parseInt(g[3]!!.value, 16)
                    } else {
                        null
                    }

                    code?.toChar()?.let {
                        replacements[it] ?: it.toString()
                    } ?: "&amp;${match.value.substring(1)}"

                }
        })
    }
}