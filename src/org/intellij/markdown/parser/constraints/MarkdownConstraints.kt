package org.intellij.markdown.parser.constraints

import org.intellij.markdown.parser.LookaheadText

public class MarkdownConstraints private constructor(private var indents: IntArray,
                                                     private var types: CharArray,
                                                     private var isExplicit: BooleanArray) {

    public fun getIndent(): Int {
        if (indents.size() == 0) {
            return 0
        }

        return indents.last()
    }

    public fun getLastType(): Char? {
        return types.lastOrNull()
    }

    public fun getLastExplicit(): Boolean? {
        return isExplicit.lastOrNull()
    }

    public fun upstreamWith(other: MarkdownConstraints): Boolean {
        return other.startsWith(this) && !containsListMarkers()
    }

    public fun extendsPrev(other: MarkdownConstraints): Boolean {
        return startsWith(other) && !containsListMarkers(other.types.size())
    }

    public fun extendsList(other: MarkdownConstraints): Boolean {
        if (other.types.size() == 0) {
            throw IllegalArgumentException("List constraints should contain at least one item")
        }
        return startsWith(other) && !containsListMarkers(other.types.size() - 1)
    }

    private fun startsWith(other: MarkdownConstraints): Boolean {
        val n = indents.size()
        val m = other.indents.size()

        if (n < m) {
            return false
        }
        for (i in 0..m - 1) {
            if (types[i] != other.types[i]) {
                return false
            }
        }
        return true
    }

    private fun containsListMarkers(): Boolean {
        return containsListMarkers(types.size())
    }

    private fun containsListMarkers(upToIndex: Int): Boolean {
        for (i in 0..upToIndex - 1) {
            if (types[i] != BQ_CHAR && isExplicit[i]) {
                return true
            }
        }
        return false
    }

    public fun addModifierIfNeeded(pos: LookaheadText.Position?): MarkdownConstraints? {
        if (pos == null || pos.char == '\n')
            return null
        return tryAddListItem(pos) ?: tryAddBlockQuote(pos)
    }

    private fun tryAddListItem(pos: LookaheadText.Position): MarkdownConstraints? {
        val line = pos.currentLine

        var offset = pos.offsetInCurrentLine
        var spacesBefore = 0
        while (offset < line.length() && line[offset] == ' ' && spacesBefore < 3) {
            spacesBefore++
            offset++
        }
        if (offset == line.length())
            return null

        val marker = fetchListMarker(pos.nextPosition(spacesBefore)!!)
                ?: return null

        offset += marker.length()
        if (offset >= line.length())
            return null

        var spacesAfter = 0
        while (offset < line.length() && line[offset] == ' ') {
            spacesAfter++
            offset++
        }

        // By the classification http://spec.commonmark.org/0.20/#list-items
        // 1. Basic case
        if (spacesAfter > 0 && spacesAfter < 5 && offset < line.length()) {
            return MarkdownConstraints(this, spacesBefore + marker.length() + spacesAfter, getMarkerType(marker), true)
        }
        if (spacesAfter >= 5 && offset < line.length() // 2. Starts with an indented code
                || offset == line.length()) {          // 3. Starts with an empty string
            return MarkdownConstraints(this, spacesBefore + marker.length() + 1, getMarkerType(marker), true)
        }

        return null
    }

    private fun tryAddBlockQuote(pos: LookaheadText.Position): MarkdownConstraints? {
        val line = pos.currentLine

        var offset = pos.offsetInCurrentLine
        var spacesBefore = 0
        while (offset < line.length() && line[offset] == ' ' && spacesBefore < 3) {
            spacesBefore++
            offset++
        }
        if (offset == line.length() || line[offset] != BQ_CHAR) {
            return null
        }
        offset++

        var spacesAfter = 0
        if (offset < line.length() && line[offset] == ' ') {
            spacesAfter = 1
        }

        return MarkdownConstraints(this, spacesBefore + 1 + spacesAfter, BQ_CHAR, true)
    }

    override fun toString(): String {
        return "MdConstraints: " + String(types) + "(" + getIndent() + ")"
    }

    companion object {
        public val BASE: MarkdownConstraints = MarkdownConstraints(IntArray(0), CharArray(0), BooleanArray(0))

        public val BQ_CHAR: Char = '>'

        private fun MarkdownConstraints(parent: MarkdownConstraints,
                                        newIndentDelta: Int,
                                        newType: Char,
                                        newExplicit: Boolean): MarkdownConstraints {
            val n = parent.indents.size()
            val _indents = IntArray(n + 1)
            val _types = CharArray(n + 1)
            val _isExplicit = BooleanArray(n + 1)
            System.arraycopy(parent.indents, 0, _indents, 0, n)
            System.arraycopy(parent.types, 0, _types, 0, n)
            System.arraycopy(parent.isExplicit, 0, _isExplicit, 0, n)

            _indents[n] = parent.getIndent() + newIndentDelta
            _types[n] = newType
            _isExplicit[n] = newExplicit
            return MarkdownConstraints(_indents, _types, _isExplicit)
        }

        public fun fromBase(pos: LookaheadText.Position, prevLineConstraints: MarkdownConstraints): MarkdownConstraints {
            assert(pos.char == '\n')

            val line = pos.currentLine
            var result = fillFromPrevious(line, 0, prevLineConstraints, BASE)

            while (true) {
                val offset = result.getIndent()
                // TODO delta
                result = result.addModifierIfNeeded(pos.nextPosition(offset))
                        ?: break
            }

            return result
        }

        private fun fillFromPrevious(line: String,
                                     startOffset: Int,
                                     prevLineConstraints: MarkdownConstraints,
                                     base: MarkdownConstraints): MarkdownConstraints {
            var offset = startOffset
            var result = base
            val prevN = prevLineConstraints.indents.size()
            var indexPrev = 0
            while (indexPrev < prevN) {
                var maxSpaces = 0

                while (indexPrev < prevN) {
                    if (prevLineConstraints.types[indexPrev] == BQ_CHAR) {
                        maxSpaces += 3
                        break
                    }

                    val deltaIndent = prevLineConstraints.indents[indexPrev] -
                            if (indexPrev == 0)
                                0
                            else
                                prevLineConstraints.indents[indexPrev - 1]

                    var deltaRemaining = deltaIndent
                    while (deltaRemaining > 0 && offset < line.length() && line.charAt(offset) == ' ') {
                        offset++;
                        deltaRemaining--
                    }
                    if (deltaRemaining > 0 && offset < line.length()) {
                        break
                    }
                    result = MarkdownConstraints(result, deltaIndent, prevLineConstraints.types[indexPrev], false)

                    indexPrev++
                }

                if (indexPrev >= prevN || prevLineConstraints.types[indexPrev] != BQ_CHAR) {
                    break
                }

                var spacesEaten = 0
                while (spacesEaten < maxSpaces && offset < line.length() && line.charAt(offset) == ' ') {
                    spacesEaten++
                    offset++
                }

                if (spacesEaten == maxSpaces && offset < line.length() && line.charAt(offset) == ' ') {
                    indexPrev = Int.MAX_VALUE
                }

                if (line.charAt(offset) == BQ_CHAR) {
                    indexPrev++
                    offset++
                    result = MarkdownConstraints(result, spacesEaten + 1, BQ_CHAR, true)
                } else {
                    indexPrev = Int.MAX_VALUE
                }
            }
            return result
        }

        private fun fetchListMarker(pos: LookaheadText.Position): CharSequence? {
            if (pos.char == '*' || pos.char == '-') {
                return pos.char.toString()
            }

            val line = pos.currentLine
            var offset = pos.offsetInCurrentLine
            while (offset < line.length() && line[offset] in '0'..'9') {
                offset++
            }
            if (offset < line.length() && (line[offset] == '.' || line[offset] == ')')) {
                return line.subSequence(pos.offsetInCurrentLine, offset + 1)
            } else {
                return null
            }
        }

        private fun getMarkerType(marker: CharSequence): Char {
            return marker[marker.length() - 1]
        }
    }

}
