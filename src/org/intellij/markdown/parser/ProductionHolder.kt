package org.intellij.markdown.parser

import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

class ProductionHolder {
    var currentPosition: Int = 0
        private set

    private val _production : MutableList<SequentialParser.Node> = ArrayList()
    val production: List<SequentialParser.Node>
        get() {
            return _production
        }

    fun updatePosition(position: Int) {
        currentPosition = position
    }

    fun addProduction(nodes: Collection<SequentialParser.Node>) {
        _production.addAll(nodes)
    }

    fun mark(): Marker {
        return Marker()
    }

    inner class Marker {
        private val startPos: Int = currentPosition

        fun done(type: IElementType) {
            _production.add(SequentialParser.Node(startPos..currentPosition, type))
        }
    }
}
