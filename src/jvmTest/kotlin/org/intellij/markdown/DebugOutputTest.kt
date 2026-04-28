package org.intellij.markdown

import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.table.GitHubTableMarkerBlock
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import org.junit.Test
import java.io.File

class DebugOutputTest {

    @Test
    fun debugTests() {
        val output = StringBuilder()
        val flavour = GFMFlavourDescriptor()
        val lexerFactory = { flavour.createInlinesLexer() }

        // Test 1: Code span with pipes
        output.appendLine("\n=== Test 1: Code span ===")
        val md1 = "| code | description |\n|------|-------------|\n| `a || b` | Either a or b |"
        val row1 = "| `a || b` | Either a or b |"
        val cells1 = GitHubTableMarkerBlock.splitByPipes(row1, lexerFactory)
        output.appendLine("Split result: $cells1")
        val tree1 = MarkdownParser(flavour).buildMarkdownTreeFromString(md1)
        val html1 = HtmlGenerator(md1, tree1, flavour, false).generateHtml()
        val clean1 = html1.replace("<body>", "").replace("</body>", "").trim()
        output.appendLine("Generated HTML:")
        output.appendLine(clean1)
        val expected1 = "<table><thead><tr><th>code</th><th>description</th></tr></thead><tbody><tr><td><code>a || b</code></td><td>Either a or b</td></tr></tbody></table>"
        output.appendLine("Expected HTML:")
        output.appendLine(expected1)
        output.appendLine("Match: ${clean1 == expected1}")

        // Test 2: Escaped backticks
        output.appendLine("\n=== Test 2: Escaped backticks ===")
        val md2 = "| code | description |\n|------|-------------|\n| \\`code\\` | Escaped backticks |"
        val row2 = "| \\`code\\` | Escaped backticks |"
        val cells2 = GitHubTableMarkerBlock.splitByPipes(row2, lexerFactory)
        output.appendLine("Split result: $cells2")
        val tree2 = MarkdownParser(flavour).buildMarkdownTreeFromString(md2)
        val html2 = HtmlGenerator(md2, tree2, flavour, false).generateHtml()
        val clean2 = html2.replace("<body>", "").replace("</body>", "").trim()
        output.appendLine("Generated HTML:")
        output.appendLine(clean2)
        val expected2 = "<table><thead><tr><th>code</th><th>description</th></tr></thead><tbody><tr><td>`code`</td><td>Escaped backticks</td></tr></tbody></table>"
        output.appendLine("Expected HTML:")
        output.appendLine(expected2)
        output.appendLine("Match: ${clean2 == expected2}")

        // Test 3: Link with pipe
        output.appendLine("\n=== Test 3: Link with pipe ===")
        val md3 = "| link | description |\n|------|-------------|\n| [example](https://example.com?a=1\\|2) | Link with pipe |"
        val row3 = "| [example](https://example.com?a=1\\|2) | Link with pipe |"
        val cells3 = GitHubTableMarkerBlock.splitByPipes(row3, lexerFactory)
        output.appendLine("Split result: $cells3")
        val tree3 = MarkdownParser(flavour).buildMarkdownTreeFromString(md3)
        val html3 = HtmlGenerator(md3, tree3, flavour, false).generateHtml()
        val clean3 = html3.replace("<body>", "").replace("</body>", "").trim()
        output.appendLine("Generated HTML:")
        output.appendLine(clean3)
        val expected3 = "<table><thead><tr><th>link</th><th>description</th></tr></thead><tbody><tr><td><a href=\"https://example.com?a=1|2\">example</a></td><td>Link with pipe</td></tr></tbody></table>"
        output.appendLine("Expected HTML:")
        output.appendLine(expected3)
        output.appendLine("Match: ${clean3 == expected3}")

        // Write to file
        val outputText = output.toString()
        println(outputText)
        File("test-output.txt").writeText(outputText)
        println("\nOutput written to test-output.txt")
    }
}
