package org.intellij.markdown.parser.markerblocks

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import java.util.*


abstract class InlineStructureHoldingMarkerBlock(
        constraints: MarkdownConstraints,
        protected val productionHolder: ProductionHolder)
: MarkerBlockImpl(constraints, productionHolder.mark()) {

    override fun acceptAction(action: MarkerBlock.ClosingAction): Boolean {
        if (action != MarkerBlock.ClosingAction.NOTHING) {
            if (action == MarkerBlock.ClosingAction.DONE
                    || action == MarkerBlock.ClosingAction.DEFAULT
                    && getDefaultAction() == MarkerBlock.ClosingAction.DONE) {
                for (range in getRangesContainingInlineStructure()) {
                    productionHolder.addProduction(
                            Collections.singletonList(SequentialParser.Node(range, MarkdownElementTypes.ATX_1)))
                }
            }
        }

        return super.acceptAction(action)
    }

    abstract fun getRangesContainingInlineStructure(): Collection<IntRange>
}
