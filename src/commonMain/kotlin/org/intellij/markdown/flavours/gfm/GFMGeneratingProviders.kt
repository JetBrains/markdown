package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.*
import org.intellij.markdown.ast.impl.ListCompositeNode
import org.intellij.markdown.ast.impl.ListItemCompositeNode
import org.intellij.markdown.html.GeneratingProvider
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.html.InlineHolderGeneratingProvider
import org.intellij.markdown.html.SimpleTagProvider
import org.intellij.markdown.html.entities.EntityConverter
import org.intellij.markdown.lexer.Compat.assert

internal class CheckedListItemGeneratingProvider : SimpleTagProvider("li") {
    override fun openTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
    }

    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        assert(node is ListItemCompositeNode)

        val checkBoxElement = node.findChildOfType(GFMTokenTypes.CHECK_BOX)
        val inputHtml: CharSequence
        val listItemClass: CharSequence?
        if (checkBoxElement != null) {
            listItemClass = "class=\"task-list-item\""
            val checkedString = getIsCheckedString(checkBoxElement, text)
            inputHtml = "<input type=\"checkbox\" class=\"task-list-item-checkbox\"$checkedString disabled />"
        } else {
            listItemClass = null
            inputHtml = ""
        }

        visitor.consumeTagOpen(node, "li", listItemClass)

        val listNode = node.parent
        assert(listNode is ListCompositeNode)

        val isLoose = (listNode as ListCompositeNode).loose

        var flushedInput = false
        for (child in node.children) {
            if (child is LeafASTNode) {
                continue
            }
            if (!flushedInput) {
                if (child.type == MarkdownElementTypes.PARAGRAPH) {
                    SubParagraphGeneratingProvider(isLoose, inputHtml).processNode(visitor, text, child)
                } else {
                    visitor.consumeHtml(inputHtml)
                    child.accept(visitor)
                }
                flushedInput = true
            } else {
                child.accept(visitor)
            }
        }

        closeTag(visitor, text, node)
    }

    private fun getIsCheckedString(node: ASTNode?, text: String): String {
        var isChecked = node?.getTextInNode(text)?.let {
            it.length > 1 && it[1] != ' '
        } == true

        val checkedString = if (isChecked) " checked" else ""
        return checkedString
    }

    private class SubParagraphGeneratingProvider(val wrapInParagraph: Boolean, val inputHtml: String) :
        InlineHolderGeneratingProvider() {
        override fun openTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
            if (wrapInParagraph) {
                visitor.consumeTagOpen(node, "p")
            }
            visitor.consumeHtml(inputHtml)
        }

        override fun closeTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
            if (wrapInParagraph) {
                visitor.consumeTagClose("p")
            }
        }
    }
}

/**
 * Special version of [org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor.CodeSpanGeneratingProvider],
 * that will correctly escape table pipes if the code span is inside a table cell.
 */
open class TableAwareCodeSpanGeneratingProvider : GeneratingProvider {
    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        val isInsideTable = isInsideTable(node)
        val nodes = collectContentNodes(node)
        val output = nodes.joinToString(separator = "") { processChild(it, text, isInsideTable).replaceNewLines() }.trimForCodeSpan()
        visitor.consumeTagOpen(node, "code")
        visitor.consumeHtml(output)
        visitor.consumeTagClose("code")
    }

    /** From GFM spec: First, line endings are converted to spaces.*/
    protected fun CharSequence.replaceNewLines(): CharSequence =
        replace("\\r\\n?|\\n".toRegex(), " ")

    /**
     * From GFM spec:
     * If the resulting string both begins and ends with a space character,
     * but does not consist entirely of space characters,
     * a single space character is removed from the front and back.
     * This allows you to include code that begins or ends with backtick characters,
     * which must be separated by whitespace from the opening or closing backtick strings.
     */
    protected fun CharSequence.trimForCodeSpan(): CharSequence =
        if (isBlank()) this
        else removeSurrounding(" ", " ")

    protected fun isInsideTable(node: ASTNode): Boolean {
        return node.getParentOfType(GFMTokenTypes.CELL) != null
    }

    protected fun collectContentNodes(node: ASTNode): List<ASTNode> {
        check(node.children.size >= 2)
        return node.children.subList(1, node.children.size - 1)
    }

    protected fun processChild(node: ASTNode, text: String, isInsideTable: Boolean): CharSequence {
        if (!isInsideTable) {
            return HtmlGenerator.leafText(text, node, replaceEscapesAndEntities = false)
        }
        val nodeText = node.getTextInNode(text).toString()
        val escaped = nodeText.replace("\\|", "|")
        return EntityConverter.replaceEntities(escaped, processEntities = false, processEscapes = false)
    }
}

