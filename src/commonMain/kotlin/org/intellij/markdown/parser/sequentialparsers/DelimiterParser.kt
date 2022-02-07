package org.intellij.markdown.parser.sequentialparsers

import org.intellij.markdown.IElementType

abstract class DelimiterParser {
    /**
     * Finds all tokens that open or close element represented by this parser.
     * Invoked by [EmphasisLikeParser.collectDelimiters].
     *
     * Delimiters matching will be handled by the [EmphasisLikeParser.balanceDelimiters]
     * after delimiters from all parsers will be collected.
     *
     * @return Number of steps to advance token iterator.
     */
    abstract fun scan(tokens: TokensCache, iterator: TokensCache.Iterator, delimiters: MutableList<Info>): Int

    /**
     * Receives a list of delimiters collected by [scan]
     * (contains tokens from all parsers) and creates actual tree nodes from them.
     *
     * If there is a matching delimiter for the current one, it will have non-negative [Info.closerIndex].
     */
    abstract fun process(
        tokens: TokensCache,
        iterator: TokensCache.Iterator,
        delimiters: MutableList<Info>,
        result: SequentialParser.ParsingResultBuilder
    )

    data class Info(
        val tokenType: IElementType,
        val position: Int,
        /**
         * Represents the length of the current delimiter run
         * and will be used to check [9 and 10 rules](https://spec.commonmark.org/0.30/#can-open-emphasis)
         * (rule of 3) for emphasises on the delimiter balancing phase (will be skipped if equals to 0).
         */
        val length: Int = 0,
        var canOpen: Boolean,
        var canClose: Boolean,
        var marker: Char,
        /**
         * Index of the closing delimiter for the current one.
         * If it is less than 0, current delimiter does not have a matching closer.
         *
         * Will be set by the [EmphasisLikeParser.balanceDelimiters] after all delimiters will be collected.
         */
        var closerIndex: Int = -1
    )

    /**
     * Checks if current token can open or close emphasis based on the current delimiter run.
     *
     * See [Rules for opening and closing emphasises](https://spec.commonmark.org/0.30/#can-open-emphasis).
     */
    open fun canOpenClose(
        tokens: TokensCache,
        left: TokensCache.Iterator,
        right: TokensCache.Iterator,
        canSplitText: Boolean
    ): Pair<Boolean, Boolean> {
        val isLeftFlanking = isLeftFlankingRun(left, right)
        val isRightFlanking = isRightFlankingRun(tokens, left, right)
        val canOpen = when {
            canSplitText -> isLeftFlanking
            else -> isLeftFlanking && (!isRightFlanking || SequentialParserUtil.isPunctuation(left, -1))
        }
        val canClose = when {
            canSplitText -> isRightFlanking
            else -> isRightFlanking && (!isLeftFlanking || SequentialParserUtil.isPunctuation(right, 1))
        }
        return canOpen to canClose
    }

    /**
     * See [left flanking delimiter run](http://spec.commonmark.org/0.30/#left-flanking-delimiter-run)
     */
    open fun isLeftFlankingRun(leftIt: TokensCache.Iterator, rightIt: TokensCache.Iterator): Boolean {
        return !isWhitespace(rightIt, 1) &&
            (!isPunctuation(rightIt, 1)
                || isWhitespace(leftIt, -1)
                || isPunctuation(leftIt, -1))
    }

    /**
     * See [right flanking delimiter run](https://spec.commonmark.org/0.30/#right-flanking-delimiter-run)
     */
    open fun isRightFlankingRun(tokens: TokensCache, leftIt: TokensCache.Iterator, rightIt: TokensCache.Iterator): Boolean {
        return leftIt.charLookup(-1) != getType(leftIt) &&
            !isWhitespace(leftIt, -1) &&
            (!isPunctuation(leftIt, -1)
                || isWhitespace(rightIt, 1)
                || isPunctuation(rightIt, 1))
    }

    open fun isWhitespace(info: TokensCache.Iterator, lookup: Int): Boolean {
        return SequentialParserUtil.isWhitespace(info, lookup)
    }

    open fun isPunctuation(info: TokensCache.Iterator, lookup: Int): Boolean {
        return SequentialParserUtil.isPunctuation(info, lookup)
    }

    companion object {
        const val maxAdvance = 50

        fun getType(iterator: TokensCache.Iterator): Char {
            return iterator.firstChar
        }
    }
}
