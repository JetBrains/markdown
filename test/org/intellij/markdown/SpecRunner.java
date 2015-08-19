package org.intellij.markdown;

import org.intellij.markdown.ast.ASTNode;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.markdown.parser.LinkMap;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.markdown.parser.dialects.commonmark.CommonMarkMarkerProcessor;

import java.io.PrintWriter;
import java.util.Scanner;

public class SpecRunner {
    public static void main(String[] args) {
        final String content = new Scanner(System.in).useDelimiter("\\Z").next();
        final ASTNode tree = new MarkdownParser(CommonMarkMarkerProcessor.Factory.INSTANCE$)
                .buildMarkdownTreeFromString(content);
        final String html = new HtmlGenerator(content, tree, LinkMap.Builder.buildLinkMap(tree, content)).generateHtml();
        final String htmlWithoutBody = html.substring("<body>".length(), html.length() - "</body>".length());

        final PrintWriter out = new PrintWriter(System.out);
//        out.write(HtmlGeneratorTest.Companion.formatHtmlForTests(htmlWithoutBody));
        out.write(htmlWithoutBody);
        out.close();
    }
}
