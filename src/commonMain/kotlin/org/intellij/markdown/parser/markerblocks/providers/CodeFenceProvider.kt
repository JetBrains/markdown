package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.CodeFenceMarkerBlock
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import kotlin.text.Regex

class CodeFenceProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(pos: LookaheadText.Position,
                                   productionHolder: ProductionHolder,
                                   stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {
        val fenceAndInfo = getFenceStartAndInfo(pos, stateInfo.currentConstraints) ?: return emptyList()
        val indent = createNodesForFenceStart(pos, fenceAndInfo, productionHolder) ?: return emptyList()
        return listOf(CodeFenceMarkerBlock(stateInfo.currentConstraints, productionHolder, fenceAndInfo.first, indent))
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        return getFenceStartAndInfo(pos, constraints) != null
    }

    private fun createNodesForFenceStart(
        pos: LookaheadText.Position,
        fenceAndInfo: Pair<String, String>,
        productionHolder: ProductionHolder
    ): Int? {
        val infoStartPosition = pos.nextLineOrEofOffset - fenceAndInfo.second.length
        // Count the number of spaces before start element, so we can exclude them from resulting tree element
        val indent = pos.charsToNonWhitespace() ?: 0
        // If the current fence is indented with more than 3 spaces, it becomes a code block
        if (indent > 3) {
            return null
        }
        @Suppress("NAME_SHADOWING")
        val pos = pos.nextPosition(indent) ?: return null
        productionHolder.addProduction(listOf(SequentialParser.Node(pos.offset..infoStartPosition, MarkdownTokenTypes.CODE_FENCE_START)))
        if (fenceAndInfo.second.isNotEmpty()) {
            productionHolder.addProduction(listOf(SequentialParser.Node(infoStartPosition..pos.nextLineOrEofOffset, MarkdownTokenTypes.FENCE_LANG)))
        }
        return indent
    }

    private fun getFenceStartAndInfo(pos: LookaheadText.Position, constraints: MarkdownConstraints): Pair<String, String>? {
        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, constraints)) {
            return null
        }
        val matchResult = REGEX.find(pos.currentLineFromPosition)
            ?: return null
        return Pair(matchResult.groups[1]?.value!!, matchResult.groups[2]?.value!!)
    }

    companion object {
        val REGEX: Regex = Regex("^ {0,3}(~~~+|```+)([^`]*)$")
    }
}
