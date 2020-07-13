package org.intellij.markdown.ast

import org.intellij.markdown.IElementType

abstract class ASTNodeImpl(override val type: IElementType, final override val startOffset: Int, final override val endOffset: Int) : ASTNode {
    init {
        assert(startOffset <= endOffset) {
            "Start offset must be less or equal to end offset, got: [$startOffset, $endOffset)"
        }
    }
    final override var parent: ASTNode? = null
        internal set

    override fun toString(): String {
        return "${javaClass.simpleName}<$type>"
    }
}
