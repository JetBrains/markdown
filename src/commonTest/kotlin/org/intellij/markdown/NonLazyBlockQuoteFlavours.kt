package org.intellij.markdown

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkMarkerProcessor
import org.intellij.markdown.flavours.gfm.GFMConstraints
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.alert.GitHubAlertMarkerProvider
import org.intellij.markdown.flavours.gfm.table.GitHubTableMarkerProvider
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.MarkerProcessorFactory
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.CommonMarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.providers.BlockQuoteProvider

/**
 * A marker processor with the CommonMark providers where [BlockQuoteProvider] does not allow lazy continuation lines,
 * surrounded by [first] and [last] providers.
 */
private class NonLazyBlockQuoteMarkerProcessor(
    productionHolder: ProductionHolder,
    constraintsBase: CommonMarkdownConstraints,
    first: List<MarkerBlockProvider<StateInfo>> = emptyList(),
    last: List<MarkerBlockProvider<StateInfo>> = emptyList()
) : CommonMarkMarkerProcessor(productionHolder, constraintsBase) {
    private val markerBlockProviders = first + super.getMarkerBlockProviders().map {
        if (it is BlockQuoteProvider) BlockQuoteProvider(lazyContinuation = false) else it
    } + last

    override fun getMarkerBlockProviders(): List<MarkerBlockProvider<StateInfo>> = markerBlockProviders
}

/**
 * The CommonMark flavour that ends a block quote at a line without the `>` marker instead of continuing its paragraph.
 */
class NonLazyBlockQuoteCommonMarkFlavour : CommonMarkFlavourDescriptor() {
    override val markerProcessorFactory: MarkerProcessorFactory = object : MarkerProcessorFactory {
        override fun createMarkerProcessor(productionHolder: ProductionHolder): MarkerProcessor<*> {
            return NonLazyBlockQuoteMarkerProcessor(productionHolder, CommonMarkdownConstraints.BASE)
        }
    }
}

/**
 * The GFM flavour that ends a block quote at a line without the `>` marker instead of continuing its paragraph.
 * Its marker processor has the providers of [org.intellij.markdown.flavours.gfm.GFMMarkerProcessor].
 */
class NonLazyBlockQuoteGfmFlavour : GFMFlavourDescriptor() {
    override val markerProcessorFactory: MarkerProcessorFactory = object : MarkerProcessorFactory {
        override fun createMarkerProcessor(productionHolder: ProductionHolder): MarkerProcessor<*> {
            return NonLazyBlockQuoteMarkerProcessor(
                productionHolder,
                GFMConstraints.BASE,
                first = listOf(GitHubAlertMarkerProvider()),
                last = listOf(GitHubTableMarkerProvider())
            )
        }
    }
}
