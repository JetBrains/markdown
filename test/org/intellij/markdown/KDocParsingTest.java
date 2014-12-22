package org.intellij.markdown;

import org.intellij.markdown.parser.MarkerProcessorFactory;
import org.intellij.markdown.parser.dialects.KDocMarkerProcessor;

public class KDocParsingTest extends BaseParsingTest {
    @Override
    protected MarkerProcessorFactory getMarkerProcessorFactory() {
        return new KDocMarkerProcessor.object.Factory();
    }

    public void testMultipleSections() {
        defaultTest();
    }

    public void testSimple() {
        defaultTest();
    }

    public void testDotSections() {
        defaultTest();
    }

    public void testDirective() {
        defaultTest();
    }

    public void testEmptyAnonymous() {
        defaultTest();
    }

    public void testSpecialSections() {
        defaultTest();
    }

    public void testSmall() {
        assertEquals(
                "Markdown:MARKDOWN_FILE\n" +
                "  Markdown:SECTION\n" +
                "    Markdown:PARAGRAPH\n" +
                "      Markdown:TEXT('Summary')\n" +
                "  Markdown:EOL('\\n')\n" +
                "  WHITE_SPACE(' ')\n" +
                "  Markdown:SECTION\n" +
                "    Markdown:SECTION_ID('$one')\n" +
                "    Markdown::(':')\n" +
                "    WHITE_SPACE(' ')\n" +
                "    Markdown:PARAGRAPH\n" +
                "      Markdown:TEXT('section one')",
                getParsedTreeText("Summary\n" +
                                  " $one: section one"));
    }

    @Override
    protected String getTestDataPath() {
        return super.getTestDataPath() + "/kdoc";
    }
}
