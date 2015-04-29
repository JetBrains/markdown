package org.intellij.markdown

public open class IElementType(public val name: String) {
    override fun toString(): String {
        return name
    }
}
