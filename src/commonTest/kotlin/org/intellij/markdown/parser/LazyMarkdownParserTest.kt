package org.intellij.markdown.parser

import org.intellij.markdown.ExperimentalApi
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownParsingException
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.CompositeASTNode
import org.intellij.markdown.ast.LazyASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.ast.accept
import org.intellij.markdown.ast.visitors.RecursiveVisitor
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.lexer.MarkdownLexer
import org.intellij.markdown.parser.LookaheadText.Position
import org.intellij.markdown.parser.constraints.CommonMarkdownConstraints
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserManager
import org.intellij.markdown.parser.sequentialparsers.TokensCache
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
class LazyMarkdownParserTest {
    @Test
    fun lazyNodeParsesChildrenOnceAndLinksParents() {
        var parseCount = 0
        val child = LeafASTNode(MarkdownTokenTypes.TEXT, 4, 8)
        val lazyNode = LazyASTNode(MarkdownElementTypes.PARAGRAPH, 2, 10) {
            parseCount++
            listOf(child)
        }
        val root = CompositeASTNode(MarkdownElementTypes.MARKDOWN_FILE, listOf(lazyNode))

        assertEquals(0, parseCount)
        assertEquals(2, lazyNode.startOffset)
        assertEquals(10, lazyNode.endOffset)
        assertSame(root, lazyNode.parent)

        val children = lazyNode.children
        assertEquals(1, parseCount)
        assertSame(children, lazyNode.children)
        assertSame(lazyNode, children.single().parent)
    }

    @Test
    fun parserDoesNotCreateInlineLexerUntilLazyNodeIsAccessed() {
        val flavour = CountingFlavour()
        val tree = MarkdownParser(flavour).parseWithLazyInlines(
            MarkdownElementTypes.MARKDOWN_FILE,
            "first **one**\n\nsecond *two*"
        )
        val paragraphs = tree.children.filterIsInstance<LazyASTNode>()

        assertEquals(2, paragraphs.size)
        assertEquals(0, flavour.inlineLexerCreations)

        val firstChildren = paragraphs[0].children
        assertEquals(1, flavour.inlineLexerCreations)
        assertTrue(firstChildren.any { it.type == MarkdownElementTypes.STRONG })
        assertSame(firstChildren, paragraphs[0].children)
        assertEquals(1, flavour.inlineLexerCreations)

        paragraphs[1].children
        assertEquals(2, flavour.inlineLexerCreations)
    }

    @Test
    fun parserCreatesLazyNodesForInlineBearingBlockKinds() {
        val text = "# ATX **bold**\n\nSetext _emphasis_\n=======\n\n| a | b |\n| --- | --- |\n| **x** ||"
        val tree = MarkdownParser(GFMFlavourDescriptor()).parseWithLazyInlines(
            MarkdownElementTypes.MARKDOWN_FILE,
            text
        )

        val atx = tree.children.first { it.type == MarkdownElementTypes.ATX_1 }
        assertTrue(atx.children.any { it.type == MarkdownTokenTypes.ATX_CONTENT && it is LazyASTNode })

        val setext = tree.children.first { it.type == MarkdownElementTypes.SETEXT_1 }
        assertTrue(setext.children.any { it.type == MarkdownTokenTypes.SETEXT_CONTENT && it is LazyASTNode })

        val table = tree.children.first { it.type == GFMElementTypes.TABLE }
        val cells = table.children.flatMap { row -> row.children.filter { it.type == GFMTokenTypes.CELL } }
        assertTrue(cells.isNotEmpty())
        assertTrue(cells.all { it is LazyASTNode })

        val emptyCell = cells.last() as LazyASTNode
        assertEquals(emptyCell.startOffset, emptyCell.endOffset)
        assertTrue(emptyCell.children.isEmpty())
    }

    @Test
    fun lazyExpansionMatchesEagerExpansion() {
        assertTreesMatch("A **paragraph** with _emphasis_, `code`, and [a link](url)")
        assertTreesMatch("# heading with [a link](url)")
        assertTreesMatch("Setext _heading_\n===============")
        assertTreesMatch("| a | b |\n| --- | --- |\n| **x** | `y` |")
    }

