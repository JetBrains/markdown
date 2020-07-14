package org.intellij.markdown.ast

import org.intellij.markdown.IElementType

open class CompositeASTNode(type: IElementType, startOffset: Int, endOffset: Int, final override val children: List<ASTNode>)
    : ASTNodeImpl(type, startOffset, endOffset) {
    init {
        for (child in children) {
            if (child is ASTNodeImpl) {
                @Suppress("LeakingThis")
                child.parent = this
            }
        }
    }


}
