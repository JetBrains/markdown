package org.intellij.markdown.parser

import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

import java.util.ArrayList

public class ProductionHolder {
    public var currentPosition: Int = 0
        private set

    private val _production : MutableList<SequentialParser.Node> = ArrayList()
    public val production: List<SequentialParser.Node>
        get() {
            return _production
        }

    public fun updatePosition(position: Int) {
        currentPosition = position
    }

    public fun addProduction(nodes: Collection<SequentialParser.Node>) {
        _production.addAll(nodes)
    }

    public fun mark(): Marker {
        return Marker()
    }

    public inner class Marker {
        private val startPos: Int

        init {
            startPos = currentPosition
        }

        public fun done(type: IElementType) {
            _production.add(SequentialParser.Node(startPos..currentPosition, type))
        }
    }
}
