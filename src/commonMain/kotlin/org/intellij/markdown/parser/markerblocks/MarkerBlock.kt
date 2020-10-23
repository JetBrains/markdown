package org.intellij.markdown.parser.markerblocks

import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints

interface MarkerBlock {

    fun getNextInterestingOffset(pos: LookaheadText.Position): Int

    fun isInterestingOffset(pos: LookaheadText.Position): Boolean

    fun allowsSubBlocks(): Boolean

    fun processToken(pos: LookaheadText.Position,
                     currentConstraints: MarkdownConstraints): ProcessingResult

    fun getBlockConstraints(): MarkdownConstraints

    /**
     * @param action to accept
     * @return true if this block is to be deleted after this action, false otherwise
     */
    fun acceptAction(action: ClosingAction): Boolean

    enum class ClosingAction {
        DONE {
            override fun doAction(marker: ProductionHolder.Marker, type: IElementType) {
                marker.done(type)
            }
        },
        DROP {
            override fun doAction(marker: ProductionHolder.Marker, type: IElementType) {
            }
        },
        DEFAULT {
            override fun doAction(marker: ProductionHolder.Marker, type: IElementType) {
                throw UnsupportedOperationException("Should not be invoked")
            }
        },
        NOTHING {
            override fun doAction(marker: ProductionHolder.Marker, type: IElementType) {
            }
        };

        abstract fun doAction(marker: ProductionHolder.Marker, `type`: IElementType)
    }

    enum class EventAction {
        PROPAGATE,
        CANCEL
    }

    class ProcessingResult internal constructor(val childrenAction: ClosingAction,
                                                       val selfAction: ClosingAction,
                                                       val eventAction: EventAction) {

        companion object {
            val PASS: ProcessingResult = ProcessingResult(ClosingAction.NOTHING, ClosingAction.NOTHING, EventAction.PROPAGATE)
            val CANCEL: ProcessingResult = ProcessingResult(ClosingAction.NOTHING, ClosingAction.NOTHING, EventAction.CANCEL)
            val DEFAULT: ProcessingResult = ProcessingResult(ClosingAction.DEFAULT, ClosingAction.DONE, EventAction.PROPAGATE)
        }

    }
}
