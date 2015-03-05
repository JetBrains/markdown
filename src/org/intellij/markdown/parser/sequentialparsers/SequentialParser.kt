package org.intellij.markdown.parser.sequentialparsers

import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.TokensCache
import java.util.ArrayList

public trait SequentialParser {

    public fun parse(tokens: TokensCache, rangesToGlue: Collection<Range<Int>>): ParsingResult

    public class Node(public val range: Range<Int>, public val type: IElementType)

    public class ParsingResult {

        private val _parsedNodes : MutableCollection<Node> = ArrayList()
        public val parsedNodes : Collection<Node>
            get() = _parsedNodes

        private val _rangesToProcessFurther : MutableCollection<Collection<Range<Int>>> = ArrayList()
        public val rangesToProcessFurther : Collection<Collection<Range<Int>>>
            get() = _rangesToProcessFurther

        public fun withNode(result: Node): ParsingResult {
            _parsedNodes.add(result)
            return this
        }

        public fun withNodes(parsedNodes: Collection<Node>): ParsingResult {
            _parsedNodes.addAll(parsedNodes)
            return this
        }

        public fun withFurtherProcessing(ranges: Collection<Range<Int>>): ParsingResult {
            _rangesToProcessFurther.add(ranges)
            return this
        }

    }
}
