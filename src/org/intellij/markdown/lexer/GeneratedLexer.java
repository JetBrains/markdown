package org.intellij.markdown.lexer;

import org.intellij.markdown.IElementType;

public interface GeneratedLexer {
    void reset(CharSequence buffer, int start, int end,int initialState);

    IElementType advance() throws java.io.IOException;

    int getTokenStart();

    int getTokenEnd();

}
