package org.intellij.markdown.flavours.glfm

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.acceptChildren
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.html.GeneratingProvider
import org.intellij.markdown.html.HtmlGenerator

/**
 * Generating provider for the GitLab `>>>` multiline blockquote.
 *
 * The block parser wraps everything between the opening and closing `>>>` in a
 * MULTILINE_BLOCK_QUOTE node. That node's children will include nested
 * BLOCK_QUOTE nodes from the fence lines themselves, plus the actual content
 * (paragraphs, etc.) between them.  We render only the content, skipping the
 * fence-line BLOCK_QUOTE children, inside a single `<blockquote>`.
 */
class MultilineBlockQuoteGeneratingProvider : GeneratingProvider {
    override fun processNode(
        visitor: HtmlGenerator.HtmlGeneratingVisitor,
        text: String,
        node: ASTNode,
    ) {
        visitor.consumeTagOpen(node, "blockquote")
        // The node's children are:
        //   WHITE_SPACE(">")  BLOCK_QUOTE(..)  EOL   ← opening fence line remnants
        //   PARAGRAPH ...
        //   ...
        //   MULTILINE_BLOCK_QUOTE(>>>)               ← closing fence
        // We skip the first non-EOL "fence" group and the last MULTILINE_BLOCK_QUOTE.
        val children = node.children

        // Skip leading fence tokens (WHITE_SPACE ">" and any BLOCK_QUOTE from the `>>` part)
        var start = 0
        while (start < children.size) {
            val t = children[start].type
            if (t == MarkdownTokenTypes.WHITE_SPACE ||
                t == MarkdownElementTypes.BLOCK_QUOTE ||
                t == MarkdownTokenTypes.EOL) {
                start++
            } else {
                break
            }
        }

        // Skip trailing closing fence (MULTILINE_BLOCK_QUOTE and trailing EOL)
        var end = children.size - 1
        while (end >= start) {
            val t = children[end].type
            if (t == GLFMElementTypes.MULTILINE_BLOCK_QUOTE ||
                t == MarkdownTokenTypes.EOL) {
                end--
            } else {
                break
            }
        }

        for (i in start..end) {
            visitor.visitNode(children[i])
        }
        visitor.consumeTagClose("blockquote")
    }
}

/**
 * Generating provider for inline diffs (additions and deletions).
 *
 * Renders:
 * - `{+ added text +}` / `{+text+}` as `<span class="idiff addition">…</span>`
 * - `[- deleted text -]` / `[-text-]` as `<span class="idiff deletion">…</span>`
 *
 * Node shapes produced by [InlineDiffParser]:
 * - Addition, single token:  [TEXT("{+text+}")]
 * - Addition, multi token:   [TEXT("{+"), …content…, TEXT("+}[trailing]")]
 * - Deletion, single content: [LBRACKET, TEXT("-text-"), RBRACKET]
 * - Deletion, multi token:    [LBRACKET, TEXT("-"), …content…, TEXT("-"), RBRACKET]
 */
class InlineDiffGeneratingProvider : GeneratingProvider {
    override fun processNode(
        visitor: HtmlGenerator.HtmlGeneratingVisitor,
        text: String,
        node: ASTNode
    ) {
        val content = node.getTextInNode(text).toString()
        val children = node.children

        val isAddition = content.startsWith("{+")
        val isDeletion = !isAddition && content.startsWith("[-")
        if (!isAddition && !isDeletion) {
            node.acceptChildren(visitor)
            return
        }
        val cssClass = if (isAddition) "addition" else "deletion"

        // Determine content child range and any trailing text after the close marker
        var from = 0
        var to = -1
        var singleTokenInner: String? = null
        var trailing = ""

        if (isAddition) {
            if (children.size == 1) {
                // "{+text+}" — one TEXT token
                singleTokenInner = content.substring(2, content.length - 2)
            } else {
                from = 1
                to = children.size - 2
                val closingText = children.last().getTextInNode(text).toString()
                if (closingText.length > 2) trailing = closingText.substring(2)
            }
        } else {
            if (children.size == 3) {
                // LBRACKET + TEXT("-text-") + RBRACKET
                val inner = children[1].getTextInNode(text).toString()
                singleTokenInner = inner.substring(1, inner.length - 1)
            } else {
                from = 2
                to = children.size - 3
            }
        }

        visitor.consumeTagOpen(node, "span", "class=\"idiff $cssClass\"")
        if (singleTokenInner != null) {
            visitor.consumeHtml(singleTokenInner)
        } else {
            for (i in from..to) {
                val child = children[i]
                if (child.children.isEmpty()) {
                    visitor.visitLeaf(child)
                } else {
                    visitor.visitNode(child)
                }
            }
        }
        visitor.consumeTagClose("span")

        if (trailing.isNotEmpty()) {
            visitor.consumeHtml(trailing)
        }
    }
}

