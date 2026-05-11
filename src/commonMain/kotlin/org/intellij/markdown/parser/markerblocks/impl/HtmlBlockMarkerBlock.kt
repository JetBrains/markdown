package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.constraints.extendsPrev
import org.intellij.markdown.parser.constraints.getCharsEaten
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import kotlin.text.Regex

class HtmlBlockMarkerBlock(myConstraints: MarkdownConstraints,
                           private val productionHolder: ProductionHolder,
                           private val endCheckingRegex: Regex?,
                           startPosition: LookaheadText.Position)
: MarkerBlockImpl(myConstraints, productionHolder.mark()) {

    private val isCommentBlock: Boolean = endCheckingRegex?.pattern == "-->"
    private var commentStartSeen: Boolean = false

    init {
        val lineText = startPosition.currentLineFromPosition
        val rangeStart = startPosition.offset
        productionHolder.addProduction(lineNodes(rangeStart, lineText))
    }

    override fun allowsSubBlocks(): Boolean = false

    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = true

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(pos: LookaheadText.Position, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (pos.offsetInCurrentLine != -1) {
            return MarkerBlock.ProcessingResult.CANCEL
        }

        val prevLine = pos.prevLine ?: return MarkerBlock.ProcessingResult.DEFAULT
        if (!constraints.applyToNextLine(pos).extendsPrev(constraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        if (endCheckingRegex == null && MarkdownParserUtil.calcNumberOfConsequentEols(pos, constraints) >= 2) {
            return MarkerBlock.ProcessingResult.DEFAULT
        } else if (endCheckingRegex != null && endCheckingRegex.find(prevLine) != null) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        if (pos.currentLine.isNotEmpty()) {
            val charsEaten = constraints.getCharsEaten(pos.currentLine)
            val lineText = pos.currentLine.substring(charsEaten)
            val rangeStart = pos.offset + 1 + charsEaten
            productionHolder.addProduction(lineNodes(rangeStart, lineText))
        }

        return MarkerBlock.ProcessingResult.CANCEL
    }

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
        return pos.nextLineOrEofOffset
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownElementTypes.HTML_BLOCK
    }

    private fun lineNodes(rangeStart: Int, lineText: CharSequence): List<SequentialParser.Node> {
        if (!isCommentBlock) {
            return listOf(SequentialParser.Node(rangeStart..rangeStart + lineText.length, MarkdownTokenTypes.HTML_BLOCK_CONTENT))
        }

        val nodes = mutableListOf<SequentialParser.Node>()
        var pos = 0

        if (!commentStartSeen) {
            val startIdx = findCommentStart(lineText)
            if (startIdx < 0) {
                return if (lineText.isEmpty()) emptyList()
                else listOf(SequentialParser.Node(rangeStart..rangeStart + lineText.length, MarkdownTokenTypes.HTML_BLOCK_CONTENT))
            }
            if (startIdx > 0) {
                nodes += SequentialParser.Node(rangeStart..rangeStart + startIdx, MarkdownTokenTypes.HTML_BLOCK_CONTENT)
            }
            nodes += SequentialParser.Node(rangeStart + startIdx..rangeStart + startIdx + 4, MarkdownTokenTypes.HTML_COMMENT_START)
            pos = startIdx + 4
            commentStartSeen = true
        }

        val endIdx = findCommentEnd(lineText, pos)
        if (endIdx < 0) {
            if (pos < lineText.length) {
                nodes += SequentialParser.Node(rangeStart + pos..rangeStart + lineText.length, MarkdownTokenTypes.HTML_COMMENT_CONTENT)
            }
            return nodes
        }

        if (pos < endIdx) {
            nodes += SequentialParser.Node(rangeStart + pos..rangeStart + endIdx, MarkdownTokenTypes.HTML_COMMENT_CONTENT)
        }
        nodes += SequentialParser.Node(rangeStart + endIdx..rangeStart + endIdx + 3, MarkdownTokenTypes.HTML_COMMENT_END)
        if (endIdx + 3 < lineText.length) {
            nodes += SequentialParser.Node(rangeStart + endIdx + 3..rangeStart + lineText.length, MarkdownTokenTypes.HTML_BLOCK_CONTENT)
        }

        return nodes
    }

    private fun findCommentStart(line: CharSequence): Int {
        var i = 0
        while (i < 3 && i < line.length && line[i] == ' ') i++
        return if (line.length >= i + 4 && line[i] == '<' && line[i + 1] == '!' && line[i + 2] == '-' && line[i + 3] == '-') i else -1
    }

    private fun findCommentEnd(line: CharSequence, from: Int): Int {
        val limit = line.length - 3
        for (i in from..limit) {
            if (line[i] == '-' && line[i + 1] == '-' && line[i + 2] == '>') return i
        }
        return -1
    }
}
