package org.intellij.markdown;

import org.intellij.markdown.ast.ASTNode;
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor;
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.markdown.parser.MarkdownParser;

import java.io.PrintWriter;
import java.util.Scanner;

public class SpecRunner {
    public static void main(String[] args) {
        final String content = new Scanner(System.in).useDelimiter("\\Z").next();

        final MarkdownFlavourDescriptor flavour = new CommonMarkFlavourDescriptor();
        final ASTNode tree = new MarkdownParser(flavour)
                .buildMarkdownTreeFromString(content);
        final String html = new HtmlGenerator(content, tree, flavour, false)
                .generateHtml();
        final String htmlWithoutBody = html.substring("<body>".length(), html.length() - "</body>".length());

        final PrintWriter out = new PrintWriter(System.out);
//        out.write(HtmlGeneratorTest.Companion.formatHtmlForTests(htmlWithoutBody));
        out.write(htmlWithoutBody);
        out.close();
    }
}