internal class MathGeneratingProvider(private val inline: Boolean = false): GeneratingProvider {
    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        val nodes = node.children.subList(1, node.children.size - 1)
        val output = nodes.joinToString(separator = "") { HtmlGenerator.leafText(text, it, false) }.trim()
        visitor.consumeTagOpen(node, "span", "class=\"math\"", "inline = \"$inline\"")
        visitor.consumeHtml(output)
        visitor.consumeTagClose("span")
    }
}

internal class TablesGeneratingProvider : GeneratingProvider {
    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        assert(node.type == GFMElementTypes.TABLE)

        val alignmentInfo = getAlignmentInfo(text, node)
        var rowsPopulated = 0

        visitor.consumeTagOpen(node, "table")
        for (child in node.children) {
            if (child.type == GFMElementTypes.HEADER) {
                visitor.consumeHtml("<thead>")
                populateRow(visitor, child, "th", alignmentInfo, -1)
                visitor.consumeHtml("</thead>")
            } else if (child.type == GFMElementTypes.ROW) {
                if (rowsPopulated == 0) {
                    visitor.consumeHtml("<tbody>")
                }
                rowsPopulated++
                populateRow(visitor, child, "td", alignmentInfo, rowsPopulated)
            }
        }
        if (rowsPopulated > 0) {
            visitor.consumeHtml("</tbody>")
        }
        visitor.consumeTagClose("table")
    }

    private fun populateRow(
        visitor: HtmlGenerator.HtmlGeneratingVisitor,
        node: ASTNode,
        cellName: String,
        alignmentInfo: List<Alignment>,
        rowNumber: Int,
    ) {
        val parityAttribute = if (rowNumber > 0 && rowNumber % 2 == 0) "class=\"intellij-row-even\"" else null

        visitor.consumeTagOpen(node, "tr", parityAttribute)
        for (child in node.children.filter { it.type == GFMTokenTypes.CELL }.withIndex()) {
            if (child.index >= alignmentInfo.size) {
                throw IllegalStateException("Too many cells in a row! Should check parser.")
            }

            val alignment = alignmentInfo[child.index]
            val alignmentAttribute = if (alignment.isDefault) null else "align=\"${alignment.htmlName}\""

            visitor.consumeTagOpen(child.value, cellName, alignmentAttribute)
            visitor.visitNode(child.value)
            visitor.consumeTagClose(cellName)
        }

        for (i in node.children.count { it.type == GFMTokenTypes.CELL }..alignmentInfo.size - 1) {
            visitor.consumeHtml("<td></td>")
        }
        visitor.consumeTagClose("tr")
    }

    private fun getAlignmentInfo(text: String, node: ASTNode): List<Alignment> {
        val separatorRow = node.findChildOfType(GFMTokenTypes.TABLE_SEPARATOR)
            ?: throw IllegalStateException("Could not find table separator")

        val result = ArrayList<Alignment>()

        val cells = SPLIT_REGEX.split(separatorRow.getTextInNode(text))
        for (i in cells.indices) {
            val cell = cells[i]
            if (!cell.isBlank() || i in 1..cells.lastIndex - 1) {
                val trimmed = cell.trim()
                val starts = trimmed.startsWith(':')
                val ends = trimmed.endsWith(':')
                result.add(
                    if (starts && ends) {
                        Alignment.CENTER
                    } else if (starts) {
                        Alignment.LEFT
                    } else if (ends) {
                        Alignment.RIGHT
                    } else {
                        DEFAULT_ALIGNMENT
                    }
                )
            }
        }
        return result
    }

    enum class Alignment(val htmlName: String, val isDefault: Boolean) {
        LEFT("left", true),
        CENTER("center", false),
        RIGHT("right", false)
    }

    companion object {
        val DEFAULT_ALIGNMENT = Alignment.values().find { it.isDefault }
            ?: throw IllegalStateException("Must be default alignment")

        val SPLIT_REGEX = Regex("\\|")
    }
}