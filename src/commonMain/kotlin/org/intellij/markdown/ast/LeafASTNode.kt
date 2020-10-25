package org.intellij.markdown.ast

import org.intellij.markdown.IElementType

open class LeafASTNode(type: IElementType, startOffset: Int, endOffset: Int) : ASTNodeImpl(type, startOffset, endOffset) {
    override val children: List<ASTNode>
        get() = EMPTY_CHILDREN

    companion object {
        private val EMPTY_CHILDREN = ArrayList<ASTNode>(0)
    }
}
