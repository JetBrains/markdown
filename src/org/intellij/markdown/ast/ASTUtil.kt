package org.intellij.markdown.ast

import org.intellij.markdown.IElementType

public fun ASTNode.findChildOfType(type: IElementType): ASTNode? {
    return children.firstOrNull { it.type == type }
}

public fun ASTNode.getTextInNode(allFileText: CharSequence): CharSequence {
    return allFileText.subSequence(startOffset, endOffset)
}