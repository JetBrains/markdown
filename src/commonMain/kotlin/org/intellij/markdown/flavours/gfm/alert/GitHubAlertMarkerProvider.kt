package org.intellij.markdown.flavours.gfm.alert

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.constraints.getCharsEaten
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider

class GitHubAlertMarkerProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(
        pos: LookaheadText.Position,
        productionHolder: ProductionHolder,
        stateInfo: MarkerProcessor.StateInfo
    ): List<MarkerBlock> {
        val currentConstraints = stateInfo.currentConstraints
        val nextConstraints = stateInfo.nextConstraints
        if (pos.offsetInCurrentLine != currentConstraints.getCharsEaten(pos.currentLine)) {
            return emptyList()
        }
        if (nextConstraints == currentConstraints || nextConstraints.types.lastOrNull() != '>') {
            return emptyList()
        }

        val markerContent =
            pos.currentLine.subSequence(nextConstraints.getCharsEaten(pos.currentLine), pos.currentLine.length)
        if (!isAlertMarker(markerContent)) {
            return emptyList()
        }

        return listOf(GitHubAlertMarkerBlock(pos, nextConstraints, productionHolder))
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean = false

    private fun isAlertMarker(content: CharSequence): Boolean {
        val marker = content.trimEnd()
        return ALERT_TYPES.any { it.contentEquals(marker, ignoreCase = true) }
    }

    companion object {
        private val ALERT_TYPES = listOf("[!NOTE]", "[!TIP]", "[!IMPORTANT]", "[!WARNING]", "[!CAUTION]")
    }
}
