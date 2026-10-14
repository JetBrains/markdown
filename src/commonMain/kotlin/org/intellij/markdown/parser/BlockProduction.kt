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
): List<MarkerBlock> {
    val markerProcessor = flavour.markerProcessorFactory.createMarkerProcessor(productionHolder)

    val rootMarker = productionHolder.mark()

    val textHolder = LookaheadText(text)
    var pos: LookaheadText.Position? = textHolder.startPosition
    while (pos != null) {
        cancellationToken.checkCancelled()
        productionHolder.updatePosition(pos.offset)
        pos = markerProcessor.processPosition(pos)
    }

    // Take a snapshot before flush
    val openMarkers = markerProcessor.markersStackSnapshot

    productionHolder.updatePosition(text.length)
    markerProcessor.flushMarkers()

    rootMarker.done(root)

    return openMarkers
}
