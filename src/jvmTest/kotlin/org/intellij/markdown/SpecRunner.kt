package org.intellij.markdown

import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import org.intellij.markdown.html.HtmlGenerator
import java.io.PrintWriter
import java.util.*
import kotlin.jvm.JvmStatic

object SpecRunner {
    @JvmStatic
    fun main(args: Array<String>) {
        val content: String = Scanner(System.`in`).useDelimiter("\\Z").next()
        val flavour: MarkdownFlavourDescriptor = CommonMarkFlavourDescriptor()
        val tree = MarkdownParser(flavour)
            .buildMarkdownTreeFromString(content)
        val html = HtmlGenerator(content, tree, flavour, false)
            .generateHtml()
        val htmlWithoutBody = html.substring("<body>".length, html.length - "</body>".length)
        val out = PrintWriter(System.out)
        //        out.write(HtmlGeneratorTest.Companion.formatHtmlForTests(htmlWithoutBody));
        out.write(htmlWithoutBody)
        out.close()
    }
}