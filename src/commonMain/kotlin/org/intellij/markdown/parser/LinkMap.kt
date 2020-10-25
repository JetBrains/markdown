package org.intellij.markdown.parser

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.accept
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.ast.visitors.RecursiveVisitor
import org.intellij.markdown.html.entities.EntityConverter
import org.intellij.markdown.html.urlEncode
import org.intellij.markdown.lexer.Compat.codePointToString
import org.intellij.markdown.lexer.Compat.forEachCodePoint
import kotlin.text.Regex

data class LinkMap private constructor(private val map: Map<CharSequence, LinkMap.LinkInfo>) {
    fun getLinkInfo(label: CharSequence): LinkInfo? {
        return map.get(normalizeLabel(label))
    }

    companion object Builder {
        fun buildLinkMap(root: ASTNode, text: CharSequence): LinkMap {
            val map = HashMap<CharSequence, LinkInfo>()

            root.accept(object : RecursiveVisitor() {
                override fun visitNode(node: ASTNode) {
                    if (node.type == MarkdownElementTypes.LINK_DEFINITION) {
                        val linkLabel = normalizeLabel(
                                node.children.first({ it.type == MarkdownElementTypes.LINK_LABEL }).getTextInNode(text)
                        )
                        if (!map.containsKey(linkLabel)) {
                            map.put(linkLabel, LinkInfo.create(node, text))
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
            return SPACES_REGEX.replace(label, " ").toLowerCase()
        }

        fun normalizeDestination(s: CharSequence, processEscapes: Boolean): CharSequence {
            val dest = EntityConverter.replaceEntities(clearBounding(s, "<>"), true, processEscapes)
            val sb = StringBuilder()
            dest.forEachCodePoint { code ->
                val c = code.toChar()
                if (code == 32) {
                    sb.append("%20")
                } else if (code < 32 || code >= 128 || "\".<>\\^_`{|}".contains(c)) {
                    sb.append(urlEncode(codePointToString(code)))
                } else {
                    sb.append(c)
                }
            }
            return sb.toString()
        }

        fun normalizeTitle(s: CharSequence): CharSequence = EntityConverter.replaceEntities(clearBounding(s, "\"\"", "''", "()"), true, true)

        private fun clearBounding(s: CharSequence, vararg boundQuotes: String): CharSequence {
            if (s.length == 0) {
                return s
            }
            for (quotePair in boundQuotes) {
                if (s[0] == quotePair[0] && s[s.length - 1] == quotePair[1]) {
                    return s.subSequence(1, s.length - 1)
                }
            }
            return s
        }

        val SPACES_REGEX = Regex("\\s+")

    }

    data class LinkInfo private constructor(val node: ASTNode, val destination: CharSequence, val title: CharSequence?) {
        companion object {
            internal fun create(node: ASTNode, fileText: CharSequence): LinkInfo {
                val destination: CharSequence = normalizeDestination(
                        node.children.first({ it.type == MarkdownElementTypes.LINK_DESTINATION }).getTextInNode(fileText),
                        true)
                val title: CharSequence? = node.children.firstOrNull({ it.type == MarkdownElementTypes.LINK_TITLE })
                        ?.getTextInNode(fileText)?.let { normalizeTitle(it) }
                return LinkInfo(node, destination, title)
            }
        }
    }
}

