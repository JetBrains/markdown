package org.intellij.markdown.parser

import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.CompositeASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

import java.util.*

public class MyBuilder {

    public fun buildTree(production: List<SequentialParser.Node>, tokensCache: TokensCache): ASTNode {
        val events = constructEvents(production)
        val markersStack = Stack<MutableList<MyASTNodeWrapper>>()

        assert(!events.isEmpty(), "nonsense")
        assert(events.get(0).info == events.get(events.size() - 1).info, "more than one root?")

        var currentTokenPosition = events.get(0).position

        for (i in events.indices) {
            val event = events.get(i)

            while (currentTokenPosition < event.position) {
                flushOneTokenToTree(tokensCache, markersStack, currentTokenPosition)
                currentTokenPosition++
            }

            if (event.isStart() && !event.isEmpty()) {
                markersStack.push(ArrayList<MyASTNodeWrapper>())
            } else {
                val currentNodeChildren = if (event.isEmpty())
                    ArrayList()
                else
                    markersStack.pop()
                val isTopmostNode = markersStack.isEmpty()

                val newNode = createASTNodeOnClosingEvent(tokensCache, event, currentNodeChildren, isTopmostNode)

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

    private fun flushOneTokenToTree(tokensCache: TokensCache, markersStack: Stack<MutableList<MyASTNodeWrapper>>, currentTokenPosition: Int) {
        val iterator = tokensCache.Iterator(currentTokenPosition)
        assert(iterator.type != null)
        val node = LeafASTNode(iterator.type!!, iterator.start, iterator.end)
        markersStack.peek().add(MyASTNodeWrapper(node, iterator.index, iterator.index + 1))
    }

    private fun constructEvents(production: List<SequentialParser.Node>): List<MyEvent> {
        val events = ArrayList<MyEvent>()
        for (result in production) {
            val startTokenId = result.range.start
            val endTokenId = result.range.end

            events.add(MyEvent(startTokenId, result))
            if (endTokenId != startTokenId) {
                events.add(MyEvent(endTokenId, result))
            }
        }
        Collections.sort<MyEvent>(events)
        return events
    }

    private fun createASTNodeOnClosingEvent(tokensCache: TokensCache, event: MyEvent, currentNodeChildren: List<MyASTNodeWrapper>, isTopmostNode: Boolean): MyASTNodeWrapper {
        val newNode: ASTNode

        val type = event.info.`type`
        val startTokenId = event.info.range.start
        val endTokenId = event.info.range.end

        val childrenWithWhitespaces = ArrayList<ASTNode>(currentNodeChildren.size())

        if (isTopmostNode) {
            addRawTokens(tokensCache, childrenWithWhitespaces, startTokenId, -1, -1)
        }
        for (i in 1..currentNodeChildren.size() - 1) {
            val prev = currentNodeChildren.get(i - 1)
            val next = currentNodeChildren.get(i)

            childrenWithWhitespaces.add(prev.astNode)

            addRawTokens(tokensCache, childrenWithWhitespaces, prev.endTokenIndex - 1, +1, tokensCache.Iterator(next.startTokenIndex).start)
        }
        if (!currentNodeChildren.isEmpty()) {
            childrenWithWhitespaces.add(currentNodeChildren.last!!.astNode)
        }
        if (isTopmostNode) {
            addRawTokens(tokensCache, childrenWithWhitespaces, endTokenId, +1, -1)
        }

        newNode = CompositeASTNode(type, childrenWithWhitespaces)
        return MyASTNodeWrapper(newNode, startTokenId, endTokenId)
    }

    private fun addRawTokens(tokensCache: TokensCache, childrenWithWhitespaces: MutableList<ASTNode>, from: Int, dx: Int, exitOffset: Int) {
        val iterator = tokensCache.Iterator(from)
        var rawIdx = 0
        while (iterator.rawLookup(rawIdx + dx) != null && iterator.rawStart(rawIdx + dx) != exitOffset) {
            rawIdx = rawIdx + dx
        }
        while (rawIdx != 0) {
            val rawType = iterator.rawLookup(rawIdx)!!
            childrenWithWhitespaces.add(LeafASTNode(rawType, iterator.rawStart(rawIdx), iterator.rawStart(rawIdx + 1)))
            rawIdx = rawIdx - dx
        }
    }

    private class MyEvent(val position: Int, val info: SequentialParser.Node) : Comparable<MyEvent> {

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
                return -(info.range.start + info.range.end - other.info.range.start - other.info.range.end)
            }
            return if (isStart()) 1 else -1
        }
    }

    private class MyASTNodeWrapper(public val astNode: ASTNode, public val startTokenIndex: Int, public val endTokenIndex: Int)

}
