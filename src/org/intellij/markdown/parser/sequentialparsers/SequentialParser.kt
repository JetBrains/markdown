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
        
        fun withOtherParsingResult(parsingResult: ParsingResult): ParsingResultBuilder {
            _parsedNodes.addAll(parsingResult.parsedNodes)
            _rangesToProcessFurther.addAll(parsingResult.rangesToProcessFurther)
            return this
        }

    }
}

data class LocalParsingResult(val iteratorPosition: TokensCache.Iterator,
                              override val parsedNodes: Collection<SequentialParser.Node>,
                              override val rangesToProcessFurther: Collection<Collection<IntRange>>) : SequentialParser.ParsingResult {
    constructor(iteratorPosition: TokensCache.Iterator,
                parsedNodes: Collection<SequentialParser.Node>,
                delegateIndices: List<Int>)
    : this(iteratorPosition, parsedNodes, listOf(SequentialParserUtil.indicesToTextRanges(delegateIndices)))
    
    constructor(iteratorPosition: TokensCache.Iterator,
                parsedNodes: Collection<SequentialParser.Node>) 
    : this(iteratorPosition, parsedNodes, emptyList<Collection<IntRange>>())
}
