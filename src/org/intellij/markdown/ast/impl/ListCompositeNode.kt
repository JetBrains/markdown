package org.intellij.markdown.ast.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.CompositeASTNode

class ListCompositeNode(type: IElementType, startOffset: Int, endOffset: Int, children: List<ASTNode>)
    : CompositeASTNode(type, startOffset, endOffset, children) {
    val loose: Boolean by lazy(LazyThreadSafetyMode.NONE) { isLoose() }

    private fun isLoose(): Boolean {
        if (hasLooseContent(this)) {
            return true
        }

        for (child in children) {
            if (child.type == MarkdownElementTypes.LIST_ITEM
                    && hasLooseContent(child)) {
                return true
            }
        }
        return false
    }

    companion object {
        private fun hasLooseContent(node: ASTNode): Boolean {
            var newlines = 0
            var seenNonWhitespace = false
            for (child in node.children) {
                when (child.type) {
                    MarkdownTokenTypes.EOL -> {
                        ++newlines
                    }
                    MarkdownTokenTypes.LIST_BULLET,
                    MarkdownTokenTypes.LIST_NUMBER,
                    MarkdownTokenTypes.WHITE_SPACE -> {
                        // do nothing;
                    }
                    else -> {
                        if (seenNonWhitespace && newlines > 1) {
                            return true
                        }
                        seenNonWhitespace = true
                        newlines = 0
                    }
                }
            }
            return false
        }
    }
}