    @Test
    fun lazyExpansionPreservesEmptyTableCellChildren() {
        val text = "| a | b |\n| --- | --- |\n| **x** ||"
        val eager = MarkdownParser(GFMFlavourDescriptor()).parse(
            MarkdownElementTypes.MARKDOWN_FILE,
            text,
            parseInlines = true
        )
        val lazy = MarkdownParser(GFMFlavourDescriptor()).parseWithLazyInlines(
            MarkdownElementTypes.MARKDOWN_FILE,
            text
        )

        val eagerCells = findCells(eager)
        val lazyCells = findCells(lazy)
        assertEquals(eagerCells.map { it.children.map { child -> child.toComparableTree() } }, lazyCells.map { it.children.map { child -> child.toComparableTree() } })
        assertEquals(eagerCells.last().children.size, lazyCells.last().children.size)
        assertTrue(lazyCells.last().startOffset > 0)
        assertEquals(lazyCells.last().startOffset, lazyCells.last().endOffset)
    }

    @Test
    fun recursiveVisitorExpandsLazyNodes() {
        val flavour = CountingFlavour()
        val tree = MarkdownParser(flavour).parseWithLazyInlines(
            MarkdownElementTypes.MARKDOWN_FILE,
            "a **strong** paragraph"
        )
        val visitedTypes = ArrayList<IElementType>()

        tree.accept(object : RecursiveVisitor() {
            override fun visitNode(node: ASTNode) {
                visitedTypes.add(node.type)
                super.visitNode(node)
            }
        })

        assertEquals(1, flavour.inlineLexerCreations)
        assertTrue(visitedTypes.contains(MarkdownElementTypes.STRONG))
    }

    @Test
    fun lazyNodePreservesBaseOffsetAndParentLinks() {
        val text = "hello **world**"
        val baseOffset = 17
        val tree = MarkdownParser(GFMFlavourDescriptor()).parseWithLazyInlines(
            MarkdownElementTypes.MARKDOWN_FILE,
            text,
            baseOffset
        )
        val paragraph = tree.children.filterIsInstance<LazyASTNode>().single()

        assertEquals(baseOffset, paragraph.startOffset)
        assertEquals(baseOffset + text.length, paragraph.endOffset)
        assertSame(tree, paragraph.parent)

        val strong = paragraph.children.first { it.type == MarkdownElementTypes.STRONG }
        assertSame(paragraph, strong.parent)
    }

    @Test
    fun deferredInlineParsingChecksCancellation() {
        var cancelled = false
        val parser = MarkdownParser(
            GFMFlavourDescriptor(),
            cancellationToken = CancellationToken {
                if (cancelled) {
                    throw TestCancellationException()
                }
            }
        )
        val tree = parser.parseWithLazyInlines(MarkdownElementTypes.MARKDOWN_FILE, "**text**")
        val paragraph = tree.children.filterIsInstance<LazyASTNode>().single()

        cancelled = true
        assertFailsWith<TestCancellationException> {
            paragraph.children
        }
    }

    @Test
    fun existingEagerAndBlockOnlyModesKeepTheirSemantics() {
        val text = "A **strong** paragraph"
        val parser = MarkdownParser(GFMFlavourDescriptor())

        val eager = parser.parse(MarkdownElementTypes.MARKDOWN_FILE, text, parseInlines = true)
        assertFalse(eager.containsLazyNode())
        assertTrue(eager.containsNodeOfType(MarkdownElementTypes.STRONG))

        val blockOnly = parser.parse(MarkdownElementTypes.MARKDOWN_FILE, text, parseInlines = false)
        assertFalse(blockOnly.containsNodeOfType(MarkdownElementTypes.STRONG))
    }

    @Test
    fun lazyModeKeepsTopLevelFallbackWhenAssertionsAreDisabled() {
        val parser = MarkdownParser(InvalidBlockFlavour(), assertionsEnabled = false)

        val tree = parser.parseWithLazyInlines(MarkdownElementTypes.MARKDOWN_FILE, "text")

        assertEquals(MarkdownElementTypes.MARKDOWN_FILE, tree.type)
        assertEquals(listOf(MarkdownElementTypes.PARAGRAPH), tree.children.map { it.type })
        assertFalse(tree.children.single() is LazyASTNode)
        assertEquals(MarkdownTokenTypes.TEXT, tree.children.single().children.single().type)
    }

