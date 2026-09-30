package org.intellij.markdown.flavours.glfm

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.acceptChildren
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.flavours.gfm.StrikeThroughDelimiterParser
import org.intellij.markdown.parser.MarkerProcessorFactory
import org.intellij.markdown.html.GeneratingProvider
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.html.URI
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.sequentialparsers.EmphasisLikeParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserManager
import org.intellij.markdown.parser.sequentialparsers.impl.*

/**
 * GitLab Flavored Markdown flavour.
 *
 * This flavour extends the GitHub Flavored Markdown implementation and
 * incrementally adds GitLab-specific behaviour. The goal is to stay close to
 * GitLab's own GLFM spec while reusing as much of the existing GFM
 * infrastructure as possible.
 */
open class GLFMFlavourDescriptor(
    useSafeLinks: Boolean = true,
    absolutizeAnchorLinks: Boolean = false,
    makeHttpsAutoLinks: Boolean = false,
    val gitlabBaseUrl: String = "",
    val gitlabProject: String = "",
    val emojiMap: Map<String, String> = EmojiGeneratingProvider.DEFAULT_EMOJI_MAP
) : GFMFlavourDescriptor(useSafeLinks, absolutizeAnchorLinks, makeHttpsAutoLinks) {

    override val markerProcessorFactory: MarkerProcessorFactory = GLFMMarkerProcessor.Factory

    override val sequentialParserManager = object : SequentialParserManager() {
        override fun getParserSequence(): List<SequentialParser> {
            return listOf(
                AutolinkParser(listOf(MarkdownTokenTypes.AUTOLINK, GFMTokenTypes.GFM_AUTOLINK)),
                BacktickParser(),
                MathParser(),
                // Emoji and references run first so they are recognized even
                // inside inline diff spans (InlineDiffParser consumes its inner
                // tokens from further processing).
                EmojiParser(),
                GitLabReferenceParser(),
                // InlineDiffParser must run before link parsers so [- deletion -]
                // is not consumed as a reference link bracket pair.
                InlineDiffParser(),
                ImageParser(),
                InlineLinkParser(),
                ReferenceLinkParser(),
                // Keep emphasis last as it's the most complex
                EmphasisLikeParser(EmphStrongDelimiterParser(), StrikeThroughDelimiterParser())
            )
        }
    }

    override fun createHtmlGeneratingProviders(
        linkMap: LinkMap,
        baseURI: URI?,
    ): Map<IElementType, GeneratingProvider> {
        // Start from the GFM providers and add/override GLFM-specific ones.
        val baseProviders = super.createHtmlGeneratingProviders(linkMap, baseURI)

        val glfmProviders = hashMapOf<IElementType, GeneratingProvider>(
            // Multiline blockquote (>>> fence)
            GLFMElementTypes.MULTILINE_BLOCK_QUOTE to MultilineBlockQuoteGeneratingProvider(),

            // Inline diff additions and deletions
            GLFMElementTypes.INLINE_DIFF_ADDITION to InlineDiffGeneratingProvider(),
            GLFMElementTypes.INLINE_DIFF_DELETION to InlineDiffGeneratingProvider(),

            // Emoji shortcodes
            GLFMElementTypes.EMOJI to EmojiGeneratingProvider(emojiMap),

            // GitLab references (@user, #issue, !MR, etc.)
            GLFMElementTypes.GITLAB_REFERENCE to GitLabReferenceGeneratingProvider(
                gitlabBaseUrl,
                gitlabProject
            ),

            // Alerts are GitLab-specific blockquotes that start with an
            // "alert marker" like `[!note]`. We render them as a dedicated
            // alert container instead of a plain `<blockquote>`.
            MarkdownElementTypes.BLOCK_QUOTE to AlertOrBlockQuoteGeneratingProvider(
                baseProviders[MarkdownElementTypes.BLOCK_QUOTE]
            ),
        )

        return baseProviders + glfmProviders
    }

    /**
     * Renders blockquotes, upgrading them to GLFM alerts when the first
     * non-whitespace content starts with an alert marker like `[!note]`.
     */
    internal class AlertOrBlockQuoteGeneratingProvider(
        private val delegate: GeneratingProvider?,
    ) : GeneratingProvider {

        override fun processNode(
            visitor: HtmlGenerator.HtmlGeneratingVisitor,
            text: String,
            node: ASTNode,
        ) {
            // Detect GitLab multiline blockquote (`>>>` fence).
            // The block parser produces triple-nested BLOCK_QUOTE for `>>>`.
            // We unwrap it into a single <blockquote> and skip the fence lines.
            val mlContent = extractMultilineContent(node)
            if (mlContent != null) {
                visitor.consumeTagOpen(node, "blockquote")
                mlContent.forEach { visitor.visitNode(it) }
                visitor.consumeTagClose("blockquote")
                return
            }

            val alertInfo = detectAlert(node, text)
            if (alertInfo == null) {
                // Fallback to the original blockquote behaviour.
                delegate?.processNode(visitor, text, node)
                    ?: node.acceptChildren(visitor)
                return
            }

            val (kind, title) = alertInfo

            // Render alert wrapper. We keep the structure simple and leave
            // styling to the consumer.
            val alertClass = "gl-alert gl-alert-$kind"
            visitor.consumeTagOpen(node, "div", "class=\"$alertClass\"")

            visitor.consumeTagOpen(node, "div", "class=\"gl-alert-title\"")
            visitor.consumeHtml(title)
            visitor.consumeTagClose("div")

            visitor.consumeTagOpen(node, "div", "class=\"gl-alert-body\"")
            // Render original blockquote content, but with the alert marker
            // stripped from the first text leaf.
            renderBodyWithoutMarker(visitor, text, node)
            visitor.consumeTagClose("div")

            visitor.consumeTagClose("div")
        }

        /**
         * Returns the content nodes to render if [node] is the outer blockquote
         * of a GitLab `>>>` multiline blockquote, or null otherwise.
         *
         * The block parser produces this AST for `>>>\ncontent\n>>>`:
         *   BLOCK_QUOTE (outer, from first `>`)
         *     BLOCK_QUOTE BLOCK_QUOTE [empty]   ← opening fence `>>>`
         *     PARAGRAPH ...                      ← content
         *     BLOCK_QUOTE BLOCK_QUOTE [empty]   ← closing fence `>>>`
         */
        private fun isMultilineBlockQuote(node: ASTNode, text: String): Boolean =
            extractMultilineContent(node) != null

        private fun extractMultilineContent(node: ASTNode): List<ASTNode>? {
            val children = node.children.filter {
                it.type != MarkdownTokenTypes.EOL &&
                it.type != MarkdownTokenTypes.WHITE_SPACE &&
                it.type != MarkdownTokenTypes.BLOCK_QUOTE
            }
            val bqChildren = node.children.filter { it.type == MarkdownElementTypes.BLOCK_QUOTE }

            // Pattern: at least 2 BQ children (opening + closing fence) and some content
            if (bqChildren.size < 2) return null

            // Opening fence: first BQ child must be a double-nested empty BQ
            if (!isEmptyDoubleBQ(bqChildren.first())) return null
            // Closing fence: last BQ child must be a double-nested empty BQ
            if (!isEmptyDoubleBQ(bqChildren.last())) return null

            // Content: all children between the first and last BQ fence
            val firstFenceEnd = node.children.indexOf(bqChildren.first())
            val lastFenceStart = node.children.lastIndexOf(bqChildren.last())
            if (firstFenceEnd < 0 || lastFenceStart <= firstFenceEnd) return null

            return node.children.subList(firstFenceEnd + 1, lastFenceStart).filter {
                it.type != MarkdownTokenTypes.EOL
            }
        }

        /** True if [node] is `BLOCK_QUOTE → BLOCK_QUOTE → (empty)`. */
        private fun isEmptyDoubleBQ(node: ASTNode): Boolean {
            if (node.type != MarkdownElementTypes.BLOCK_QUOTE) return false
            val innerBQs = node.children.filter { it.type == MarkdownElementTypes.BLOCK_QUOTE }
            if (innerBQs.size != 1) return false
            val innermost = innerBQs.first()
            val innermostContent = innermost.children.filter {
                it.type != MarkdownTokenTypes.EOL && it.type != MarkdownTokenTypes.WHITE_SPACE &&
                it.type != MarkdownTokenTypes.BLOCK_QUOTE
            }
            return innermostContent.isEmpty()
        }

        private fun detectAlert(node: ASTNode, text: String): Pair<String, String>? {
            // Collect all leaves in document order and join their text to
            // reconstruct the inline content.  The alert marker `[!note]` is
            // tokenized as four separate leaves: `[`, `!`, `note`, `]`.
            val leaves = node.children.flatMap { collectLeaves(it) }
            val joined = leaves.joinToString("") { it.getTextInNode(text) }.trimStart()

            // Strip block-quote `>` markers and whitespace that appear at the
            // start (they are included as BLOCK_QUOTE token leaves).
            val stripped = joined.trimStart('>', ' ', '\t')

            if (!stripped.startsWith("[!")) return null

            val closing = stripped.indexOf(']')
            if (closing <= 2) return null

            val marker = stripped.substring(0, closing + 1)
            val kind = when (marker.lowercase()) {
                "[!note]" -> "note"
                "[!tip]" -> "tip"
                "[!important]" -> "important"
                "[!caution]" -> "caution"
                "[!warning]" -> "warning"
                else -> return null
            }

            val title = kind.replaceFirstChar { it.uppercaseChar() }
            return kind to title
        }

        private fun renderBodyWithoutMarker(
            visitor: HtmlGenerator.HtmlGeneratingVisitor,
            text: String,
            node: ASTNode,
        ) {
            // Find the marker node (SHORT_REFERENCE_LINK or bare token group for `[!kind]`)
            // by its offset range so we can skip it during rendering.
            val markerRange = findMarkerOffsetRange(node, text)

            fun renderNode(n: ASTNode) {
                // Suppress any node that is entirely within the marker range
                if (markerRange != null &&
                    n.startOffset >= markerRange.first &&
                    n.endOffset <= markerRange.last
                ) return

                if (n.children.isEmpty()) {
                    visitor.visitLeaf(n)
                } else {
                    visitor.visitNode(n)
                }
            }

            // Walk blockquote children — for PARAGRAPH nodes recurse to allow
            // per-child suppression
            fun renderContainer(container: ASTNode) {
                for (child in container.children) {
                    if (child.type == MarkdownElementTypes.PARAGRAPH) {
                        visitor.consumeTagOpen(child, "p")
                        child.children.forEach { renderNode(it) }
                        visitor.consumeTagClose("p")
                    } else {
                        renderNode(child)
                    }
                }
            }

            renderContainer(node)
        }

        /**
         * Returns the char-offset range covering the `[!kind]` marker in
         * the first content line of this blockquote node, or null if not found.
         */
        private fun findMarkerOffsetRange(node: ASTNode, text: String): IntRange? {
            // The marker may be parsed as SHORT_REFERENCE_LINK([!note]) or as
            // bare tokens `[` `!` `TEXT` `]`. Either way, we find the leftmost
            // `[!` sequence in the first non-empty, non-blockquote leaf cluster.
            val leaves = node.children.flatMap { collectLeaves(it) }
            var idx = 0
            while (idx < leaves.size) {
                val t = leaves[idx].getTextInNode(text).toString()
                if (t.trimStart(' ', '\t') == ">" || t.isBlank() ||
                    leaves[idx].type == MarkdownTokenTypes.BLOCK_QUOTE) {
                    idx++; continue
                }
                break
            }
            if (idx >= leaves.size) return null
            val firstLeaf = leaves[idx]
            // The marker start offset is the start of `[`
            if (firstLeaf.getTextInNode(text).toString().trimStart() != "[") return null
            val markerStart = firstLeaf.startOffset
            // Scan forward for `]` within a few leaves
            var j = idx + 1
            while (j < leaves.size && j < idx + 8) {
                if (leaves[j].getTextInNode(text) == "]") {
                    return markerStart..leaves[j].endOffset
                }
                j++
            }
            return null
        }

        private fun collectLeaves(node: ASTNode): List<ASTNode> {
            if (node.children.isEmpty()) return listOf(node)
            return node.children.flatMap { collectLeaves(it) }
        }
    }
}
