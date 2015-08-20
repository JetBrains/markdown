package org.intellij.markdown.parser

import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.ParagraphMarkerBlock
import java.util.ArrayList

public abstract class MarkerProcessor<T : MarkerProcessor.StateInfo>(private val productionHolder: ProductionHolder,
                                                                     private val startConstraints: MarkdownConstraints) {

    protected val NO_BLOCKS: List<MarkerBlock> = emptyList()

    protected val markersStack: MutableList<MarkerBlock> = ArrayList()

    private var cachedPermutation: List<Int>? = null

    protected var topBlockConstraints: MarkdownConstraints = startConstraints

    protected abstract val stateInfo: T

    protected abstract fun getPrioritizedMarkerPermutation(): List<Int>

    protected abstract fun getMarkerBlockProviders(): List<MarkerBlockProvider<T>>

    protected abstract fun updateStateInfo(pos: LookaheadText.Position)

    private var nextInterestingPosForExistingMarkers: Int = -1

    private val interruptsParagraph: (LookaheadText.Position, MarkdownConstraints) -> Boolean = { position, constraints ->
        var result = false
        for (provider in getMarkerBlockProviders()) {
            if (provider.interruptsParagraph(position, constraints)) {
                result = true
                break
            }
        }
        result
    }

    public open fun createNewMarkerBlocks(pos: LookaheadText.Position,
                                          productionHolder: ProductionHolder): List<MarkerBlock> {
        for (provider in getMarkerBlockProviders()) {
            val list = provider.createMarkerBlocks(pos, productionHolder, stateInfo)
            if (list.isNotEmpty()) {
                return list
            }
        }

        if (!Character.isWhitespace(pos.char)
                && stateInfo.paragraphBlock == null
                && pos.offsetInCurrentLine >= stateInfo.nextConstraints.getIndent()) {
            return listOf(ParagraphMarkerBlock(stateInfo.currentConstraints, productionHolder.mark(), interruptsParagraph))
        }

        return emptyList()
    }

    public fun processPosition(pos: LookaheadText.Position): LookaheadText.Position? {
        updateStateInfo(pos)

        val someoneHasCancelledEvent: Boolean
        var shouldRecalcNextPos = false

        if (pos.offset >= nextInterestingPosForExistingMarkers) {
            someoneHasCancelledEvent = processMarkers(pos)
            shouldRecalcNextPos = true
        } else {
            someoneHasCancelledEvent = false
        }

        if (!someoneHasCancelledEvent) {
            val newMarkerBlocks = createNewMarkerBlocks(pos, productionHolder)
            for (newMarkerBlock in newMarkerBlocks) {
                addNewMarkerBlock(newMarkerBlock)
                shouldRecalcNextPos = true
            }
        }

        if (shouldRecalcNextPos) {
            nextInterestingPosForExistingMarkers = calculateNextPosForExistingMarkers(pos)
        }

        if (pos.char == '\n') {
            return pos.nextPosition(stateInfo.nextConstraints.getIndentAdapted(pos.currentLine) + 1)
        }

        return pos.nextPosition()
    }

    private fun calculateNextPosForExistingMarkers(pos: LookaheadText.Position): Int {
        if (cachedPermutation == null) {
            cachedPermutation = getPrioritizedMarkerPermutation()
        }

        var result: Int? = null
        for (index in cachedPermutation!!) {
            if (index >= markersStack.size()) {
                continue
            }

            val markerBlock = markersStack.get(index)
            val offset = markerBlock.getNextInterestingOffset(pos)
            if (result == null || offset != null && result > offset) {
                result = offset
            }
        }
        return if (result == null)
            Int.MAX_VALUE
        else
            result
    }

    public fun addNewMarkerBlock(newMarkerBlock: MarkerBlock) {
        markersStack.add(newMarkerBlock)
        cachedPermutation = null
        relaxTopConstraints()
    }

    public fun flushMarkers() {
        closeChildren(-1, MarkerBlock.ClosingAction.DEFAULT)
    }

    /**
     * @return true if some markerBlock has canceled the event, false otherwise
     */
    private fun processMarkers(pos: LookaheadText.Position): Boolean {
        if (cachedPermutation == null) {
            cachedPermutation = getPrioritizedMarkerPermutation()
        }

        try {
            val currentPermutation = cachedPermutation!!
            for (index in currentPermutation) {
                if (index >= markersStack.size()) {
                    continue
                }

                val markerBlock = markersStack.get(index)
                val processingResult = markerBlock.processToken(pos, stateInfo.currentConstraints)
                if (processingResult == MarkerBlock.ProcessingResult.PASS) {
                    continue
                }

                applyProcessingResult(index, markerBlock, processingResult)

                if (processingResult.eventAction == MarkerBlock.EventAction.CANCEL) {
                    return true
                }
            }

            return false
        } finally {
            // Stack was changed
            if (cachedPermutation == null || markersStack.size() != cachedPermutation!!.size()) {
                cachedPermutation = null
            }
        }

    }

    private fun applyProcessingResult(index: Int?, markerBlock: MarkerBlock, processingResult: MarkerBlock.ProcessingResult) {
        closeChildren(index!!, processingResult.childrenAction)

        // process self
        if (markerBlock.acceptAction(processingResult.selfAction)) {
            markersStack.remove(index)
            relaxTopConstraints()
        }
    }

    private fun closeChildren(index: Int, childrenAction: MarkerBlock.ClosingAction) {
        if (childrenAction != MarkerBlock.ClosingAction.NOTHING) {
            var latterIndex = markersStack.size() - 1
            while (latterIndex > index) {
                val result = markersStack.get(latterIndex).acceptAction(childrenAction)
                assert(result, "If closing action is not NOTHING, marker should be gone")

                markersStack.remove(latterIndex)
                --latterIndex
            }
            relaxTopConstraints()
        }
    }

    private fun relaxTopConstraints() {
        topBlockConstraints = if (markersStack.isEmpty())
            startConstraints
        else
            markersStack.last().getBlockConstraints()
    }

    public open data class StateInfo(public val currentConstraints: MarkdownConstraints,
                                     public val nextConstraints: MarkdownConstraints,
                                     private val markersStack: List<MarkerBlock>) {

        public val paragraphBlock: ParagraphMarkerBlock?
            get() = markersStack.firstOrNull { block -> block is ParagraphMarkerBlock } as ParagraphMarkerBlock?

        public val lastBlock: MarkerBlock?
            get() = markersStack.lastOrNull()

    }

}
