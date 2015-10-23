package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.ast.impl.ListItemCompositeNode
import org.intellij.markdown.html.GeneratingProvider
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.html.InlineHolderGeneratingProvider
import org.intellij.markdown.html.SimpleTagProvider
import java.util.*
import kotlin.text.Regex

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

        val isLoose = (node as ListItemCompositeNode).parent!!.loose

        var flushedInput = false
        for (child in node.children) {
            if (child is LeafASTNode) {
                continue
            }
            if (!flushedInput) {
                if (child.type == MarkdownElementTypes.PARAGRAPH) {
                    SubParagraphGeneratingProvider(isLoose, inputHtml).
                            processNode(visitor, text, child)
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

    private class SubParagraphGeneratingProvider(val wrapInParagraph: Boolean, val inputHtml: String)
    : InlineHolderGeneratingProvider() {
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

    private fun populateRow(visitor: HtmlGenerator.HtmlGeneratingVisitor,
                            node: ASTNode,
                            cellName: String,
                            alignmentInfo: List<Alignment>,
                            rowNumber: Int) {
        val parityAttribute = if (rowNumber > 0 && rowNumber % 2 == 0) "class=\"intellij-row-even\"" else null

        visitor.consumeTagOpen(node, "tr", parityAttribute)
        for (child in node.children.filter { it.type == GFMTokenTypes.CELL }.withIndex()) {
            val alignment = alignmentInfo[child.index]
            val alignmentAttribute = if (alignment.isDefault) null else "align=\"${alignment.htmlName}\""

            visitor.consumeTagOpen(child.value, cellName, alignmentAttribute)
            visitor.visitNode(child.value)
            visitor.consumeTagClose(cellName)
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
                result.add(if (starts && ends) {
                    Alignment.CENTER
                } else if (starts) {
                    Alignment.LEFT
                } else if (ends) {
                    Alignment.RIGHT
                } else {
                    DEFAULT_ALIGNMENT
                })
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
        val DEFAULT_ALIGNMENT = Alignment.values.find { it.isDefault }
                ?: throw IllegalStateException("Must me default alignment")

        val SPLIT_REGEX = Regex("\\|")
    }
}