package org.intellij.markdown

import org.intellij.markdown.html.URI
import org.intellij.markdown.html.resolveToStringSafe
import kotlin.test.*

class URITest {
    @Test
    fun testEmptyURI() {
        val uri = ""
        assertEquals(uri, URI(uri).toString())
    }

    @Test
    fun testQualifiedURI1() {
        val uri = "https://google.com"
        assertEquals(uri, URI(uri).toString())
    }

    @Test
    fun testQualifiedURI2() {
        val uri = "https://google.com/"
        assertEquals(uri, URI(uri).toString())
    }

    @Test
    fun testQualifiedURI3() {
        val uri = "https://google.com/#"
        assertEquals(uri, URI(uri).toString())
    }

    @Test
    fun testQualifiedURI4() {
        val uri = "https://google.com/foo"
        assertEquals(uri, URI(uri).toString())
    }

    @Test
    fun testQualifiedURI5() {
        val uri = "https://google.com/?bar=baz"
        assertEquals(uri, URI(uri).toString())
    }

    @Test
    fun testUnqualifiedURI1() {
        val uri = "#"
        assertEquals(uri, URI(uri).toString())
    }

    @Test
    fun testUnqualifiedURI2() {
        val uri = "foo/bar"
        assertEquals(uri, URI(uri).toString())
    }

    @Test
    fun testUnqualifiedURI3() {
        val uri = "/foo/bar?baz"
        assertEquals(uri, URI(uri).toString())
    }

    @Test
    fun testResolveQualifiedToRelative1() {
        assertEquals("https://google.com/bar", URI("https://google.com/foo").resolve("bar").toString())
    }

    @Test
    fun testResolveQualifiedToRelative2() {
        assertEquals("https://google.com/foo/bar", URI("https://google.com/foo/").resolve("bar").toString())
    }

    @Test
    fun testResolveQualifiedToRelative3() {
        assertEquals("https://google.com/bar", URI("https://google.com/").resolve("bar").toString())
    }

    @Test
    fun testResolveUnqualified1() {
        assertEquals("user/repo-name/blob/baz.html", URI("user/repo-name/blob/master").resolve("baz.html").toString())
    }

    @Test
    fun testResolveUnqualified2() {
        assertEquals("/user/repo-name/blob/baz.html", URI("/user/repo-name/blob/master").resolve("baz.html").toString())
    }

    @Test
    fun testResolveUnqualified3() {
        assertEquals("/root", URI("user/repo-name/blob/master").resolve("/root").toString())
    }

    @Test
    fun testResolveUnqualified4() {
        assertEquals("/root", URI("/user/repo-name/blob/master").resolve("/root").toString())
    }

    // JVM thinks that `bar` is continuation of `com` which is weird. But it's stdlib so not sure it must be fixed.
    @Ignore
    @Test
    fun testResolveQualifiedToRelative4() {
        assertEquals("https://google.com/bar", URI("https://google.com").resolve("bar").toString())
    }

    @Test
    fun testResolveQualifiedToAbsolute1() {
        assertEquals("https://google.com/bar", URI("https://google.com/foo").resolve("/bar").toString())
    }

    @Test
    fun testResolveQualifiedToAbsolute2() {
        assertEquals("https://google.com/bar", URI("https://google.com/").resolve("/bar").toString())
    }

    @Test
    fun testResolveQualifiedToQualified() {
        assertEquals("https://fooble.com/bar", URI("https://google.com/foo").resolve("https://fooble.com/bar").toString())
    }

    @Test
    fun testResolveDocToAnchor() {
        assertEquals("https://google.com/foo#bar-baz", URI("https://google.com/foo").resolve("#bar-baz").toString())
    }

    @Test
    fun testResolveAnchorToAnotherAnchor() {
        assertEquals("https://google.com/foo#bar-baz", URI("https://google.com/foo#some-anchor").resolve("#bar-baz").toString())
    }

    @Test
    fun testStrangeURIs() {
        val uri = "http://0.0.0.0x100/"
        assertEquals(uri, URI(uri).toString())

        assertEquals(uri, URI("https://google.com").resolveToStringSafe(uri))
    }

    @Test
    fun testBug27() {
        val uri = "https://blogs.msdn.microsoft.com%2Fdotnet%2F2018%2F05%2F07%2Fintroducing-ml-net-cross-platform-proven-and-open-source-machine-learning-framework%2F&formCheck=f8307abbbb11b8c559607432591df0ae"
        assertEquals(uri, URI(uri).toString())

        assertEquals(uri, URI("https://google.com/foo").resolveToStringSafe(uri))
    }
}
