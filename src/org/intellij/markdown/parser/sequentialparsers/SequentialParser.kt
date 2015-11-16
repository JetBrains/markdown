package org.intellij.markdown.parser.sequentialparsers

import org.intellij.markdown.IElementType
import java.util.*

interface SequentialParser {

    fun parse(tokens: TokensCache, rangesToGlue: Collection<IntRange>): ParsingResult

    data class Node(val range: IntRange, val type: IElementType)

    class ParsingResult {

        private val _parsedNodes : MutableCollection<Node> = ArrayList()
        val parsedNodes : Collection<Node>
            get() = _parsedNodes

        private val _rangesToProcessFurther : MutableCollection<Collection<IntRange>> = ArrayList()
        val rangesToProcessFurther : Collection<Collection<IntRange>>
            get() = _rangesToProcessFurther

        fun withNode(result: Node): ParsingResult {
            _parsedNodes.add(result)
            return this
        }

        fun withNodes(parsedNodes: Collection<Node>): ParsingResult {
            _parsedNodes.addAll(parsedNodes)
            return this
        }

        fun withFurtherProcessing(ranges: Collection<IntRange>): ParsingResult {
            _rangesToProcessFurther.add(ranges)
            return this
        }

    }
}
