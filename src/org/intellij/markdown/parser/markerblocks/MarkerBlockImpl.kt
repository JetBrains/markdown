package org.intellij.markdown.parser.markerblocks

import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.MarkdownConstraints
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.TokensCache


public abstract class MarkerBlockImpl(protected val constraints: MarkdownConstraints,
                                      protected val marker: ProductionHolder.Marker,
                                      private val interestingTypes: Set<IElementType>? = null) : MarkerBlock {

    override fun processToken(tokenType: IElementType, builder: TokensCache.Iterator, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (interestingTypes != null && !interestingTypes.contains(tokenType)) {
            return MarkerBlock.ProcessingResult.PASS
        }
        return doProcessToken(tokenType, builder, currentConstraints)
    }

    override fun getBlockConstraints(): MarkdownConstraints {
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

    protected abstract fun doProcessToken(tokenType: IElementType, iterator: TokensCache.Iterator, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult

    public abstract fun getDefaultNodeType(): IElementType
}
