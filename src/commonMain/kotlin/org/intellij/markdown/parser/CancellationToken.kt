package org.intellij.markdown.parser

import org.intellij.markdown.ExperimentalApi

@ExperimentalApi
fun interface CancellationToken {
    fun checkCancelled()

    object NonCancellable: CancellationToken {
        override fun checkCancelled() = Unit
    }
}
