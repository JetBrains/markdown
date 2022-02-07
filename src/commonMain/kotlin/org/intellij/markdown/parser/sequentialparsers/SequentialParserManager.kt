package org.intellij.markdown.parser.sequentialparsers

abstract class SequentialParserManager {
    abstract fun getParserSequence(): List<SequentialParser>

    fun runParsingSequence(tokensCache: TokensCache, rangesToParse: List<IntRange>): Collection<SequentialParser.Node> {
        if (rangesToParse.isEmpty())
            return emptyList()

        val result = ArrayList<SequentialParser.Node>()

        var parsingSpaces = ArrayList<List<IntRange>>()
        parsingSpaces.add(rangesToParse)

        for (sequentialParser in getParserSequence()) {
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
