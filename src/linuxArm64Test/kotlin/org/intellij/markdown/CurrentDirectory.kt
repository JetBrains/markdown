package org.intellij.markdown

import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
actual fun getcwd(buffer: CValuesRef<ByteVar>, maxPath: Int): CPointer<ByteVar>? {
  return platform.posix.getcwd(buffer, maxPath.convert())
}
