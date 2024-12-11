package org.intellij.markdown

open class MarkdownElementType(name: String, val isToken: Boolean = false) : IElementType(name) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MarkdownElementType) return false
        if (!super.equals(other)) return false
        return isToken == other.isToken
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + isToken.hashCode()
        return result
    }

    override fun toString(): String {
        return "Markdown:" + super.toString()
    }
}
