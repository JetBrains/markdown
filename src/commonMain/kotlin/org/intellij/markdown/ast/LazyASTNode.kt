package org.intellij.markdown.ast

import org.intellij.markdown.ExperimentalApi
import org.intellij.markdown.IElementType

@ExperimentalApi
class LazyASTNode(
    type: IElementType,
    startOffset: Int,
    endOffset: Int,
    private val childrenParser: () -> List<ASTNode>
) : ASTNodeImpl(type, startOffset, endOffset) {
    override val children: List<ASTNode> by lazy {
        childrenParser().also { parsedChildren ->
            for (child in parsedChildren) {
                if (child is ASTNodeImpl) {
                    child.parent = this
                }
            }
        }
    }
}
