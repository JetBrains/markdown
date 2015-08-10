package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil
import org.intellij.markdown.parser.sequentialparsers.TokensCache
import java.util.ArrayList

public class LinkDefinitionParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: Collection<Range<Int>>): SequentialParser.ParsingResult {
        val resultNodes = ArrayList<SequentialParser.Node>()
        val delegateIndices = ArrayList<Int>()
        val indices = SequentialParserUtil.textRangesToIndices(rangesToGlue)

        val iterator = tokens.ListIterator(indices, 0)

        if (parseLinkDefinition(resultNodes, delegateIndices, iterator) != null) {
            return SequentialParser.ParsingResult().withNodes(resultNodes).withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(delegateIndices))
        }

        return SequentialParser.ParsingResult().withFurtherProcessing(rangesToGlue)
    }

    private fun parseLinkDefinition(result: MutableCollection<SequentialParser.Node>, delegateIndices: MutableList<Int>, iterator: TokensCache.Iterator): TokensCache.Iterator? {
        val startIndex = iterator.index
        var it = iterator
        var testingIt : TokensCache.Iterator?

        testingIt = LinkParserUtil.parseLinkLabel(result, delegateIndices, it)
        if (testingIt == null) {
            return null
        }
        it = testingIt
        if (it.rawLookup(1) != MarkdownTokenTypes.COLON) {
            return null
        }
        it = it.advance().advance()
        if (it.type == MarkdownTokenTypes.EOL) {
            it = it.advance()
        }

        testingIt = LinkParserUtil.parseLinkDestination(result, it)
        if (testingIt == null) {
            return null
        }
        it = testingIt
        it = it.advance()
        if (it.type == MarkdownTokenTypes.EOL) {
            it = it.advance()
        }

        testingIt = LinkParserUtil.parseLinkTitle(result, it)
        if (testingIt == null) {
            return null
        }
        it = testingIt

        val nextType = it.advance().type
        if (nextType != null && nextType != MarkdownTokenTypes.EOL) {
            return null
        }

        result.add(SequentialParser.Node(startIndex..it.index + 1, MarkdownElementTypes.LINK_DEFINITION))
        return it
    }

}
