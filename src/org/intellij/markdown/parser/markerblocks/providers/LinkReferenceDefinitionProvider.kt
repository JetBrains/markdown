package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.LinkReferenceDefinitionMarkerBlock
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import java.util.*
import kotlin.text.Regex

public class LinkReferenceDefinitionProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(pos: LookaheadText.Position,
                                    productionHolder: ProductionHolder,
                                    stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {

        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, stateInfo.currentConstraints)) {
            return emptyList()
        }

        val matchResult = matchLinkDefinition(pos.textFromPosition) ?: return emptyList()
        for ((i, range) in matchResult.withIndex()) {
            productionHolder.addProduction(listOf(SequentialParser.Node(
                    addToRangeAndWiden(range, pos.offset), when (i) {
                0 -> MarkdownElementTypes.LINK_LABEL
                1 -> MarkdownElementTypes.LINK_DESTINATION
                2 -> MarkdownElementTypes.LINK_TITLE
                else -> throw AssertionError("There are no more than three groups in this regex")
            })))
        }

        val matchLength = matchResult.last().endInclusive + 1
        val endPosition = pos.nextPosition(matchLength)

        if (endPosition != null && !isEndOfLine(endPosition)) {
            return emptyList()
        }
        return listOf(LinkReferenceDefinitionMarkerBlock(stateInfo.currentConstraints, productionHolder.mark(),
                pos.offset + matchLength))
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        return false
    }

    companion object {
        val WHSP = "[ \\t]*"

        val NOT_CHARS = { c: String ->
            val nonWhitespace = "(?:\\\\[$c]|[^ \\t\\n$c])"
            val anyChar = "(?:\\\\[$c]|[^$c\\n])"
            "$anyChar*(?:\\n$anyChar*$nonWhitespace$WHSP)*(?:\\n$WHSP)?"
        }

        val NONCONTROL = "(?:\\\\[\\(\\)]|[^ \\n\\t\\(\\)])"

        val LINK_DESTINATION = Regex("(?:<(?:\\\\[<>]|[^<>\\n])*>|${NONCONTROL}*\\(${NONCONTROL}*\\)${NONCONTROL}*|${NONCONTROL}+)")

        val LINK_TITLE = "(?:\"${NOT_CHARS("\"")}\"|'${NOT_CHARS("'")}'|\\(${NOT_CHARS("\\)")}\\))"

        val LINK_DESTINATION_REGEX = Regex("\\A$LINK_DESTINATION")

        val LINK_TITLE_REGEX = Regex("\\A$LINK_TITLE")


        fun addToRangeAndWiden(range: IntRange, t: Int): IntRange {
            return IntRange(range.start + t, range.endInclusive + t + 1)
        }

        fun isEndOfLine(pos: LookaheadText.Position): Boolean {
            return pos.offsetInCurrentLine == -1 || pos.charsToNonWhitespace() == null
        }
        
        fun matchLinkDefinition(text: CharSequence): List<IntRange>? {
            var offset = MarkerBlockProvider.passSmallIndent(text)
            val linkLabel = matchLinkLabel(text, offset) ?: return null
            offset = linkLabel.endInclusive + 1
            if (offset >= text.length || text[offset] != ':') 
                return null
            offset++
            
            offset = passOneNewline(text, offset)

            val destination = LINK_DESTINATION_REGEX.find(text.subSequence(offset, text.length))
                    ?: return null
            val destinationRange = IntRange(destination.range.start + offset, destination.range.endInclusive + offset)
            
            offset += destination.range.endInclusive - destination.range.start + 1
            offset = passOneNewline(text, offset)

            val title = LINK_TITLE_REGEX.find(text.subSequence(offset, text.length))

            val result = ArrayList<IntRange>()
            result.add(linkLabel)
            result.add(destinationRange)
            if (title != null) {
                val titleRange = IntRange(title.range.start + offset, title.range.endInclusive + offset)
                
                offset += title.range.endInclusive - title.range.start + 1
                while (offset < text.length && text[offset].let { it == ' ' || it == '\t' })
                    offset++
                if (offset >= text.length || text[offset] == '\n') {
                    result.add(titleRange)
                }
            }
            
            return result
        }
        
        fun matchLinkLabel(text: CharSequence, start: Int): IntRange? {
            var offset = start
            if (offset >= text.length || text[offset] != '[') {
                return null
            }
            offset++
            
            var seenNonWhitespace = false
            
            for (i in 1..999) {
                if (offset >= text.length)
                    return null
                var c = text[offset]
                if (c == '[' || c == ']')
                    break
                if (c == '\\') {
                    offset++
                    if (offset >= text.length)
                        return null
                    c = text[offset] 
                }
                if (!c.isWhitespace()) {
                    seenNonWhitespace = true
                }
                offset++
            }
            if (!seenNonWhitespace || offset >= text.length || text[offset] != ']') {
                return null;
            }
            return start..offset
        }
        
        private fun passOneNewline(text: CharSequence, start: Int): Int {
            var offset = start
            while (offset < text.length && text[offset].let { it == ' ' || it == '\t' }) 
                offset++
            if (offset < text.length && text[offset] == '\n') {
                offset++
                while (offset < text.length && text[offset].let { it == ' ' || it == '\t' })
                    offset++
            }
            return offset
        }
    }
}
