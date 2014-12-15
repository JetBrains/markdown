package org.intellij.markdown;

import org.intellij.markdown.parser.dialects.commonmark.CommonMarkMarkerProcessor;

public class MarkdownParsingTest extends BaseParsingTest {

    @Override
    protected CommonMarkMarkerProcessor.object.Factory getMarkerProcessorFactory() {
        return new CommonMarkMarkerProcessor.object.Factory();
    }

    public void testEmpty() {
        assertEquals("Markdown:MARKDOWN_FILE", getParsedTreeText(""));
    }

    public void testSmall1() {
        assertEquals("Markdown:MARKDOWN_FILE\n  Markdown:EOL('\\n')",
                getParsedTreeText("\n"));
    }

    public void testSmall2() {
        assertEquals(
                "Markdown:MARKDOWN_FILE\n" +
                "  Markdown:PARAGRAPH\n" +
                "    Markdown:TEXT('test')",
                getParsedTreeText("test"));
    }

    public void testSmall3() {
        assertEquals(
                "Markdown:MARKDOWN_FILE\n" +
                "  Markdown:PARAGRAPH\n" +
                "    Markdown:EMPH\n" +
                "      Markdown:EMPH('*')\n" +
                "      Markdown:TEXT('test')\n" +
                "      Markdown:EMPH('*')",
                getParsedTreeText("*test*"));
    }

    public void testSimple() {
        defaultTest();
    }

    public void testCodeBlocks() {
        defaultTest();
    }

    public void testUnorderedLists() {
        defaultTest();
    }

    public void testOrderedLists() {
        defaultTest();
    }

    public void testBlockquotes() {
        defaultTest();
    }

    public void testHeaders() {
        defaultTest();
    }

    public void testHtmlBlocks() {
        defaultTest();
    }

    public void testEmphStrong() {
        defaultTest();
    }

    public void testCodeFence() {
        defaultTest();
    }

    public void testCodeSpan() {
        defaultTest();
    }

    public void testLinkDefinitions() {
        defaultTest();
    }

    public void testInlineLinks() {
        defaultTest();
    }

    public void testReferenceLinks() {
        defaultTest();
    }

}
