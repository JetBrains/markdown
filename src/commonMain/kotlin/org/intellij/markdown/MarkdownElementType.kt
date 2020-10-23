package org.intellij.markdown

open class MarkdownElementType(name: String, val isToken: Boolean = false) : IElementType(name) {

    override fun toString(): String {
        return "Markdown:" + super.toString()
    }
}
