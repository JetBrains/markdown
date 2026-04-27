package org.intellij.markdown.flavours.gfm.table

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.lexer.MarkdownLexer
import org.intellij.markdown.parser.sequentialparsers.LexerBasedTokensCache
import org.intellij.markdown.parser.sequentialparsers.impl.BacktickParser
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.constraints.eatItselfFromString
import org.intellij.markdown.parser.constraints.getCharsEaten
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

class GitHubTableMarkerBlock(pos: LookaheadText.Position,
                             constraints: MarkdownConstraints,
                             private val productionHolder: ProductionHolder,
                             private val tableColumnsNumber: Int,
                             private val lexerFactory: () -> MarkdownLexer)
: MarkerBlockImpl(constraints, productionHolder.mark()) {

    var currentLine = 0

    init {
        productionHolder.addProduction(listOf(SequentialParser.Node(pos.offset..pos.nextLineOrEofOffset,
                GFMElementTypes.HEADER)))
        productionHolder.addProduction(fillCells(pos))
    }

    override fun doProcessToken(pos: LookaheadText.Position, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        currentLine++
        // That means it's table header separator line
        if (currentLine == 1) {
            productionHolder.addProduction(listOf(SequentialParser.Node(pos.offset + 1..pos.nextLineOrEofOffset,
                    GFMTokenTypes.TABLE_SEPARATOR)))
            return MarkerBlock.ProcessingResult.CANCEL
        }

        val line = pos.currentLine
        if (!isProbablyTableLine(line)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }
        val cellsAndSeps = fillCells(pos)
        if (cellsAndSeps.isEmpty()) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }
        productionHolder.addProduction(
                listOf(SequentialParser.Node(cellsAndSeps.first().range.first..cellsAndSeps.last().range.last,
                        GFMElementTypes.ROW))
                        + cellsAndSeps)
        return MarkerBlock.ProcessingResult.CANCEL
    }

    override fun getDefaultAction() = MarkerBlock.ClosingAction.DONE

    override fun calcNextInterestingOffset(pos: LookaheadText.Position) = pos.nextLineOrEofOffset

    override fun getDefaultNodeType() = GFMElementTypes.TABLE

    override fun isInterestingOffset(pos: LookaheadText.Position) = pos.offsetInCurrentLine == -1

    override fun allowsSubBlocks() = false

    private fun fillCells(pos: LookaheadText.Position): List<SequentialParser.Node> {
        val result = ArrayList<SequentialParser.Node>()

        var offset = pos.offset
        if (pos.offsetInCurrentLine == -1) {
            offset += 1 + constraints.getCharsEaten(pos.currentLine)
        }

        val line = constraints.eatItselfFromString(pos.currentLine)

        val cells = splitByPipes(line, lexerFactory)
        var cellNodesAdded = 0
        for (i in cells.indices) {
            val cell = cells[i]
            if (!cell.isBlank() || i in 1..cells.lastIndex - 1) {
                result.add(SequentialParser.Node(offset..offset + cell.length, GFMTokenTypes.CELL))
                cellNodesAdded++
            }
            offset += cell.length
            if (i < cells.lastIndex) {
                result.add(SequentialParser.Node(offset..offset + 1, GFMTokenTypes.TABLE_SEPARATOR))
            }
            offset += 1

            if (cellNodesAdded >= tableColumnsNumber) {
                if (offset < pos.nextLineOrEofOffset) {
                    result.add(SequentialParser.Node(offset..pos.nextLineOrEofOffset, GFMTokenTypes.TABLE_SEPARATOR))
                }
                break
            }
        }
        return result
    }

    private fun isProbablyTableLine(line: CharSequence): Boolean {
        return line.contains('|')
    }

    companion object {
        fun splitByPipes(text: CharSequence, lexerFactory: () -> MarkdownLexer): List<String> {
            val codeSpanRanges = findCodeSpanRanges(text, lexerFactory)
            val result = arrayListOf<String>()
            var startIndex = 0
            var index = 0
            var rangeIdx = 0
            while (index < text.length) {
                if (rangeIdx < codeSpanRanges.size && index == codeSpanRanges[rangeIdx].first) {
                    index = codeSpanRanges[rangeIdx].last + 1
                    rangeIdx++
                } else {
                    if (text[index] == '|' && (index == 0 || text[index - 1] != '\\')) {
                        result.add(text.substring(startIndex, index))
                        startIndex = index + 1
                    }
                    index++
                }
            }
            result.add(text.substring(startIndex))
            return result
        }

        private fun findCodeSpanRanges(text: CharSequence, lexerFactory: () -> MarkdownLexer): List<IntRange> {
            val lexer = lexerFactory()
            lexer.start(text)
            val cache = LexerBasedTokensCache(lexer)
            val allTokenRanges = listOf(cache.filteredTokens.indices)
            return BacktickParser().parse(cache, allTokenRanges).parsedNodes
                .filter { it.type == MarkdownElementTypes.CODE_SPAN }
                .map { node ->
                    cache.filteredTokens[node.range.first].tokenStart until
                    cache.filteredTokens[node.range.last - 1].tokenEnd
                }
        }
    }
}
