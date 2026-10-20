package org.intellij.markdown.html

import org.intellij.markdown.html.entities.EntityConverter
import kotlin.test.Test
import kotlin.test.assertEquals

class EntityConverterTest {
    private fun convert(text: String) = EntityConverter.replaceEntities(text, processEntities = true, processEscapes = true)

    @Test
    fun decimalReferenceOutsideBmp() {
        assertEquals("😀", convert("&#128512;"))
    }

    @Test
    fun hexReferenceOutsideBmp() {
        assertEquals("😀", convert("&#x1F600;"))
    }

    @Test
    fun namedReferenceOutsideBmp() {
        assertEquals("𝔄", convert("&Afr;"))
    }

    @Test
    fun invalidCodePointsAreReplaced() {
        assertEquals("�", convert("&#0;"))
        assertEquals("�", convert("&#xD800;"))
        assertEquals("�", convert("&#x110000;"))
    }

    @Test
    fun tooLongNumericReferencesAreNotDecoded() {
        assertEquals("&amp;#87654321;", convert("&#87654321;"))
        assertEquals("&amp;#x1234567;", convert("&#x1234567;"))
    }
}
