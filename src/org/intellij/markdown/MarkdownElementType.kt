package org.intellij.markdown

public class MarkdownElementType(name: String, public val isToken: Boolean = false) : IElementType(name) {

    override fun toString(): String {
        return "Markdown:" + super.toString()
    }
}
