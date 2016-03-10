package org.intellij.markdown.parser.sequentialparsers

import org.intellij.markdown.IElementType
import java.util.*

interface SequentialParser {

    fun parse(tokens: TokensCache, rangesToGlue: Collection<IntRange>): ParsingResult

    data class Node(val range: IntRange, val type: IElementType)

    interface ParsingResult {
        val parsedNodes: Collection<Node>
        val rangesToProcessFurther: Collection<Collection<IntRange>>
    }
    
    class ParsingResultBuilder : ParsingResult {

        private val _parsedNodes : MutableCollection<Node> = ArrayList()
        override val parsedNodes : Collection<Node>
            get() = _parsedNodes

        private val _rangesToProcessFurther : MutableCollection<Collection<IntRange>> = ArrayList()
        override val rangesToProcessFurther : Collection<Collection<IntRange>>
            get() = _rangesToProcessFurther

        fun withNode(result: Node): ParsingResultBuilder {
            _parsedNodes.add(result)
            return this
        }

        fun withNodes(parsedNodes: Collection<Node>): ParsingResultBuilder {
            _parsedNodes.addAll(parsedNodes)
            return this
        }

        fun withFurtherProcessing(ranges: Collection<IntRange>): ParsingResultBuilder {
            _rangesToProcessFurther.add(ranges)
            return this
        }

    }
}

data class LocalParsingResult(val iteratorPosition: TokensCache.Iterator,
                              override val parsedNodes: Collection<SequentialParser.Node>,
                              val delegateIndices: List<Int>) : SequentialParser.ParsingResult {
    override val rangesToProcessFurther: Collection<Collection<IntRange>>
        get() = listOf(SequentialParserUtil.indicesToTextRanges(delegateIndices))
}
