package org.intellij.markdown

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
expect fun getcwd(buffer: CValuesRef<ByteVar>, maxPath: Int): CPointer<ByteVar>?
