package org.intellij.markdown.flavours

import org.intellij.markdown.IElementType
import org.intellij.markdown.html.GeneratingProvider
import org.intellij.markdown.lexer.MarkdownLexer
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkerProcessorFactory
import org.intellij.markdown.parser.sequentialparsers.SequentialParserManager
import java.io.File

public interface MarkdownFlavourDescriptor {
    val markerProcessorFactory: MarkerProcessorFactory

    val sequentialParserManager: SequentialParserManager

    fun createInlinesLexer(): MarkdownLexer

    fun createHtmlGeneratingProviders(linkMap: LinkMap, documentBase: File?): Map<IElementType, GeneratingProvider>
}