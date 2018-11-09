package org.intellij.markdown.html

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.acceptChildren
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.ast.visitors.RecursiveVisitor
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.html.entities.EntityConverter
import org.intellij.markdown.parser.LinkMap

typealias AttributesCustomizer = (node: ASTNode, tagName: CharSequence, attributes: Iterable<CharSequence?>) -> Iterable<CharSequence?>
val DUMMY_ATTRIBUTES_CUSTOMIZER: AttributesCustomizer = { _, _, attributes -> attributes }

class HtmlGenerator(private val markdownText: String,
                           private val root: ASTNode,
                           private val providers: Map<IElementType, GeneratingProvider>,
                           private val includeSrcPositions: Boolean = false) {

    constructor(markdownText: String,
                root: ASTNode,
                flavour: MarkdownFlavourDescriptor,
                includeSrcPositions: Boolean = false)
    : this(markdownText,
            root,
            flavour.createHtmlGeneratingProviders(LinkMap.buildLinkMap(root, markdownText), null),
            includeSrcPositions)  
    
    private val htmlString: StringBuilder = StringBuilder()

    @Deprecated("To be removed, pass custom visitor instead", 
            ReplaceWith("generateHtml(HtmlGeneratingVisitor)"))
    fun generateHtml(customizer: AttributesCustomizer = DUMMY_ATTRIBUTES_CUSTOMIZER): String {
        HtmlGeneratingVisitor(DefaultTagRenderer(customizer, includeSrcPositions)).visitNode(root)
        return htmlString.toString()
    }

    @JvmOverloads
    fun generateHtml(tagRenderer: TagRenderer =
                             DefaultTagRenderer(DUMMY_ATTRIBUTES_CUSTOMIZER, includeSrcPositions)): String {
        HtmlGeneratingVisitor(tagRenderer).visitNode(root)
        return htmlString.toString()
    }

    inner class HtmlGeneratingVisitor(private val tagRenderer: TagRenderer) : RecursiveVisitor() {
        override fun visitNode(node: ASTNode) {
            providers[node.type]?.processNode(this, markdownText, node)
                    ?: node.acceptChildren(this)
        }

        fun visitLeaf(node: ASTNode) {
            providers[node.type]?.processNode(this, markdownText, node)
                    ?: consumeHtml(leafText(markdownText, node))
        }

        fun consumeTagOpen(node: ASTNode,
                                tagName: CharSequence,
                                vararg attributes: CharSequence?,
                                autoClose: Boolean = false) {
            htmlString.append(tagRenderer.openTag(node, tagName, *attributes, autoClose = autoClose))
        }

        fun consumeTagClose(tagName: CharSequence) {
            htmlString.append(tagRenderer.closeTag(tagName))
        }

        fun consumeHtml(html: CharSequence) {
            htmlString.append(tagRenderer.printHtml(html))
        }
    }
    
    open class DefaultTagRenderer(protected val customizer: AttributesCustomizer, 
                                  protected val includeSrcPositions: Boolean) : TagRenderer {
        override fun openTag(node: ASTNode, tagName: CharSequence, vararg attributes: CharSequence?, autoClose: Boolean): CharSequence {
            return buildString {
                append("<$tagName")
                for (attribute in customizer.invoke(node, tagName, attributes.asIterable())) {
                    if (attribute != null) {
                        append(" $attribute")
                    }
                }
                if (includeSrcPositions) {
                    append(" ${getSrcPosAttribute(node)}")
                }

                if (autoClose) {
                    append(" />")
                } else {
                    append(">")
                }
            }
        }

        override fun closeTag(tagName: CharSequence): CharSequence = "</$tagName>"

        override fun printHtml(html: CharSequence): CharSequence = html
    }
    
    interface TagRenderer {
        fun openTag(node: ASTNode,
                    tagName: CharSequence,
                    vararg attributes: CharSequence?,
                    autoClose: Boolean = false): CharSequence
        
        fun closeTag(tagName: CharSequence): CharSequence
        
        fun printHtml(html: CharSequence): CharSequence
    }

    companion object {
        val SRC_ATTRIBUTE_NAME = "md-src-pos"

        fun leafText(text: String, node: ASTNode, replaceEscapesAndEntities: Boolean = true): CharSequence {
            if (node.type == MarkdownTokenTypes.BLOCK_QUOTE) {
                return ""
            }
            return EntityConverter.replaceEntities(node.getTextInNode(text), replaceEscapesAndEntities, replaceEscapesAndEntities)
        }

        fun getSrcPosAttribute(node: ASTNode): CharSequence {
            return "$SRC_ATTRIBUTE_NAME=\"${node.startOffset}..${node.endOffset}\""
        }

        fun trimIndents(text: CharSequence, indent: Int): CharSequence {
            if (indent == 0) {
                return text
            }

            val buffer = StringBuilder()

            var lastFlushed = 0
            var offset = 0
            while (offset < text.length) {
                if (offset == 0 || text[offset - 1] == '\n') {
                    buffer.append(text.subSequence(lastFlushed, offset))
                    var indentEaten = 0

                    eatIndentLoop@
                    while (indentEaten < indent && offset < text.length) {
                        when (text[offset]) {
                            ' ' -> indentEaten++
                            '\t' -> indentEaten += 4 - indentEaten % 4
                            else -> break@eatIndentLoop
                        }
                        offset++
                    }

                    if (indentEaten > indent) {
                        buffer.append(" ".repeat(indentEaten - indent))
                    }
                    lastFlushed = offset
                }

                offset++
            }

            buffer.append(text.subSequence(lastFlushed, text.length))
            return buffer
        }

    }
}