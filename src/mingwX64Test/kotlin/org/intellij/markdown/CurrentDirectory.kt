package org.intellij.markdown

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual fun getcwd(buffer: CValuesRef<ByteVar>, maxPath: Int): CPointer<ByteVar>? {
  return platform.posix.getcwd(buffer, maxPath)
}
