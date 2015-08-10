package org.intellij.markdown

public open class MarkdownElementType(name: String, public val isToken: Boolean = false) : IElementType(name) {

    override fun toString(): String {
        return "Markdown:" + super.toString()
    }
}
