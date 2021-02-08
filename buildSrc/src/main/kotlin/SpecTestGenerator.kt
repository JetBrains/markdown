import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

private data class TestSpec(
        val markdown: String,
        val html: String,
        val example: Int,
        val start_line: Int,
        val end_line: Int,
        val section: String
)

private fun escape(str: String) = str
        .replace("\\", "\\\\")
        .replace("\n", "\\n")
        .replace("\t", "\\t")
        .replace("\"", "\\\"")
        .replace("$", "\\$")

fun generateSpecTest(testDump: String, className: String, flavor: String) {
    val testSpecs = Gson().fromJson<List<TestSpec>>(testDump, object : TypeToken<List<TestSpec>>() {}.type)
    val output = buildString {
        append("package org.intellij.markdown\n\n")
        append("import kotlin.test.Test\n\n")
        append("class $className : SpecTest($flavor()) {\n")
        for (spec in testSpecs) {
            val name = spec.section.split(" ")
                    .filter { it != "(extension)" }
                    .joinToString("", prefix = "test", postfix = "Example${spec.example}") {
                        it.first().toUpperCase() + it.drop(1)
                    }
            append("    @Test\n")
            append("    fun ").append(name).append("() = doTest(\n")
            append("            markdown = \"").append(escape(spec.markdown)).append("\",\n")
            append("            html = \"").append(escape(spec.html)).append("\"\n")
            append("    )\n\n")
        }
        append("}\n")
    }
    File("src/commonTest/kotlin/org/intellij/markdown/$className.kt").writeText(output)
}

