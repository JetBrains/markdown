package org.intellij.markdown.parser

import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.ASTNodeBuilder
import org.intellij.markdown.lexer.Compat.assert
import org.intellij.markdown.lexer.Stack
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

abstract class TreeBuilder(protected val nodeBuilder: ASTNodeBuilder) {

    fun buildTree(production: List<SequentialParser.Node>): ASTNode {
        val events = constructEvents(production)
        val markersStack = Stack<Pair<MyEvent, MutableList<MyASTNodeWrapper>>>()

        assert(events.isNotEmpty()) { "nonsense" }
        assert(events.first().info == events.last().info) {
            "more than one root?\nfirst: ${events.first().info}\nlast: ${events.last().info}"
        }

        for (i in events.indices) {
            val event = events[i]

            flushEverythingBeforeEvent(event, if (markersStack.isEmpty()) null else markersStack.peek().second)


            if (event.isStart()) {
                markersStack.push(Pair(event, ArrayList()))
            } else {
                val currentNodeChildren = if (event.isEmpty()) {
                    ArrayList()
                } else {
                    val eventAndChildren = markersStack.pop()
                    assert(eventAndChildren.first.info == event.info) {
                        "Intersecting parsed nodes detected: ${eventAndChildren.first.info} vs ${event.info}"
                    }
                    eventAndChildren.second
                }
                val isTopmostNode = markersStack.isEmpty()

                val newNode = createASTNodeOnClosingEvent(event, currentNodeChildren, isTopmostNode)

                if (isTopmostNode) {
                    assert(i + 1 == events.size)
                    return newNode.astNode
                } else {
                    markersStack.peek().second.add(newNode)
                }
            }
        }

        throw AssertionError("markers stack should close some time thus would not be here!")
    }

    protected abstract fun createASTNodeOnClosingEvent(event: MyEvent, currentNodeChildren: List<MyASTNodeWrapper>, isTopmostNode: Boolean): MyASTNodeWrapper

    protected abstract fun flushEverythingBeforeEvent(event: MyEvent, currentNodeChildren: MutableList<MyASTNodeWrapper>?)

    private fun constructEvents(production: List<SequentialParser.Node>): List<MyEvent> {
        val events = ArrayList<MyEvent>()
        for (index in production.indices) {
            val result = production[index]
            val startTokenId = result.range.first
            val endTokenId = result.range.last

            events.add(MyEvent(startTokenId, index, result))
            if (endTokenId != startTokenId) {
                events.add(MyEvent(endTokenId, index, result))
            }
        }
        events.sort()
        return events
    }


    protected class MyEvent(val position: Int,
                          val timeClosed: Int,
                          val info: SequentialParser.Node) : Comparable<MyEvent> {

        fun isStart(): Boolean {
            return info.range.last != position
        }

        fun isEmpty(): Boolean {
            return info.range.first == info.range.last
        }

        override fun compareTo(other: MyEvent): Int {
            if (position != other.position) {
                return position - other.position
            }
            if (isStart() == other.isStart()) {
                val positionDiff = info.range.first + info.range.last - (other.info.range.first + other.info.range.last)
                if (positionDiff != 0) {
                    if (isEmpty()) {
                        return positionDiff
                    }
                    return -positionDiff
                }

                val timeDiff = timeClosed - other.timeClosed
                return if (isStart()) {
                    -timeDiff
                } else {
                    timeDiff
                }
            }
            return if (isStart()) 1 else -1
        }

        override fun toString(): String {
            return "${if (isStart()) "Open" else "Close"}: $position (${info})"
        }
    }

    protected class MyASTNodeWrapper(val astNode: ASTNode, val startTokenIndex: Int, val endTokenIndex: Int)

}
