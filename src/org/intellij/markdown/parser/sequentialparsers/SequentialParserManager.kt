package org.intellij.markdown.parser.sequentialparsers

import java.util.*

abstract class SequentialParserManager {
    abstract fun getParserSequence(): List<SequentialParser>

    fun runParsingSequence(tokensCache: TokensCache, rangesToParse: Collection<IntRange>): Collection<SequentialParser.Node> {
        val result = ArrayList<SequentialParser.Node>()

        var parsingSpaces = ArrayList<Collection<IntRange>>()
        parsingSpaces.add(rangesToParse)

        for (sequentialParser in getParserSequence()) {
            val nextLevelSpaces = ArrayList<Collection<IntRange>>()

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
