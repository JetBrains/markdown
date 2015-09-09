package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.constraints.MarkdownConstraints

public class GFMConstraints(indents: IntArray,
                            types: CharArray,
                            isExplicit: BooleanArray,
                            charsEaten: Int) : MarkdownConstraints(indents, types, isExplicit, charsEaten) {
    override val base: MarkdownConstraints
        get() = BASE

    override fun createNewConstraints(indents: IntArray, types: CharArray, isExplicit: BooleanArray, charsEaten: Int): MarkdownConstraints {
        return GFMConstraints(indents, types, isExplicit, charsEaten)
    }

    override fun fetchListMarker(pos: LookaheadText.Position): CharSequence? {
        return super.fetchListMarker(pos)
    }

    companion object {
        public val BASE: GFMConstraints = GFMConstraints(IntArray(0), CharArray(0), BooleanArray(0), 0)
    }
}