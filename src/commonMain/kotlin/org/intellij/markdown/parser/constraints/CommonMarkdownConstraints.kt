package org.intellij.markdown.parser.constraints

import org.intellij.markdown.lexer.Compat.assert
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.markerblocks.providers.HorizontalRuleProvider
import kotlin.math.min

open class CommonMarkdownConstraints protected constructor(private val indents: IntArray,
                                                           override val types: CharArray,
                                                           override val isExplicit: BooleanArray,
                                                           override val charsEaten: Int) : MarkdownConstraints {

    protected open val base: CommonMarkdownConstraints
        get() = BASE

    protected open fun createNewConstraints(indents: IntArray,
                                         types: CharArray,
                                         isExplicit: BooleanArray,
                                         charsEaten: Int): CommonMarkdownConstraints {
        return CommonMarkdownConstraints(indents, types, isExplicit, charsEaten)
    }

    override val indent: Int get() = indents.lastOrNull() ?: 0

    override fun startsWith(other: MarkdownConstraints): Boolean {
        if (other !is CommonMarkdownConstraints) return false
        val n = indents.size
        val m = other.indents.size

        if (n < m) {
            return false
        }
        return (0 until m).none { types[it] != other.types[it] }
    }

    override fun containsListMarkers(upToIndex: Int): Boolean {
        return (0 until upToIndex).any { types[it] != BQ_CHAR && isExplicit[it] }
    }

    override fun addModifierIfNeeded(pos: LookaheadText.Position?): CommonMarkdownConstraints? {
        if (pos == null || pos.offsetInCurrentLine == -1)
            return null
        if (HorizontalRuleProvider.isHorizontalRule(pos.currentLine, pos.offsetInCurrentLine)) {
            return null
        }
        return tryAddListItem(pos) ?: tryAddBlockQuote(pos)
    }

    override fun applyToNextLine(pos: LookaheadText.Position?): CommonMarkdownConstraints {
        if (pos == null) {
            return base
        }
        assert(pos.offsetInCurrentLine == -1) { "given $pos" }

        val line = pos.currentLine
        val prevN = indents.size
        if (prevN == 0) {
            return base
        }

        // The continued constraints are accumulated into these arrays and materialized exactly once at the end.
        // Building them via `create` one item at a time would copy the arrays on every step, making a single call
        // O(depth^2). Since every open block re-runs this for every line, that adds up to a cubic parse time
        // on deeply nested lists/quotes.
        val newIndents = IntArray(prevN)
        val newTypes = CharArray(prevN)
        val newExplicit = BooleanArray(prevN)
        var newN = 0
        var currentIndent = 0

        var offset = 0
        var indexPrev = 0

        var totalSpaces = 0
        var spacesSeen = 0
        val hasKMoreSpaces = { k: Int ->
            val oldSpacesSeen = spacesSeen
            val oldOffset = offset
            afterSpaces@
            while (spacesSeen < k && offset < line.length) {
                val deltaSpaces = when (line[offset]) {
                    ' ' -> 1
                    '\t' -> 4 - totalSpaces % 4
                    else -> break@afterSpaces
                }
                spacesSeen += deltaSpaces
                totalSpaces += deltaSpaces
                offset++
            }
            if (offset == line.length) {
                spacesSeen = Int.MAX_VALUE
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

        // Each round continues (at most) one blockquote marker followed by a run of list indents.
        // The whitespace accounting is restarted on every round.
        while (indexPrev < prevN) {
            totalSpaces = 0
            spacesSeen = 0

            val bqIndent: Int?
            if (types[indexPrev] == BQ_CHAR) {
                bqIndent = getBlockQuoteIndent(line, offset) ?: break
                offset += bqIndent
                indexPrev++
            } else {
                bqIndent = null
            }

            val oldIndexPrev = indexPrev
            while (indexPrev < prevN && types[indexPrev] != BQ_CHAR) {
                if (!hasKMoreSpaces(indentDelta(indexPrev))) {
                    break
                }
                indexPrev++
            }

            if (bqIndent == null && oldIndexPrev == indexPrev) {
                break
            }

            if (bqIndent != null) {
                val bonusForTheBlockquote = if (hasKMoreSpaces(1)) 1 else 0
                currentIndent += bqIndent + bonusForTheBlockquote
                newIndents[newN] = currentIndent
                newTypes[newN] = BQ_CHAR
                newExplicit[newN] = true
                newN++
            }
            for (index in oldIndexPrev until indexPrev) {
                currentIndent += indentDelta(index)
                newIndents[newN] = currentIndent
                newTypes[newN] = types[index]
                newExplicit[newN] = false
                newN++
            }
        }

        if (newN == 0) {
            return base
        }
        if (newN == prevN) {
            return createNewConstraints(newIndents, newTypes, newExplicit, offset)
        }
        return createNewConstraints(newIndents.copyOf(newN), newTypes.copyOf(newN), newExplicit.copyOf(newN), offset)
    }

    private fun indentDelta(index: Int): Int {
        return indents[index] - if (index == 0) 0 else indents[index - 1]
    }

    private fun getBlockQuoteIndent(line: CharSequence, startOffset: Int): Int? {
        var offset = startOffset
        var blockQuoteIndent = 0

        // '\t' can be omitted here since it'll add at least 4 indent
        while (blockQuoteIndent < 3 && offset < line.length && line[offset] == ' ') {
            blockQuoteIndent++
            offset++
        }

        return if (offset < line.length && line[offset] == BQ_CHAR) {
            blockQuoteIndent + 1
        } else {
            null
        }
    }


    protected open fun fetchListMarker(pos: LookaheadText.Position): ListMarkerInfo? {
        val c = pos.char
        if (c == '*' || c == '-' || c == '+') {
            return ListMarkerInfo(1, c, 1)
        }

        val line = pos.currentLine
        var offset = pos.offsetInCurrentLine
        val maxDigitsEnd = min(line.length, pos.offsetInCurrentLine + MAX_ORDERED_MARKER_DIGITS + 1)
        while (offset < maxDigitsEnd && line[offset] in '0'..'9') {
            offset++
        }
        return if (offset > pos.offsetInCurrentLine
                && offset - pos.offsetInCurrentLine <= MAX_ORDERED_MARKER_DIGITS
                && offset < line.length
                && (line[offset] == '.' || line[offset] == ')')) {
            ListMarkerInfo(offset + 1 - pos.offsetInCurrentLine,
                    line[offset],
                    offset + 1 - pos.offsetInCurrentLine)
        } else {
            null
        }
    }

    protected data class ListMarkerInfo(val markerLength: Int, val markerType: Char, val markerIndent: Int)


    private fun tryAddListItem(pos: LookaheadText.Position): CommonMarkdownConstraints? {
        val line = pos.currentLine

        var offset = pos.offsetInCurrentLine
        var spacesBefore = if (offset > 0 && line[offset - 1] == '\t')
            (4 - indent % 4) % 4
        else
            0
        // '\t' can be omitted here since it'll add at least 4 indent
        while (offset < line.length && line[offset] == ' ' && spacesBefore < 3) {
            spacesBefore++
            offset++
        }
        if (offset == line.length)
            return null

        val markerInfo = fetchListMarker(pos.nextPosition(offset - pos.offsetInCurrentLine)!!)
                ?: return null

        offset += markerInfo.markerLength
        var spacesAfter = 0

        val markerEndOffset = offset
        afterSpaces@
        while (offset < line.length) {
            when (line[offset]) {
                ' ' -> spacesAfter++
                '\t' -> spacesAfter += 4 - spacesAfter % 4
                else -> break@afterSpaces
            }
            offset++
        }

        // By the classification http://spec.commonmark.org/0.20/#list-items
        // 1. Basic case
        if (spacesAfter in 1..4 && offset < line.length) {
            return create(this, spacesBefore + markerInfo.markerIndent + spacesAfter, markerInfo.markerType, true, offset)
        }
        if (spacesAfter >= 5 && offset < line.length // 2. Starts with an indented code
                || offset == line.length) {
            // 3. Starts with an empty string
            return create(this, spacesBefore + markerInfo.markerIndent + 1, markerInfo.markerType, true,
                    min(offset, markerEndOffset + 1))
        }

        return null
    }

    private fun tryAddBlockQuote(pos: LookaheadText.Position): CommonMarkdownConstraints? {
        val line = pos.currentLine

        var offset = pos.offsetInCurrentLine
        var spacesBefore = 0
        // '\t' can be omitted here since it'll add at least 4 indent
        while (offset < line.length && line[offset] == ' ' && spacesBefore < 3) {
            spacesBefore++
            offset++
        }
        if (offset == line.length || line[offset] != BQ_CHAR) {
            return null
        }
        offset++

        var spacesAfter = 0
        if (offset >= line.length || line[offset] == ' ' || line[offset] == '\t') {
            spacesAfter = 1

            if (offset < line.length) {
                offset++
            }
        }

        return create(this, spacesBefore + 1 + spacesAfter, BQ_CHAR, true, offset)
    }

    override fun toString(): String {
        return "MdConstraints: " + types.concatToString() + "(" + indent + ")"
    }

    companion object {
        val BASE: CommonMarkdownConstraints = CommonMarkdownConstraints(IntArray(0), CharArray(0), BooleanArray(0), 0)

        const val BQ_CHAR: Char = '>'

        private const val MAX_ORDERED_MARKER_DIGITS = 9

        private fun create(parent: CommonMarkdownConstraints,
                           newIndentDelta: Int,
                           newType: Char,
                           newExplicit: Boolean,
                           newOffset: Int): CommonMarkdownConstraints {
            val n = parent.indents.size
            val indents = parent.indents.copyOf(n + 1)
            val types = parent.types.copyOf(n + 1)
            val isExplicit = parent.isExplicit.copyOf(n + 1)

            indents[n] = parent.indent + newIndentDelta
            types[n] = newType
            isExplicit[n] = newExplicit
            return parent.createNewConstraints(indents, types, isExplicit, newOffset)
        }
    }

}

