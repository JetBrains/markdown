package org.intellij.markdown.lexer

import org.intellij.markdown.IElementType

data class TokenInfo(
    val type: IElementType?,
    val tokenStart: Int,
    val tokenEnd: Int,
    val rawIndex: Int,
    val normIndex: Int
)