package org.intellij.markdown.parser.sequentialparsers

/**
 * Should be used to parse emphasis-like elements with the same priority that should be aware of each other.
 * Accepts a list of [DelimiterParser] that finds target delimiters runs
 * and stores containing delimiters in the shared delimiters list.
 * For each of those delimiters, this parser tries to find a matching closing delimiter.
 * Matching information then is passed back to the [DelimiterParser]
 * that should use it to create actual tree nodes.
 */
class EmphasisLikeParser(private vararg val parsers: DelimiterParser): SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResultBuilder()
        val iterator = tokens.RangesListIterator(rangesToGlue)
        val delimiters = collectDelimiters(tokens, iterator)
        balanceDelimiters(delimiters)
        for (parser in parsers) {
            parser.process(tokens, iterator, delimiters, result)
        }
        return result
    }

    private fun collectDelimiters(tokens: TokensCache, iterator: TokensCache.Iterator): ArrayList<DelimiterParser.Info> {
        val delimiters = arrayListOf<DelimiterParser.Info>()
        var position = iterator
        outer@while (position.type != null) {
            var iteratorMoved = 0
            for (parser in parsers) {
                val steps = parser.scan(tokens, position, delimiters)
                iteratorMoved += steps
                for (step in 0 until steps) {
                    if (position.type == null) {
                        break@outer
                    }
                    position = position.advance()
                }
            }
            if (iteratorMoved == 0) {
                position = position.advance()
            }
        }
        return delimiters
    }

    /**
     * For each delimiter from [delimiters] tries to find matching closing delimiter.
     * If such a delimiter is found, sets [DelimiterParser.Info.closerIndex] for the opening delimiter
     * to the index of the closing one.
     *
     * Heavily based on [cmark](https://github.com/commonmark/cmark) and
     * [markdown-it](https://github.com/markdown-it/markdown-it) implementations.
     */
    private fun balanceDelimiters(delimiters: ArrayList<DelimiterParser.Info>) {
        var runStartIndex = 0
        var previousDelimiterIndex = -2
        val openersIndices = Array(delimiters.size) { 0 }
        // Used to set lower bounds for openers searches in case some previous match failed.
        // From the spec (https://spec.commonmark.org/0.30/#delimiter-stack):
        // We keep track of the openers_bottom for each delimiter type (*, _),
        // indexed to the length of the closing delimiter run (modulo 3) and
        // to whether the closing delimiter can also be an opener.
        // https://github.com/commonmark/cmark/issues/178#issuecomment-270417442
        // https://github.com/commonmark/cmark/commit/34250e12ccebdc6372b8b49c44fab57c72443460
        val openersBottom = HashMap<Char, Array<Int>>()
        for ((closerIndex, closer) in delimiters.withIndex()) {
            // Initiate a new delimiter run if the current delimiter can not belong to the current run
            // (should have same markers and adjacent indices)
            if (delimiters[runStartIndex].marker != closer.marker || previousDelimiterIndex != closer.position - 1) {
                runStartIndex = closerIndex
            }
            previousDelimiterIndex = closer.position
            if (!closer.canClose) {
                continue
            }
            if (!openersBottom.containsKey(closer.marker)) {
                openersBottom[closer.marker] = arrayOf(-1, -1, -1, -1, -1, -1)
            }
            // Now, look back in the stack (staying above stack_bottom and the openers_bottom for this delimiter type)
            // for the first matching potential opener ("matching" means same delimiter).
            val minOpenerIndex = openersBottom[closer.marker]!![(if (closer.canOpen) 3 else 0) + (closer.length % 3)]
            var openerIndex = runStartIndex - openersIndices[runStartIndex] - 1
            var newMinOpenerIndex = openerIndex
            while (openerIndex > minOpenerIndex) {
                val opener = delimiters[openerIndex]
                if (opener.marker != closer.marker) {
                    openerIndex -= openersIndices[openerIndex] + 1
                    continue
                }
                if (opener.canOpen && opener.closerIndex < 0 && !violatesRuleOfThree(opener, closer)) {
                    // If the previous delimiter cannot be an opener, skip the entire sequence in future checks.
                    val lastIndex = when {
                        openerIndex > 0 && !delimiters[openerIndex - 1].canOpen -> openersIndices[openerIndex - 1] + 1
                        else -> 0
                    }
                    openersIndices[openerIndex] = lastIndex
                    openersIndices[closerIndex] = closerIndex - openerIndex + lastIndex
                    closer.canOpen = false
                    opener.closerIndex = closerIndex
                    opener.canClose = false
                    newMinOpenerIndex = -1
                    // Start new delimiter run
                    previousDelimiterIndex = -2
                    break
                }
                // Search for other opener before
                openerIndex -= openersIndices[openerIndex] + 1
            }
            // Search has failed, so update future search lower bound
            if (newMinOpenerIndex != -1) {
                openersBottom[closer.marker]!![(if (closer.canOpen) 3 else 0) + (closer.length % 3)] = newMinOpenerIndex
            }
        }
    }

    /**
     * If one of the delimiters can both open and close emphasis, then the
     * sum of the lengths of the delimiter runs containing the opening and
     * closing delimiters must not be a multiple of 3 unless both lengths
     * are multiples of 3.
     *
     * See [rules 9 and 10](https://spec.commonmark.org/0.30/#can-open-emphasis).
     */
    private fun violatesRuleOfThree(opener: DelimiterParser.Info, closer: DelimiterParser.Info): Boolean {
        return (opener.canClose || closer.canOpen) &&
            ((opener.length + closer.length) % 3 == 0) &&
            (opener.length % 3 != 0 || closer.length % 3 != 0)
    }
}
