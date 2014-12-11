package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.MarkdownConstraints
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.TokensCache
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl
import org.intellij.markdown.parser.markerblocks.MarkerBlock

public class CodeFenceMarkerBlock(myConstraints: MarkdownConstraints, marker: ProductionHolder.Marker) : MarkerBlockImpl(myConstraints, marker) {

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(tokenType: IElementType, iterator: TokensCache.Iterator, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (tokenType == MarkdownTokenTypes.CODE_FENCE_END) {
            return MarkerBlock.ProcessingResult(MarkerBlock.ClosingAction.DEFAULT, MarkerBlock.ClosingAction.DONE, MarkerBlock.EventAction.CANCEL).postpone()
        }
        // Allow top-level blocks to interrupt this one
        if (tokenType == MarkdownTokenTypes.EOL) {
            return MarkerBlock.ProcessingResult.PASS
        }

        return MarkerBlock.ProcessingResult.CANCEL
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownElementTypes.CODE_FENCE
    }
}
