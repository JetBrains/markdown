package org.intellij.markdown.parser

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.ast.visitors.RecursiveVisitor
import org.intellij.markdown.html.entities.EntityConverter
import java.net.URLEncoder
import java.util.HashMap
import java.util.Locale
import kotlin.text.Regex

public data class LinkMap private constructor(private val map: Map<CharSequence, LinkMap.LinkInfo>) {
    public fun getLinkInfo(label: CharSequence): LinkInfo? {
        return map.get(normalizeLabel(label))
    }

    companion object Builder {
        public fun buildLinkMap(root: ASTNode, text: CharSequence): LinkMap {
            val map = HashMap<CharSequence, LinkInfo>()

            root.accept(object : RecursiveVisitor() {
                override fun visitNode(node: ASTNode) {
                    if (node.type == MarkdownElementTypes.LINK_DEFINITION) {
                        val linkLabel = normalizeLabel(
                                node.children.first({ it.type == MarkdownElementTypes.LINK_LABEL }).getTextInNode(text)
                        )
                        if (!map.containsKey(linkLabel)) {
                            map.put(linkLabel, LinkInfo(node, text))
                        }
                    }
                    else {
                        super.visitNode(node)
                    }
                }
            })

            return LinkMap(map)
        }

        private fun normalizeLabel(label: CharSequence): CharSequence {
            return SPACES_REGEX.replace(label, " ").toLowerCase(Locale.US)
        }

        public fun normalizeDestination(s: CharSequence): CharSequence {
            val dest = EntityConverter.replaceEntities(clearBounding(s, "<>"), true, true)
            val sb = StringBuilder()
            for (c in dest) {
                val code = c.toInt()
                if (code == 32) {
                    sb.append("%20")
                } else if (code < 32 || code >= 128 || "\".<>\\^_`{|}~".contains(c)) {
                    sb.append(URLEncoder.encode("${c}", "UTF-8"))
                } else {
                    sb.append(c)
                }
            }
            return sb.toString()
        }

        public fun normalizeTitle(s: CharSequence): CharSequence = EntityConverter.replaceEntities(clearBounding(s, "\"\"", "''", "()"), true, true)

        private fun clearBounding(s: CharSequence, vararg boundQuotes: String): CharSequence {
            if (s.length() == 0) {
                return s
            }
            for (quotePair in boundQuotes) {
                if (s[0] == quotePair[0] && s[s.length() - 1] == quotePair[1]) {
                    return s.subSequence(1, s.length() - 1)
                }
            }
            return s
        }

        val SPACES_REGEX = Regex("\\s+")

    }

    public data class LinkInfo internal constructor(val node: ASTNode, fileText: CharSequence) {
        val destination: CharSequence = normalizeDestination(
                node.children.first({ it.type == MarkdownElementTypes.LINK_DESTINATION }).getTextInNode(fileText))
        val title: CharSequence? = node.children.firstOrNull({ it.type == MarkdownElementTypes.LINK_TITLE })
                ?.getTextInNode(fileText)?.let { normalizeTitle(it) }
    }
}

