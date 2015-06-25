package org.intellij.markdown.parser

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode

public class MarkdownParser(private val markerProcessorFactory: MarkerProcessorFactory) {

    public fun buildMarkdownTreeFromString(text: String): ASTNode {
        return parse(MarkdownElementTypes.MARKDOWN_FILE, text)
    }

    public fun parse(root: IElementType, text: String): ASTNode {
        val productionHolder = ProductionHolder()
        val markerProcessor = markerProcessorFactory.createMarkerProcessor(productionHolder)

        val rootMarker = productionHolder.mark()

        val textHolder = LookaheadText(text)
        var pos: LookaheadText.Position? = textHolder.startPosition
        productionHolder.updatePosition(pos?.offset ?: text.length())
        while (pos != null) {
            pos = markerProcessor.processToken(pos)
            productionHolder.updatePosition(pos?.offset ?: text.length())
        }
        markerProcessor.flushMarkers()

        rootMarker.done(root)

        val builder = MyRawBuilder()

        return builder.buildTree(productionHolder.production)
    }

}
