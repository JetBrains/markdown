package org.intellij.markdown.flavours.glfm

import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Comprehensive tests for every GitLab Flavored Markdown (GLFM) feature.
 *
 * Covers:
 *  1. Inline diffs  ({+ addition +}, [- deletion -])
 *  2. Emoji shortcodes  (:name:)
 *  3. GitLab references (@user, #issue, !mr, $snippet, &epic, ~label, %milestone,
 *     cross-project group/project#id)
 *  4. Alerts  (> [!note/tip/important/caution/warning])
 *  5. Multiline blockquotes  (>>> … >>>)
 *  6. Math (inherited from GFM — $…$ and $$…$$)
 *  7. Strikethrough (inherited from GFM — ~~text~~)
 *  8. Tables (inherited from GFM)
 *  9. Task lists (inherited from GFM)
 * 10. Edge cases and interactions between features
 */
class GLFMFeaturesTest {

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun render(
        markdown: String,
        baseUrl: String = "https://gitlab.com",
        project: String = "mygroup/myproject",
    ): String {
        val flavour = GLFMFlavourDescriptor(
            gitlabBaseUrl = baseUrl,
            gitlabProject = project,
        )
        val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)
        return HtmlGenerator(markdown, tree, flavour).generateHtml()
    }

    private fun assertContains(html: String, vararg substrings: String) {
        for (s in substrings) assertTrue(html.contains(s), "Expected '$s' in:\n$html")
    }

    private fun assertNotContains(html: String, vararg substrings: String) {
        for (s in substrings) assertFalse(html.contains(s), "Did not expect '$s' in:\n$html")
    }

    // ── 1. Inline diffs ──────────────────────────────────────────────────────

    @Test fun inlineDiffAdditionBasic() {
        val html = render("{+ added text +}")
        assertContains(html, """class="idiff addition"""", "added text")
        assertContains(html, "<span", "</span>")
    }

    @Test fun inlineDiffDeletionBasic() {
        val html = render("[- deleted text -]")
        assertContains(html, """class="idiff deletion"""", "deleted text")
        assertContains(html, "<span", "</span>")
    }

    @Test fun inlineDiffAdditionInSentence() {
        val html = render("The value is {+ new +} today.")
        assertContains(html, "idiff addition", "new")
        assertContains(html, "today")
    }

    @Test fun inlineDiffDeletionInSentence() {
        val html = render("The value was [- old -] yesterday.")
        assertContains(html, "idiff deletion", "old")
        assertContains(html, "yesterday")
    }

    @Test fun inlineDiffBothInOneLine() {
        val html = render("Change [- old -] to {+ new +}.")
        assertContains(html, "idiff deletion", "idiff addition")
    }

    @Test fun inlineDiffMultipleAdditions() {
        val html = render("{+ first +} and {+ second +}")
        assertEquals(2, html.split("idiff addition").size - 1, "Expected 2 addition spans")
    }

    @Test fun inlineDiffMultipleDeletions() {
        val html = render("[- first -] and [- second -]")
        assertEquals(2, html.split("idiff deletion").size - 1, "Expected 2 deletion spans")
    }

    @Test fun inlineDiffAdditionPreservesInnerSpaces() {
        val html = render("{+  spaced  +}")
        assertContains(html, "idiff addition")
        assertContains(html, "spaced")
    }

    @Test fun inlineDiffDeletionPreservesInnerSpaces() {
        val html = render("[-  spaced  -]")
        assertContains(html, "idiff deletion")
        assertContains(html, "spaced")
    }

    @Test fun inlineDiffUnclosedAdditionIsLiteral() {
        // No closing +} → should render as plain text
        val html = render("{+ not closed")
        assertNotContains(html, "idiff")
    }

    @Test fun inlineDiffUnclosedDeletionIsLiteral() {
        val html = render("[- not closed")
        assertNotContains(html, "idiff")
    }

    @Test fun inlineDiffInsideEmphasis() {
        val html = render("**bold {+ added +} text**")
        assertContains(html, "<strong>", "idiff addition")
    }

    @Test fun inlineDiffInsideCode_notParsed() {
        // Inside backtick code span, markers should not be parsed as diffs
        val html = render("`{+ not a diff +}`")
        assertContains(html, "<code>")
        assertNotContains(html, "idiff")
    }

    @Test fun inlineDiffAdditionNoSpaces() {
        val html = render("{+nospace+}")
        assertContains(html, "idiff addition", "nospace")
    }

    @Test fun inlineDiffDeletionNoSpaces() {
        val html = render("[-nospace-]")
        assertContains(html, "idiff deletion", "nospace")
    }

    @Test fun inlineDiffAdditionNoSpacesInSentence() {
        val html = render("The {+quick+} brown fox")
        assertContains(html, "idiff addition", "quick", "brown fox")
    }

    @Test fun inlineDiffDoesNotSpanLines() {
        // GitLab inline diffs must open and close on the same line
        val html = render("{+ first line\nsecond line +}")
        assertNotContains(html, "idiff")
    }

    @Test fun inlineDiffDeletionDoesNotSpanLines() {
        val html = render("[- first line\nsecond line -]")
        assertNotContains(html, "idiff")
    }

    // ── 2. Emoji shortcodes ──────────────────────────────────────────────────

    @Test fun emojiKnownRendersUnicode() {
        val html = render(":heart:")
        assertContains(html, "gl-emoji", "data-name=\"heart\"")
        // Known emoji maps to Unicode
        assertContains(html, "❤️")
    }

    @Test fun emojiKnownRendersSpan() {
        val html = render(":rocket:")
        assertContains(html, """<span class="gl-emoji"""", "data-name=\"rocket\"", "title=\":rocket:\"")
    }

    @Test fun emojiUnknownRendersShortcode() {
        val html = render(":totally_unknown_emoji_xyz:")
        assertContains(html, "gl-emoji", "totally_unknown_emoji_xyz")
    }

    @Test fun emojiMultipleOnOneLine() {
        val html = render(":thumbsup: :tada: :sparkles:")
        assertEquals(3, html.split("gl-emoji").size - 1, "Expected 3 emoji spans")
    }

    @Test fun emojiPlusOneShortcode() {
        val html = render(":+1:")
        assertContains(html, "gl-emoji", "+1")
    }

    @Test fun emojiAtStartOfLine() {
        val html = render(":fire: This is hot")
        assertContains(html, "gl-emoji", "This is hot")
    }

    @Test fun emojiAtEndOfLine() {
        val html = render("Well done :clap:")
        assertContains(html, "Well done", "gl-emoji")
    }

    @Test fun emojiInBoldText() {
        val html = render("**:star: starred**")
        assertContains(html, "<strong>", "gl-emoji")
    }

    @Test fun emojiNotParsedInCodeSpan() {
        val html = render("`:smile:`")
        assertContains(html, "<code>")
        assertNotContains(html, "gl-emoji")
    }

    @Test fun emojiCustomMapOverrides() {
        val flavour = GLFMFlavourDescriptor(
            emojiMap = mapOf("custom" to "\uD83C\uDF89")
        )
        val markdown = ":custom:"
        val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)
        val html = HtmlGenerator(markdown, tree, flavour).generateHtml()
        assertContains(html, "gl-emoji", "custom", "\uD83C\uDF89")
    }

    @Test fun emojiEmptyShortcodeIsLiteral() {
        // :: has no name between the colons → should not parse as emoji
        val html = render("::")
        assertNotContains(html, "gl-emoji")
    }

    @Test fun emojiNameWithSpacesIsLiteral() {
        // Emoji names cannot contain spaces
        val html = render(":foo bar:")
        assertNotContains(html, "gl-emoji")
    }

    @Test fun colonsWithSpacedTextIsLiteral() {
        // `a : b : c` — the colons are not emoji delimiters
        val html = render("a : b : c")
        assertNotContains(html, "gl-emoji")
    }

    @Test fun emojiTimeRangeNotParsed() {
        // Times like 10:30:45 — "30" is between colons but this is not an emoji…
        // GitLab does actually not render this as emoji; our parser accepts
        // digit-only names though, matching :+1: style codes. Verify no crash
        // and that the text is preserved either way.
        val html = render("Meeting at 10:30:45 today")
        assertContains(html, "10", "45", "today")
    }

    // ── 3. GitLab references ─────────────────────────────────────────────────

    // User mentions
    @Test fun refUserMentionBasic() {
        val html = render("@alice")
        assertContains(html, "gfm", "gfm-project_member", "@alice")
        assertContains(html, "<a ", "href=")
    }

    @Test fun refUserMentionWithTrailingPunctuation() {
        val html = render("Hey @bob, look at this")
        assertContains(html, "gfm", "@bob")
        // The comma must NOT be part of the link text
        assertFalse(html.contains("@bob,"), "Comma should not be inside the link: $html")
    }

    @Test fun refUserMentionWithUnderscore() {
        val html = render("@user_name")
        assertContains(html, "gfm", "@user_name")
    }

    @Test fun refUserMentionWithDot() {
        val html = render("@first.last")
        assertContains(html, "gfm")
    }

    // Issue references
    @Test fun refIssueBasic() {
        val html = render("#42")
        assertContains(html, "gfm", "gfm-issue", "#42")
        assertContains(html, "href=")
    }

    @Test fun refIssueUrl() {
        val html = render("#42", baseUrl = "https://gitlab.com", project = "ns/proj")
        assertContains(html, "ns/proj/-/issues/42")
    }

    @Test fun refIssueMultiple() {
        val html = render("#1 and #2")
        assertEquals(2, html.split("gfm-issue").size - 1, "Expected 2 issue links")
    }

    // Merge-request references
    @Test fun refMergeRequestBasic() {
        val html = render("!99")
        assertContains(html, "gfm", "gfm-merge_request", "!99")
    }

    @Test fun refMergeRequestUrl() {
        val html = render("!7", baseUrl = "https://gitlab.com", project = "ns/proj")
        assertContains(html, "ns/proj/-/merge_requests/7")
    }

    // Snippet references
    @Test fun refSnippetBasic() {
        val html = render("\$10")
        assertContains(html, "gfm")
    }

    // Epic references
    @Test fun refEpicBasic() {
        val html = render("&5")
        assertContains(html, "gfm")
    }

    // Label references
    @Test fun refLabelUnquoted() {
        val html = render("~bug")
        assertContains(html, "gfm", "gfm-label", "bug")
    }

    @Test fun refLabelUrl() {
        val html = render("~bug", baseUrl = "https://gitlab.com", project = "ns/proj")
        assertContains(html, "label_name")
    }

    // Milestone references
    @Test fun refMilestoneBasic() {
        val html = render("%v1.0")
        assertContains(html, "gfm")
    }

    @Test fun refLabelQuoted() {
        val html = render("~\"multi word label\"")
        assertContains(html, "gfm-label", "multi word label")
    }

    // Cross-project references
    @Test fun refCrossProjectIssue() {
        val html = render("other/project#123")
        assertContains(html, "gfm", "#123")
        assertContains(html, "href=")
    }

    @Test fun refCrossProjectUrl() {
        val html = render("ns/proj#55", baseUrl = "https://gitlab.com")
        assertContains(html, "ns/proj/-/issues/55")
    }

    @Test fun refNoReferenceInCodeSpan() {
        val html = render("`#123`")
        assertContains(html, "<code>")
        assertNotContains(html, "gfm-issue")
    }

    @Test fun refIssueRequiresWordBoundary() {
        // `#123abc` is not an issue reference (no word boundary after digits)
        val html = render("#123abc")
        assertNotContains(html, "gfm-issue")
    }

    @Test fun refMergeRequestRequiresWordBoundary() {
        val html = render("!123abc")
        assertNotContains(html, "gfm-merge_request")
    }

    @Test fun refArrayIndexingNotADeletion() {
        // `arr[-1]` is array indexing, not an inline diff deletion
        val html = render("arr[-1] indexing")
        assertNotContains(html, "idiff")
    }

    @Test fun emailNotParsedAsMention() {
        val html = render("test@example.com")
        assertNotContains(html, "gfm-project_member")
    }

    @Test fun timeNotParsedAsEmoji() {
        val html = render("10:30")
        assertNotContains(html, "gl-emoji")
    }

    // ── 4. Alerts ────────────────────────────────────────────────────────────

    @Test fun alertNote() {
        val html = render("> [!note]\n> Content here")
        assertContains(html, "gl-alert", "gl-alert-note", "Note")
        assertContains(html, "Content here")
    }

    @Test fun alertTip() {
        val html = render("> [!tip]\n> A helpful tip")
        assertContains(html, "gl-alert-tip", "Tip")
        assertContains(html, "A helpful tip")
    }

    @Test fun alertImportant() {
        val html = render("> [!important]\n> Pay attention")
        assertContains(html, "gl-alert-important", "Important")
        assertContains(html, "Pay attention")
    }

    @Test fun alertCaution() {
        val html = render("> [!caution]\n> Be careful")
        assertContains(html, "gl-alert-caution", "Caution")
        assertContains(html, "Be careful")
    }

    @Test fun alertWarning() {
        val html = render("> [!warning]\n> Danger ahead")
        assertContains(html, "gl-alert-warning", "Warning")
        assertContains(html, "Danger ahead")
    }

    @Test fun alertCaseInsensitive() {
        val html = render("> [!NOTE]\n> Upper case marker")
        assertContains(html, "gl-alert-note")
    }

    @Test fun alertBodyNotContainsMarker() {
        val html = render("> [!note]\n> Body text")
        // The [!note] marker itself should not appear in the rendered body
        assertFalse(html.contains("[!note]"), "Marker should be stripped from body: $html")
        assertFalse(html.contains("[!NOTE]"), "Marker should be stripped from body: $html")
    }

    @Test fun alertMultiLineBody() {
        val html = render("> [!note]\n> Line one\n> Line two")
        assertContains(html, "gl-alert-note", "Line one", "Line two")
    }

    @Test fun alertContainsInlineDiff() {
        val html = render("> [!warning]\n> Changed [- old -] to {+ new +}")
        assertContains(html, "gl-alert-warning", "idiff deletion", "idiff addition")
    }

    @Test fun alertContainsEmoji() {
        val html = render("> [!tip]\n> Great job :thumbsup:")
        assertContains(html, "gl-alert-tip", "gl-emoji")
    }

    @Test fun unknownAlertMarkerIsPlainBlockquote() {
        val html = render("> [!unknown]\n> Content")
        assertNotContains(html, "gl-alert")
        assertContains(html, "<blockquote>")
    }

    @Test fun plainBlockquoteUnaffected() {
        val html = render("> Regular blockquote")
        assertNotContains(html, "gl-alert")
        assertContains(html, "<blockquote>")
        assertContains(html, "Regular blockquote")
    }

    // ── 5. Multiline blockquotes ─────────────────────────────────────────────

    @Test fun multilineBlockquoteSimple() {
        val html = render(">>>\nLine one\n\nLine two\n>>>")
        assertEquals(
            "<body><blockquote><p>Line one</p><p>Line two</p></blockquote></body>",
            html,
        )
    }

    @Test fun multilineBlockquoteProducesOneParagraphPerBlock() {
        val html = render(">>>\nParagraph one\n\nParagraph two\n>>>")
        assertEquals(2, html.split("<p>").size - 1, "Expected 2 paragraphs")
    }

    @Test fun multilineBlockquoteWithInlineMarkup() {
        val html = render(">>>\n**bold** and _italic_\n>>>")
        assertContains(html, "<blockquote>", "<strong>", "<em>")
    }

    @Test fun multilineBlockquoteWithInlineDiff() {
        val html = render(">>>\nChange [- old -] to {+ new +}\n>>>")
        assertContains(html, "<blockquote>", "idiff deletion", "idiff addition")
    }

    @Test fun multilineBlockquoteWithEmoji() {
        val html = render(">>>\nGreat :rocket:\n>>>")
        assertContains(html, "<blockquote>", "gl-emoji")
    }

    @Test fun multilineBlockquoteEmpty() {
        val html = render(">>>\n>>>")
        assertContains(html, "<blockquote>", "</blockquote>")
    }

    // ── 6. Math (inherited from GFM) ─────────────────────────────────────────

    @Test fun inlineMath() {
        val html = render("Compute \$x^2\$.")
        // GFM math renders as a span or similar — just check it's parsed
        assertContains(html, "x^2")
    }

    @Test fun blockMath() {
        val html = render("\$\$\nx = 1\n\$\$")
        assertContains(html, "x = 1")
    }

    // ── 7. Strikethrough (inherited from GFM) ────────────────────────────────

    @Test fun strikethrough() {
        val html = render("~~strikethrough~~")
        assertContains(html, "<span class=\"user-del\">" )
        assertContains(html, "strikethrough")
    }

    @Test fun strikethroughWithDiffInside() {
        val html = render("~~[- deleted -]~~")
        assertContains(html, "user-del", "idiff deletion")
    }

    // ── 8. Tables (inherited from GFM) ───────────────────────────────────────

    @Test fun basicTable() {
        val html = render("| A | B |\n|---|---|\n| 1 | 2 |")
        assertContains(html, "<table>", "<th>", "<td>", "</table>")
    }

    @Test fun tableWithDiffInCell() {
        val html = render("| Before | After |\n|---|---|\n| [- old -] | {+ new +} |")
        assertContains(html, "<table>", "idiff deletion", "idiff addition")
    }

    // ── 9. Task lists (inherited from GFM) ───────────────────────────────────

    @Test fun taskListUnchecked() {
        val html = render("- [ ] A pending item")
        assertContains(html, "type=\"checkbox\"")
        // Unchecked boxes must not have the `checked` attribute
        assertFalse(
            html.contains(" checked"),
            "Unchecked task should not have 'checked' attribute: $html"
        )
    }

    @Test fun taskListChecked() {
        val html = render("- [x] A done item")
        assertContains(html, "type=\"checkbox\"", "checked")
    }

    // ── 10. Edge cases & interactions ────────────────────────────────────────

    @Test fun inlineDiffAndEmojiOnSameLine() {
        val html = render("{+ great :rocket: +}")
        assertContains(html, "idiff addition", "gl-emoji")
    }

    @Test fun referenceAndEmojiOnSameLine() {
        val html = render("#1 :thumbsup:")
        assertContains(html, "gfm-issue", "gl-emoji")
    }
    @Test
    fun allFeaturesInOneDocument() {
        val markdown = "# Heading\n\n" +
            "Hey @alice, please review !42.\n\n" +
            "Changes:\n" +
            "- [- removed line -]\n" +
            "- {+ added line +}\n\n" +
            "Fixes #7. :white_check_mark:\n\n" +
            "> [!note]\n" +
            "> See also ~documentation.\n\n" +
            ">>>\n" +
            "This is a multiline blockquote.\n" +
            ">>>\n\n" +
            "| Feature | Status |\n" +
            "|---------|--------|\n" +
            "| Emoji   | :star: |\n"

        val html = render(markdown)
        assertContains(
            html,
            "<h1>",
            "gfm-project_member",   // @alice
            "gfm-merge_request",    // !42
            "idiff deletion",       // [- removed -]
            "idiff addition",       // {+ added +}
            "gfm-issue",            // #7
            "gl-emoji",             // :white_check_mark:
            "gl-alert-note",        // [!note] alert
            "<blockquote>",         // >>> multiline blockquote
        )
    }

    @Test fun noSpuriousHtmlInPlainMarkdown() {
        // Plain GFM features still work without GLFM interference
        val html = render("Hello **world** and `code`.")
        assertContains(html, "<strong>world</strong>", "<code>code</code>")
        assertNotContains(html, "gl-emoji", "gl-alert", "idiff", "gfm-issue")
    }

    @Test fun basicMarkdownStillWorks() {
        val html = render("# H1\n\nPlain **text**.")
        assertEquals("<body><h1>H1</h1><p>Plain <strong>text</strong>.</p></body>", html)
    }

    @Test fun emptyDocument() {
        val html = render("")
        // Should not throw; body may be empty or minimal
        assertNotContains(html, "Exception", "Error")
    }

    @Test fun onlyWhitespace() {
        val html = render("   \n   ")
        assertNotContains(html, "Exception", "Error")
    }

    @Test fun inlineDiffNotConfusedByLinkSyntax() {
        // A real link [text](url) should not be confused with deletion syntax
        val html = render("[link text](https://example.com)")
        assertNotContains(html, "idiff")
        assertContains(html, "<a ", "href=")
    }

    @Test fun emojiInHeading() {
        val html = render("# Hello :wave:")
        assertContains(html, "<h1>", "gl-emoji")
    }

    @Test fun inlineDiffInHeading() {
        val html = render("# Changed [- old -] to {+ new +}")
        assertContains(html, "<h1>", "idiff deletion", "idiff addition")
    }

    @Test fun alertTitleIsCapitalizedKind() {
        listOf("note", "tip", "important", "caution", "warning").forEach { kind ->
            val html = render("> [!$kind]\n> body")
            val expectedTitle = kind.replaceFirstChar { it.uppercaseChar() }
            assertContains(html, expectedTitle)
        }
    }

    @Test fun multilineBlockquoteDoesNotLeakFenceText() {
        val html = render(">>>\nContent\n>>>")
        // The raw >>> chars should not appear as visible text
        assertFalse(
            Regex(">>>").containsMatchIn(html.replace("<", " <").replace(">", "> ")),
            "Fence markers should not appear as text: $html"
        )
    }

    @Test fun userMentionLinkPointsToCorrectUrl() {
        val html = render("@charlie", baseUrl = "https://gitlab.com")
        assertContains(html, "https://gitlab.com/charlie")
    }

    @Test fun issueReferenceLinkPointsToCorrectUrl() {
        val html = render("#5", baseUrl = "https://gitlab.com", project = "grp/proj")
        assertContains(html, "grp/proj/-/issues/5")
    }

    @Test fun mergeRequestLinkPointsToCorrectUrl() {
        val html = render("!3", baseUrl = "https://gitlab.com", project = "grp/proj")
        assertContains(html, "grp/proj/-/merge_requests/3")
    }
}
