package org.intellij.markdown.ast

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.impl.ListCompositeNode
import org.intellij.markdown.ast.impl.ListItemCompositeNode
import org.intellij.markdown.parser.CancellationToken

open class ASTNodeBuilder(
    protected val text: CharSequence,
    protected val cancellationToken: CancellationToken,
    private val baseOffset: Int
) {
    /**
     * For compatibility only.
     */
    @Deprecated("Use constructor with CancellationToken")
    constructor(text: CharSequence): this(text, CancellationToken.NonCancellable)

    /**
     * For ABI compatibility.
     */
    constructor(text: CharSequence, cancellationToken: CancellationToken): this(text, cancellationToken, 0)

    open fun createLeafNodes(type: IElementType, startOffset: Int, endOffset: Int): List<ASTNode> {
        if (type == MarkdownTokenTypes.WHITE_SPACE) {
            val result = ArrayList<ASTNode>()
            var lastEol = startOffset
            while (lastEol < endOffset) {
                cancellationToken.checkCancelled()

                val nextEol = indexOfSubSeq(text, lastEol, endOffset, '\n', '\r')
                if (nextEol == -1) {
                    break
                }

                if (nextEol > lastEol) {
                    result.add(LeafASTNode(MarkdownTokenTypes.WHITE_SPACE, lastEol + baseOffset, nextEol + baseOffset))
                }
                lastEol = nextEol + if (text[nextEol] == '\r' && nextEol + 1 < endOffset && text[nextEol + 1] == '\n') 2 else 1
                result.add(LeafASTNode(MarkdownTokenTypes.EOL, nextEol + baseOffset, lastEol + baseOffset))
            }
            if (endOffset > lastEol) {
                result.add(LeafASTNode(MarkdownTokenTypes.WHITE_SPACE, lastEol + baseOffset, endOffset + baseOffset))
            }

            return result
        }
        return listOf(LeafASTNode(type, startOffset + baseOffset, endOffset + baseOffset))
    }

    open fun createCompositeNode(type: IElementType, children: List<ASTNode>): CompositeASTNode {
        cancellationToken.checkCancelled()
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
        fun indexOfSubSeq(s: CharSequence, from: Int, to: Int, c: Char): Int =
            indexOfSubSeq(s, from, to, c, c)

        private fun indexOfSubSeq(s: CharSequence, from: Int, to: Int, c: Char, alternative: Char): Int {
            for (i in from until to) {
                val char = s[i]
                if (char == c || char == alternative) {
                    return i
                }
            }
            return -1
        }
    }
}
