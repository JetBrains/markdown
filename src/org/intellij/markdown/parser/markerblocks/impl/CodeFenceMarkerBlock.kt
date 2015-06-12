package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.TokensCache
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl

public class CodeFenceMarkerBlock(myConstraints: MarkdownConstraints, marker: ProductionHolder.Marker) : MarkerBlockImpl(myConstraints, marker) {

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(tokenType: IElementType, iterator: TokensCache.Iterator, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (tokenType == MarkdownTokenTypes.CODE_FENCE_END) {
            return MarkerBlock.ProcessingResult(MarkerBlock.ClosingAction.DEFAULT, MarkerBlock.ClosingAction.DONE, MarkerBlock.EventAction.CANCEL).postpone()
        }
        if (tokenType == MarkdownTokenTypes.EOL) {
            val nextLineConstraints = MarkdownConstraints.fromBase(iterator, 1, currentConstraints)
            if (!nextLineConstraints.extendsPrev(currentConstraints)) {
                return MarkerBlock.ProcessingResult.DEFAULT
            }
        }

        return MarkerBlock.ProcessingResult.CANCEL
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownElementTypes.CODE_FENCE
    }
}
