package org.intellij.markdown.parser.markerblocks

import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints


public abstract class MarkerBlockImpl(protected val constraints: MarkdownConstraints,
                                      protected val marker: ProductionHolder.Marker) : MarkerBlock {

    private var getLastInterestingOffset: Int? = -1

    final override fun getNextInterestingOffset(pos: LookaheadText.Position): Int? {
        if (getLastInterestingOffset != null && getLastInterestingOffset!! < pos.offset) {
            getLastInterestingOffset = calcNextInterestingOffset(pos)
        }
        return getLastInterestingOffset
    }

    final override fun processToken(pos: LookaheadText.Position,
                              currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (getNextInterestingOffset(pos) > pos.offset) {
            return MarkerBlock.ProcessingResult.PASS
        }
        return doProcessToken(pos, currentConstraints)
    }

    final override fun getBlockConstraints(): MarkdownConstraints {
        return constraints
    }

    override fun acceptAction(action: MarkerBlock.ClosingAction): Boolean {
        var actionToRun = action
        if (actionToRun == MarkerBlock.ClosingAction.DEFAULT) {
            actionToRun = getDefaultAction()
        }

        actionToRun.doAction(marker, getDefaultNodeType())

        return actionToRun != MarkerBlock.ClosingAction.NOTHING

    }

    protected abstract fun getDefaultAction(): MarkerBlock.ClosingAction

    protected abstract fun doProcessToken(pos: LookaheadText.Position,
                                          currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult

    protected abstract fun calcNextInterestingOffset(pos: LookaheadText.Position): Int?

    public abstract fun getDefaultNodeType(): IElementType
}
