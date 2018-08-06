package org.intellij.markdown.ast

import org.intellij.markdown.IElementType

abstract class ASTNodeImpl(override val type: IElementType, override val startOffset: Int, override val endOffset: Int) : ASTNode {
    final override var parent: ASTNode? = null
        internal set
}
