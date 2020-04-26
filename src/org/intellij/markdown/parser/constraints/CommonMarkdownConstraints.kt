package org.intellij.markdown.parser.constraints

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
        var indexPrev = 0

        val getBlockQuoteIndent = { startOffset: Int ->
            var offset = startOffset
            var blockQuoteIndent = 0

            // '\t' can be omitted here since it'll add at least 4 indent
            while (blockQuoteIndent < 3 && offset < line.length && line[offset] == ' ') {
                blockQuoteIndent++
                offset++
            }

            if (offset < line.length && line[offset] == BQ_CHAR) {
                blockQuoteIndent + 1
            } else {
                null
            }
        }

        val fillMaybeBlockquoteAndListIndents = fun(constraints: CommonMarkdownConstraints): CommonMarkdownConstraints {
            if (indexPrev >= prevN) {
                return constraints
            }

            var offset = constraints.getCharsEaten(line)
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
            if (types[indexPrev] == BQ_CHAR) {
                bqIndent = getBlockQuoteIndent(offset)
                        ?: return constraints
                offset += bqIndent
                indexPrev++
            } else {
                bqIndent = null
            }

            val oldIndexPrev = indexPrev
            while (indexPrev < prevN && types[indexPrev] != BQ_CHAR) {
                val deltaIndent = indents[indexPrev] -
                        if (indexPrev == 0)
                            0
                        else
                            indents[indexPrev - 1]

                if (!hasKMoreSpaces(deltaIndent)) {
                    break
                }

                indexPrev++
            }

            var result = constraints
            if (bqIndent != null) {
                val bonusForTheBlockquote = if (hasKMoreSpaces(1)) 1 else 0
                result = create(result, bqIndent + bonusForTheBlockquote, BQ_CHAR, true, offset)
            }
            for (index in oldIndexPrev until indexPrev) {
                val deltaIndent = indents[index] -
                        if (index == 0)
                            0
                        else
                            indents[index - 1]
                result = create(result, deltaIndent, types[index], false, offset)
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


    protected open fun fetchListMarker(pos: LookaheadText.Position): ListMarkerInfo? {
        val c = pos.char
        if (c == '*' || c == '-' || c == '+') {
            return ListMarkerInfo(1, c, 1)
        }

        val line = pos.currentLine
        var offset = pos.offsetInCurrentLine
        while (offset < line.length && line[offset] in '0'..'9') {
            offset++
        }
        return if (offset > pos.offsetInCurrentLine
                && offset - pos.offsetInCurrentLine <= 9
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
        return "MdConstraints: " + String(types) + "(" + indent + ")"
    }

    companion object {
        val BASE: CommonMarkdownConstraints = CommonMarkdownConstraints(IntArray(0), CharArray(0), BooleanArray(0), 0)

        const val BQ_CHAR: Char = '>'

        private fun create(parent: CommonMarkdownConstraints,
                           newIndentDelta: Int,
                           newType: Char,
                           newExplicit: Boolean,
                           newOffset: Int): CommonMarkdownConstraints {
            val n = parent.indents.size
            val indents = IntArray(n + 1)
            val types = CharArray(n + 1)
            val isExplicit = BooleanArray(n + 1)
            System.arraycopy(parent.indents, 0, indents, 0, n)
            System.arraycopy(parent.types, 0, types, 0, n)
            System.arraycopy(parent.isExplicit, 0, isExplicit, 0, n)

            indents[n] = parent.indent + newIndentDelta
            types[n] = newType
            isExplicit[n] = newExplicit
            return parent.createNewConstraints(indents, types, isExplicit, newOffset)
        }
    }

}

