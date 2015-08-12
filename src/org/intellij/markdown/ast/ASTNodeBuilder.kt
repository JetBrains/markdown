package org.intellij.markdown.ast

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.impl.ListCompositeNode
import org.intellij.markdown.ast.impl.ListItemCompositeNode
import java.util.ArrayList

public open class ASTNodeBuilder(protected val text: CharSequence) {
    public open fun createLeafNodes(type: IElementType, startOffset: Int, endOffset: Int): List<ASTNode> {
        if (type == MarkdownTokenTypes.WHITE_SPACE) {
            val result = ArrayList<ASTNode>()
            var lastEol = startOffset
            while (lastEol < endOffset) {
                val nextEol = indexOfSubSeq(text, lastEol, endOffset, '\n')
                if (nextEol == -1) {
                    break
                }

                if (nextEol > lastEol) {
                    result.add(LeafASTNode(MarkdownTokenTypes.WHITE_SPACE, lastEol, nextEol))
                }
                result.add(LeafASTNode(MarkdownTokenTypes.EOL, nextEol, nextEol + 1))
                lastEol = nextEol + 1
            }
            if (endOffset > lastEol) {
                result.add(LeafASTNode(MarkdownTokenTypes.WHITE_SPACE, lastEol, endOffset))
            }

            return result
        }
        return listOf(LeafASTNode(type, startOffset, endOffset))
    }

    public open fun createCompositeNode(type: IElementType, children: List<ASTNode>): CompositeASTNode {
        when (type) {
            MarkdownElementTypes.UNORDERED_LIST,
            MarkdownElementTypes.ORDERED_LIST -> {
                return ListCompositeNode(type, children)
            }
            MarkdownElementTypes.LIST_ITEM -> {
                return ListItemCompositeNode(children)
            }
            else -> {
                return CompositeASTNode(type, children)
            }
        }
    }

    companion object {
        fun indexOfSubSeq(s: CharSequence, from: Int, to: Int, c: Char): Int {
            for (i in from..to - 1) {
                if (s[i] == c) {
                    return i
                }
            }
            return -1
        }
    }
}