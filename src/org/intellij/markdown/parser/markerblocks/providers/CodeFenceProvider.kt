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

public class CodeFenceProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(pos: LookaheadText.Position,
                                   productionHolder: ProductionHolder,
                                   stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {
        val fenceStart = getFenceStart(pos, stateInfo.currentConstraints)
        if (fenceStart != null) {
            productionHolder.addProduction(listOf(SequentialParser.Node(pos.offset..pos.nextLineOrEofOffset,
                    MarkdownTokenTypes.CODE_FENCE_START)))
            return listOf(CodeFenceMarkerBlock(stateInfo.currentConstraints, productionHolder, fenceStart))
        } else {
            return emptyList()
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        return getFenceStart(pos, constraints) != null
    }

    private fun getFenceStart(pos: LookaheadText.Position, constraints: MarkdownConstraints): String? {
        if (pos.offsetInCurrentLine != constraints.getIndent()) {
            return null
        }
        val matchResult = REGEX.match(pos.currentLine.subSequence(pos.offsetInCurrentLine, pos.currentLine.length()))
            ?: return null
        return matchResult.groups[1]?.value
    }

    companion object {
        val REGEX: Regex = Regex("^ {0,3}(~~~+|```+)[^`]*$")
    }
}