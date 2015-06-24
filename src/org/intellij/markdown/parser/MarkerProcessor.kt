package org.intellij.markdown.parser

import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.ParagraphMarkerBlock
import java.util.ArrayList
import java.util.TreeMap

public abstract class MarkerProcessor<T : MarkerProcessor.PositionInfo>(protected val productionHolder: ProductionHolder,
                                                                        private val startConstraints: MarkdownConstraints) {

    protected val NO_BLOCKS: Array<MarkerBlock> = arrayOf()

    protected val markersStack: MutableList<MarkerBlock> = ArrayList()

    private val postponedActions = TreeMap<Int, MarkerBlock.ProcessingResult>()

    private var cachedPermutation: List<Int>? = null

    private var topBlockConstraints: MarkdownConstraints = startConstraints

    protected abstract var positionInfo: T

    protected abstract fun getPrioritizedMarkerPermutation(): List<Int>

    protected abstract fun getMarkerBlockProviders(): List<MarkerBlockProvider<T>>

    public open fun createNewMarkerBlocks(pos: LookaheadText.Position, productionHolder: ProductionHolder): Array<MarkerBlock> {
        for (provider in getMarkerBlockProviders()) {
            val markerBlock = provider.createMarkerBlock(pos, positionInfo)
            if (markerBlock != null) {
                return arrayOf(markerBlock)
            }
        }
        return emptyArray()
    }

    public fun processToken(pos: LookaheadText.Position): LookaheadText.Position? {
        processPostponedActions()

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
            null
        else
            pos.nextPosition(result - pos.offset)
    }

    private fun processPostponedActions() {
        while (!postponedActions.isEmpty()) {
            val lastEntry = postponedActions.pollLastEntry()

            val stackIndex = lastEntry.getKey()
            applyProcessingResult(stackIndex, markersStack.get(stackIndex!!), lastEntry.getValue())
        }
    }

    public fun addNewMarkerBlock(newMarkerBlock: MarkerBlock) {
        markersStack.add(newMarkerBlock)
        topBlockConstraints = newMarkerBlock.getBlockConstraints()
        cachedPermutation = null
    }

    public fun flushMarkers() {
        processPostponedActions()

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

                if (processingResult.isPostponed) {
                    postponedActions.put(index, processingResult)
                } else {
                    if (processingResult == MarkerBlock.ProcessingResult.PASS) {
                        continue
                    }

                    applyProcessingResult(index, markerBlock, processingResult)
                }

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
                if (postponedActions.containsKey(latterIndex)) {
                    System.err?.println("Processing postponed marker block :(")
                    postponedActions.remove(latterIndex)
                }

                val result = markersStack.get(latterIndex).acceptAction(childrenAction)
                assert(result, "If closing action is not NOTHING, marker should be gone")

                markersStack.remove(latterIndex)
                --latterIndex
            }
        }
    }

    public inner open data class PositionInfo(public val currentConstraints: MarkdownConstraints,
                                              public val newConstraints: MarkdownConstraints) {

        public val productionHolder: ProductionHolder
            get() = this@MarkerProcessor.productionHolder

        public val paragraphBlock: ParagraphMarkerBlock?
            get() = markersStack.firstOrNull { block -> block is ParagraphMarkerBlock } as ParagraphMarkerBlock?

        public fun getLastBlock(): MarkerBlock? {
            return markersStack.lastOrNull()
        }
    }

}
