package org.intellij.markdown.parser.markerblocks

import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.MarkdownConstraints
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.TokensCache
import org.intellij.markdown.parser.sequentialparsers.SequentialParserManager


public abstract class InlineStructureHoldingMarkerBlock(
        constraints: MarkdownConstraints,
        protected val tokensCache: TokensCache,
        protected val productionHolder: ProductionHolder,
        interestingTypes: Set<IElementType>?)
    : MarkerBlockImpl(constraints, productionHolder.mark(), interestingTypes) {

    override fun acceptAction(action: MarkerBlock.ClosingAction): Boolean {
        if (action != MarkerBlock.ClosingAction.NOTHING) {
            if (action == MarkerBlock.ClosingAction.DONE || action == MarkerBlock.ClosingAction.DEFAULT && getDefaultAction() == MarkerBlock.ClosingAction.DONE) {
                val results = SequentialParserManager().runParsingSequence(tokensCache, getRangesContainingInlineStructure())

                productionHolder.addProduction(results)
            }
        }

        return super.acceptAction(action)
    }

    public abstract fun getRangesContainingInlineStructure(): Collection<Range<Int>>
}
