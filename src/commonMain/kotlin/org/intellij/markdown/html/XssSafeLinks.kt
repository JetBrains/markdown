package org.intellij.markdown.html

import org.intellij.markdown.ast.ASTNode

private val UNSAFE_LINK_REGEX = Regex("^(vbscript|javascript|file|data):", RegexOption.IGNORE_CASE)
private val ALLOWED_DATA_LINK_REGEX = Regex("^data:image/(gif|png|jpeg|webp);", RegexOption.IGNORE_CASE)

fun makeXssSafeDestination(s: CharSequence): CharSequence {
    return s.takeIf {
        if (UNSAFE_LINK_REGEX.containsMatchIn(s.trim()))
            ALLOWED_DATA_LINK_REGEX.containsMatchIn(s.trim())
        else
            true
    } ?: "#"
}

fun LinkGeneratingProvider.makeXssSafe(useSafeLinks: Boolean = true): LinkGeneratingProvider {
    if (!useSafeLinks) return this

    return object : LinkGeneratingProvider(baseURI) {
        override fun renderLink(
            visitor: HtmlGenerator.HtmlGeneratingVisitor,
            text: String,
            node: ASTNode,
            info: RenderInfo
        ) {
            this@makeXssSafe.renderLink(visitor, text, node, info)
        }

        override fun getRenderInfo(text: String, node: ASTNode): RenderInfo? {
            return this@makeXssSafe.getRenderInfo(text, node)?.let {
                it.copy(destination = makeXssSafeDestination(it.destination))
            }
        }
    }
}