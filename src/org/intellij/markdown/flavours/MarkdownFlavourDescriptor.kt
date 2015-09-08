package org.intellij.markdown.flavours

import org.intellij.markdown.IElementType
import org.intellij.markdown.html.GeneratingProvider
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkerProcessorFactory
import org.intellij.markdown.parser.sequentialparsers.SequentialParserManager

public interface MarkdownFlavourDescriptor {
    val markerProcessorFactory: MarkerProcessorFactory

    val sequentialParserManager: SequentialParserManager

    fun createHtmlGeneratingProviders(linkMap: LinkMap): Map<IElementType, GeneratingProvider>
}