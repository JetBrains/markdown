package org.intellij.markdown.parser.dialects

import org.intellij.markdown.parser.dialects.commonmark.CommonMarkMarkerProcessor
import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.TokensCache
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.markerblocks.impl.KDocSectionMarkerBlock
import org.intellij.markdown.parser.MarkerProcessorFactory
import org.intellij.markdown.parser.MarkerProcessor
import java.util.ArrayList

public class KDocMarkerProcessor(productionHolder: ProductionHolder, tokensCache: TokensCache)
        : CommonMarkMarkerProcessor(productionHolder, tokensCache) {

    private var delegateRestrictingBound: Int = 0

    override fun createNewMarkerBlocks(tokenType: IElementType, iterator: TokensCache.Iterator, productionHolder: ProductionHolder): Array<MarkerBlock> {
        val myList = ArrayList<MarkerBlock>()
        if (iterator.index == 0 ||
                tokenType == MarkdownTokenTypes.SECTION_ID
                && iterator.advance().type == MarkdownTokenTypes.COLON
                && CommonMarkMarkerProcessor.isAtLineStart(iterator)) {

            myList.add(KDocSectionMarkerBlock(currentConstraints, productionHolder.mark()))
            // Let's restrict the creation of anything while passing the section id
            if (tokenType == MarkdownTokenTypes.SECTION_ID && iterator.advance().type == MarkdownTokenTypes.COLON) {
                delegateRestrictingBound = iterator.index + 1
            }
        }

        if (iterator.index > delegateRestrictingBound) {
            myList.addAll(super.createNewMarkerBlocks(tokenType, iterator, productionHolder))
        }

        return myList.copyToArray()
    }

    class object {
        public class Factory : MarkerProcessorFactory {
            override fun createMarkerProcessor(productionHolder: ProductionHolder, tokensCache: TokensCache): MarkerProcessor {
                return KDocMarkerProcessor(productionHolder, tokensCache)
            }

        }
    }
}