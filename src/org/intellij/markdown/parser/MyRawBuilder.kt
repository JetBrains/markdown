package org.intellij.markdown.parser

import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.ASTNodeBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import java.util.ArrayList
import java.util.Collections
import java.util.Stack

public class MyRawBuilder {

    public fun buildTree(production: List<SequentialParser.Node>): ASTNode {
        val events = constructEvents(production)
        val markersStack = Stack<MutableList<MyASTNodeWrapper>>()

        assert(!events.isEmpty(), "nonsense")
        assert(events.get(0).info == events.get(events.size() - 1).info, "more than one root?")

        for (i in events.indices) {
            val event = events.get(i)

            if (event.isStart() && !event.isEmpty()) {
                markersStack.push(ArrayList<MyASTNodeWrapper>())
            } else {
                val currentNodeChildren = if (event.isEmpty())
                    ArrayList()
                else
                    markersStack.pop()
                val isTopmostNode = markersStack.isEmpty()

                val newNode = createASTNodeOnClosingEvent(event, currentNodeChildren, isTopmostNode)

                if (isTopmostNode) {
                    assert(i + 1 == events.size())
                    return newNode.astNode
                } else {
                    markersStack.peek().add(newNode)
                }
            }
        }

        throw AssertionError("markers stack should close some time thus would not be here!")
    }

    private fun constructEvents(production: List<SequentialParser.Node>): List<MyEvent> {
        val events = ArrayList<MyEvent>()
        for (index in production.indices) {
            val result = production.get(index)
            val startTokenId = result.range.start
            val endTokenId = result.range.end

            events.add(MyEvent(startTokenId, index, result))
            if (endTokenId != startTokenId) {
                events.add(MyEvent(endTokenId, index, result))
            }
        }
        Collections.sort(events)
        return events
    }

    private fun createASTNodeOnClosingEvent(event: MyEvent, currentNodeChildren: List<MyASTNodeWrapper>, isTopmostNode: Boolean): MyASTNodeWrapper {
        val newNode: ASTNode

        val type = event.info.`type`
        val startOffset = event.info.range.start
        val endOffset = event.info.range.end

        val childrenWithWhitespaces = ArrayList<ASTNode>(currentNodeChildren.size())

        addRawTokens(childrenWithWhitespaces,
                startOffset,
                currentNodeChildren.firstOrNull()?.startTokenIndex ?: endOffset)

        for (i in 1..currentNodeChildren.size() - 1) {
            val prev = currentNodeChildren.get(i - 1)
            val next = currentNodeChildren.get(i)

            childrenWithWhitespaces.add(prev.astNode)

            addRawTokens(childrenWithWhitespaces, prev.endTokenIndex, next.startTokenIndex)
        }
        if (!currentNodeChildren.isEmpty()) {
            childrenWithWhitespaces.add(currentNodeChildren.last().astNode)
            addRawTokens(childrenWithWhitespaces, currentNodeChildren.last().endTokenIndex, endOffset)
        }

        newNode = ASTNodeBuilder.createCompositeNode(type, childrenWithWhitespaces)
        return MyASTNodeWrapper(newNode, startOffset, endOffset)
    }

    private fun addRawTokens(childrenWithWhitespaces: MutableList<ASTNode>, from: Int, to: Int) {
        // Let's for now assume that it's just whitespace
        if (from != to) {
            childrenWithWhitespaces.add(ASTNodeBuilder.createLeafNode(MarkdownTokenTypes.WHITE_SPACE, from, to))
        }
    }

    private class MyEvent(val position: Int,
                          val timeClosed: Int,
                          val info: SequentialParser.Node) : Comparable<MyEvent> {

        public fun isStart(): Boolean {
            return info.range.start == position
        }

        public fun isEmpty(): Boolean {
            return info.range.start == info.range.end
        }

        override fun compareTo(other: MyEvent): Int {
            if (position != other.position) {
                return position - other.position
            }
            if (isStart() == other.isStart()) {
                val positionDiff = info.range.start + info.range.end - (other.info.range.start + other.info.range.end)
                if (positionDiff != 0) {
                    return -positionDiff
                }

                val timeDiff = timeClosed - other.timeClosed
                if (isStart()) {
                    return -timeDiff
                } else {
                    return timeDiff
                }
            }
            return if (isStart()) 1 else -1
        }
    }

    private class MyASTNodeWrapper(public val astNode: ASTNode, public val startTokenIndex: Int, public val endTokenIndex: Int)

}
