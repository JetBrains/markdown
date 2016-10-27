package org.intellij.markdown.parser

interface MarkerProcessorFactory {
    fun createMarkerProcessor(productionHolder: ProductionHolder): MarkerProcessor<*>
}

