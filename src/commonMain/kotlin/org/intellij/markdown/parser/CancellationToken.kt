package org.intellij.markdown.parser

fun interface CancellationToken {
    fun checkCancelled()

    object NonCancellable: CancellationToken {
        override fun checkCancelled() = Unit
    }
}
