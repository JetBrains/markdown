package org.intellij.markdown.parser

import org.intellij.markdown.IElementType
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.parser.markerblocks.MarkerBlock

/**
 * Fills [productionHolder] with the blocks of [text], and returns the blocks that are still open at the end of
 * [text]. The step finds the block boundaries only. It builds no [ASTNode], and it parses no inline content.
 */
fun collectBlockProduction(
    flavour: MarkdownFlavourDescriptor,
    root: IElementType,
    text: CharSequence,
    productionHolder: ProductionHolder,
    cancellationToken: CancellationToken
): List<MarkerBlock> = collectBlockProduction(flavour, root, text, productionHolder, 0, false, cancellationToken)

internal fun collectBlockProduction(
    flavour: MarkdownFlavourDescriptor,
    root: IElementType,
    text: CharSequence,
    productionHolder: ProductionHolder,
    startOffset: Int,
    stopAtNextBlock: Boolean,
    cancellationToken: CancellationToken,
): List<MarkerBlock> {
    val markerProcessor = flavour.markerProcessorFactory.createMarkerProcessor(productionHolder)

    productionHolder.updatePosition(startOffset)
    val rootMarker = productionHolder.mark()

    val textHolder = LookaheadText(text, startOffset)
    var pos: LookaheadText.Position? = textHolder.startPosition
    while (pos != null) {
        cancellationToken.checkCancelled()
        if (stopAtNextBlock && pos.offset > startOffset && pos.offsetInCurrentLine == 0 && !markerProcessor.hasOpenBlocks) {
            break
        }
        productionHolder.updatePosition(pos.offset)
        pos = markerProcessor.processPosition(pos)
    }

    // Take a snapshot before flush
    val openMarkers = markerProcessor.markersStackSnapshot

    productionHolder.updatePosition(pos?.offset ?: text.length)
    markerProcessor.flushMarkers()

    rootMarker.done(root)

    return openMarkers
}
