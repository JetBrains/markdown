package org.intellij.markdown.parser.constraints

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.markerblocks.providers.HorizontalRuleProvider

public class MarkdownConstraints private constructor(private val indents: IntArray,
                                                     private val types: CharArray,
                                                     private val isExplicit: BooleanArray,
                                                     private val charsEaten: Int) {

    public fun eatItselfFromString(s: CharSequence) : CharSequence {
        if (s.length() < charsEaten) {
            return ""
        } else {
            return s.subSequence(charsEaten, s.length())
        }
    }

    public fun getIndent(): Int {
        if (indents.size() == 0) {
            return 0
        }

        return indents.last()
    }

    public fun getCharsEaten(s: CharSequence): Int {
        return Math.min(charsEaten, s.length())
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
        if (HorizontalRuleProvider.isHorizontalRule(pos.currentLine, pos.offsetInCurrentLine)) {
            return null
        }
        return tryAddListItem(pos) ?: tryAddBlockQuote(pos)
    }

    private fun tryAddListItem(pos: LookaheadText.Position): MarkdownConstraints? {
        val line = pos.currentLine

        var offset = pos.offsetInCurrentLine
        var spacesBefore = 0
        // '\t' can be omitted here since it'll add at least 4 indent
        while (offset < line.length() && line[offset] == ' ' && spacesBefore < 3) {
            spacesBefore++
            offset++
        }
        if (offset == line.length())
            return null

        val marker = fetchListMarker(pos.nextPosition(spacesBefore)!!)
                ?: return null

        offset += marker.length()
        var spacesAfter = 0

        afterSpaces@
        while (offset < line.length()) {
            when (line[offset]) {
                ' ' -> spacesAfter++
                '\t' -> spacesAfter += 4 - spacesAfter % 4
                else -> break@afterSpaces
            }
            offset++
        }

        // By the classification http://spec.commonmark.org/0.20/#list-items
        // 1. Basic case
        if (spacesAfter > 0 && spacesAfter < 5 && offset < line.length()) {
            return MarkdownConstraints(this, spacesBefore + marker.length() + spacesAfter, getMarkerType(marker), true, offset)
        }
        if (spacesAfter >= 5 && offset < line.length() // 2. Starts with an indented code
                || offset == line.length()) {
            // 3. Starts with an empty string
            return MarkdownConstraints(this, spacesBefore + marker.length() + 1, getMarkerType(marker), true, offset)
        }

        return null
    }

    private fun tryAddBlockQuote(pos: LookaheadText.Position): MarkdownConstraints? {
        val line = pos.currentLine

        var offset = pos.offsetInCurrentLine
        var spacesBefore = 0
        // '\t' can be omitted here since it'll add at least 4 indent
        while (offset < line.length() && line[offset] == ' ' && spacesBefore < 3) {
            spacesBefore++
            offset++
        }
        if (offset == line.length() || line[offset] != BQ_CHAR) {
            return null
        }
        offset++

        var spacesAfter = 0
        if (offset >= line.length() || line[offset] == ' ' || line[offset] == '\t') {
            spacesAfter = 1

            if (offset < line.length()) {
                offset++
            }
        }

        return MarkdownConstraints(this, spacesBefore + 1 + spacesAfter, BQ_CHAR, true, offset)
    }

    override fun toString(): String {
        return "MdConstraints: " + String(types) + "(" + getIndent() + ")"
    }

    companion object {
        public val BASE: MarkdownConstraints = MarkdownConstraints(IntArray(0), CharArray(0), BooleanArray(0), 0)

        public val BQ_CHAR: Char = '>'

        private fun MarkdownConstraints(parent: MarkdownConstraints,
                                        newIndentDelta: Int,
                                        newType: Char,
                                        newExplicit: Boolean,
                                        newOffset: Int): MarkdownConstraints {
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
            return MarkdownConstraints(_indents, _types, _isExplicit, newOffset)
        }

        public fun fromBase(pos: LookaheadText.Position, prevLineConstraints: MarkdownConstraints): MarkdownConstraints {
            assert(pos.char == '\n')

            val line = pos.currentLine
            var result = fillFromPrevious(line, 0, prevLineConstraints, BASE)

            while (true) {
                val offset = result.getCharsEaten(line)
                result = result.addModifierIfNeeded(pos.nextPosition(1 + offset))
                        ?: break
            }

            return result
        }

        public fun fillFromPrevious(line: String,
                                    startOffset: Int,
                                    prevLineConstraints: MarkdownConstraints,
                                    base: MarkdownConstraints): MarkdownConstraints {
            if (HorizontalRuleProvider.isHorizontalRule(line, startOffset)) {
                return base;
            }

            val prevN = prevLineConstraints.indents.size()
            var indexPrev = 0

            val getBlockQuoteIndent = { startOffset: Int ->
                var offset = startOffset
                var blockQuoteIndent = 0

                // '\t' can be omitted here since it'll add at least 4 indent
                while (blockQuoteIndent < 3 && offset < line.length() && line[offset] == ' ') {
                    blockQuoteIndent++
                    offset++
                }

                if (offset < line.length() && line[offset] == BQ_CHAR) {
                    blockQuoteIndent + 1
                } else {
                    null
                }
            }

            val fillMaybeBlockquoteAndListIndents = fun(constraints: MarkdownConstraints): MarkdownConstraints {
                if (indexPrev >= prevN) {
                    return constraints
                }

                var offset = startOffset + constraints.getCharsEaten(line)
                var spacesSeen = 0
                val hasKMoreSpaces = { k: Int ->
                    val oldSpacesSeen = spacesSeen
                    val oldOffset = offset
                    afterSpaces@
                    while (spacesSeen < k && offset < line.length()) {
                        when (line[offset]) {
                            ' ' -> spacesSeen++
                            '\t' -> spacesSeen += 4 - spacesSeen % 4
                            else -> break@afterSpaces
                        }
                        offset++
                    }
                    if (offset == line.length()) {
                        spacesSeen = Integer.MAX_VALUE
                    }

                    if (k <= spacesSeen) {
                        spacesSeen -= k
                        true
                    } else {
                        offset = oldOffset
                        spacesSeen = oldSpacesSeen
                        false
                    }
                }

                val bqIndent: Int?
                if (prevLineConstraints.types[indexPrev] == BQ_CHAR) {
                    bqIndent = getBlockQuoteIndent(offset)
                        ?: return constraints
                    offset += bqIndent
                    indexPrev++
                } else {
                    bqIndent = null
                }

                val oldIndexPrev = indexPrev
                while (indexPrev < prevN && prevLineConstraints.types[indexPrev] != BQ_CHAR) {
                    val deltaIndent = prevLineConstraints.indents[indexPrev] -
                            if (indexPrev == 0)
                                0
                            else
                                prevLineConstraints.indents[indexPrev - 1]

                    if (!hasKMoreSpaces(deltaIndent)) {
                        break
                    }

                    indexPrev++
                }

                var result = constraints
                if (bqIndent != null) {
                    val bonusForTheBlockquote = if (hasKMoreSpaces(1)) 1 else 0
                    result = MarkdownConstraints(result, bqIndent + bonusForTheBlockquote, BQ_CHAR, true, offset)
                }
                for (index in oldIndexPrev..indexPrev - 1) {
                    val deltaIndent = prevLineConstraints.indents[index] -
                            if (index == 0)
                                0
                            else
                                prevLineConstraints.indents[index - 1]
                    result = MarkdownConstraints(result, deltaIndent, prevLineConstraints.types[index], false, offset)
                }
                return result
            }

            var result = base
            while (true) {
                val nextConstraints = fillMaybeBlockquoteAndListIndents(result)
                if (nextConstraints == result) {
                    return result
                }
                result = nextConstraints
            }
        }

        private fun fetchListMarker(pos: LookaheadText.Position): CharSequence? {
            if (pos.char == '*' || pos.char == '-' || pos.char == '+') {
                return pos.char.toString()
            }

            val line = pos.currentLine
            var offset = pos.offsetInCurrentLine
            while (offset < line.length() && line[offset] in '0'..'9') {
                offset++
            }
            if (offset > pos.offsetInCurrentLine
                    && offset - pos.offsetInCurrentLine <= 9
                    && offset < line.length()
                    && (line[offset] == '.' || line[offset] == ')')) {
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
