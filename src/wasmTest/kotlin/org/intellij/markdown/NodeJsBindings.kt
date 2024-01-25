package org.intellij.markdown.nodejs

@JsModule("path")
external object Path {
   val sep: String
}

@JsModule("fs")
external object FileSystem {
    fun existsSync(path: String): Boolean
    fun readFileSync(path: String, encoding: String): String
}

external interface Process {
    fun cwd(): String
}

external val process: Process