    @Test
    fun deferredInlineParsingKeepsInlineFallbackWhenAssertionsAreDisabled() {
        val parser = MarkdownParser(InvalidInlineFlavour(), assertionsEnabled = false)
        val tree = parser.parseWithLazyInlines(MarkdownElementTypes.MARKDOWN_FILE, "text")
        val paragraph = tree.children.filterIsInstance<LazyASTNode>().single()

        assertEquals(listOf(MarkdownTokenTypes.TEXT), paragraph.children.map { it.type })
    }

    @Test
    fun lazyModeStillThrowsWithAssertionsEnabled() {
        assertFailsWith<MarkdownParsingException> {
            MarkdownParser(InvalidBlockFlavour()).parseWithLazyInlines(
                MarkdownElementTypes.MARKDOWN_FILE,
                "text"
            )
        }
    }

    private fun assertTreesMatch(text: String) {
        val flavour = GFMFlavourDescriptor()
        val eager = MarkdownParser(flavour).parse(MarkdownElementTypes.MARKDOWN_FILE, text, parseInlines = true)
        val lazy = MarkdownParser(flavour).parseWithLazyInlines(MarkdownElementTypes.MARKDOWN_FILE, text)
        assertEquals(eager.toComparableTree(), lazy.toComparableTree(), text)
    }

    private fun findCells(root: ASTNode): List<ASTNode> {
        return root.children
            .first { it.type == GFMElementTypes.TABLE }
            .children
            .flatMap { row -> row.children.filter { it.type == GFMTokenTypes.CELL } }
    }

    private fun ASTNode.containsNodeOfType(type: IElementType): Boolean {
        return this.type == type || children.any { it.containsNodeOfType(type) }
    }

    private fun ASTNode.containsLazyNode(): Boolean {
        return this is LazyASTNode || children.any { it.containsLazyNode() }
    }

    private fun ASTNode.toComparableTree(): ComparableNode {
        return ComparableNode(type, startOffset, endOffset, children.map { it.toComparableTree() })
    }

    private data class ComparableNode(
        val type: IElementType,
        val startOffset: Int,
        val endOffset: Int,
        val children: List<ComparableNode>
    )

    private class CountingFlavour : GFMFlavourDescriptor() {
        var inlineLexerCreations = 0

        override fun createInlinesLexer(): MarkdownLexer {
            inlineLexerCreations++
            return super.createInlinesLexer()
        }
    }

    private class InvalidBlockFlavour : GFMFlavourDescriptor() {
        override val markerProcessorFactory: MarkerProcessorFactory = object : MarkerProcessorFactory {
            override fun createMarkerProcessor(productionHolder: ProductionHolder): MarkerProcessor<*> {
                return InvalidBlockMarkerProcessor(productionHolder)
            }
        }
    }

    private class InvalidBlockMarkerProcessor(
        productionHolder: ProductionHolder
    ) : MarkerProcessor<MarkerProcessor.StateInfo>(productionHolder, CommonMarkdownConstraints.BASE) {
        override val stateInfo: StateInfo = StateInfo(
            startConstraints,
            startConstraints,
            emptyList()
        )

        override fun getMarkerBlockProviders(): List<MarkerBlockProvider<StateInfo>> = emptyList()

        override fun updateStateInfo(pos: Position) {
        }

        override fun populateConstraintsTokens(pos: Position, constraints: MarkdownConstraints, productionHolder: ProductionHolder) {
        }

        override fun createNewMarkerBlocks(pos: Position, productionHolder: ProductionHolder): List<MarkerBlock> {
            productionHolder.addProduction(
                listOf(SequentialParser.Node(100..100, MarkdownElementTypes.EMPH))
            )
            return emptyList()
        }
    }

    private class InvalidInlineFlavour : GFMFlavourDescriptor() {
        override val sequentialParserManager: SequentialParserManager = object : SequentialParserManager() {
            override fun getParserSequence(): List<SequentialParser> = listOf(
                object : SequentialParser {
                    override fun parse(
                        tokens: TokensCache,
                        rangesToGlue: List<IntRange>
                    ): SequentialParser.ParsingResult {
                        val invalidOffset = tokens.filteredTokens.size + 1
                        return object : SequentialParser.ParsingResult {
                            override val parsedNodes = listOf(
                                SequentialParser.Node(invalidOffset..invalidOffset, MarkdownElementTypes.EMPH)
                            )
                            override val rangesToProcessFurther = emptyList<List<IntRange>>()
                        }
                    }
                }
            )
        }
    }

    private class TestCancellationException : RuntimeException()
}
