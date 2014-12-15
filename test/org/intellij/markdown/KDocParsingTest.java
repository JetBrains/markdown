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

    @Override
    protected String getTestDataPath() {
        return super.getTestDataPath() + "/kdoc";
    }
}
