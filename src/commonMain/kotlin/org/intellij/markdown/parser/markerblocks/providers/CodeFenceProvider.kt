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

open class CodeFenceProvider: MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(
        pos: LookaheadText.Position,
        productionHolder: ProductionHolder,
        stateInfo: MarkerProcessor.StateInfo
    ): List<MarkerBlock> {
        val openingInfo = obtainFenceOpeningInfo(pos, stateInfo.currentConstraints) ?: return emptyList()
        createNodesForFenceStart(pos, openingInfo, productionHolder)
        return listOf(CodeFenceMarkerBlock(stateInfo.currentConstraints, productionHolder, openingInfo.delimiter))
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        return obtainFenceOpeningInfo(pos, constraints) != null
    }

    private fun createNodesForFenceStart(pos: LookaheadText.Position, openingInfo: OpeningInfo, productionHolder: ProductionHolder) {
        val (_, info) = openingInfo
        val infoStartPosition = pos.nextLineOrEofOffset - info.length
        productionHolder.addProduction(listOf(SequentialParser.Node(pos.offset..infoStartPosition, MarkdownTokenTypes.CODE_FENCE_START)))
        if (openingInfo.info.isNotEmpty()) {
            productionHolder.addProduction(listOf(SequentialParser.Node(infoStartPosition..pos.nextLineOrEofOffset, MarkdownTokenTypes.FENCE_LANG)))
        }
    }

    /**
     * Holds information of fence opening parts.
     *
     * Typically, a code fence consists of:
     * * Opening and closing delimiters (usually matching) - marks the start and the end of the fence
     * * Info string - text on the same line as opening delimiter right after it
     * * Fence content - lines between opening and closing delimiters.
     *
     * Example:
     * ````markdown
     * vvv---------------opening delimiter
     * ```kotlin <-------info string
     * val some = 42 <---fence content
     * ``` <-------------closing delimiter
     * ````
     * Default implementation expects opening and closing delimiter to match.
     *
     * This API is a subject to change in the future!
     */
    protected data class OpeningInfo(
        /**
         * Delimiter used by this particular fence.
         */
        val delimiter: String,
        /**
         * Info string for this fence.
         */
        val info: String
    )

    /**
     * Can be used for customizing conditions for the fence opening.
     * See [OpeningInfo] for more details.
     *
     * This API is a subject to change in the future.
     */
    protected open fun obtainFenceOpeningInfo(pos: LookaheadText.Position, constraints: MarkdownConstraints): OpeningInfo? {
        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, constraints)) {
            return null
        }
        val matchResult = REGEX.find(pos.currentLineFromPosition) ?: return null
        return OpeningInfo(matchResult.groups[1]?.value!!, matchResult.groups[2]?.value!!)
    }

    companion object {
        private val REGEX: Regex = Regex("^ {0,3}(~~~+|```+)([^`]*)$")
    }
}
