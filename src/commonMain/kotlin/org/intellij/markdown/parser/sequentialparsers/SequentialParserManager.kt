package org.intellij.markdown.parser.sequentialparsers

import org.intellij.markdown.ExperimentalApi
import org.intellij.markdown.parser.CancellationToken

abstract class SequentialParserManager {
    abstract fun getParserSequence(): List<SequentialParser>

    @OptIn(ExperimentalApi::class)
    fun runParsingSequence(
        tokensCache: TokensCache,
        rangesToParse: List<IntRange>,
    ): Collection<SequentialParser.Node> {
        return runParsingSequence(tokensCache, rangesToParse, CancellationToken.NonCancellable)
    }

    @ExperimentalApi
    fun runParsingSequence(
        tokensCache: TokensCache,
        rangesToParse: List<IntRange>,
        cancellationToken: CancellationToken
    ): Collection<SequentialParser.Node> {
        val result = ArrayList<SequentialParser.Node>()

        var parsingSpaces = ArrayList<List<IntRange>>()
        parsingSpaces.add(rangesToParse)

        for (sequentialParser in getParserSequence()) {
            cancellationToken.checkCancelled()
            val nextLevelSpaces = ArrayList<List<IntRange>>()

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
