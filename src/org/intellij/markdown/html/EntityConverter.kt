package org.intellij.markdown.html

import java.util.HashMap

public class EntityConverter {
    private val escapeAllowedString = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
    private val replacements: Map<String, String> = initRepalcements()

    private fun initRepalcements(): Map<String, String> {
        val explicitReplacements = hashMapOf(
                "\"" to "&quot;",
                "&" to "&amp;",
                "<" to "&lt;",
                ">" to "&gt;"
        )

        val result = HashMap<String, String>()
        for (char in escapeAllowedString) {
            result.put("\\${char}", explicitReplacements.get("" + char) ?: "" + char)
        }
        result.putAll(explicitReplacements)
        return result
    }

    public fun replaceEntities(text: CharSequence, processEscapes: Boolean): String {
        val result = StringBuilder()
        var i = 0
        while (i < text.length()) {
            val c = "" + text[i]
            if (replacements.containsKey(c)) {
                result.append(replacements.get(c));
                i++
                continue
            }
            if (processEscapes && c[0] == '\\' && i + 1 < text.length()) {
                val replacement = replacements.get("\\${text[i + 1]}")
                if (replacement != null) {
                    result.append(replacement)
                    i += 2
                    continue
                }
            }

            result.append(c);
            i++
        }
        return result.toString()
    }
}