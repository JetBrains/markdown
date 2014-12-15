package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.TokensCache
import org.intellij.markdown.parser.MarkdownConstraints
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.MarkdownElementTypes

public class KDocSectionMarkerBlock(constraints: MarkdownConstraints, marker: ProductionHolder.Marker)
        : MarkerBlockImpl(constraints, marker, setOf(MarkdownTokenTypes.EOL)) {

    override fun doProcessToken(tokenType: IElementType, iterator: TokensCache.Iterator, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        assert(tokenType == MarkdownTokenTypes.EOL);

        var afterEol: IElementType? = iterator.advance().type
        if (afterEol == MarkdownTokenTypes.SECTION_ID && iterator.advance().advance().type == MarkdownTokenTypes.COLON) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        return MarkerBlock.ProcessingResult.PASS
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownElementTypes.SECTION
    }

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

}

