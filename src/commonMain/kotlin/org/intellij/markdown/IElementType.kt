package org.intellij.markdown

open class IElementType(val name: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IElementType) return false
        return this.name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return name
    }
}
