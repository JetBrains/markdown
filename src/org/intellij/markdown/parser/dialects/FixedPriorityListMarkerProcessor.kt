package org.intellij.markdown.parser.dialects

import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.MarkdownConstraints
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl

import java.util.*
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.TokensCache

public abstract class FixedPriorityListMarkerProcessor(productionHolder: ProductionHolder, tokensCache: TokensCache, startingConstraints: MarkdownConstraints)
        : MarkerProcessor(productionHolder, tokensCache, startingConstraints) {
    private val priorityMap: Map<IElementType, Int>

    init {
        val priorityList = getPriorityList()

        val _priorityMap = IdentityHashMap<IElementType, Int>()
        for (pair in priorityList) {
            _priorityMap.put(pair.first, pair.second)
        }

        priorityMap = _priorityMap
    }

    protected abstract fun getPriorityList(): List<Pair<IElementType, Int>>

    override fun getPrioritizedMarkerPermutation(): List<Int> {
        val result = ArrayList<Int>(markersStack.size())
        for (i in markersStack.indices) {
            result.add(i)
        }

        Collections.sort<Int>(result, object : Comparator<Int> {
            override fun compare(o1: Int, o2: Int): Int {
                if (priorityMap.isEmpty()) {
                    return o2 - o1
                }

                val block1 = markersStack.get(o1)
                val block2 = markersStack.get(o2)

                val diff = getPriority(block1) - getPriority(block2)
                if (diff != 0) {
                    return -diff
                }
                return o2 - o1
            }
        })
        return result
    }

    private fun getPriority(block: MarkerBlock): Int {
        if (block !is MarkerBlockImpl) {
            return 0
        }

        val `type` = (block : MarkerBlockImpl).getDefaultNodeType()
        if (priorityMap.containsKey(`type`)) {
            return priorityMap.get(`type`)?:0
        }

        return 0
    }

}
