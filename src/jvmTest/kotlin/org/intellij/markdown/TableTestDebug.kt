package org.intellij.markdown

import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.table.GitHubTableMarkerBlock
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import org.junit.Test

class TableTestDebug {
    
    private fun testMarkdown(name: String, markdown: String, expectedHtml: String) {
        println("\n=== Test: $name ===")
        println("Markdown:\n$markdown\n")
        
        val flavour = GFMFlavourDescriptor()
        val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)
        val html = HtmlGenerator(markdown, tree, flavour, false).generateHtml()
        
        println("Generated HTML:")
        println(html)
        println("\nExpected HTML:")
        println(expectedHtml)
        
        val normalizedGenerated = html.replace(Regex("<body>|</body>"), "").trim()
        val normalizedExpected = expectedHtml.trim()
        
        println("\nMatch: ${normalizedGenerated == normalizedExpected}")
        if (normalizedGenerated != normalizedExpected) {
            println("\nDifference:")
            println("Generated length: ${normalizedGenerated.length}")
            println("Expected length: ${normalizedExpected.length}")
        }
    }
    
    @Test
    fun testAllCases() {
        testMarkdown(
            "Pipe in code span",
            "| code | description |\n|------|-------------|\n| `a || b` | Either a or b |",
            "<table><thead><tr><th>code</th><th>description</th></tr></thead><tbody><tr><td><code>a || b</code></td><td>Either a or b</td></tr></tbody></table>"
        )
        
        testMarkdown(
            "Escaped backticks",
            "| code | description |\n|------|-------------|\n| \\`code\\` | Escaped backticks |",
            "<table><thead><tr><th>code</th><th>description</th></tr></thead><tbody><tr><td>`code`</td><td>Escaped backticks</td></tr></tbody></table>"
        )
        
        testMarkdown(
            "Link with pipe in URL",
            "| link | description |\n|------|-------------|\n| [example](https://example.com?a=1\\|2) | Link with pipe |",
            "<table><thead><tr><th>link</th><th>description</th></tr></thead><tbody><tr><td><a href=\"https://example.com?a=1|2\">example</a></td><td>Link with pipe</td></tr></tbody></table>"
        )
        
        // Also test the split function directly
        println("\n\n=== Direct split tests ===")
        val flavour = GFMFlavourDescriptor()
        val lexerFactory = { flavour.createInlinesLexer() }
        
        val testCases = listOf(
            "| `a || b` | desc |" to listOf("", " `a || b` ", " desc ", ""),
            "| \\`code\\` | desc |" to listOf("", " \\`code\\` ", " desc ", ""),
            "| [example](url\\|with) | desc |" to listOf("", " [example](url\\|with) ", " desc ", "")
        )
        
        testCases.forEach { (input, expected) ->
            val result = GitHubTableMarkerBlock.splitByPipes(input, lexerFactory)
            println("\nInput: $input")
            println("Expected: $expected")
            println("Got:      $result")
            println("Match: ${result == expected}")
        }
    }
}
