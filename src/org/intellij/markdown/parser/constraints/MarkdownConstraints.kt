package org.intellij.markdown.parser.constraints

import org.intellij.markdown.parser.LookaheadText
import kotlin.math.min

/**
 * A generalized "indent" in markdown world. Stores types of blocks spawning indents alongside indents themselves.
 */
interface MarkdownConstraints {
    val indent: Int
    val types: CharArray
    val isExplicit: BooleanArray
    val charsEaten: Int

    /**
     * Whether another constraints can "continue" opened blocks corresponding to {@code this} constraints
     */
    fun startsWith(other: MarkdownConstraints): Boolean

    /**
     * Leaking abstraction: whether there are "breaking" modifiers which do not continue other constraints
     * even if all types and indents are correct. In fact, means whether there are list markers or not.
     */
    fun containsListMarkers(upToIndex: Int): Boolean

    /**
     * Return new constraints with a new modifier added or {@code null} if position does not add new modifiers
     */
    fun addModifierIfNeeded(pos: LookaheadText.Position?): MarkdownConstraints?

    /**
     * Returns a constraints for the next line (at given pos) by continuing as much as possible from {@code this}
     * constraints without adding any new modifiers
     */
    fun applyToNextLine(pos: LookaheadText.Position?): MarkdownConstraints
}

fun MarkdownConstraints.extendsList(other: MarkdownConstraints): Boolean {
    if (other.types.isEmpty()) {
        throw IllegalArgumentException("List constraints should contain at least one item")
    }
    return startsWith(other) && !containsListMarkers(other.types.size - 1)
}

fun MarkdownConstraints.extendsPrev(other: MarkdownConstraints): Boolean {
    return startsWith(other) && !containsListMarkers(other.types.size)
}

fun MarkdownConstraints.upstreamWith(other: MarkdownConstraints): Boolean {
    return other.startsWith(this) && !containsListMarkers()
}

fun MarkdownConstraints.eatItselfFromString(s: CharSequence): CharSequence {
    return if (s.length < charsEaten) {
        ""
    } else {
        s.subSequence(charsEaten, s.length)
    }
}

fun MarkdownConstraints.applyToNextLineAndAddModifiers(pos: LookaheadText.Position): MarkdownConstraints {
    assert(pos.offsetInCurrentLine == -1)

    var result = applyToNextLine(pos)
    val line = pos.currentLine

    while (true) {
        val offset = result.getCharsEaten(line)
        result = result.addModifierIfNeeded(pos.nextPosition(1 + offset))
                ?: break
    }

    return result
}

fun MarkdownConstraints.getCharsEaten(s: CharSequence): Int {
    return min(charsEaten, s.length)
}

private fun MarkdownConstraints.containsListMarkers(): Boolean {
    return containsListMarkers(types.size)
}