/**
 * Generating provider for emoji shortcodes.
 *
 * Renders `:emoji_name:` as either:
 * - Unicode emoji (if mapping is available)
 * - `<span class="gl-emoji" data-name="emoji_name" title=":emoji_name:">:emoji_name:</span>`
 *
 * The actual emoji rendering can be customized by providing an emoji map.
 */
class EmojiGeneratingProvider(
    private val emojiMap: Map<String, String> = DEFAULT_EMOJI_MAP
) : GeneratingProvider {
    override fun processNode(
        visitor: HtmlGenerator.HtmlGeneratingVisitor,
        text: String,
        node: ASTNode
    ) {
        val fullText = node.getTextInNode(text).toString()

        // Extract emoji name (remove surrounding colons)
        val emojiName = if (fullText.startsWith(":") && fullText.endsWith(":")) {
            fullText.substring(1, fullText.length - 1)
        } else {
            fullText
        }

        // Look up emoji in map
        val emoji = emojiMap[emojiName]

        if (emoji != null) {
            // Render as Unicode emoji
            visitor.consumeTagOpen(
                node,
                "span",
                "class=\"gl-emoji\"",
                "data-name=\"$emojiName\"",
                "title=\":$emojiName:\""
            )
            visitor.consumeHtml(emoji)
            visitor.consumeTagClose("span")
        } else {
            // Fallback to showing the shortcode
            visitor.consumeTagOpen(
                node,
                "span",
                "class=\"gl-emoji\"",
                "data-name=\"$emojiName\"",
                "title=\":$emojiName:\""
            )
            visitor.consumeHtml(":$emojiName:")
            visitor.consumeTagClose("span")
        }
    }

    companion object {
        /**
         * Default emoji map with common emojis.
         * In a real implementation, this would be much more comprehensive.
         */
        val DEFAULT_EMOJI_MAP = mapOf(
            "smile" to "😄",
            "laughing" to "😆",
            "blush" to "😊",
            "heart" to "❤️",
            "thumbsup" to "👍",
            "thumbsdown" to "👎",
            "+1" to "👍",
            "-1" to "👎",
            "ok_hand" to "👌",
            "wave" to "👋",
            "tada" to "🎉",
            "rocket" to "🚀",
            "fire" to "🔥",
            "sparkles" to "✨",
            "star" to "⭐",
            "white_check_mark" to "✅",
            "x" to "❌",
            "warning" to "⚠️",
            "bulb" to "💡",
            "book" to "📖",
            "pencil" to "📝",
            "mag" to "🔍",
            "link" to "🔗",
            "lock" to "🔒",
            "unlock" to "🔓",
            "eyes" to "👀",
            "thinking" to "🤔",
            "clap" to "👏",
            "muscle" to "💪"
        )
    }
}

/**
 * Generating provider for GitLab references.
 *
 * Renders references as links with appropriate classes:
 * - `@username` -> User profile link
 * - `#123` -> Issue link
 * - `!123` -> Merge request link
 * - `$123` -> Snippet link
 * - `&123` -> Epic link
 * - `~label` -> Label link
 * - `%milestone` -> Milestone link
 *
 * The base URL for links can be customized.
 */
