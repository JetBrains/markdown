package org.intellij.markdown.parser.sequentialparsers

import org.intellij.markdown.parser.sequentialparsers.impl.*
import java.util.ArrayList

public class SequentialParserManager {
    protected fun getParserSequence(): List<SequentialParser> {
        return listOf(AutolinkParser(),
                      BacktickParser(),
                      LinkDefinitionParser(),
                      InlineLinkParser(),
                      ReferenceLinkParser(),
                      EmphStrongParser())
    }

    public fun runParsingSequence(tokensCache: TokensCache, rangesToParse: Collection<Range<Int>>): Collection<SequentialParser.Node> {
        val result = ArrayList<SequentialParser.Node>()

        var parsingSpaces = ArrayList<Collection<Range<Int>>>()
        parsingSpaces.add(rangesToParse)

        for (sequentialParser in getParserSequence()) {
            val nextLevelSpaces = ArrayList<Collection<Range<Int>>>()

            for (parsingSpace in parsingSpaces) {
                val currentResult = sequentialParser.parse(tokensCache, parsingSpace)
                result.addAll(currentResult.parsedNodes)
                nextLevelSpaces.addAll(currentResult.rangesToProcessFurther)
            }

            parsingSpaces = nextLevelSpaces
        }

        return result
    }
}
