package org.intellij.markdown.parser

public trait MarkerProcessorFactory {
    fun createMarkerProcessor(productionHolder: ProductionHolder, tokensCache: TokensCache): MarkerProcessor
}

