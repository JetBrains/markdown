package org.intellij.markdown;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.UsefulTestCase;
import org.intellij.markdown.ast.ASTNode;
import org.intellij.markdown.ast.LeafASTNode;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.markdown.parser.MarkerProcessorFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public abstract class BaseParsingTest extends UsefulTestCase {
    protected String getTestDataPath() {
        return new File("test/data/parser").getAbsolutePath();
    }

    protected void defaultTest() {
        String src;
        try {
            src = FileUtil.loadFile(new File(getTestDataPath() + "/" + getTestName(true) + ".md")).trim();
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError("failed to read src");
        }

        String result = getParsedTreeText(src);

        assertSameLinesWithFile(getTestDataPath() + "/" + getTestName(false) + ".txt", result);
    }

    @NotNull
    protected String getParsedTreeText(@NotNull String inputText) {
        ASTNode tree = new MarkdownParser(getMarkerProcessorFactory()).buildMarkdownTreeFromString(inputText);
        return treeToStr(inputText, tree);
    }

    protected abstract MarkerProcessorFactory getMarkerProcessorFactory();

    protected String treeToStr(String src, @NotNull ASTNode tree) {
        return treeToStr(src, tree, new StringBuilder(), 0).toString();
    }

    private static StringBuilder treeToStr(String src, @NotNull ASTNode tree, @NotNull StringBuilder sb, int depth) {
        if (sb.length() > 0) {
            sb.append('\n');
        }
        for (int i = 0; i < depth * 2; ++i) {
            sb.append(' ');
        }

        sb.append(tree.getType().toString());
        if (tree instanceof LeafASTNode) {
            final String str = src.substring(tree.getStartOffset(), tree.getEndOffset());
            sb.append("('").append(str.replaceAll("\\n", "\\\\n")).append("')");
        }
        for (ASTNode child : tree.getChildren()) {
            treeToStr(src, child, sb, depth + 1);
        }

        return sb;
    }
}
