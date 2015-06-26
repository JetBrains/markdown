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

    protected abstract var stateInfo: T

    protected abstract fun getPrioritizedMarkerPermutation(): List<Int>

    protected abstract fun getMarkerBlockProviders(): List<MarkerBlockProvider<T>>

    private val interruptsParagraph: (LookaheadText.Position) -> Boolean = { position ->
        var result = false
        for (provider in getMarkerBlockProviders()) {
            if (provider.interruptsParagraph(position)) {
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

        if (!Character.isWhitespace(pos.char) && stateInfo.paragraphBlock == null) {
            return listOf(ParagraphMarkerBlock(stateInfo.currentConstraints, productionHolder.mark(), interruptsParagraph))
        }

        return emptyList()
    }

    public fun processToken(pos: LookaheadText.Position): LookaheadText.Position? {
        val someoneHasCancelledEvent = processMarkers(pos)
        if (!someoneHasCancelledEvent) {
            val newMarkerBlocks = createNewMarkerBlocks(pos, productionHolder)
            for (newMarkerBlock in newMarkerBlocks) {
                addNewMarkerBlock(newMarkerBlock)
            }
        }

        var nextPos = calculateNextPos(pos)
        return nextPos
    }

    private fun calculateNextPos(pos: LookaheadText.Position): LookaheadText.Position? {
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
            pos.nextPosition()
        else
            pos.nextPosition(result - pos.offset)
    }

    public fun addNewMarkerBlock(newMarkerBlock: MarkerBlock) {
        markersStack.add(newMarkerBlock)
        topBlockConstraints = newMarkerBlock.getBlockConstraints()
        cachedPermutation = null
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
                val processingResult = markerBlock.processToken(pos, topBlockConstraints)
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
                //noinspection ConstantConditions
                topBlockConstraints = if (markersStack.isEmpty())
                    startConstraints
                else
                    markersStack.last().getBlockConstraints()
            }
        }

    }

    private fun applyProcessingResult(index: Int?, markerBlock: MarkerBlock, processingResult: MarkerBlock.ProcessingResult) {
        closeChildren(index!!, processingResult.childrenAction)

        // process self
        if (markerBlock.acceptAction(processingResult.selfAction)) {
            markersStack.remove(index)
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
        }
    }

    public open data class StateInfo(public val currentConstraints: MarkdownConstraints,
                                     public val newConstraints: MarkdownConstraints,
                                     private val markersStack: List<MarkerBlock>) {

        public val paragraphBlock: ParagraphMarkerBlock?
            get() = markersStack.firstOrNull { block -> block is ParagraphMarkerBlock } as ParagraphMarkerBlock?

        public val lastBlock: MarkerBlock?
            get() = markersStack.lastOrNull()

    }

}