class GitLabReferenceGeneratingProvider(
    private val baseUrl: String = "",
    private val currentProject: String = ""
) : GeneratingProvider {
    override fun processNode(
        visitor: HtmlGenerator.HtmlGeneratingVisitor,
        text: String,
        node: ASTNode
    ) {
        val raw = node.getTextInNode(text).toString()
        if (raw.isEmpty()) return

        // Trim trailing punctuation that got included in the token
        val refText = trimTrailingPunctuation(raw)
        val suffix = raw.substring(refText.length)

        val (url, cssClass, title) = parseReference(refText)

        if (url != null) {
            visitor.consumeTagOpen(
                node,
                "a",
                "href=\"$url\"",
                "class=\"gfm $cssClass\"",
                "title=\"$title\""
            )
            visitor.consumeHtml(refText)
            visitor.consumeTagClose("a")
            if (suffix.isNotEmpty()) visitor.consumeHtml(suffix)
        } else {
            visitor.consumeHtml(raw)
        }
    }

    private fun trimTrailingPunctuation(s: String): String {
        if (s.isEmpty()) return s
        val marker = s[0]
        return when (marker) {
            '@' -> {
                // scan past '@' then take valid username chars
                var end = 1
                while (end < s.length && isValidUsernameChar(s[end])) end++
                s.substring(0, end)
            }
            '#', '!', '&', '$' -> {
                var end = 1
                while (end < s.length && s[end].isDigit()) end++
                s.substring(0, end)
            }
            '%' -> {
                var end = 1
                while (end < s.length && (s[end].isLetterOrDigit() || s[end] == '.' || s[end] == '_' || s[end] == '-')) end++
                s.substring(0, end)
            }
            '~' -> {
                // Quoted label ~"multi word" — keep everything up to the closing quote
                if (s.length > 1 && s[1] == '"') {
                    val close = s.indexOf('"', 2)
                    if (close > 0) s.substring(0, close + 1) else s
                } else {
                    var end = 1
                    while (end < s.length && (s[end].isLetterOrDigit() || s[end] == '_' || s[end] == '-')) end++
                    s.substring(0, end)
                }
            }
            else -> s
        }
    }

    private fun isValidUsernameChar(c: Char) = c.isLetterOrDigit() || c == '_' || c == '-' || c == '.'

    private fun parseReference(refText: String): Triple<String?, String, String> {
        if (refText.isEmpty()) return Triple(null, "", "")

        val marker = refText[0]

        return when (marker) {
            '@' -> {
                val username = refText.substring(1)
                Triple(
                    "$baseUrl/$username",
                    "gfm-project_member",
                    "User: $username"
                )
            }

            '#' -> {
                val (project, id) = parseProjectReference(refText.substring(1))
                val projectPath = project ?: currentProject
                Triple(
                    "$baseUrl/$projectPath/-/issues/$id",
                    "gfm-issue",
                    "Issue #$id"
                )
            }

            '!' -> {
                val (project, id) = parseProjectReference(refText.substring(1))
                val projectPath = project ?: currentProject
                Triple(
                    "$baseUrl/$projectPath/-/merge_requests/$id",
                    "gfm-merge_request",
                    "Merge request !$id"
                )
            }

            '$' -> {
                val (project, id) = parseProjectReference(refText.substring(1))
                val projectPath = project ?: currentProject
                Triple(
                    "$baseUrl/$projectPath/-/snippets/$id",
                    "gfm-snippet",
                    "Snippet \$$id"
                )
            }

            '&' -> {
                val id = refText.substring(1)
                Triple(
                    "$baseUrl/groups/$currentProject/-/epics/$id",
                    "gfm-epic",
                    "Epic &$id"
                )
            }

            '~' -> {
                val label = refText.substring(1).trim('"')
                Triple(
                    "$baseUrl/$currentProject/-/issues?label_name[]=$label",
                    "gfm-label",
                    "Label: $label"
                )
            }

            '%' -> {
                val milestone = refText.substring(1).trim('"')
                Triple(
                    "$baseUrl/$currentProject/-/milestones",
                    "gfm-milestone",
                    "Milestone: $milestone"
                )
            }

            else -> {
                // Cross-project reference: group/project#123
                val hashIdx = refText.indexOf('#')
                if (hashIdx > 0) {
                    val projectPath = refText.substring(0, hashIdx)
                    val id = refText.substring(hashIdx + 1)
                    Triple(
                        "$baseUrl/$projectPath/-/issues/$id",
                        "gfm-issue",
                        "Issue #$id in $projectPath"
                    )
                } else {
                    Triple(null, "", "")
                }
            }
        }
    }

    /**
     * Parse a reference that may include a project path.
     * Returns (project, id) where project may be null if not specified.
     */
    private fun parseProjectReference(ref: String): Pair<String?, String> {
        val lastMarkerIndex = ref.lastIndexOfAny(charArrayOf('#', '!', '$'))

        if (lastMarkerIndex > 0) {
            // Has project prefix
            val project = ref.substring(0, lastMarkerIndex)
            val id = ref.substring(lastMarkerIndex + 1)
            return project to id
        }

        // No project prefix
        return null to ref
    }
}
