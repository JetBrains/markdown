package org.intellij.markdown

import kotlinx.cinterop.*
import platform.posix.*
import kotlin.test.assertEquals

@OptIn(ExperimentalForeignApi::class)
actual fun readFromFile(path: String): String {
    val file = requireNotNull(fopen(path, "r")) { "Invalid path $path" }
    file.usePinned {  }
    val bytes = mutableListOf<Byte>()
    try {
        while (true) {
            val c = fgetc(file)
            if (c == EOF) break
            bytes.add(c.toByte())
        }
    } finally {
        fclose(file)
    }
    return bytes.toByteArray().decodeToString()
}

actual fun assertSameLinesWithFile(path: String, result: String) {
    val fileText = readFromFile(path)
    assertEquals(fileText, result)
}

@OptIn(ExperimentalForeignApi::class)
private fun obtainCurrentDirectory(): String? {
    return memScoped {
        val buffer = allocArray<ByteVar>(PATH_MAX)
        val directory = getcwd(buffer, PATH_MAX.convert())?.toKString()
        return@memScoped directory?.replace("\\", "/")
    }
}

private fun obtainProjectHome(): String {
    val currentDirectory = obtainCurrentDirectory() ?: error("Failed to obtain current directory")
    var root = currentDirectory
    while (access(root, F_OK) == -1) {
        root = root.substringBeforeLast("/")
        check(root.isNotEmpty()) { "could not find repo root. cwd=$currentDirectory" }
    }
    return root
}

private val intellijMarkdownHome by lazy { obtainProjectHome() }

actual fun getIntellijMarkdownHome(): String {
    return intellijMarkdownHome
}

actual abstract class TestCase {
    actual fun getName(): String {
        try {
            throw Exception()
        } catch (exception: Exception) {
            val stack = exception.getStackTrace()
            val pattern = Regex("""(?:kfun:)?org\.intellij\.markdown\.\w+Test#(test\w+)\(""")
            return stack.firstNotNullOf { pattern.find(it)?.groupValues?.get(1) }
        }
    }
}
