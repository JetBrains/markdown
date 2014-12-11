package org.intellij.markdown

public class MarkdownElementType(name: String) : IElementType(name) {

    override fun toString(): String {
        return "Markdown:" + super.toString()
    }
}
