package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.constraints.MarkdownConstraints

public class GFMConstraints(indents: IntArray,
                            types: CharArray,
                            isExplicit: BooleanArray,
                            charsEaten: Int,
                            private val isCheckbox: Boolean) : MarkdownConstraints(indents, types, isExplicit, charsEaten) {
    override val base: MarkdownConstraints
        get() = BASE

    override fun createNewConstraints(indents: IntArray, types: CharArray, isExplicit: BooleanArray, charsEaten: Int): MarkdownConstraints {
        val initialType = types[types.size - 1]
        val originalType = toOriginalType(initialType)
        types[types.size - 1] = originalType
        return GFMConstraints(indents, types, isExplicit, charsEaten, initialType != originalType)
    }

    public fun hasCheckbox(): Boolean {
        return isCheckbox
    }

    override fun fetchListMarker(pos: LookaheadText.Position): MarkdownConstraints.ListMarkerInfo? {
        val baseMarkerInfo = super.fetchListMarker(pos)
                ?: return null

        val line = pos.currentLine
        var offset = pos.offsetInCurrentLine + baseMarkerInfo.markerText.length

        while (offset < line.length && (line[offset] == ' ' || line[offset] == '\t')) {
            offset++
        }

        if (offset + 3 <= line.length
                && line[offset] == '['
                && line[offset + 2] == ']'
                && (line[offset + 1] == 'x' || line[offset + 1] == 'X' || line[offset + 1] == ' ')) {
            return MarkdownConstraints.ListMarkerInfo(line.subSequence(pos.offsetInCurrentLine, offset + 3),
                    toCheckboxType(baseMarkerInfo.markerType),
                    baseMarkerInfo.markerText.length)
        } else {
            return baseMarkerInfo
        }
    }

    companion object {
        public val BASE: GFMConstraints = GFMConstraints(IntArray(0), CharArray(0), BooleanArray(0), 0, false)

        private fun toCheckboxType(originalType: Char): Char {
            return (originalType.toInt() + 100).toChar()
        }

        private fun toOriginalType(checkboxType: Char): Char {
            if (checkboxType.toInt() < 128) {
                return checkboxType
            }
            return (checkboxType.toInt() - 100).toChar()
        }
    }
}