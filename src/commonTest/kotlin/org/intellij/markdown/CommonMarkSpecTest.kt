package org.intellij.markdown

import kotlin.test.Ignore
import kotlin.test.Test

class CommonMarkSpecTest : SpecTest(org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor()) {
    @Test
    fun testTabsExample1() = doTest(
            markdown = "\tfoo\tbaz\t\tbim\n",
            html = "<pre><code>foo\tbaz\t\tbim\n</code></pre>\n"
    )

    @Test
    fun testTabsExample2() = doTest(
            markdown = "  \tfoo\tbaz\t\tbim\n",
            html = "<pre><code>foo\tbaz\t\tbim\n</code></pre>\n"
    )

    @Test
    fun testTabsExample3() = doTest(
            markdown = "    a\ta\n    ὐ\ta\n",
            html = "<pre><code>a\ta\nὐ\ta\n</code></pre>\n"
    )

    @Test
    fun testTabsExample4() = doTest(
            markdown = "  - foo\n\n\tbar\n",
            html = "<ul>\n<li>\n<p>foo</p>\n<p>bar</p>\n</li>\n</ul>\n"
    )

    @Test
    @Ignore
    fun testTabsExample5() = doTest(
            markdown = "- foo\n\n\t\tbar\n",
            html = "<ul>\n<li>\n<p>foo</p>\n<pre><code>  bar\n</code></pre>\n</li>\n</ul>\n"
    )

    @Test
    @Ignore
    fun testTabsExample6() = doTest(
            markdown = ">\t\tfoo\n",
            html = "<blockquote>\n<pre><code>  foo\n</code></pre>\n</blockquote>\n"
    )

    @Test
    @Ignore
    fun testTabsExample7() = doTest(
            markdown = "-\t\tfoo\n",
            html = "<ul>\n<li>\n<pre><code>  foo\n</code></pre>\n</li>\n</ul>\n"
    )

    @Test
    fun testTabsExample8() = doTest(
            markdown = "    foo\n\tbar\n",
            html = "<pre><code>foo\nbar\n</code></pre>\n"
    )

    @Test
    fun testTabsExample9() = doTest(
            markdown = " - foo\n   - bar\n\t - baz\n",
            html = "<ul>\n<li>foo\n<ul>\n<li>bar\n<ul>\n<li>baz</li>\n</ul>\n</li>\n</ul>\n</li>\n</ul>\n"
    )

    @Test
    @Ignore
    fun testTabsExample10() = doTest(
            markdown = "#\tFoo\n",
            html = "<h1>Foo</h1>\n"
    )

    @Test
    fun testTabsExample11() = doTest(
            markdown = "*\t*\t*\t\n",
            html = "<hr />\n"
    )

    @Test
    fun testBackslashEscapesExample12() = doTest(
            markdown = "\\!\\\"\\#\\\$\\%\\&\\'\\(\\)\\*\\+\\,\\-\\.\\/\\:\\;\\<\\=\\>\\?\\@\\[\\\\\\]\\^\\_\\`\\{\\|\\}\\~\n",
            html = "<p>!&quot;#\$%&amp;'()*+,-./:;&lt;=&gt;?@[\\]^_`{|}~</p>\n"
    )

    @Test
    fun testBackslashEscapesExample13() = doTest(
            markdown = "\\\t\\A\\a\\ \\3\\φ\\«\n",
            html = "<p>\\\t\\A\\a\\ \\3\\φ\\«</p>\n"
    )

    @Test
    fun testBackslashEscapesExample14() = doTest(
            markdown = "\\*not emphasized*\n\\<br/> not a tag\n\\[not a link](/foo)\n\\`not code`\n1\\. not a list\n\\* not a list\n\\# not a heading\n\\[foo]: /url \"not a reference\"\n\\&ouml; not a character entity\n",
            html = "<p>*not emphasized*\n&lt;br/&gt; not a tag\n[not a link](/foo)\n`not code`\n1. not a list\n* not a list\n# not a heading\n[foo]: /url &quot;not a reference&quot;\n&amp;ouml; not a character entity</p>\n"
    )

    @Test
    fun testBackslashEscapesExample15() = doTest(
            markdown = "\\\\*emphasis*\n",
            html = "<p>\\<em>emphasis</em></p>\n"
    )

    @Test
    fun testBackslashEscapesExample16() = doTest(
            markdown = "foo\\\nbar\n",
            html = "<p>foo<br />\nbar</p>\n"
    )

    @Test
    fun testBackslashEscapesExample17() = doTest(
            markdown = "`` \\[\\` ``\n",
            html = "<p><code>\\[\\`</code></p>\n"
    )

    @Test
    fun testBackslashEscapesExample18() = doTest(
            markdown = "    \\[\\]\n",
            html = "<pre><code>\\[\\]\n</code></pre>\n"
    )

    @Test
    fun testBackslashEscapesExample19() = doTest(
            markdown = "~~~\n\\[\\]\n~~~\n",
            html = "<pre><code>\\[\\]\n</code></pre>\n"
    )

    @Test
    fun testBackslashEscapesExample20() = doTest(
            markdown = "<http://example.com?find=\\*>\n",
            html = "<p><a href=\"http://example.com?find=%5C*\">http://example.com?find=\\*</a></p>\n"
    )

    @Test
    fun testBackslashEscapesExample21() = doTest(
            markdown = "<a href=\"/bar\\/)\">\n",
            html = "<a href=\"/bar\\/)\">\n"
    )

    @Test
    fun testBackslashEscapesExample22() = doTest(
            markdown = "[foo](/bar\\* \"ti\\*tle\")\n",
            html = "<p><a href=\"/bar*\" title=\"ti*tle\">foo</a></p>\n"
    )

    @Test
    fun testBackslashEscapesExample23() = doTest(
            markdown = "[foo]\n\n[foo]: /bar\\* \"ti\\*tle\"\n",
            html = "<p><a href=\"/bar*\" title=\"ti*tle\">foo</a></p>\n"
    )

    @Test
    fun testBackslashEscapesExample24() = doTest(
            markdown = "``` foo\\+bar\nfoo\n```\n",
            html = "<pre><code class=\"language-foo+bar\">foo\n</code></pre>\n"
    )

    @Test
    @Ignore
    fun testEntityAndNumericCharacterReferencesExample25() = doTest(
            markdown = "&nbsp; &amp; &copy; &AElig; &Dcaron;\n&frac34; &HilbertSpace; &DifferentialD;\n&ClockwiseContourIntegral; &ngE;\n",
            html = "<p>  &amp; © Æ Ď\n¾ ℋ ⅆ\n∲ ≧̸</p>\n"
    )

    @Test
    @Ignore
    fun testEntityAndNumericCharacterReferencesExample26() = doTest(
            markdown = "&#35; &#1234; &#992; &#0;\n",
            html = "<p># Ӓ Ϡ �</p>\n"
    )

    @Test
    fun testEntityAndNumericCharacterReferencesExample27() = doTest(
            markdown = "&#X22; &#XD06; &#xcab;\n",
            html = "<p>&quot; ആ ಫ</p>\n"
    )

    @Test
    @Ignore
    fun testEntityAndNumericCharacterReferencesExample28() = doTest(
            markdown = "&nbsp &x; &#; &#x;\n&#87654321;\n&#abcdef0;\n&ThisIsNotDefined; &hi?;\n",
            html = "<p>&amp;nbsp &amp;x; &amp;#; &amp;#x;\n&amp;#87654321;\n&amp;#abcdef0;\n&amp;ThisIsNotDefined; &amp;hi?;</p>\n"
    )

    @Test
    fun testEntityAndNumericCharacterReferencesExample29() = doTest(
            markdown = "&copy\n",
            html = "<p>&amp;copy</p>\n"
    )

    @Test
    fun testEntityAndNumericCharacterReferencesExample30() = doTest(
            markdown = "&MadeUpEntity;\n",
            html = "<p>&amp;MadeUpEntity;</p>\n"
    )

    @Test
    fun testEntityAndNumericCharacterReferencesExample31() = doTest(
            markdown = "<a href=\"&ouml;&ouml;.html\">\n",
            html = "<a href=\"&ouml;&ouml;.html\">\n"
    )

    @Test
    fun testEntityAndNumericCharacterReferencesExample32() = doTest(
            markdown = "[foo](/f&ouml;&ouml; \"f&ouml;&ouml;\")\n",
            html = "<p><a href=\"/f%C3%B6%C3%B6\" title=\"föö\">foo</a></p>\n"
    )

    @Test
    fun testEntityAndNumericCharacterReferencesExample33() = doTest(
            markdown = "[foo]\n\n[foo]: /f&ouml;&ouml; \"f&ouml;&ouml;\"\n",
            html = "<p><a href=\"/f%C3%B6%C3%B6\" title=\"föö\">foo</a></p>\n"
    )

    @Test
    fun testEntityAndNumericCharacterReferencesExample34() = doTest(
            markdown = "``` f&ouml;&ouml;\nfoo\n```\n",
            html = "<pre><code class=\"language-föö\">foo\n</code></pre>\n"
    )

    @Test
    fun testEntityAndNumericCharacterReferencesExample35() = doTest(
            markdown = "`f&ouml;&ouml;`\n",
            html = "<p><code>f&amp;ouml;&amp;ouml;</code></p>\n"
    )

    @Test
    fun testEntityAndNumericCharacterReferencesExample36() = doTest(
            markdown = "    f&ouml;f&ouml;\n",
            html = "<pre><code>f&amp;ouml;f&amp;ouml;\n</code></pre>\n"
    )

    @Test
    fun testEntityAndNumericCharacterReferencesExample37() = doTest(
            markdown = "&#42;foo&#42;\n*foo*\n",
            html = "<p>*foo*\n<em>foo</em></p>\n"
    )

    @Test
    fun testEntityAndNumericCharacterReferencesExample38() = doTest(
            markdown = "&#42; foo\n\n* foo\n",
            html = "<p>* foo</p>\n<ul>\n<li>foo</li>\n</ul>\n"
    )

    @Test
    fun testEntityAndNumericCharacterReferencesExample39() = doTest(
            markdown = "foo&#10;&#10;bar\n",
            html = "<p>foo\n\nbar</p>\n"
    )

    @Test
    fun testEntityAndNumericCharacterReferencesExample40() = doTest(
            markdown = "&#9;foo\n",
            html = "<p>\tfoo</p>\n"
    )

    @Test
    fun testEntityAndNumericCharacterReferencesExample41() = doTest(
            markdown = "[a](url &quot;tit&quot;)\n",
            html = "<p>[a](url &quot;tit&quot;)</p>\n"
    )

    @Test
    fun testPrecedenceExample42() = doTest(
            markdown = "- `one\n- two`\n",
            html = "<ul>\n<li>`one</li>\n<li>two`</li>\n</ul>\n"
    )

    @Test
    fun testThematicBreaksExample43() = doTest(
            markdown = "***\n---\n___\n",
            html = "<hr />\n<hr />\n<hr />\n"
    )

    @Test
    fun testThematicBreaksExample44() = doTest(
            markdown = "+++\n",
            html = "<p>+++</p>\n"
    )

    @Test
    fun testThematicBreaksExample45() = doTest(
            markdown = "===\n",
            html = "<p>===</p>\n"
    )

    @Test
    fun testThematicBreaksExample46() = doTest(
            markdown = "--\n**\n__\n",
            html = "<p>--\n**\n__</p>\n"
    )

    @Test
    fun testThematicBreaksExample47() = doTest(
            markdown = " ***\n  ***\n   ***\n",
            html = "<hr />\n<hr />\n<hr />\n"
    )

    @Test
    fun testThematicBreaksExample48() = doTest(
            markdown = "    ***\n",
            html = "<pre><code>***\n</code></pre>\n"
    )

    @Test
    @Ignore
    fun testThematicBreaksExample49() = doTest(
            markdown = "Foo\n    ***\n",
            html = "<p>Foo\n***</p>\n"
    )

    @Test
    fun testThematicBreaksExample50() = doTest(
            markdown = "_____________________________________\n",
            html = "<hr />\n"
    )

    @Test
    fun testThematicBreaksExample51() = doTest(
            markdown = " - - -\n",
            html = "<hr />\n"
    )

    @Test
    fun testThematicBreaksExample52() = doTest(
            markdown = " **  * ** * ** * **\n",
            html = "<hr />\n"
    )

    @Test
    fun testThematicBreaksExample53() = doTest(
            markdown = "-     -      -      -\n",
            html = "<hr />\n"
    )

    @Test
    fun testThematicBreaksExample54() = doTest(
            markdown = "- - - -    \n",
            html = "<hr />\n"
    )

    @Test
    fun testThematicBreaksExample55() = doTest(
            markdown = "_ _ _ _ a\n\na------\n\n---a---\n",
            html = "<p>_ _ _ _ a</p>\n<p>a------</p>\n<p>---a---</p>\n"
    )

    @Test
    fun testThematicBreaksExample56() = doTest(
            markdown = " *-*\n",
            html = "<p><em>-</em></p>\n"
    )

    @Test
    fun testThematicBreaksExample57() = doTest(
            markdown = "- foo\n***\n- bar\n",
            html = "<ul>\n<li>foo</li>\n</ul>\n<hr />\n<ul>\n<li>bar</li>\n</ul>\n"
    )

    @Test
    fun testThematicBreaksExample58() = doTest(
            markdown = "Foo\n***\nbar\n",
            html = "<p>Foo</p>\n<hr />\n<p>bar</p>\n"
    )

    @Test
    fun testThematicBreaksExample59() = doTest(
            markdown = "Foo\n---\nbar\n",
            html = "<h2>Foo</h2>\n<p>bar</p>\n"
    )

    @Test
    fun testThematicBreaksExample60() = doTest(
            markdown = "* Foo\n* * *\n* Bar\n",
            html = "<ul>\n<li>Foo</li>\n</ul>\n<hr />\n<ul>\n<li>Bar</li>\n</ul>\n"
    )

    @Test
    fun testThematicBreaksExample61() = doTest(
            markdown = "- Foo\n- * * *\n",
            html = "<ul>\n<li>Foo</li>\n<li>\n<hr />\n</li>\n</ul>\n"
    )

    @Test
    fun testATXHeadingsExample62() = doTest(
            markdown = "# foo\n## foo\n### foo\n#### foo\n##### foo\n###### foo\n",
            html = "<h1>foo</h1>\n<h2>foo</h2>\n<h3>foo</h3>\n<h4>foo</h4>\n<h5>foo</h5>\n<h6>foo</h6>\n"
    )

    @Test
    fun testATXHeadingsExample63() = doTest(
            markdown = "####### foo\n",
            html = "<p>####### foo</p>\n"
    )

    @Test
    fun testATXHeadingsExample64() = doTest(
            markdown = "#5 bolt\n\n#hashtag\n",
            html = "<p>#5 bolt</p>\n<p>#hashtag</p>\n"
    )

    @Test
    fun testATXHeadingsExample65() = doTest(
            markdown = "\\## foo\n",
            html = "<p>## foo</p>\n"
    )

    @Test
    fun testATXHeadingsExample66() = doTest(
            markdown = "# foo *bar* \\*baz\\*\n",
            html = "<h1>foo <em>bar</em> *baz*</h1>\n"
    )

    @Test
    fun testATXHeadingsExample67() = doTest(
            markdown = "#                  foo                     \n",
            html = "<h1>foo</h1>\n"
    )

    @Test
    fun testATXHeadingsExample68() = doTest(
            markdown = " ### foo\n  ## foo\n   # foo\n",
            html = "<h3>foo</h3>\n<h2>foo</h2>\n<h1>foo</h1>\n"
    )

    @Test
    fun testATXHeadingsExample69() = doTest(
            markdown = "    # foo\n",
            html = "<pre><code># foo\n</code></pre>\n"
    )

    @Test
    @Ignore
    fun testATXHeadingsExample70() = doTest(
            markdown = "foo\n    # bar\n",
            html = "<p>foo\n# bar</p>\n"
    )

    @Test
    fun testATXHeadingsExample71() = doTest(
            markdown = "## foo ##\n  ###   bar    ###\n",
            html = "<h2>foo</h2>\n<h3>bar</h3>\n"
    )

    @Test
    fun testATXHeadingsExample72() = doTest(
            markdown = "# foo ##################################\n##### foo ##\n",
            html = "<h1>foo</h1>\n<h5>foo</h5>\n"
    )

    @Test
    fun testATXHeadingsExample73() = doTest(
            markdown = "### foo ###     \n",
            html = "<h3>foo</h3>\n"
    )

    @Test
    fun testATXHeadingsExample74() = doTest(
            markdown = "### foo ### b\n",
            html = "<h3>foo ### b</h3>\n"
    )

    @Test
    fun testATXHeadingsExample75() = doTest(
            markdown = "# foo#\n",
            html = "<h1>foo#</h1>\n"
    )

    @Test
    fun testATXHeadingsExample76() = doTest(
            markdown = "### foo \\###\n## foo #\\##\n# foo \\#\n",
            html = "<h3>foo ###</h3>\n<h2>foo ###</h2>\n<h1>foo #</h1>\n"
    )

    @Test
    fun testATXHeadingsExample77() = doTest(
            markdown = "****\n## foo\n****\n",
            html = "<hr />\n<h2>foo</h2>\n<hr />\n"
    )

    @Test
    fun testATXHeadingsExample78() = doTest(
            markdown = "Foo bar\n# baz\nBar foo\n",
            html = "<p>Foo bar</p>\n<h1>baz</h1>\n<p>Bar foo</p>\n"
    )

    @Test
    fun testATXHeadingsExample79() = doTest(
            markdown = "## \n#\n### ###\n",
            html = "<h2></h2>\n<h1></h1>\n<h3></h3>\n"
    )

    @Test
    fun testSetextHeadingsExample80() = doTest(
            markdown = "Foo *bar*\n=========\n\nFoo *bar*\n---------\n",
            html = "<h1>Foo <em>bar</em></h1>\n<h2>Foo <em>bar</em></h2>\n"
    )

    @Test
    @Ignore
    fun testSetextHeadingsExample81() = doTest(
            markdown = "Foo *bar\nbaz*\n====\n",
            html = "<h1>Foo <em>bar\nbaz</em></h1>\n"
    )

    @Test
    @Ignore
    fun testSetextHeadingsExample82() = doTest(
            markdown = "  Foo *bar\nbaz*\t\n====\n",
            html = "<h1>Foo <em>bar\nbaz</em></h1>\n"
    )

    @Test
    fun testSetextHeadingsExample83() = doTest(
            markdown = "Foo\n-------------------------\n\nFoo\n=\n",
            html = "<h2>Foo</h2>\n<h1>Foo</h1>\n"
    )

    @Test
    fun testSetextHeadingsExample84() = doTest(
            markdown = "   Foo\n---\n\n  Foo\n-----\n\n  Foo\n  ===\n",
            html = "<h2>Foo</h2>\n<h2>Foo</h2>\n<h1>Foo</h1>\n"
    )

    @Test
    fun testSetextHeadingsExample85() = doTest(
            markdown = "    Foo\n    ---\n\n    Foo\n---\n",
            html = "<pre><code>Foo\n---\n\nFoo\n</code></pre>\n<hr />\n"
    )

    @Test
    fun testSetextHeadingsExample86() = doTest(
            markdown = "Foo\n   ----      \n",
            html = "<h2>Foo</h2>\n"
    )

    @Test
    @Ignore
    fun testSetextHeadingsExample87() = doTest(
            markdown = "Foo\n    ---\n",
            html = "<p>Foo\n---</p>\n"
    )

    @Test
    fun testSetextHeadingsExample88() = doTest(
            markdown = "Foo\n= =\n\nFoo\n--- -\n",
            html = "<p>Foo\n= =</p>\n<p>Foo</p>\n<hr />\n"
    )

    @Test
    fun testSetextHeadingsExample89() = doTest(
            markdown = "Foo  \n-----\n",
            html = "<h2>Foo</h2>\n"
    )

    @Test
    fun testSetextHeadingsExample90() = doTest(
            markdown = "Foo\\\n----\n",
            html = "<h2>Foo\\</h2>\n"
    )

    @Test
    fun testSetextHeadingsExample91() = doTest(
            markdown = "`Foo\n----\n`\n\n<a title=\"a lot\n---\nof dashes\"/>\n",
            html = "<h2>`Foo</h2>\n<p>`</p>\n<h2>&lt;a title=&quot;a lot</h2>\n<p>of dashes&quot;/&gt;</p>\n"
    )

    @Test
    fun testSetextHeadingsExample92() = doTest(
            markdown = "> Foo\n---\n",
            html = "<blockquote>\n<p>Foo</p>\n</blockquote>\n<hr />\n"
    )

    @Test
    fun testSetextHeadingsExample93() = doTest(
            markdown = "> foo\nbar\n===\n",
            html = "<blockquote>\n<p>foo\nbar\n===</p>\n</blockquote>\n"
    )

    @Test
    fun testSetextHeadingsExample94() = doTest(
            markdown = "- Foo\n---\n",
            html = "<ul>\n<li>Foo</li>\n</ul>\n<hr />\n"
    )

    @Test
    @Ignore
    fun testSetextHeadingsExample95() = doTest(
            markdown = "Foo\nBar\n---\n",
            html = "<h2>Foo\nBar</h2>\n"
    )

    @Test
    fun testSetextHeadingsExample96() = doTest(
            markdown = "---\nFoo\n---\nBar\n---\nBaz\n",
            html = "<hr />\n<h2>Foo</h2>\n<h2>Bar</h2>\n<p>Baz</p>\n"
    )

    @Test
    fun testSetextHeadingsExample97() = doTest(
            markdown = "\n====\n",
            html = "<p>====</p>\n"
    )

    @Test
    fun testSetextHeadingsExample98() = doTest(
            markdown = "---\n---\n",
            html = "<hr />\n<hr />\n"
    )

    @Test
    fun testSetextHeadingsExample99() = doTest(
            markdown = "- foo\n-----\n",
            html = "<ul>\n<li>foo</li>\n</ul>\n<hr />\n"
    )

    @Test
    fun testSetextHeadingsExample100() = doTest(
            markdown = "    foo\n---\n",
            html = "<pre><code>foo\n</code></pre>\n<hr />\n"
    )

    @Test
    fun testSetextHeadingsExample101() = doTest(
            markdown = "> foo\n-----\n",
            html = "<blockquote>\n<p>foo</p>\n</blockquote>\n<hr />\n"
    )

    @Test
    fun testSetextHeadingsExample102() = doTest(
            markdown = "\\> foo\n------\n",
            html = "<h2>&gt; foo</h2>\n"
    )

    @Test
    fun testSetextHeadingsExample103() = doTest(
            markdown = "Foo\n\nbar\n---\nbaz\n",
            html = "<p>Foo</p>\n<h2>bar</h2>\n<p>baz</p>\n"
    )

    @Test
    fun testSetextHeadingsExample104() = doTest(
            markdown = "Foo\nbar\n\n---\n\nbaz\n",
            html = "<p>Foo\nbar</p>\n<hr />\n<p>baz</p>\n"
    )

    @Test
    fun testSetextHeadingsExample105() = doTest(
            markdown = "Foo\nbar\n* * *\nbaz\n",
            html = "<p>Foo\nbar</p>\n<hr />\n<p>baz</p>\n"
    )

    @Test
    fun testSetextHeadingsExample106() = doTest(
            markdown = "Foo\nbar\n\\---\nbaz\n",
            html = "<p>Foo\nbar\n---\nbaz</p>\n"
    )

    @Test
    fun testIndentedCodeBlocksExample107() = doTest(
            markdown = "    a simple\n      indented code block\n",
            html = "<pre><code>a simple\n  indented code block\n</code></pre>\n"
    )

    @Test
    fun testIndentedCodeBlocksExample108() = doTest(
            markdown = "  - foo\n\n    bar\n",
            html = "<ul>\n<li>\n<p>foo</p>\n<p>bar</p>\n</li>\n</ul>\n"
    )

    @Test
    fun testIndentedCodeBlocksExample109() = doTest(
            markdown = "1.  foo\n\n    - bar\n",
            html = "<ol>\n<li>\n<p>foo</p>\n<ul>\n<li>bar</li>\n</ul>\n</li>\n</ol>\n"
    )

    @Test
    fun testIndentedCodeBlocksExample110() = doTest(
            markdown = "    <a/>\n    *hi*\n\n    - one\n",
            html = "<pre><code>&lt;a/&gt;\n*hi*\n\n- one\n</code></pre>\n"
    )

    @Test
    fun testIndentedCodeBlocksExample111() = doTest(
            markdown = "    chunk1\n\n    chunk2\n  \n \n \n    chunk3\n",
            html = "<pre><code>chunk1\n\nchunk2\n\n\n\nchunk3\n</code></pre>\n"
    )

    @Test
    fun testIndentedCodeBlocksExample112() = doTest(
            markdown = "    chunk1\n      \n      chunk2\n",
            html = "<pre><code>chunk1\n  \n  chunk2\n</code></pre>\n"
    )

    @Test
    @Ignore
    fun testIndentedCodeBlocksExample113() = doTest(
            markdown = "Foo\n    bar\n\n",
            html = "<p>Foo\nbar</p>\n"
    )

    @Test
    fun testIndentedCodeBlocksExample114() = doTest(
            markdown = "    foo\nbar\n",
            html = "<pre><code>foo\n</code></pre>\n<p>bar</p>\n"
    )

    @Test
    fun testIndentedCodeBlocksExample115() = doTest(
            markdown = "# Heading\n    foo\nHeading\n------\n    foo\n----\n",
            html = "<h1>Heading</h1>\n<pre><code>foo\n</code></pre>\n<h2>Heading</h2>\n<pre><code>foo\n</code></pre>\n<hr />\n"
    )

    @Test
    fun testIndentedCodeBlocksExample116() = doTest(
            markdown = "        foo\n    bar\n",
            html = "<pre><code>    foo\nbar\n</code></pre>\n"
    )

    @Test
    fun testIndentedCodeBlocksExample117() = doTest(
            markdown = "\n    \n    foo\n    \n\n",
            html = "<pre><code>foo\n</code></pre>\n"
    )

    @Test
    fun testIndentedCodeBlocksExample118() = doTest(
            markdown = "    foo  \n",
            html = "<pre><code>foo  \n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample119() = doTest(
            markdown = "```\n<\n >\n```\n",
            html = "<pre><code>&lt;\n &gt;\n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample120() = doTest(
            markdown = "~~~\n<\n >\n~~~\n",
            html = "<pre><code>&lt;\n &gt;\n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample121() = doTest(
            markdown = "``\nfoo\n``\n",
            html = "<p><code>foo</code></p>\n"
    )

    @Test
    fun testFencedCodeBlocksExample122() = doTest(
            markdown = "```\naaa\n~~~\n```\n",
            html = "<pre><code>aaa\n~~~\n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample123() = doTest(
            markdown = "~~~\naaa\n```\n~~~\n",
            html = "<pre><code>aaa\n```\n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample124() = doTest(
            markdown = "````\naaa\n```\n``````\n",
            html = "<pre><code>aaa\n```\n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample125() = doTest(
            markdown = "~~~~\naaa\n~~~\n~~~~\n",
            html = "<pre><code>aaa\n~~~\n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample126() = doTest(
            markdown = "```\n",
            html = "<pre><code></code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample127() = doTest(
            markdown = "`````\n\n```\naaa\n",
            html = "<pre><code>\n```\naaa\n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample128() = doTest(
            markdown = "> ```\n> aaa\n\nbbb\n",
            html = "<blockquote>\n<pre><code>aaa\n</code></pre>\n</blockquote>\n<p>bbb</p>\n"
    )

    @Test
    fun testFencedCodeBlocksExample129() = doTest(
            markdown = "```\n\n  \n```\n",
            html = "<pre><code>\n  \n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample130() = doTest(
            markdown = "```\n```\n",
            html = "<pre><code></code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample131() = doTest(
            markdown = " ```\n aaa\naaa\n```\n",
            html = "<pre><code>aaa\naaa\n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample132() = doTest(
            markdown = "  ```\naaa\n  aaa\naaa\n  ```\n",
            html = "<pre><code>aaa\naaa\naaa\n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample133() = doTest(
            markdown = "   ```\n   aaa\n    aaa\n  aaa\n   ```\n",
            html = "<pre><code>aaa\n aaa\naaa\n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample134() = doTest(
            markdown = "    ```\n    aaa\n    ```\n",
            html = "<pre><code>```\naaa\n```\n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample135() = doTest(
            markdown = "```\naaa\n  ```\n",
            html = "<pre><code>aaa\n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample136() = doTest(
            markdown = "   ```\naaa\n  ```\n",
            html = "<pre><code>aaa\n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample137() = doTest(
            markdown = "```\naaa\n    ```\n",
            html = "<pre><code>aaa\n    ```\n</code></pre>\n"
    )

    @Test
    @Ignore
    fun testFencedCodeBlocksExample138() = doTest(
            markdown = "``` ```\naaa\n",
            html = "<p><code> </code>\naaa</p>\n"
    )

    @Test
    fun testFencedCodeBlocksExample139() = doTest(
            markdown = "~~~~~~\naaa\n~~~ ~~\n",
            html = "<pre><code>aaa\n~~~ ~~\n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample140() = doTest(
            markdown = "foo\n```\nbar\n```\nbaz\n",
            html = "<p>foo</p>\n<pre><code>bar\n</code></pre>\n<p>baz</p>\n"
    )

    @Test
    fun testFencedCodeBlocksExample141() = doTest(
            markdown = "foo\n---\n~~~\nbar\n~~~\n# baz\n",
            html = "<h2>foo</h2>\n<pre><code>bar\n</code></pre>\n<h1>baz</h1>\n"
    )

    @Test
    fun testFencedCodeBlocksExample142() = doTest(
            markdown = "```ruby\ndef foo(x)\n  return 3\nend\n```\n",
            html = "<pre><code class=\"language-ruby\">def foo(x)\n  return 3\nend\n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample143() = doTest(
            markdown = "~~~~    ruby startline=3 \$%@#\$\ndef foo(x)\n  return 3\nend\n~~~~~~~\n",
            html = "<pre><code class=\"language-ruby\">def foo(x)\n  return 3\nend\n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample144() = doTest(
            markdown = "````;\n````\n",
            html = "<pre><code class=\"language-;\"></code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample145() = doTest(
            markdown = "``` aa ```\nfoo\n",
            html = "<p><code>aa</code>\nfoo</p>\n"
    )

    @Test
    @Ignore
    fun testFencedCodeBlocksExample146() = doTest(
            markdown = "~~~ aa ``` ~~~\nfoo\n~~~\n",
            html = "<pre><code class=\"language-aa\">foo\n</code></pre>\n"
    )

    @Test
    fun testFencedCodeBlocksExample147() = doTest(
            markdown = "```\n``` aaa\n```\n",
            html = "<pre><code>``` aaa\n</code></pre>\n"
    )

    @Test
    @Ignore
    fun testHTMLBlocksExample148() = doTest(
            markdown = "<table><tr><td>\n<pre>\n**Hello**,\n\n_world_.\n</pre>\n</td></tr></table>\n",
            html = "<table><tr><td>\n<pre>\n**Hello**,\n<p><em>world</em>.\n</pre></p>\n</td></tr></table>\n"
    )

    @Test
    fun testHTMLBlocksExample149() = doTest(
            markdown = "<table>\n  <tr>\n    <td>\n           hi\n    </td>\n  </tr>\n</table>\n\nokay.\n",
            html = "<table>\n  <tr>\n    <td>\n           hi\n    </td>\n  </tr>\n</table>\n<p>okay.</p>\n"
    )

    @Test
    fun testHTMLBlocksExample150() = doTest(
            markdown = " <div>\n  *hello*\n         <foo><a>\n",
            html = " <div>\n  *hello*\n         <foo><a>\n"
    )

    @Test
    fun testHTMLBlocksExample151() = doTest(
            markdown = "</div>\n*foo*\n",
            html = "</div>\n*foo*\n"
    )

    @Test
    fun testHTMLBlocksExample152() = doTest(
            markdown = "<DIV CLASS=\"foo\">\n\n*Markdown*\n\n</DIV>\n",
            html = "<DIV CLASS=\"foo\">\n<p><em>Markdown</em></p>\n</DIV>\n"
    )

    @Test
    fun testHTMLBlocksExample153() = doTest(
            markdown = "<div id=\"foo\"\n  class=\"bar\">\n</div>\n",
            html = "<div id=\"foo\"\n  class=\"bar\">\n</div>\n"
    )

    @Test
    fun testHTMLBlocksExample154() = doTest(
            markdown = "<div id=\"foo\" class=\"bar\n  baz\">\n</div>\n",
            html = "<div id=\"foo\" class=\"bar\n  baz\">\n</div>\n"
    )

    @Test
    fun testHTMLBlocksExample155() = doTest(
            markdown = "<div>\n*foo*\n\n*bar*\n",
            html = "<div>\n*foo*\n<p><em>bar</em></p>\n"
    )

    @Test
    @Ignore
    fun testHTMLBlocksExample156() = doTest(
            markdown = "<div id=\"foo\"\n*hi*\n",
            html = "<div id=\"foo\"\n*hi*\n"
    )

    @Test
    @Ignore
    fun testHTMLBlocksExample157() = doTest(
            markdown = "<div class\nfoo\n",
            html = "<div class\nfoo\n"
    )

    @Test
    @Ignore
    fun testHTMLBlocksExample158() = doTest(
            markdown = "<div *???-&&&-<---\n*foo*\n",
            html = "<div *???-&&&-<---\n*foo*\n"
    )

    @Test
    fun testHTMLBlocksExample159() = doTest(
            markdown = "<div><a href=\"bar\">*foo*</a></div>\n",
            html = "<div><a href=\"bar\">*foo*</a></div>\n"
    )

    @Test
    fun testHTMLBlocksExample160() = doTest(
            markdown = "<table><tr><td>\nfoo\n</td></tr></table>\n",
            html = "<table><tr><td>\nfoo\n</td></tr></table>\n"
    )

    @Test
    fun testHTMLBlocksExample161() = doTest(
            markdown = "<div></div>\n``` c\nint x = 33;\n```\n",
            html = "<div></div>\n``` c\nint x = 33;\n```\n"
    )

    @Test
    fun testHTMLBlocksExample162() = doTest(
            markdown = "<a href=\"foo\">\n*bar*\n</a>\n",
            html = "<a href=\"foo\">\n*bar*\n</a>\n"
    )

    @Test
    fun testHTMLBlocksExample163() = doTest(
            markdown = "<Warning>\n*bar*\n</Warning>\n",
            html = "<Warning>\n*bar*\n</Warning>\n"
    )

    @Test
    fun testHTMLBlocksExample164() = doTest(
            markdown = "<i class=\"foo\">\n*bar*\n</i>\n",
            html = "<i class=\"foo\">\n*bar*\n</i>\n"
    )

    @Test
    fun testHTMLBlocksExample165() = doTest(
            markdown = "</ins>\n*bar*\n",
            html = "</ins>\n*bar*\n"
    )

    @Test
    fun testHTMLBlocksExample166() = doTest(
            markdown = "<del>\n*foo*\n</del>\n",
            html = "<del>\n*foo*\n</del>\n"
    )

    @Test
    fun testHTMLBlocksExample167() = doTest(
            markdown = "<del>\n\n*foo*\n\n</del>\n",
            html = "<del>\n<p><em>foo</em></p>\n</del>\n"
    )

    @Test
    fun testHTMLBlocksExample168() = doTest(
            markdown = "<del>*foo*</del>\n",
            html = "<p><del><em>foo</em></del></p>\n"
    )

    @Test
    fun testHTMLBlocksExample169() = doTest(
            markdown = "<pre language=\"haskell\"><code>\nimport Text.HTML.TagSoup\n\nmain :: IO ()\nmain = print \$ parseTags tags\n</code></pre>\nokay\n",
            html = "<pre language=\"haskell\"><code>\nimport Text.HTML.TagSoup\n\nmain :: IO ()\nmain = print \$ parseTags tags\n</code></pre>\n<p>okay</p>\n"
    )

    @Test
    fun testHTMLBlocksExample170() = doTest(
            markdown = "<script type=\"text/javascript\">\n// JavaScript example\n\ndocument.getElementById(\"demo\").innerHTML = \"Hello JavaScript!\";\n</script>\nokay\n",
            html = "<script type=\"text/javascript\">\n// JavaScript example\n\ndocument.getElementById(\"demo\").innerHTML = \"Hello JavaScript!\";\n</script>\n<p>okay</p>\n"
    )

    @Test
    @Ignore
    fun testHTMLBlocksExample171() = doTest(
            markdown = "<textarea>\n\n*foo*\n\n_bar_\n\n</textarea>\n",
            html = "<textarea>\n\n*foo*\n\n_bar_\n\n</textarea>\n"
    )

    @Test
    fun testHTMLBlocksExample172() = doTest(
            markdown = "<style\n  type=\"text/css\">\nh1 {color:red;}\n\np {color:blue;}\n</style>\nokay\n",
            html = "<style\n  type=\"text/css\">\nh1 {color:red;}\n\np {color:blue;}\n</style>\n<p>okay</p>\n"
    )

    @Test
    fun testHTMLBlocksExample173() = doTest(
            markdown = "<style\n  type=\"text/css\">\n\nfoo\n",
            html = "<style\n  type=\"text/css\">\n\nfoo\n"
    )

    @Test
    fun testHTMLBlocksExample174() = doTest(
            markdown = "> <div>\n> foo\n\nbar\n",
            html = "<blockquote>\n<div>\nfoo\n</blockquote>\n<p>bar</p>\n"
    )

    @Test
    fun testHTMLBlocksExample175() = doTest(
            markdown = "- <div>\n- foo\n",
            html = "<ul>\n<li>\n<div>\n</li>\n<li>foo</li>\n</ul>\n"
    )

    @Test
    fun testHTMLBlocksExample176() = doTest(
            markdown = "<style>p{color:red;}</style>\n*foo*\n",
            html = "<style>p{color:red;}</style>\n<p><em>foo</em></p>\n"
    )

    @Test
    fun testHTMLBlocksExample177() = doTest(
            markdown = "<!-- foo -->*bar*\n*baz*\n",
            html = "<!-- foo -->*bar*\n<p><em>baz</em></p>\n"
    )

    @Test
    fun testHTMLBlocksExample178() = doTest(
            markdown = "<script>\nfoo\n</script>1. *bar*\n",
            html = "<script>\nfoo\n</script>1. *bar*\n"
    )

    @Test
    fun testHTMLBlocksExample179() = doTest(
            markdown = "<!-- Foo\n\nbar\n   baz -->\nokay\n",
            html = "<!-- Foo\n\nbar\n   baz -->\n<p>okay</p>\n"
    )

    @Test
    fun testHTMLBlocksExample180() = doTest(
            markdown = "<?php\n\n  echo '>';\n\n?>\nokay\n",
            html = "<?php\n\n  echo '>';\n\n?>\n<p>okay</p>\n"
    )

    @Test
    fun testHTMLBlocksExample181() = doTest(
            markdown = "<!DOCTYPE html>\n",
            html = "<!DOCTYPE html>\n"
    )

    @Test
    fun testHTMLBlocksExample182() = doTest(
            markdown = "<![CDATA[\nfunction matchwo(a,b)\n{\n  if (a < b && a < 0) then {\n    return 1;\n\n  } else {\n\n    return 0;\n  }\n}\n]]>\nokay\n",
            html = "<![CDATA[\nfunction matchwo(a,b)\n{\n  if (a < b && a < 0) then {\n    return 1;\n\n  } else {\n\n    return 0;\n  }\n}\n]]>\n<p>okay</p>\n"
    )

    @Test
    fun testHTMLBlocksExample183() = doTest(
            markdown = "  <!-- foo -->\n\n    <!-- foo -->\n",
            html = "  <!-- foo -->\n<pre><code>&lt;!-- foo --&gt;\n</code></pre>\n"
    )

    @Test
    fun testHTMLBlocksExample184() = doTest(
            markdown = "  <div>\n\n    <div>\n",
            html = "  <div>\n<pre><code>&lt;div&gt;\n</code></pre>\n"
    )

    @Test
    fun testHTMLBlocksExample185() = doTest(
            markdown = "Foo\n<div>\nbar\n</div>\n",
            html = "<p>Foo</p>\n<div>\nbar\n</div>\n"
    )

    @Test
    fun testHTMLBlocksExample186() = doTest(
            markdown = "<div>\nbar\n</div>\n*foo*\n",
            html = "<div>\nbar\n</div>\n*foo*\n"
    )

    @Test
    fun testHTMLBlocksExample187() = doTest(
            markdown = "Foo\n<a href=\"bar\">\nbaz\n",
            html = "<p>Foo\n<a href=\"bar\">\nbaz</p>\n"
    )

    @Test
    fun testHTMLBlocksExample188() = doTest(
            markdown = "<div>\n\n*Emphasized* text.\n\n</div>\n",
            html = "<div>\n<p><em>Emphasized</em> text.</p>\n</div>\n"
    )

    @Test
    fun testHTMLBlocksExample189() = doTest(
            markdown = "<div>\n*Emphasized* text.\n</div>\n",
            html = "<div>\n*Emphasized* text.\n</div>\n"
    )

    @Test
    fun testHTMLBlocksExample190() = doTest(
            markdown = "<table>\n\n<tr>\n\n<td>\nHi\n</td>\n\n</tr>\n\n</table>\n",
            html = "<table>\n<tr>\n<td>\nHi\n</td>\n</tr>\n</table>\n"
    )

    @Test
    fun testHTMLBlocksExample191() = doTest(
            markdown = "<table>\n\n  <tr>\n\n    <td>\n      Hi\n    </td>\n\n  </tr>\n\n</table>\n",
            html = "<table>\n  <tr>\n<pre><code>&lt;td&gt;\n  Hi\n&lt;/td&gt;\n</code></pre>\n  </tr>\n</table>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample192() = doTest(
            markdown = "[foo]: /url \"title\"\n\n[foo]\n",
            html = "<p><a href=\"/url\" title=\"title\">foo</a></p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample193() = doTest(
            markdown = "   [foo]: \n      /url  \n           'the title'  \n\n[foo]\n",
            html = "<p><a href=\"/url\" title=\"the title\">foo</a></p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample194() = doTest(
            markdown = "[Foo*bar\\]]:my_(url) 'title (with parens)'\n\n[Foo*bar\\]]\n",
            html = "<p><a href=\"my_(url)\" title=\"title (with parens)\">Foo*bar]</a></p>\n"
    )

    @Test
    @Ignore
    fun testLinkReferenceDefinitionsExample195() = doTest(
            markdown = "[Foo bar]:\n<my url>\n'title'\n\n[Foo bar]\n",
            html = "<p><a href=\"my%20url\" title=\"title\">Foo bar</a></p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample196() = doTest(
            markdown = "[foo]: /url '\ntitle\nline1\nline2\n'\n\n[foo]\n",
            html = "<p><a href=\"/url\" title=\"\ntitle\nline1\nline2\n\">foo</a></p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample197() = doTest(
            markdown = "[foo]: /url 'title\n\nwith blank line'\n\n[foo]\n",
            html = "<p>[foo]: /url 'title</p>\n<p>with blank line'</p>\n<p>[foo]</p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample198() = doTest(
            markdown = "[foo]:\n/url\n\n[foo]\n",
            html = "<p><a href=\"/url\">foo</a></p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample199() = doTest(
            markdown = "[foo]:\n\n[foo]\n",
            html = "<p>[foo]:</p>\n<p>[foo]</p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample200() = doTest(
            markdown = "[foo]: <>\n\n[foo]\n",
            html = "<p><a href=\"\">foo</a></p>\n"
    )

    @Test
    @Ignore
    fun testLinkReferenceDefinitionsExample201() = doTest(
            markdown = "[foo]: <bar>(baz)\n\n[foo]\n",
            html = "<p>[foo]: <bar>(baz)</p>\n<p>[foo]</p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample202() = doTest(
            markdown = "[foo]: /url\\bar\\*baz \"foo\\\"bar\\baz\"\n\n[foo]\n",
            html = "<p><a href=\"/url%5Cbar*baz\" title=\"foo&quot;bar\\baz\">foo</a></p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample203() = doTest(
            markdown = "[foo]\n\n[foo]: url\n",
            html = "<p><a href=\"url\">foo</a></p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample204() = doTest(
            markdown = "[foo]\n\n[foo]: first\n[foo]: second\n",
            html = "<p><a href=\"first\">foo</a></p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample205() = doTest(
            markdown = "[FOO]: /url\n\n[Foo]\n",
            html = "<p><a href=\"/url\">Foo</a></p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample206() = doTest(
            markdown = "[ΑΓΩ]: /φου\n\n[αγω]\n",
            html = "<p><a href=\"/%CF%86%CE%BF%CF%85\">αγω</a></p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample207() = doTest(
            markdown = "[foo]: /url\n",
            html = ""
    )

    @Test
    fun testLinkReferenceDefinitionsExample208() = doTest(
            markdown = "[\nfoo\n]: /url\nbar\n",
            html = "<p>bar</p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample209() = doTest(
            markdown = "[foo]: /url \"title\" ok\n",
            html = "<p>[foo]: /url &quot;title&quot; ok</p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample210() = doTest(
            markdown = "[foo]: /url\n\"title\" ok\n",
            html = "<p>&quot;title&quot; ok</p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample211() = doTest(
            markdown = "    [foo]: /url \"title\"\n\n[foo]\n",
            html = "<pre><code>[foo]: /url &quot;title&quot;\n</code></pre>\n<p>[foo]</p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample212() = doTest(
            markdown = "```\n[foo]: /url\n```\n\n[foo]\n",
            html = "<pre><code>[foo]: /url\n</code></pre>\n<p>[foo]</p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample213() = doTest(
            markdown = "Foo\n[bar]: /baz\n\n[bar]\n",
            html = "<p>Foo\n[bar]: /baz</p>\n<p>[bar]</p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample214() = doTest(
            markdown = "# [Foo]\n[foo]: /url\n> bar\n",
            html = "<h1><a href=\"/url\">Foo</a></h1>\n<blockquote>\n<p>bar</p>\n</blockquote>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample215() = doTest(
            markdown = "[foo]: /url\nbar\n===\n[foo]\n",
            html = "<h1>bar</h1>\n<p><a href=\"/url\">foo</a></p>\n"
    )

    @Test
    @Ignore
    fun testLinkReferenceDefinitionsExample216() = doTest(
            markdown = "[foo]: /url\n===\n[foo]\n",
            html = "<p>===\n<a href=\"/url\">foo</a></p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample217() = doTest(
            markdown = "[foo]: /foo-url \"foo\"\n[bar]: /bar-url\n  \"bar\"\n[baz]: /baz-url\n\n[foo],\n[bar],\n[baz]\n",
            html = "<p><a href=\"/foo-url\" title=\"foo\">foo</a>,\n<a href=\"/bar-url\" title=\"bar\">bar</a>,\n<a href=\"/baz-url\">baz</a></p>\n"
    )

    @Test
    fun testLinkReferenceDefinitionsExample218() = doTest(
            markdown = "[foo]\n\n> [foo]: /url\n",
            html = "<p><a href=\"/url\">foo</a></p>\n<blockquote>\n</blockquote>\n"
    )

    @Test
    fun testParagraphsExample219() = doTest(
            markdown = "aaa\n\nbbb\n",
            html = "<p>aaa</p>\n<p>bbb</p>\n"
    )

    @Test
    fun testParagraphsExample220() = doTest(
            markdown = "aaa\nbbb\n\nccc\nddd\n",
            html = "<p>aaa\nbbb</p>\n<p>ccc\nddd</p>\n"
    )

    @Test
    fun testParagraphsExample221() = doTest(
            markdown = "aaa\n\n\nbbb\n",
            html = "<p>aaa</p>\n<p>bbb</p>\n"
    )

    @Test
    @Ignore
    fun testParagraphsExample222() = doTest(
            markdown = "  aaa\n bbb\n",
            html = "<p>aaa\nbbb</p>\n"
    )

    @Test
    @Ignore
    fun testParagraphsExample223() = doTest(
            markdown = "aaa\n             bbb\n                                       ccc\n",
            html = "<p>aaa\nbbb\nccc</p>\n"
    )

    @Test
    fun testParagraphsExample224() = doTest(
            markdown = "   aaa\nbbb\n",
            html = "<p>aaa\nbbb</p>\n"
    )

    @Test
    fun testParagraphsExample225() = doTest(
            markdown = "    aaa\nbbb\n",
            html = "<pre><code>aaa\n</code></pre>\n<p>bbb</p>\n"
    )

    @Test
    fun testParagraphsExample226() = doTest(
            markdown = "aaa     \nbbb     \n",
            html = "<p>aaa<br />\nbbb</p>\n"
    )

    @Test
    fun testBlankLinesExample227() = doTest(
            markdown = "  \n\naaa\n  \n\n# aaa\n\n  \n",
            html = "<p>aaa</p>\n<h1>aaa</h1>\n"
    )

    @Test
    @Ignore
    fun testBlockQuotesExample228() = doTest(
            markdown = "> # Foo\n> bar\n> baz\n",
            html = "<blockquote>\n<h1>Foo</h1>\n<p>bar\nbaz</p>\n</blockquote>\n"
    )

    @Test
    @Ignore
    fun testBlockQuotesExample229() = doTest(
            markdown = "># Foo\n>bar\n> baz\n",
            html = "<blockquote>\n<h1>Foo</h1>\n<p>bar\nbaz</p>\n</blockquote>\n"
    )

    @Test
    @Ignore
    fun testBlockQuotesExample230() = doTest(
            markdown = "   > # Foo\n   > bar\n > baz\n",
            html = "<blockquote>\n<h1>Foo</h1>\n<p>bar\nbaz</p>\n</blockquote>\n"
    )

    @Test
    fun testBlockQuotesExample231() = doTest(
            markdown = "    > # Foo\n    > bar\n    > baz\n",
            html = "<pre><code>&gt; # Foo\n&gt; bar\n&gt; baz\n</code></pre>\n"
    )

    @Test
    fun testBlockQuotesExample232() = doTest(
            markdown = "> # Foo\n> bar\nbaz\n",
            html = "<blockquote>\n<h1>Foo</h1>\n<p>bar\nbaz</p>\n</blockquote>\n"
    )

    @Test
    @Ignore
    fun testBlockQuotesExample233() = doTest(
            markdown = "> bar\nbaz\n> foo\n",
            html = "<blockquote>\n<p>bar\nbaz\nfoo</p>\n</blockquote>\n"
    )

    @Test
    fun testBlockQuotesExample234() = doTest(
            markdown = "> foo\n---\n",
            html = "<blockquote>\n<p>foo</p>\n</blockquote>\n<hr />\n"
    )

    @Test
    fun testBlockQuotesExample235() = doTest(
            markdown = "> - foo\n- bar\n",
            html = "<blockquote>\n<ul>\n<li>foo</li>\n</ul>\n</blockquote>\n<ul>\n<li>bar</li>\n</ul>\n"
    )

    @Test
    fun testBlockQuotesExample236() = doTest(
            markdown = ">     foo\n    bar\n",
            html = "<blockquote>\n<pre><code>foo\n</code></pre>\n</blockquote>\n<pre><code>bar\n</code></pre>\n"
    )

    @Test
    fun testBlockQuotesExample237() = doTest(
            markdown = "> ```\nfoo\n```\n",
            html = "<blockquote>\n<pre><code></code></pre>\n</blockquote>\n<p>foo</p>\n<pre><code></code></pre>\n"
    )

    @Test
    @Ignore
    fun testBlockQuotesExample238() = doTest(
            markdown = "> foo\n    - bar\n",
            html = "<blockquote>\n<p>foo\n- bar</p>\n</blockquote>\n"
    )

    @Test
    fun testBlockQuotesExample239() = doTest(
            markdown = ">\n",
            html = "<blockquote>\n</blockquote>\n"
    )

    @Test
    fun testBlockQuotesExample240() = doTest(
            markdown = ">\n>  \n> \n",
            html = "<blockquote>\n</blockquote>\n"
    )

    @Test
    fun testBlockQuotesExample241() = doTest(
            markdown = ">\n> foo\n>  \n",
            html = "<blockquote>\n<p>foo</p>\n</blockquote>\n"
    )

    @Test
    fun testBlockQuotesExample242() = doTest(
            markdown = "> foo\n\n> bar\n",
            html = "<blockquote>\n<p>foo</p>\n</blockquote>\n<blockquote>\n<p>bar</p>\n</blockquote>\n"
    )

    @Test
    @Ignore
    fun testBlockQuotesExample243() = doTest(
            markdown = "> foo\n> bar\n",
            html = "<blockquote>\n<p>foo\nbar</p>\n</blockquote>\n"
    )

    @Test
    fun testBlockQuotesExample244() = doTest(
            markdown = "> foo\n>\n> bar\n",
            html = "<blockquote>\n<p>foo</p>\n<p>bar</p>\n</blockquote>\n"
    )

    @Test
    fun testBlockQuotesExample245() = doTest(
            markdown = "foo\n> bar\n",
            html = "<p>foo</p>\n<blockquote>\n<p>bar</p>\n</blockquote>\n"
    )

    @Test
    fun testBlockQuotesExample246() = doTest(
            markdown = "> aaa\n***\n> bbb\n",
            html = "<blockquote>\n<p>aaa</p>\n</blockquote>\n<hr />\n<blockquote>\n<p>bbb</p>\n</blockquote>\n"
    )

    @Test
    fun testBlockQuotesExample247() = doTest(
            markdown = "> bar\nbaz\n",
            html = "<blockquote>\n<p>bar\nbaz</p>\n</blockquote>\n"
    )

    @Test
    fun testBlockQuotesExample248() = doTest(
            markdown = "> bar\n\nbaz\n",
            html = "<blockquote>\n<p>bar</p>\n</blockquote>\n<p>baz</p>\n"
    )

    @Test
    fun testBlockQuotesExample249() = doTest(
            markdown = "> bar\n>\nbaz\n",
            html = "<blockquote>\n<p>bar</p>\n</blockquote>\n<p>baz</p>\n"
    )

    @Test
    fun testBlockQuotesExample250() = doTest(
            markdown = "> > > foo\nbar\n",
            html = "<blockquote>\n<blockquote>\n<blockquote>\n<p>foo\nbar</p>\n</blockquote>\n</blockquote>\n</blockquote>\n"
    )

    @Test
    @Ignore
    fun testBlockQuotesExample251() = doTest(
            markdown = ">>> foo\n> bar\n>>baz\n",
            html = "<blockquote>\n<blockquote>\n<blockquote>\n<p>foo\nbar\nbaz</p>\n</blockquote>\n</blockquote>\n</blockquote>\n"
    )

    @Test
    fun testBlockQuotesExample252() = doTest(
            markdown = ">     code\n\n>    not code\n",
            html = "<blockquote>\n<pre><code>code\n</code></pre>\n</blockquote>\n<blockquote>\n<p>not code</p>\n</blockquote>\n"
    )

    @Test
    fun testListItemsExample253() = doTest(
            markdown = "A paragraph\nwith two lines.\n\n    indented code\n\n> A block quote.\n",
            html = "<p>A paragraph\nwith two lines.</p>\n<pre><code>indented code\n</code></pre>\n<blockquote>\n<p>A block quote.</p>\n</blockquote>\n"
    )

    @Test
    @Ignore
    fun testListItemsExample254() = doTest(
            markdown = "1.  A paragraph\n    with two lines.\n\n        indented code\n\n    > A block quote.\n",
            html = "<ol>\n<li>\n<p>A paragraph\nwith two lines.</p>\n<pre><code>indented code\n</code></pre>\n<blockquote>\n<p>A block quote.</p>\n</blockquote>\n</li>\n</ol>\n"
    )

    @Test
    fun testListItemsExample255() = doTest(
            markdown = "- one\n\n two\n",
            html = "<ul>\n<li>one</li>\n</ul>\n<p>two</p>\n"
    )

    @Test
    fun testListItemsExample256() = doTest(
            markdown = "- one\n\n  two\n",
            html = "<ul>\n<li>\n<p>one</p>\n<p>two</p>\n</li>\n</ul>\n"
    )

    @Test
    fun testListItemsExample257() = doTest(
            markdown = " -    one\n\n     two\n",
            html = "<ul>\n<li>one</li>\n</ul>\n<pre><code> two\n</code></pre>\n"
    )

    @Test
    fun testListItemsExample258() = doTest(
            markdown = " -    one\n\n      two\n",
            html = "<ul>\n<li>\n<p>one</p>\n<p>two</p>\n</li>\n</ul>\n"
    )

    @Test
    fun testListItemsExample259() = doTest(
            markdown = "   > > 1.  one\n>>\n>>     two\n",
            html = "<blockquote>\n<blockquote>\n<ol>\n<li>\n<p>one</p>\n<p>two</p>\n</li>\n</ol>\n</blockquote>\n</blockquote>\n"
    )

    @Test
    fun testListItemsExample260() = doTest(
            markdown = ">>- one\n>>\n  >  > two\n",
            html = "<blockquote>\n<blockquote>\n<ul>\n<li>one</li>\n</ul>\n<p>two</p>\n</blockquote>\n</blockquote>\n"
    )

    @Test
    fun testListItemsExample261() = doTest(
            markdown = "-one\n\n2.two\n",
            html = "<p>-one</p>\n<p>2.two</p>\n"
    )

    @Test
    @Ignore
    fun testListItemsExample262() = doTest(
            markdown = "- foo\n\n\n  bar\n",
            html = "<ul>\n<li>\n<p>foo</p>\n<p>bar</p>\n</li>\n</ul>\n"
    )

    @Test
    fun testListItemsExample263() = doTest(
            markdown = "1.  foo\n\n    ```\n    bar\n    ```\n\n    baz\n\n    > bam\n",
            html = "<ol>\n<li>\n<p>foo</p>\n<pre><code>bar\n</code></pre>\n<p>baz</p>\n<blockquote>\n<p>bam</p>\n</blockquote>\n</li>\n</ol>\n"
    )

    @Test
    fun testListItemsExample264() = doTest(
            markdown = "- Foo\n\n      bar\n\n\n      baz\n",
            html = "<ul>\n<li>\n<p>Foo</p>\n<pre><code>bar\n\n\nbaz\n</code></pre>\n</li>\n</ul>\n"
    )

    @Test
    fun testListItemsExample265() = doTest(
            markdown = "123456789. ok\n",
            html = "<ol start=\"123456789\">\n<li>ok</li>\n</ol>\n"
    )

    @Test
    fun testListItemsExample266() = doTest(
            markdown = "1234567890. not ok\n",
            html = "<p>1234567890. not ok</p>\n"
    )

    @Test
    fun testListItemsExample267() = doTest(
            markdown = "0. ok\n",
            html = "<ol start=\"0\">\n<li>ok</li>\n</ol>\n"
    )

    @Test
    fun testListItemsExample268() = doTest(
            markdown = "003. ok\n",
            html = "<ol start=\"3\">\n<li>ok</li>\n</ol>\n"
    )

    @Test
    fun testListItemsExample269() = doTest(
            markdown = "-1. not ok\n",
            html = "<p>-1. not ok</p>\n"
    )

    @Test
    fun testListItemsExample270() = doTest(
            markdown = "- foo\n\n      bar\n",
            html = "<ul>\n<li>\n<p>foo</p>\n<pre><code>bar\n</code></pre>\n</li>\n</ul>\n"
    )

    @Test
    fun testListItemsExample271() = doTest(
            markdown = "  10.  foo\n\n           bar\n",
            html = "<ol start=\"10\">\n<li>\n<p>foo</p>\n<pre><code>bar\n</code></pre>\n</li>\n</ol>\n"
    )

    @Test
    fun testListItemsExample272() = doTest(
            markdown = "    indented code\n\nparagraph\n\n    more code\n",
            html = "<pre><code>indented code\n</code></pre>\n<p>paragraph</p>\n<pre><code>more code\n</code></pre>\n"
    )

    @Test
    fun testListItemsExample273() = doTest(
            markdown = "1.     indented code\n\n   paragraph\n\n       more code\n",
            html = "<ol>\n<li>\n<pre><code>indented code\n</code></pre>\n<p>paragraph</p>\n<pre><code>more code\n</code></pre>\n</li>\n</ol>\n"
    )

    @Test
    fun testListItemsExample274() = doTest(
            markdown = "1.      indented code\n\n   paragraph\n\n       more code\n",
            html = "<ol>\n<li>\n<pre><code> indented code\n</code></pre>\n<p>paragraph</p>\n<pre><code>more code\n</code></pre>\n</li>\n</ol>\n"
    )

    @Test
    fun testListItemsExample275() = doTest(
            markdown = "   foo\n\nbar\n",
            html = "<p>foo</p>\n<p>bar</p>\n"
    )

    @Test
    fun testListItemsExample276() = doTest(
            markdown = "-    foo\n\n  bar\n",
            html = "<ul>\n<li>foo</li>\n</ul>\n<p>bar</p>\n"
    )

    @Test
    fun testListItemsExample277() = doTest(
            markdown = "-  foo\n\n   bar\n",
            html = "<ul>\n<li>\n<p>foo</p>\n<p>bar</p>\n</li>\n</ul>\n"
    )

    @Test
    fun testListItemsExample278() = doTest(
            markdown = "-\n  foo\n-\n  ```\n  bar\n  ```\n-\n      baz\n",
            html = "<ul>\n<li>foo</li>\n<li>\n<pre><code>bar\n</code></pre>\n</li>\n<li>\n<pre><code>baz\n</code></pre>\n</li>\n</ul>\n"
    )

    @Test
    fun testListItemsExample279() = doTest(
            markdown = "-   \n  foo\n",
            html = "<ul>\n<li>foo</li>\n</ul>\n"
    )

    @Test
    @Ignore
    fun testListItemsExample280() = doTest(
            markdown = "-\n\n  foo\n",
            html = "<ul>\n<li></li>\n</ul>\n<p>foo</p>\n"
    )

    @Test
    fun testListItemsExample281() = doTest(
            markdown = "- foo\n-\n- bar\n",
            html = "<ul>\n<li>foo</li>\n<li></li>\n<li>bar</li>\n</ul>\n"
    )

    @Test
    fun testListItemsExample282() = doTest(
            markdown = "- foo\n-   \n- bar\n",
            html = "<ul>\n<li>foo</li>\n<li></li>\n<li>bar</li>\n</ul>\n"
    )

    @Test
    fun testListItemsExample283() = doTest(
            markdown = "1. foo\n2.\n3. bar\n",
            html = "<ol>\n<li>foo</li>\n<li></li>\n<li>bar</li>\n</ol>\n"
    )

    @Test
    fun testListItemsExample284() = doTest(
            markdown = "*\n",
            html = "<ul>\n<li></li>\n</ul>\n"
    )

    @Test
    @Ignore
    fun testListItemsExample285() = doTest(
            markdown = "foo\n*\n\nfoo\n1.\n",
            html = "<p>foo\n*</p>\n<p>foo\n1.</p>\n"
    )

    @Test
    @Ignore
    fun testListItemsExample286() = doTest(
            markdown = " 1.  A paragraph\n     with two lines.\n\n         indented code\n\n     > A block quote.\n",
            html = "<ol>\n<li>\n<p>A paragraph\nwith two lines.</p>\n<pre><code>indented code\n</code></pre>\n<blockquote>\n<p>A block quote.</p>\n</blockquote>\n</li>\n</ol>\n"
    )

    @Test
    @Ignore
    fun testListItemsExample287() = doTest(
            markdown = "  1.  A paragraph\n      with two lines.\n\n          indented code\n\n      > A block quote.\n",
            html = "<ol>\n<li>\n<p>A paragraph\nwith two lines.</p>\n<pre><code>indented code\n</code></pre>\n<blockquote>\n<p>A block quote.</p>\n</blockquote>\n</li>\n</ol>\n"
    )

    @Test
    @Ignore
    fun testListItemsExample288() = doTest(
            markdown = "   1.  A paragraph\n       with two lines.\n\n           indented code\n\n       > A block quote.\n",
            html = "<ol>\n<li>\n<p>A paragraph\nwith two lines.</p>\n<pre><code>indented code\n</code></pre>\n<blockquote>\n<p>A block quote.</p>\n</blockquote>\n</li>\n</ol>\n"
    )

    @Test
    fun testListItemsExample289() = doTest(
            markdown = "    1.  A paragraph\n        with two lines.\n\n            indented code\n\n        > A block quote.\n",
            html = "<pre><code>1.  A paragraph\n    with two lines.\n\n        indented code\n\n    &gt; A block quote.\n</code></pre>\n"
    )

    @Test
    fun testListItemsExample290() = doTest(
            markdown = "  1.  A paragraph\nwith two lines.\n\n          indented code\n\n      > A block quote.\n",
            html = "<ol>\n<li>\n<p>A paragraph\nwith two lines.</p>\n<pre><code>indented code\n</code></pre>\n<blockquote>\n<p>A block quote.</p>\n</blockquote>\n</li>\n</ol>\n"
    )

    @Test
    @Ignore
    fun testListItemsExample291() = doTest(
            markdown = "  1.  A paragraph\n    with two lines.\n",
            html = "<ol>\n<li>A paragraph\nwith two lines.</li>\n</ol>\n"
    )

    @Test
    fun testListItemsExample292() = doTest(
            markdown = "> 1. > Blockquote\ncontinued here.\n",
            html = "<blockquote>\n<ol>\n<li>\n<blockquote>\n<p>Blockquote\ncontinued here.</p>\n</blockquote>\n</li>\n</ol>\n</blockquote>\n"
    )

    @Test
    @Ignore
    fun testListItemsExample293() = doTest(
            markdown = "> 1. > Blockquote\n> continued here.\n",
            html = "<blockquote>\n<ol>\n<li>\n<blockquote>\n<p>Blockquote\ncontinued here.</p>\n</blockquote>\n</li>\n</ol>\n</blockquote>\n"
    )

    @Test
    fun testListItemsExample294() = doTest(
            markdown = "- foo\n  - bar\n    - baz\n      - boo\n",
            html = "<ul>\n<li>foo\n<ul>\n<li>bar\n<ul>\n<li>baz\n<ul>\n<li>boo</li>\n</ul>\n</li>\n</ul>\n</li>\n</ul>\n</li>\n</ul>\n"
    )

    @Test
    fun testListItemsExample295() = doTest(
            markdown = "- foo\n - bar\n  - baz\n   - boo\n",
            html = "<ul>\n<li>foo</li>\n<li>bar</li>\n<li>baz</li>\n<li>boo</li>\n</ul>\n"
    )

    @Test
    fun testListItemsExample296() = doTest(
            markdown = "10) foo\n    - bar\n",
            html = "<ol start=\"10\">\n<li>foo\n<ul>\n<li>bar</li>\n</ul>\n</li>\n</ol>\n"
    )

    @Test
    fun testListItemsExample297() = doTest(
            markdown = "10) foo\n   - bar\n",
            html = "<ol start=\"10\">\n<li>foo</li>\n</ol>\n<ul>\n<li>bar</li>\n</ul>\n"
    )

    @Test
    fun testListItemsExample298() = doTest(
            markdown = "- - foo\n",
            html = "<ul>\n<li>\n<ul>\n<li>foo</li>\n</ul>\n</li>\n</ul>\n"
    )

    @Test
    fun testListItemsExample299() = doTest(
            markdown = "1. - 2. foo\n",
            html = "<ol>\n<li>\n<ul>\n<li>\n<ol start=\"2\">\n<li>foo</li>\n</ol>\n</li>\n</ul>\n</li>\n</ol>\n"
    )

    @Test
    fun testListItemsExample300() = doTest(
            markdown = "- # Foo\n- Bar\n  ---\n  baz\n",
            html = "<ul>\n<li>\n<h1>Foo</h1>\n</li>\n<li>\n<h2>Bar</h2>\nbaz</li>\n</ul>\n"
    )

    @Test
    fun testListsExample301() = doTest(
            markdown = "- foo\n- bar\n+ baz\n",
            html = "<ul>\n<li>foo</li>\n<li>bar</li>\n</ul>\n<ul>\n<li>baz</li>\n</ul>\n"
    )

    @Test
    fun testListsExample302() = doTest(
            markdown = "1. foo\n2. bar\n3) baz\n",
            html = "<ol>\n<li>foo</li>\n<li>bar</li>\n</ol>\n<ol start=\"3\">\n<li>baz</li>\n</ol>\n"
    )

    @Test
    fun testListsExample303() = doTest(
            markdown = "Foo\n- bar\n- baz\n",
            html = "<p>Foo</p>\n<ul>\n<li>bar</li>\n<li>baz</li>\n</ul>\n"
    )

    @Test
    @Ignore
    fun testListsExample304() = doTest(
            markdown = "The number of windows in my house is\n14.  The number of doors is 6.\n",
            html = "<p>The number of windows in my house is\n14.  The number of doors is 6.</p>\n"
    )

    @Test
    fun testListsExample305() = doTest(
            markdown = "The number of windows in my house is\n1.  The number of doors is 6.\n",
            html = "<p>The number of windows in my house is</p>\n<ol>\n<li>The number of doors is 6.</li>\n</ol>\n"
    )

    @Test
    @Ignore
    fun testListsExample306() = doTest(
            markdown = "- foo\n\n- bar\n\n\n- baz\n",
            html = "<ul>\n<li>\n<p>foo</p>\n</li>\n<li>\n<p>bar</p>\n</li>\n<li>\n<p>baz</p>\n</li>\n</ul>\n"
    )

    @Test
    @Ignore
    fun testListsExample307() = doTest(
            markdown = "- foo\n  - bar\n    - baz\n\n\n      bim\n",
            html = "<ul>\n<li>foo\n<ul>\n<li>bar\n<ul>\n<li>\n<p>baz</p>\n<p>bim</p>\n</li>\n</ul>\n</li>\n</ul>\n</li>\n</ul>\n"
    )

    @Test
    fun testListsExample308() = doTest(
            markdown = "- foo\n- bar\n\n<!-- -->\n\n- baz\n- bim\n",
            html = "<ul>\n<li>foo</li>\n<li>bar</li>\n</ul>\n<!-- -->\n<ul>\n<li>baz</li>\n<li>bim</li>\n</ul>\n"
    )

    @Test
    fun testListsExample309() = doTest(
            markdown = "-   foo\n\n    notcode\n\n-   foo\n\n<!-- -->\n\n    code\n",
            html = "<ul>\n<li>\n<p>foo</p>\n<p>notcode</p>\n</li>\n<li>\n<p>foo</p>\n</li>\n</ul>\n<!-- -->\n<pre><code>code\n</code></pre>\n"
    )

    @Test
    fun testListsExample310() = doTest(
            markdown = "- a\n - b\n  - c\n   - d\n  - e\n - f\n- g\n",
            html = "<ul>\n<li>a</li>\n<li>b</li>\n<li>c</li>\n<li>d</li>\n<li>e</li>\n<li>f</li>\n<li>g</li>\n</ul>\n"
    )

    @Test
    fun testListsExample311() = doTest(
            markdown = "1. a\n\n  2. b\n\n   3. c\n",
            html = "<ol>\n<li>\n<p>a</p>\n</li>\n<li>\n<p>b</p>\n</li>\n<li>\n<p>c</p>\n</li>\n</ol>\n"
    )

    @Test
    @Ignore
    fun testListsExample312() = doTest(
            markdown = "- a\n - b\n  - c\n   - d\n    - e\n",
            html = "<ul>\n<li>a</li>\n<li>b</li>\n<li>c</li>\n<li>d\n- e</li>\n</ul>\n"
    )

    @Test
    @Ignore
    fun testListsExample313() = doTest(
            markdown = "1. a\n\n  2. b\n\n    3. c\n",
            html = "<ol>\n<li>\n<p>a</p>\n</li>\n<li>\n<p>b</p>\n</li>\n</ol>\n<pre><code>3. c\n</code></pre>\n"
    )

    @Test
    fun testListsExample314() = doTest(
            markdown = "- a\n- b\n\n- c\n",
            html = "<ul>\n<li>\n<p>a</p>\n</li>\n<li>\n<p>b</p>\n</li>\n<li>\n<p>c</p>\n</li>\n</ul>\n"
    )

    @Test
    fun testListsExample315() = doTest(
            markdown = "* a\n*\n\n* c\n",
            html = "<ul>\n<li>\n<p>a</p>\n</li>\n<li></li>\n<li>\n<p>c</p>\n</li>\n</ul>\n"
    )

    @Test
    fun testListsExample316() = doTest(
            markdown = "- a\n- b\n\n  c\n- d\n",
            html = "<ul>\n<li>\n<p>a</p>\n</li>\n<li>\n<p>b</p>\n<p>c</p>\n</li>\n<li>\n<p>d</p>\n</li>\n</ul>\n"
    )

    @Test
    fun testListsExample317() = doTest(
            markdown = "- a\n- b\n\n  [ref]: /url\n- d\n",
            html = "<ul>\n<li>\n<p>a</p>\n</li>\n<li>\n<p>b</p>\n</li>\n<li>\n<p>d</p>\n</li>\n</ul>\n"
    )

    @Test
    fun testListsExample318() = doTest(
            markdown = "- a\n- ```\n  b\n\n\n  ```\n- c\n",
            html = "<ul>\n<li>a</li>\n<li>\n<pre><code>b\n\n\n</code></pre>\n</li>\n<li>c</li>\n</ul>\n"
    )

    @Test
    fun testListsExample319() = doTest(
            markdown = "- a\n  - b\n\n    c\n- d\n",
            html = "<ul>\n<li>a\n<ul>\n<li>\n<p>b</p>\n<p>c</p>\n</li>\n</ul>\n</li>\n<li>d</li>\n</ul>\n"
    )

    @Test
    fun testListsExample320() = doTest(
            markdown = "* a\n  > b\n  >\n* c\n",
            html = "<ul>\n<li>a\n<blockquote>\n<p>b</p>\n</blockquote>\n</li>\n<li>c</li>\n</ul>\n"
    )

    @Test
    fun testListsExample321() = doTest(
            markdown = "- a\n  > b\n  ```\n  c\n  ```\n- d\n",
            html = "<ul>\n<li>a\n<blockquote>\n<p>b</p>\n</blockquote>\n<pre><code>c\n</code></pre>\n</li>\n<li>d</li>\n</ul>\n"
    )

    @Test
    fun testListsExample322() = doTest(
            markdown = "- a\n",
            html = "<ul>\n<li>a</li>\n</ul>\n"
    )

    @Test
    fun testListsExample323() = doTest(
            markdown = "- a\n  - b\n",
            html = "<ul>\n<li>a\n<ul>\n<li>b</li>\n</ul>\n</li>\n</ul>\n"
    )

    @Test
    fun testListsExample324() = doTest(
            markdown = "1. ```\n   foo\n   ```\n\n   bar\n",
            html = "<ol>\n<li>\n<pre><code>foo\n</code></pre>\n<p>bar</p>\n</li>\n</ol>\n"
    )

    @Test
    fun testListsExample325() = doTest(
            markdown = "* foo\n  * bar\n\n  baz\n",
            html = "<ul>\n<li>\n<p>foo</p>\n<ul>\n<li>bar</li>\n</ul>\n<p>baz</p>\n</li>\n</ul>\n"
    )

    @Test
    fun testListsExample326() = doTest(
            markdown = "- a\n  - b\n  - c\n\n- d\n  - e\n  - f\n",
            html = "<ul>\n<li>\n<p>a</p>\n<ul>\n<li>b</li>\n<li>c</li>\n</ul>\n</li>\n<li>\n<p>d</p>\n<ul>\n<li>e</li>\n<li>f</li>\n</ul>\n</li>\n</ul>\n"
    )

    @Test
    fun testInlinesExample327() = doTest(
            markdown = "`hi`lo`\n",
            html = "<p><code>hi</code>lo`</p>\n"
    )

    @Test
    fun testCodeSpansExample328() = doTest(
            markdown = "`foo`\n",
            html = "<p><code>foo</code></p>\n"
    )

    @Test
    fun testCodeSpansExample329() = doTest(
            markdown = "`` foo ` bar ``\n",
            html = "<p><code>foo ` bar</code></p>\n"
    )

    @Test
    fun testCodeSpansExample330() = doTest(
            markdown = "` `` `\n",
            html = "<p><code>``</code></p>\n"
    )

    @Test
    @Ignore
    fun testCodeSpansExample331() = doTest(
            markdown = "`  ``  `\n",
            html = "<p><code> `` </code></p>\n"
    )

    @Test
    @Ignore
    fun testCodeSpansExample332() = doTest(
            markdown = "` a`\n",
            html = "<p><code> a</code></p>\n"
    )

    @Test
    @Ignore
    fun testCodeSpansExample333() = doTest(
            markdown = "` b `\n",
            html = "<p><code> b </code></p>\n"
    )

    @Test
    @Ignore
    fun testCodeSpansExample334() = doTest(
            markdown = "` `\n`  `\n",
            html = "<p><code> </code>\n<code>  </code></p>\n"
    )

    @Test
    @Ignore
    fun testCodeSpansExample335() = doTest(
            markdown = "``\nfoo\nbar  \nbaz\n``\n",
            html = "<p><code>foo bar   baz</code></p>\n"
    )

    @Test
    @Ignore
    fun testCodeSpansExample336() = doTest(
            markdown = "``\nfoo \n``\n",
            html = "<p><code>foo </code></p>\n"
    )

    @Test
    @Ignore
    fun testCodeSpansExample337() = doTest(
            markdown = "`foo   bar \nbaz`\n",
            html = "<p><code>foo   bar  baz</code></p>\n"
    )

    @Test
    @Ignore
    fun testCodeSpansExample338() = doTest(
            markdown = "`foo\\`bar`\n",
            html = "<p><code>foo\\</code>bar`</p>\n"
    )

    @Test
    fun testCodeSpansExample339() = doTest(
            markdown = "``foo`bar``\n",
            html = "<p><code>foo`bar</code></p>\n"
    )

    @Test
    fun testCodeSpansExample340() = doTest(
            markdown = "` foo `` bar `\n",
            html = "<p><code>foo `` bar</code></p>\n"
    )

    @Test
    fun testCodeSpansExample341() = doTest(
            markdown = "*foo`*`\n",
            html = "<p>*foo<code>*</code></p>\n"
    )

    @Test
    fun testCodeSpansExample342() = doTest(
            markdown = "[not a `link](/foo`)\n",
            html = "<p>[not a <code>link](/foo</code>)</p>\n"
    )

    @Test
    @Ignore
    fun testCodeSpansExample343() = doTest(
            markdown = "`<a href=\"`\">`\n",
            html = "<p><code>&lt;a href=&quot;</code>&quot;&gt;`</p>\n"
    )

    @Test
    fun testCodeSpansExample344() = doTest(
            markdown = "<a href=\"`\">`\n",
            html = "<p><a href=\"`\">`</p>\n"
    )

    @Test
    @Ignore
    fun testCodeSpansExample345() = doTest(
            markdown = "`<http://foo.bar.`baz>`\n",
            html = "<p><code>&lt;http://foo.bar.</code>baz&gt;`</p>\n"
    )

    @Test
    fun testCodeSpansExample346() = doTest(
            markdown = "<http://foo.bar.`baz>`\n",
            html = "<p><a href=\"http://foo.bar.%60baz\">http://foo.bar.`baz</a>`</p>\n"
    )

    @Test
    fun testCodeSpansExample347() = doTest(
            markdown = "```foo``\n",
            html = "<p>```foo``</p>\n"
    )

    @Test
    fun testCodeSpansExample348() = doTest(
            markdown = "`foo\n",
            html = "<p>`foo</p>\n"
    )

    @Test
    fun testCodeSpansExample349() = doTest(
            markdown = "`foo``bar``\n",
            html = "<p>`foo<code>bar</code></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample350() = doTest(
            markdown = "*foo bar*\n",
            html = "<p><em>foo bar</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample351() = doTest(
            markdown = "a * foo bar*\n",
            html = "<p>a * foo bar*</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample352() = doTest(
            markdown = "a*\"foo\"*\n",
            html = "<p>a*&quot;foo&quot;*</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample353() = doTest(
            markdown = "* a *\n",
            html = "<p>* a *</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample354() = doTest(
            markdown = "foo*bar*\n",
            html = "<p>foo<em>bar</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample355() = doTest(
            markdown = "5*6*78\n",
            html = "<p>5<em>6</em>78</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample356() = doTest(
            markdown = "_foo bar_\n",
            html = "<p><em>foo bar</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample357() = doTest(
            markdown = "_ foo bar_\n",
            html = "<p>_ foo bar_</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample358() = doTest(
            markdown = "a_\"foo\"_\n",
            html = "<p>a_&quot;foo&quot;_</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample359() = doTest(
            markdown = "foo_bar_\n",
            html = "<p>foo_bar_</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample360() = doTest(
            markdown = "5_6_78\n",
            html = "<p>5_6_78</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample361() = doTest(
            markdown = "пристаням_стремятся_\n",
            html = "<p>пристаням_стремятся_</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample362() = doTest(
            markdown = "aa_\"bb\"_cc\n",
            html = "<p>aa_&quot;bb&quot;_cc</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample363() = doTest(
            markdown = "foo-_(bar)_\n",
            html = "<p>foo-<em>(bar)</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample364() = doTest(
            markdown = "_foo*\n",
            html = "<p>_foo*</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample365() = doTest(
            markdown = "*foo bar *\n",
            html = "<p>*foo bar *</p>\n"
    )

    @Test
    @Ignore
    fun testEmphasisAndStrongEmphasisExample366() = doTest(
            markdown = "*foo bar\n*\n",
            html = "<p>*foo bar\n*</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample367() = doTest(
            markdown = "*(*foo)\n",
            html = "<p>*(*foo)</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample368() = doTest(
            markdown = "*(*foo*)*\n",
            html = "<p><em>(<em>foo</em>)</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample369() = doTest(
            markdown = "*foo*bar\n",
            html = "<p><em>foo</em>bar</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample370() = doTest(
            markdown = "_foo bar _\n",
            html = "<p>_foo bar _</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample371() = doTest(
            markdown = "_(_foo)\n",
            html = "<p>_(_foo)</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample372() = doTest(
            markdown = "_(_foo_)_\n",
            html = "<p><em>(<em>foo</em>)</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample373() = doTest(
            markdown = "_foo_bar\n",
            html = "<p>_foo_bar</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample374() = doTest(
            markdown = "_пристаням_стремятся\n",
            html = "<p>_пристаням_стремятся</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample375() = doTest(
            markdown = "_foo_bar_baz_\n",
            html = "<p><em>foo_bar_baz</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample376() = doTest(
            markdown = "_(bar)_.\n",
            html = "<p><em>(bar)</em>.</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample377() = doTest(
            markdown = "**foo bar**\n",
            html = "<p><strong>foo bar</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample378() = doTest(
            markdown = "** foo bar**\n",
            html = "<p>** foo bar**</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample379() = doTest(
            markdown = "a**\"foo\"**\n",
            html = "<p>a**&quot;foo&quot;**</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample380() = doTest(
            markdown = "foo**bar**\n",
            html = "<p>foo<strong>bar</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample381() = doTest(
            markdown = "__foo bar__\n",
            html = "<p><strong>foo bar</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample382() = doTest(
            markdown = "__ foo bar__\n",
            html = "<p>__ foo bar__</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample383() = doTest(
            markdown = "__\nfoo bar__\n",
            html = "<p>__\nfoo bar__</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample384() = doTest(
            markdown = "a__\"foo\"__\n",
            html = "<p>a__&quot;foo&quot;__</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample385() = doTest(
            markdown = "foo__bar__\n",
            html = "<p>foo__bar__</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample386() = doTest(
            markdown = "5__6__78\n",
            html = "<p>5__6__78</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample387() = doTest(
            markdown = "пристаням__стремятся__\n",
            html = "<p>пристаням__стремятся__</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample388() = doTest(
            markdown = "__foo, __bar__, baz__\n",
            html = "<p><strong>foo, <strong>bar</strong>, baz</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample389() = doTest(
            markdown = "foo-__(bar)__\n",
            html = "<p>foo-<strong>(bar)</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample390() = doTest(
            markdown = "**foo bar **\n",
            html = "<p>**foo bar **</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample391() = doTest(
            markdown = "**(**foo)\n",
            html = "<p>**(**foo)</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample392() = doTest(
            markdown = "*(**foo**)*\n",
            html = "<p><em>(<strong>foo</strong>)</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample393() = doTest(
            markdown = "**Gomphocarpus (*Gomphocarpus physocarpus*, syn.\n*Asclepias physocarpa*)**\n",
            html = "<p><strong>Gomphocarpus (<em>Gomphocarpus physocarpus</em>, syn.\n<em>Asclepias physocarpa</em>)</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample394() = doTest(
            markdown = "**foo \"*bar*\" foo**\n",
            html = "<p><strong>foo &quot;<em>bar</em>&quot; foo</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample395() = doTest(
            markdown = "**foo**bar\n",
            html = "<p><strong>foo</strong>bar</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample396() = doTest(
            markdown = "__foo bar __\n",
            html = "<p>__foo bar __</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample397() = doTest(
            markdown = "__(__foo)\n",
            html = "<p>__(__foo)</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample398() = doTest(
            markdown = "_(__foo__)_\n",
            html = "<p><em>(<strong>foo</strong>)</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample399() = doTest(
            markdown = "__foo__bar\n",
            html = "<p>__foo__bar</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample400() = doTest(
            markdown = "__пристаням__стремятся\n",
            html = "<p>__пристаням__стремятся</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample401() = doTest(
            markdown = "__foo__bar__baz__\n",
            html = "<p><strong>foo__bar__baz</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample402() = doTest(
            markdown = "__(bar)__.\n",
            html = "<p><strong>(bar)</strong>.</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample403() = doTest(
            markdown = "*foo [bar](/url)*\n",
            html = "<p><em>foo <a href=\"/url\">bar</a></em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample404() = doTest(
            markdown = "*foo\nbar*\n",
            html = "<p><em>foo\nbar</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample405() = doTest(
            markdown = "_foo __bar__ baz_\n",
            html = "<p><em>foo <strong>bar</strong> baz</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample406() = doTest(
            markdown = "_foo _bar_ baz_\n",
            html = "<p><em>foo <em>bar</em> baz</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample407() = doTest(
            markdown = "__foo_ bar_\n",
            html = "<p><em><em>foo</em> bar</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample408() = doTest(
            markdown = "*foo *bar**\n",
            html = "<p><em>foo <em>bar</em></em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample409() = doTest(
            markdown = "*foo **bar** baz*\n",
            html = "<p><em>foo <strong>bar</strong> baz</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample410() = doTest(
            markdown = "*foo**bar**baz*\n",
            html = "<p><em>foo<strong>bar</strong>baz</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample411() = doTest(
            markdown = "*foo**bar*\n",
            html = "<p><em>foo**bar</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample412() = doTest(
            markdown = "***foo** bar*\n",
            html = "<p><em><strong>foo</strong> bar</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample413() = doTest(
            markdown = "*foo **bar***\n",
            html = "<p><em>foo <strong>bar</strong></em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample414() = doTest(
            markdown = "*foo**bar***\n",
            html = "<p><em>foo<strong>bar</strong></em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample415() = doTest(
            markdown = "foo***bar***baz\n",
            html = "<p>foo<em><strong>bar</strong></em>baz</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample416() = doTest(
            markdown = "foo******bar*********baz\n",
            html = "<p>foo<strong><strong><strong>bar</strong></strong></strong>***baz</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample417() = doTest(
            markdown = "*foo **bar *baz* bim** bop*\n",
            html = "<p><em>foo <strong>bar <em>baz</em> bim</strong> bop</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample418() = doTest(
            markdown = "*foo [*bar*](/url)*\n",
            html = "<p><em>foo <a href=\"/url\"><em>bar</em></a></em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample419() = doTest(
            markdown = "** is not an empty emphasis\n",
            html = "<p>** is not an empty emphasis</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample420() = doTest(
            markdown = "**** is not an empty strong emphasis\n",
            html = "<p>**** is not an empty strong emphasis</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample421() = doTest(
            markdown = "**foo [bar](/url)**\n",
            html = "<p><strong>foo <a href=\"/url\">bar</a></strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample422() = doTest(
            markdown = "**foo\nbar**\n",
            html = "<p><strong>foo\nbar</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample423() = doTest(
            markdown = "__foo _bar_ baz__\n",
            html = "<p><strong>foo <em>bar</em> baz</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample424() = doTest(
            markdown = "__foo __bar__ baz__\n",
            html = "<p><strong>foo <strong>bar</strong> baz</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample425() = doTest(
            markdown = "____foo__ bar__\n",
            html = "<p><strong><strong>foo</strong> bar</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample426() = doTest(
            markdown = "**foo **bar****\n",
            html = "<p><strong>foo <strong>bar</strong></strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample427() = doTest(
            markdown = "**foo *bar* baz**\n",
            html = "<p><strong>foo <em>bar</em> baz</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample428() = doTest(
            markdown = "**foo*bar*baz**\n",
            html = "<p><strong>foo<em>bar</em>baz</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample429() = doTest(
            markdown = "***foo* bar**\n",
            html = "<p><strong><em>foo</em> bar</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample430() = doTest(
            markdown = "**foo *bar***\n",
            html = "<p><strong>foo <em>bar</em></strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample431() = doTest(
            markdown = "**foo *bar **baz**\nbim* bop**\n",
            html = "<p><strong>foo <em>bar <strong>baz</strong>\nbim</em> bop</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample432() = doTest(
            markdown = "**foo [*bar*](/url)**\n",
            html = "<p><strong>foo <a href=\"/url\"><em>bar</em></a></strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample433() = doTest(
            markdown = "__ is not an empty emphasis\n",
            html = "<p>__ is not an empty emphasis</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample434() = doTest(
            markdown = "____ is not an empty strong emphasis\n",
            html = "<p>____ is not an empty strong emphasis</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample435() = doTest(
            markdown = "foo ***\n",
            html = "<p>foo ***</p>\n"
    )

    @Test
    @Ignore
    fun testEmphasisAndStrongEmphasisExample436() = doTest(
            markdown = "foo *\\**\n",
            html = "<p>foo <em>*</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample437() = doTest(
            markdown = "foo *_*\n",
            html = "<p>foo <em>_</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample438() = doTest(
            markdown = "foo *****\n",
            html = "<p>foo *****</p>\n"
    )

    @Test
    @Ignore
    fun testEmphasisAndStrongEmphasisExample439() = doTest(
            markdown = "foo **\\***\n",
            html = "<p>foo <strong>*</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample440() = doTest(
            markdown = "foo **_**\n",
            html = "<p>foo <strong>_</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample441() = doTest(
            markdown = "**foo*\n",
            html = "<p>*<em>foo</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample442() = doTest(
            markdown = "*foo**\n",
            html = "<p><em>foo</em>*</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample443() = doTest(
            markdown = "***foo**\n",
            html = "<p>*<strong>foo</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample444() = doTest(
            markdown = "****foo*\n",
            html = "<p>***<em>foo</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample445() = doTest(
            markdown = "**foo***\n",
            html = "<p><strong>foo</strong>*</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample446() = doTest(
            markdown = "*foo****\n",
            html = "<p><em>foo</em>***</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample447() = doTest(
            markdown = "foo ___\n",
            html = "<p>foo ___</p>\n"
    )

    @Test
    @Ignore
    fun testEmphasisAndStrongEmphasisExample448() = doTest(
            markdown = "foo _\\__\n",
            html = "<p>foo <em>_</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample449() = doTest(
            markdown = "foo _*_\n",
            html = "<p>foo <em>*</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample450() = doTest(
            markdown = "foo _____\n",
            html = "<p>foo _____</p>\n"
    )

    @Test
    @Ignore
    fun testEmphasisAndStrongEmphasisExample451() = doTest(
            markdown = "foo __\\___\n",
            html = "<p>foo <strong>_</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample452() = doTest(
            markdown = "foo __*__\n",
            html = "<p>foo <strong>*</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample453() = doTest(
            markdown = "__foo_\n",
            html = "<p>_<em>foo</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample454() = doTest(
            markdown = "_foo__\n",
            html = "<p><em>foo</em>_</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample455() = doTest(
            markdown = "___foo__\n",
            html = "<p>_<strong>foo</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample456() = doTest(
            markdown = "____foo_\n",
            html = "<p>___<em>foo</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample457() = doTest(
            markdown = "__foo___\n",
            html = "<p><strong>foo</strong>_</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample458() = doTest(
            markdown = "_foo____\n",
            html = "<p><em>foo</em>___</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample459() = doTest(
            markdown = "**foo**\n",
            html = "<p><strong>foo</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample460() = doTest(
            markdown = "*_foo_*\n",
            html = "<p><em><em>foo</em></em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample461() = doTest(
            markdown = "__foo__\n",
            html = "<p><strong>foo</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample462() = doTest(
            markdown = "_*foo*_\n",
            html = "<p><em><em>foo</em></em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample463() = doTest(
            markdown = "****foo****\n",
            html = "<p><strong><strong>foo</strong></strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample464() = doTest(
            markdown = "____foo____\n",
            html = "<p><strong><strong>foo</strong></strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample465() = doTest(
            markdown = "******foo******\n",
            html = "<p><strong><strong><strong>foo</strong></strong></strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample466() = doTest(
            markdown = "***foo***\n",
            html = "<p><em><strong>foo</strong></em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample467() = doTest(
            markdown = "_____foo_____\n",
            html = "<p><em><strong><strong>foo</strong></strong></em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample468() = doTest(
            markdown = "*foo _bar* baz_\n",
            html = "<p><em>foo _bar</em> baz_</p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample469() = doTest(
            markdown = "*foo __bar *baz bim__ bam*\n",
            html = "<p><em>foo <strong>bar *baz bim</strong> bam</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample470() = doTest(
            markdown = "**foo **bar baz**\n",
            html = "<p>**foo <strong>bar baz</strong></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample471() = doTest(
            markdown = "*foo *bar baz*\n",
            html = "<p>*foo <em>bar baz</em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample472() = doTest(
            markdown = "*[bar*](/url)\n",
            html = "<p>*<a href=\"/url\">bar*</a></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample473() = doTest(
            markdown = "_foo [bar_](/url)\n",
            html = "<p>_foo <a href=\"/url\">bar_</a></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample474() = doTest(
            markdown = "*<img src=\"foo\" title=\"*\"/>\n",
            html = "<p>*<img src=\"foo\" title=\"*\"/></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample475() = doTest(
            markdown = "**<a href=\"**\">\n",
            html = "<p>**<a href=\"**\"></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample476() = doTest(
            markdown = "__<a href=\"__\">\n",
            html = "<p>__<a href=\"__\"></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample477() = doTest(
            markdown = "*a `*`*\n",
            html = "<p><em>a <code>*</code></em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample478() = doTest(
            markdown = "_a `_`_\n",
            html = "<p><em>a <code>_</code></em></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample479() = doTest(
            markdown = "**a<http://foo.bar/?q=**>\n",
            html = "<p>**a<a href=\"http://foo.bar/?q=**\">http://foo.bar/?q=**</a></p>\n"
    )

    @Test
    fun testEmphasisAndStrongEmphasisExample480() = doTest(
            markdown = "__a<http://foo.bar/?q=__>\n",
            html = "<p>__a<a href=\"http://foo.bar/?q=__\">http://foo.bar/?q=__</a></p>\n"
    )

    @Test
    fun testLinksExample481() = doTest(
            markdown = "[link](/uri \"title\")\n",
            html = "<p><a href=\"/uri\" title=\"title\">link</a></p>\n"
    )

    @Test
    fun testLinksExample482() = doTest(
            markdown = "[link](/uri)\n",
            html = "<p><a href=\"/uri\">link</a></p>\n"
    )

    @Test
    fun testLinksExample483() = doTest(
            markdown = "[](./target.md)\n",
            html = "<p><a href=\"./target.md\"></a></p>\n"
    )

    @Test
    fun testLinksExample484() = doTest(
            markdown = "[link]()\n",
            html = "<p><a href=\"\">link</a></p>\n"
    )

    @Test
    fun testLinksExample485() = doTest(
            markdown = "[link](<>)\n",
            html = "<p><a href=\"\">link</a></p>\n"
    )

    @Test
    fun testLinksExample486() = doTest(
            markdown = "[]()\n",
            html = "<p><a href=\"\"></a></p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample487() = doTest(
            markdown = "[link](/my uri)\n",
            html = "<p>[link](/my uri)</p>\n"
    )

    @Test
    fun testLinksExample488() = doTest(
            markdown = "[link](</my uri>)\n",
            html = "<p><a href=\"/my%20uri\">link</a></p>\n"
    )

    @Test
    fun testLinksExample489() = doTest(
            markdown = "[link](foo\nbar)\n",
            html = "<p>[link](foo\nbar)</p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample490() = doTest(
            markdown = "[link](<foo\nbar>)\n",
            html = "<p>[link](<foo\nbar>)</p>\n"
    )

    @Test
    fun testLinksExample491() = doTest(
            markdown = "[a](<b)c>)\n",
            html = "<p><a href=\"b)c\">a</a></p>\n"
    )

    @Test
    fun testLinksExample492() = doTest(
            markdown = "[link](<foo\\>)\n",
            html = "<p>[link](&lt;foo&gt;)</p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample493() = doTest(
            markdown = "[a](<b)c\n[a](<b)c>\n[a](<b>c)\n",
            html = "<p>[a](&lt;b)c\n[a](&lt;b)c&gt;\n[a](<b>c)</p>\n"
    )

    @Test
    fun testLinksExample494() = doTest(
            markdown = "[link](\\(foo\\))\n",
            html = "<p><a href=\"(foo)\">link</a></p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample495() = doTest(
            markdown = "[link](foo(and(bar)))\n",
            html = "<p><a href=\"foo(and(bar))\">link</a></p>\n"
    )

    @Test
    fun testLinksExample496() = doTest(
            markdown = "[link](foo(and(bar))\n",
            html = "<p>[link](foo(and(bar))</p>\n"
    )

    @Test
    fun testLinksExample497() = doTest(
            markdown = "[link](foo\\(and\\(bar\\))\n",
            html = "<p><a href=\"foo(and(bar)\">link</a></p>\n"
    )

    @Test
    fun testLinksExample498() = doTest(
            markdown = "[link](<foo(and(bar)>)\n",
            html = "<p><a href=\"foo(and(bar)\">link</a></p>\n"
    )

    @Test
    fun testLinksExample499() = doTest(
            markdown = "[link](foo\\)\\:)\n",
            html = "<p><a href=\"foo):\">link</a></p>\n"
    )

    @Test
    fun testLinksExample500() = doTest(
            markdown = "[link](#fragment)\n\n[link](http://example.com#fragment)\n\n[link](http://example.com?foo=3#frag)\n",
            html = "<p><a href=\"#fragment\">link</a></p>\n<p><a href=\"http://example.com#fragment\">link</a></p>\n<p><a href=\"http://example.com?foo=3#frag\">link</a></p>\n"
    )

    @Test
    fun testLinksExample501() = doTest(
            markdown = "[link](foo\\bar)\n",
            html = "<p><a href=\"foo%5Cbar\">link</a></p>\n"
    )

    @Test
    fun testLinksExample502() = doTest(
            markdown = "[link](foo%20b&auml;)\n",
            html = "<p><a href=\"foo%20b%C3%A4\">link</a></p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample503() = doTest(
            markdown = "[link](\"title\")\n",
            html = "<p><a href=\"%22title%22\">link</a></p>\n"
    )

    @Test
    fun testLinksExample504() = doTest(
            markdown = "[link](/url \"title\")\n[link](/url 'title')\n[link](/url (title))\n",
            html = "<p><a href=\"/url\" title=\"title\">link</a>\n<a href=\"/url\" title=\"title\">link</a>\n<a href=\"/url\" title=\"title\">link</a></p>\n"
    )

    @Test
    fun testLinksExample505() = doTest(
            markdown = "[link](/url \"title \\\"&quot;\")\n",
            html = "<p><a href=\"/url\" title=\"title &quot;&quot;\">link</a></p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample506() = doTest(
            markdown = "[link](/url \"title\")\n",
            html = "<p><a href=\"/url%C2%A0%22title%22\">link</a></p>\n"
    )

    @Test
    fun testLinksExample507() = doTest(
            markdown = "[link](/url \"title \"and\" title\")\n",
            html = "<p>[link](/url &quot;title &quot;and&quot; title&quot;)</p>\n"
    )

    @Test
    fun testLinksExample508() = doTest(
            markdown = "[link](/url 'title \"and\" title')\n",
            html = "<p><a href=\"/url\" title=\"title &quot;and&quot; title\">link</a></p>\n"
    )

    @Test
    fun testLinksExample509() = doTest(
            markdown = "[link](   /uri\n  \"title\"  )\n",
            html = "<p><a href=\"/uri\" title=\"title\">link</a></p>\n"
    )

    @Test
    fun testLinksExample510() = doTest(
            markdown = "[link] (/uri)\n",
            html = "<p>[link] (/uri)</p>\n"
    )

    @Test
    fun testLinksExample511() = doTest(
            markdown = "[link [foo [bar]]](/uri)\n",
            html = "<p><a href=\"/uri\">link [foo [bar]]</a></p>\n"
    )

    @Test
    fun testLinksExample512() = doTest(
            markdown = "[link] bar](/uri)\n",
            html = "<p>[link] bar](/uri)</p>\n"
    )

    @Test
    fun testLinksExample513() = doTest(
            markdown = "[link [bar](/uri)\n",
            html = "<p>[link <a href=\"/uri\">bar</a></p>\n"
    )

    @Test
    fun testLinksExample514() = doTest(
            markdown = "[link \\[bar](/uri)\n",
            html = "<p><a href=\"/uri\">link [bar</a></p>\n"
    )

    @Test
    fun testLinksExample515() = doTest(
            markdown = "[link *foo **bar** `#`*](/uri)\n",
            html = "<p><a href=\"/uri\">link <em>foo <strong>bar</strong> <code>#</code></em></a></p>\n"
    )

    @Test
    fun testLinksExample516() = doTest(
            markdown = "[![moon](moon.jpg)](/uri)\n",
            html = "<p><a href=\"/uri\"><img src=\"moon.jpg\" alt=\"moon\" /></a></p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample517() = doTest(
            markdown = "[foo [bar](/uri)](/uri)\n",
            html = "<p>[foo <a href=\"/uri\">bar</a>](/uri)</p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample518() = doTest(
            markdown = "[foo *[bar [baz](/uri)](/uri)*](/uri)\n",
            html = "<p>[foo <em>[bar <a href=\"/uri\">baz</a>](/uri)</em>](/uri)</p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample519() = doTest(
            markdown = "![[[foo](uri1)](uri2)](uri3)\n",
            html = "<p><img src=\"uri3\" alt=\"[foo](uri2)\" /></p>\n"
    )

    @Test
    fun testLinksExample520() = doTest(
            markdown = "*[foo*](/uri)\n",
            html = "<p>*<a href=\"/uri\">foo*</a></p>\n"
    )

    @Test
    fun testLinksExample521() = doTest(
            markdown = "[foo *bar](baz*)\n",
            html = "<p><a href=\"baz*\">foo *bar</a></p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample522() = doTest(
            markdown = "*foo [bar* baz]\n",
            html = "<p><em>foo [bar</em> baz]</p>\n"
    )

    @Test
    fun testLinksExample523() = doTest(
            markdown = "[foo <bar attr=\"](baz)\">\n",
            html = "<p>[foo <bar attr=\"](baz)\"></p>\n"
    )

    @Test
    fun testLinksExample524() = doTest(
            markdown = "[foo`](/uri)`\n",
            html = "<p>[foo<code>](/uri)</code></p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample525() = doTest(
            markdown = "[foo<http://example.com/?search=](uri)>\n",
            html = "<p>[foo<a href=\"http://example.com/?search=%5D(uri)\">http://example.com/?search=](uri)</a></p>\n"
    )

    @Test
    fun testLinksExample526() = doTest(
            markdown = "[foo][bar]\n\n[bar]: /url \"title\"\n",
            html = "<p><a href=\"/url\" title=\"title\">foo</a></p>\n"
    )

    @Test
    fun testLinksExample527() = doTest(
            markdown = "[link [foo [bar]]][ref]\n\n[ref]: /uri\n",
            html = "<p><a href=\"/uri\">link [foo [bar]]</a></p>\n"
    )

    @Test
    fun testLinksExample528() = doTest(
            markdown = "[link \\[bar][ref]\n\n[ref]: /uri\n",
            html = "<p><a href=\"/uri\">link [bar</a></p>\n"
    )

    @Test
    fun testLinksExample529() = doTest(
            markdown = "[link *foo **bar** `#`*][ref]\n\n[ref]: /uri\n",
            html = "<p><a href=\"/uri\">link <em>foo <strong>bar</strong> <code>#</code></em></a></p>\n"
    )

    @Test
    fun testLinksExample530() = doTest(
            markdown = "[![moon](moon.jpg)][ref]\n\n[ref]: /uri\n",
            html = "<p><a href=\"/uri\"><img src=\"moon.jpg\" alt=\"moon\" /></a></p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample531() = doTest(
            markdown = "[foo [bar](/uri)][ref]\n\n[ref]: /uri\n",
            html = "<p>[foo <a href=\"/uri\">bar</a>]<a href=\"/uri\">ref</a></p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample532() = doTest(
            markdown = "[foo *bar [baz][ref]*][ref]\n\n[ref]: /uri\n",
            html = "<p>[foo <em>bar <a href=\"/uri\">baz</a></em>]<a href=\"/uri\">ref</a></p>\n"
    )

    @Test
    fun testLinksExample533() = doTest(
            markdown = "*[foo*][ref]\n\n[ref]: /uri\n",
            html = "<p>*<a href=\"/uri\">foo*</a></p>\n"
    )

    @Test
    fun testLinksExample534() = doTest(
            markdown = "[foo *bar][ref]*\n\n[ref]: /uri\n",
            html = "<p><a href=\"/uri\">foo *bar</a>*</p>\n"
    )

    @Test
    fun testLinksExample535() = doTest(
            markdown = "[foo <bar attr=\"][ref]\">\n\n[ref]: /uri\n",
            html = "<p>[foo <bar attr=\"][ref]\"></p>\n"
    )

    @Test
    fun testLinksExample536() = doTest(
            markdown = "[foo`][ref]`\n\n[ref]: /uri\n",
            html = "<p>[foo<code>][ref]</code></p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample537() = doTest(
            markdown = "[foo<http://example.com/?search=][ref]>\n\n[ref]: /uri\n",
            html = "<p>[foo<a href=\"http://example.com/?search=%5D%5Bref%5D\">http://example.com/?search=][ref]</a></p>\n"
    )

    @Test
    fun testLinksExample538() = doTest(
            markdown = "[foo][BaR]\n\n[bar]: /url \"title\"\n",
            html = "<p><a href=\"/url\" title=\"title\">foo</a></p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample539() = doTest(
            markdown = "[ẞ]\n\n[SS]: /url\n",
            html = "<p><a href=\"/url\">ẞ</a></p>\n"
    )

    @Test
    fun testLinksExample540() = doTest(
            markdown = "[Foo\n  bar]: /url\n\n[Baz][Foo bar]\n",
            html = "<p><a href=\"/url\">Baz</a></p>\n"
    )

    @Test
    fun testLinksExample541() = doTest(
            markdown = "[foo] [bar]\n\n[bar]: /url \"title\"\n",
            html = "<p>[foo] <a href=\"/url\" title=\"title\">bar</a></p>\n"
    )

    @Test
    fun testLinksExample542() = doTest(
            markdown = "[foo]\n[bar]\n\n[bar]: /url \"title\"\n",
            html = "<p>[foo]\n<a href=\"/url\" title=\"title\">bar</a></p>\n"
    )

    @Test
    fun testLinksExample543() = doTest(
            markdown = "[foo]: /url1\n\n[foo]: /url2\n\n[bar][foo]\n",
            html = "<p><a href=\"/url1\">bar</a></p>\n"
    )

    @Test
    fun testLinksExample544() = doTest(
            markdown = "[bar][foo\\!]\n\n[foo!]: /url\n",
            html = "<p>[bar][foo!]</p>\n"
    )

    @Test
    fun testLinksExample545() = doTest(
            markdown = "[foo][ref[]\n\n[ref[]: /uri\n",
            html = "<p>[foo][ref[]</p>\n<p>[ref[]: /uri</p>\n"
    )

    @Test
    fun testLinksExample546() = doTest(
            markdown = "[foo][ref[bar]]\n\n[ref[bar]]: /uri\n",
            html = "<p>[foo][ref[bar]]</p>\n<p>[ref[bar]]: /uri</p>\n"
    )

    @Test
    fun testLinksExample547() = doTest(
            markdown = "[[[foo]]]\n\n[[[foo]]]: /url\n",
            html = "<p>[[[foo]]]</p>\n<p>[[[foo]]]: /url</p>\n"
    )

    @Test
    fun testLinksExample548() = doTest(
            markdown = "[foo][ref\\[]\n\n[ref\\[]: /uri\n",
            html = "<p><a href=\"/uri\">foo</a></p>\n"
    )

    @Test
    fun testLinksExample549() = doTest(
            markdown = "[bar\\\\]: /uri\n\n[bar\\\\]\n",
            html = "<p><a href=\"/uri\">bar\\</a></p>\n"
    )

    @Test
    fun testLinksExample550() = doTest(
            markdown = "[]\n\n[]: /uri\n",
            html = "<p>[]</p>\n<p>[]: /uri</p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample551() = doTest(
            markdown = "[\n ]\n\n[\n ]: /uri\n",
            html = "<p>[\n]</p>\n<p>[\n]: /uri</p>\n"
    )

    @Test
    fun testLinksExample552() = doTest(
            markdown = "[foo][]\n\n[foo]: /url \"title\"\n",
            html = "<p><a href=\"/url\" title=\"title\">foo</a></p>\n"
    )

    @Test
    fun testLinksExample553() = doTest(
            markdown = "[*foo* bar][]\n\n[*foo* bar]: /url \"title\"\n",
            html = "<p><a href=\"/url\" title=\"title\"><em>foo</em> bar</a></p>\n"
    )

    @Test
    fun testLinksExample554() = doTest(
            markdown = "[Foo][]\n\n[foo]: /url \"title\"\n",
            html = "<p><a href=\"/url\" title=\"title\">Foo</a></p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample555() = doTest(
            markdown = "[foo] \n[]\n\n[foo]: /url \"title\"\n",
            html = "<p><a href=\"/url\" title=\"title\">foo</a>\n[]</p>\n"
    )

    @Test
    fun testLinksExample556() = doTest(
            markdown = "[foo]\n\n[foo]: /url \"title\"\n",
            html = "<p><a href=\"/url\" title=\"title\">foo</a></p>\n"
    )

    @Test
    fun testLinksExample557() = doTest(
            markdown = "[*foo* bar]\n\n[*foo* bar]: /url \"title\"\n",
            html = "<p><a href=\"/url\" title=\"title\"><em>foo</em> bar</a></p>\n"
    )

    @Test
    fun testLinksExample558() = doTest(
            markdown = "[[*foo* bar]]\n\n[*foo* bar]: /url \"title\"\n",
            html = "<p>[<a href=\"/url\" title=\"title\"><em>foo</em> bar</a>]</p>\n"
    )

    @Test
    fun testLinksExample559() = doTest(
            markdown = "[[bar [foo]\n\n[foo]: /url\n",
            html = "<p>[[bar <a href=\"/url\">foo</a></p>\n"
    )

    @Test
    fun testLinksExample560() = doTest(
            markdown = "[Foo]\n\n[foo]: /url \"title\"\n",
            html = "<p><a href=\"/url\" title=\"title\">Foo</a></p>\n"
    )

    @Test
    fun testLinksExample561() = doTest(
            markdown = "[foo] bar\n\n[foo]: /url\n",
            html = "<p><a href=\"/url\">foo</a> bar</p>\n"
    )

    @Test
    fun testLinksExample562() = doTest(
            markdown = "\\[foo]\n\n[foo]: /url \"title\"\n",
            html = "<p>[foo]</p>\n"
    )

    @Test
    fun testLinksExample563() = doTest(
            markdown = "[foo*]: /url\n\n*[foo*]\n",
            html = "<p>*<a href=\"/url\">foo*</a></p>\n"
    )

    @Test
    fun testLinksExample564() = doTest(
            markdown = "[foo][bar]\n\n[foo]: /url1\n[bar]: /url2\n",
            html = "<p><a href=\"/url2\">foo</a></p>\n"
    )

    @Test
    fun testLinksExample565() = doTest(
            markdown = "[foo][]\n\n[foo]: /url1\n",
            html = "<p><a href=\"/url1\">foo</a></p>\n"
    )

    @Test
    fun testLinksExample566() = doTest(
            markdown = "[foo]()\n\n[foo]: /url1\n",
            html = "<p><a href=\"\">foo</a></p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample567() = doTest(
            markdown = "[foo](not a link)\n\n[foo]: /url1\n",
            html = "<p><a href=\"/url1\">foo</a>(not a link)</p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample568() = doTest(
            markdown = "[foo][bar][baz]\n\n[baz]: /url\n",
            html = "<p>[foo]<a href=\"/url\">bar</a></p>\n"
    )

    @Test
    fun testLinksExample569() = doTest(
            markdown = "[foo][bar][baz]\n\n[baz]: /url1\n[bar]: /url2\n",
            html = "<p><a href=\"/url2\">foo</a><a href=\"/url1\">baz</a></p>\n"
    )

    @Test
    @Ignore
    fun testLinksExample570() = doTest(
            markdown = "[foo][bar][baz]\n\n[baz]: /url1\n[foo]: /url2\n",
            html = "<p>[foo]<a href=\"/url1\">bar</a></p>\n"
    )

    @Test
    fun testImagesExample571() = doTest(
            markdown = "![foo](/url \"title\")\n",
            html = "<p><img src=\"/url\" alt=\"foo\" title=\"title\" /></p>\n"
    )

    @Test
    fun testImagesExample572() = doTest(
            markdown = "![foo *bar*]\n\n[foo *bar*]: train.jpg \"train & tracks\"\n",
            html = "<p><img src=\"train.jpg\" alt=\"foo bar\" title=\"train &amp; tracks\" /></p>\n"
    )

    @Test
    @Ignore
    fun testImagesExample573() = doTest(
            markdown = "![foo ![bar](/url)](/url2)\n",
            html = "<p><img src=\"/url2\" alt=\"foo bar\" /></p>\n"
    )

    @Test
    @Ignore
    fun testImagesExample574() = doTest(
            markdown = "![foo [bar](/url)](/url2)\n",
            html = "<p><img src=\"/url2\" alt=\"foo bar\" /></p>\n"
    )

    @Test
    fun testImagesExample575() = doTest(
            markdown = "![foo *bar*][]\n\n[foo *bar*]: train.jpg \"train & tracks\"\n",
            html = "<p><img src=\"train.jpg\" alt=\"foo bar\" title=\"train &amp; tracks\" /></p>\n"
    )

    @Test
    fun testImagesExample576() = doTest(
            markdown = "![foo *bar*][foobar]\n\n[FOOBAR]: train.jpg \"train & tracks\"\n",
            html = "<p><img src=\"train.jpg\" alt=\"foo bar\" title=\"train &amp; tracks\" /></p>\n"
    )

    @Test
    fun testImagesExample577() = doTest(
            markdown = "![foo](train.jpg)\n",
            html = "<p><img src=\"train.jpg\" alt=\"foo\" /></p>\n"
    )

    @Test
    fun testImagesExample578() = doTest(
            markdown = "My ![foo bar](/path/to/train.jpg  \"title\"   )\n",
            html = "<p>My <img src=\"/path/to/train.jpg\" alt=\"foo bar\" title=\"title\" /></p>\n"
    )

    @Test
    fun testImagesExample579() = doTest(
            markdown = "![foo](<url>)\n",
            html = "<p><img src=\"url\" alt=\"foo\" /></p>\n"
    )

    @Test
    fun testImagesExample580() = doTest(
            markdown = "![](/url)\n",
            html = "<p><img src=\"/url\" alt=\"\" /></p>\n"
    )

    @Test
    fun testImagesExample581() = doTest(
            markdown = "![foo][bar]\n\n[bar]: /url\n",
            html = "<p><img src=\"/url\" alt=\"foo\" /></p>\n"
    )

    @Test
    fun testImagesExample582() = doTest(
            markdown = "![foo][bar]\n\n[BAR]: /url\n",
            html = "<p><img src=\"/url\" alt=\"foo\" /></p>\n"
    )

    @Test
    fun testImagesExample583() = doTest(
            markdown = "![foo][]\n\n[foo]: /url \"title\"\n",
            html = "<p><img src=\"/url\" alt=\"foo\" title=\"title\" /></p>\n"
    )

    @Test
    fun testImagesExample584() = doTest(
            markdown = "![*foo* bar][]\n\n[*foo* bar]: /url \"title\"\n",
            html = "<p><img src=\"/url\" alt=\"foo bar\" title=\"title\" /></p>\n"
    )

    @Test
    fun testImagesExample585() = doTest(
            markdown = "![Foo][]\n\n[foo]: /url \"title\"\n",
            html = "<p><img src=\"/url\" alt=\"Foo\" title=\"title\" /></p>\n"
    )

    @Test
    @Ignore
    fun testImagesExample586() = doTest(
            markdown = "![foo] \n[]\n\n[foo]: /url \"title\"\n",
            html = "<p><img src=\"/url\" alt=\"foo\" title=\"title\" />\n[]</p>\n"
    )

    @Test
    fun testImagesExample587() = doTest(
            markdown = "![foo]\n\n[foo]: /url \"title\"\n",
            html = "<p><img src=\"/url\" alt=\"foo\" title=\"title\" /></p>\n"
    )

    @Test
    fun testImagesExample588() = doTest(
            markdown = "![*foo* bar]\n\n[*foo* bar]: /url \"title\"\n",
            html = "<p><img src=\"/url\" alt=\"foo bar\" title=\"title\" /></p>\n"
    )

    @Test
    fun testImagesExample589() = doTest(
            markdown = "![[foo]]\n\n[[foo]]: /url \"title\"\n",
            html = "<p>![[foo]]</p>\n<p>[[foo]]: /url &quot;title&quot;</p>\n"
    )

    @Test
    fun testImagesExample590() = doTest(
            markdown = "![Foo]\n\n[foo]: /url \"title\"\n",
            html = "<p><img src=\"/url\" alt=\"Foo\" title=\"title\" /></p>\n"
    )

    @Test
    fun testImagesExample591() = doTest(
            markdown = "!\\[foo]\n\n[foo]: /url \"title\"\n",
            html = "<p>![foo]</p>\n"
    )

    @Test
    fun testImagesExample592() = doTest(
            markdown = "\\![foo]\n\n[foo]: /url \"title\"\n",
            html = "<p>!<a href=\"/url\" title=\"title\">foo</a></p>\n"
    )

    @Test
    fun testAutolinksExample593() = doTest(
            markdown = "<http://foo.bar.baz>\n",
            html = "<p><a href=\"http://foo.bar.baz\">http://foo.bar.baz</a></p>\n"
    )

    @Test
    fun testAutolinksExample594() = doTest(
            markdown = "<http://foo.bar.baz/test?q=hello&id=22&boolean>\n",
            html = "<p><a href=\"http://foo.bar.baz/test?q=hello&amp;id=22&amp;boolean\">http://foo.bar.baz/test?q=hello&amp;id=22&amp;boolean</a></p>\n"
    )

    @Test
    fun testAutolinksExample595() = doTest(
            markdown = "<irc://foo.bar:2233/baz>\n",
            html = "<p><a href=\"irc://foo.bar:2233/baz\">irc://foo.bar:2233/baz</a></p>\n"
    )

    @Test
    fun testAutolinksExample596() = doTest(
            markdown = "<MAILTO:FOO@BAR.BAZ>\n",
            html = "<p><a href=\"MAILTO:FOO@BAR.BAZ\">MAILTO:FOO@BAR.BAZ</a></p>\n"
    )

    @Test
    @Ignore
    fun testAutolinksExample597() = doTest(
            markdown = "<a+b+c:d>\n",
            html = "<p><a href=\"a+b+c:d\">a+b+c:d</a></p>\n"
    )

    @Test
    @Ignore
    fun testAutolinksExample598() = doTest(
            markdown = "<made-up-scheme://foo,bar>\n",
            html = "<p><a href=\"made-up-scheme://foo,bar\">made-up-scheme://foo,bar</a></p>\n"
    )

    @Test
    fun testAutolinksExample599() = doTest(
            markdown = "<http://../>\n",
            html = "<p><a href=\"http://../\">http://../</a></p>\n"
    )

    @Test
    fun testAutolinksExample600() = doTest(
            markdown = "<localhost:5001/foo>\n",
            html = "<p><a href=\"localhost:5001/foo\">localhost:5001/foo</a></p>\n"
    )

    @Test
    fun testAutolinksExample601() = doTest(
            markdown = "<http://foo.bar/baz bim>\n",
            html = "<p>&lt;http://foo.bar/baz bim&gt;</p>\n"
    )

    @Test
    @Ignore
    fun testAutolinksExample602() = doTest(
            markdown = "<http://example.com/\\[\\>\n",
            html = "<p><a href=\"http://example.com/%5C%5B%5C\">http://example.com/\\[\\</a></p>\n"
    )

    @Test
    @Ignore
    fun testAutolinksExample603() = doTest(
            markdown = "<foo@bar.example.com>\n",
            html = "<p><a href=\"mailto:foo@bar.example.com\">foo@bar.example.com</a></p>\n"
    )

    @Test
    @Ignore
    fun testAutolinksExample604() = doTest(
            markdown = "<foo+special@Bar.baz-bar0.com>\n",
            html = "<p><a href=\"mailto:foo+special@Bar.baz-bar0.com\">foo+special@Bar.baz-bar0.com</a></p>\n"
    )

    @Test
    fun testAutolinksExample605() = doTest(
            markdown = "<foo\\+@bar.example.com>\n",
            html = "<p>&lt;foo+@bar.example.com&gt;</p>\n"
    )

    @Test
    fun testAutolinksExample606() = doTest(
            markdown = "<>\n",
            html = "<p>&lt;&gt;</p>\n"
    )

    @Test
    fun testAutolinksExample607() = doTest(
            markdown = "< http://foo.bar >\n",
            html = "<p>&lt; http://foo.bar &gt;</p>\n"
    )

    @Test
    @Ignore
    fun testAutolinksExample608() = doTest(
            markdown = "<m:abc>\n",
            html = "<p>&lt;m:abc&gt;</p>\n"
    )

    @Test
    fun testAutolinksExample609() = doTest(
            markdown = "<foo.bar.baz>\n",
            html = "<p>&lt;foo.bar.baz&gt;</p>\n"
    )

    @Test
    fun testAutolinksExample610() = doTest(
            markdown = "http://example.com\n",
            html = "<p>http://example.com</p>\n"
    )

    @Test
    fun testAutolinksExample611() = doTest(
            markdown = "foo@bar.example.com\n",
            html = "<p>foo@bar.example.com</p>\n"
    )

    @Test
    fun testRawHTMLExample612() = doTest(
            markdown = "<a><bab><c2c>\n",
            html = "<p><a><bab><c2c></p>\n"
    )

    @Test
    fun testRawHTMLExample613() = doTest(
            markdown = "<a/><b2/>\n",
            html = "<p><a/><b2/></p>\n"
    )

    @Test
    @Ignore
    fun testRawHTMLExample614() = doTest(
            markdown = "<a  /><b2\ndata=\"foo\" >\n",
            html = "<p><a  /><b2\ndata=\"foo\" ></p>\n"
    )

    @Test
    @Ignore
    fun testRawHTMLExample615() = doTest(
            markdown = "<a foo=\"bar\" bam = 'baz <em>\"</em>'\n_boolean zoop:33=zoop:33 />\n",
            html = "<p><a foo=\"bar\" bam = 'baz <em>\"</em>'\n_boolean zoop:33=zoop:33 /></p>\n"
    )

    @Test
    @Ignore
    fun testRawHTMLExample616() = doTest(
            markdown = "Foo <responsive-image src=\"foo.jpg\" />\n",
            html = "<p>Foo <responsive-image src=\"foo.jpg\" /></p>\n"
    )

    @Test
    fun testRawHTMLExample617() = doTest(
            markdown = "<33> <__>\n",
            html = "<p>&lt;33&gt; &lt;__&gt;</p>\n"
    )

    @Test
    fun testRawHTMLExample618() = doTest(
            markdown = "<a h*#ref=\"hi\">\n",
            html = "<p>&lt;a h*#ref=&quot;hi&quot;&gt;</p>\n"
    )

    @Test
    fun testRawHTMLExample619() = doTest(
            markdown = "<a href=\"hi'> <a href=hi'>\n",
            html = "<p>&lt;a href=&quot;hi'&gt; &lt;a href=hi'&gt;</p>\n"
    )

    @Test
    fun testRawHTMLExample620() = doTest(
            markdown = "< a><\nfoo><bar/ >\n<foo bar=baz\nbim!bop />\n",
            html = "<p>&lt; a&gt;&lt;\nfoo&gt;&lt;bar/ &gt;\n&lt;foo bar=baz\nbim!bop /&gt;</p>\n"
    )

    @Test
    fun testRawHTMLExample621() = doTest(
            markdown = "<a href='bar'title=title>\n",
            html = "<p>&lt;a href='bar'title=title&gt;</p>\n"
    )

    @Test
    fun testRawHTMLExample622() = doTest(
            markdown = "</a></foo >\n",
            html = "<p></a></foo ></p>\n"
    )

    @Test
    fun testRawHTMLExample623() = doTest(
            markdown = "</a href=\"foo\">\n",
            html = "<p>&lt;/a href=&quot;foo&quot;&gt;</p>\n"
    )

    @Test
    fun testRawHTMLExample624() = doTest(
            markdown = "foo <!-- this is a\ncomment - with hyphen -->\n",
            html = "<p>foo <!-- this is a\ncomment - with hyphen --></p>\n"
    )

    @Test
    fun testRawHTMLExample625() = doTest(
            markdown = "foo <!-- not a comment -- two hyphens -->\n",
            html = "<p>foo &lt;!-- not a comment -- two hyphens --&gt;</p>\n"
    )

    @Test
    @Ignore
    fun testRawHTMLExample626() = doTest(
            markdown = "foo <!--> foo -->\n\nfoo <!-- foo--->\n",
            html = "<p>foo &lt;!--&gt; foo --&gt;</p>\n<p>foo &lt;!-- foo---&gt;</p>\n"
    )

    @Test
    fun testRawHTMLExample627() = doTest(
            markdown = "foo <?php echo \$a; ?>\n",
            html = "<p>foo <?php echo \$a; ?></p>\n"
    )

    @Test
    @Ignore
    fun testRawHTMLExample628() = doTest(
            markdown = "foo <!ELEMENT br EMPTY>\n",
            html = "<p>foo <!ELEMENT br EMPTY></p>\n"
    )

    @Test
    fun testRawHTMLExample629() = doTest(
            markdown = "foo <![CDATA[>&<]]>\n",
            html = "<p>foo <![CDATA[>&<]]></p>\n"
    )

    @Test
    fun testRawHTMLExample630() = doTest(
            markdown = "foo <a href=\"&ouml;\">\n",
            html = "<p>foo <a href=\"&ouml;\"></p>\n"
    )

    @Test
    fun testRawHTMLExample631() = doTest(
            markdown = "foo <a href=\"\\*\">\n",
            html = "<p>foo <a href=\"\\*\"></p>\n"
    )

    @Test
    @Ignore
    fun testRawHTMLExample632() = doTest(
            markdown = "<a href=\"\\\"\">\n",
            html = "<p>&lt;a href=&quot;&quot;&quot;&gt;</p>\n"
    )

    @Test
    fun testHardLineBreaksExample633() = doTest(
            markdown = "foo  \nbaz\n",
            html = "<p>foo<br />\nbaz</p>\n"
    )

    @Test
    fun testHardLineBreaksExample634() = doTest(
            markdown = "foo\\\nbaz\n",
            html = "<p>foo<br />\nbaz</p>\n"
    )

    @Test
    fun testHardLineBreaksExample635() = doTest(
            markdown = "foo       \nbaz\n",
            html = "<p>foo<br />\nbaz</p>\n"
    )

    @Test
    @Ignore
    fun testHardLineBreaksExample636() = doTest(
            markdown = "foo  \n     bar\n",
            html = "<p>foo<br />\nbar</p>\n"
    )

    @Test
    @Ignore
    fun testHardLineBreaksExample637() = doTest(
            markdown = "foo\\\n     bar\n",
            html = "<p>foo<br />\nbar</p>\n"
    )

    @Test
    fun testHardLineBreaksExample638() = doTest(
            markdown = "*foo  \nbar*\n",
            html = "<p><em>foo<br />\nbar</em></p>\n"
    )

    @Test
    fun testHardLineBreaksExample639() = doTest(
            markdown = "*foo\\\nbar*\n",
            html = "<p><em>foo<br />\nbar</em></p>\n"
    )

    @Test
    @Ignore
    fun testHardLineBreaksExample640() = doTest(
            markdown = "`code  \nspan`\n",
            html = "<p><code>code   span</code></p>\n"
    )

    @Test
    @Ignore
    fun testHardLineBreaksExample641() = doTest(
            markdown = "`code\\\nspan`\n",
            html = "<p><code>code\\ span</code></p>\n"
    )

    @Test
    @Ignore
    fun testHardLineBreaksExample642() = doTest(
            markdown = "<a href=\"foo  \nbar\">\n",
            html = "<p><a href=\"foo  \nbar\"></p>\n"
    )

    @Test
    @Ignore
    fun testHardLineBreaksExample643() = doTest(
            markdown = "<a href=\"foo\\\nbar\">\n",
            html = "<p><a href=\"foo\\\nbar\"></p>\n"
    )

    @Test
    fun testHardLineBreaksExample644() = doTest(
            markdown = "foo\\\n",
            html = "<p>foo\\</p>\n"
    )

    @Test
    fun testHardLineBreaksExample645() = doTest(
            markdown = "foo  \n",
            html = "<p>foo</p>\n"
    )

    @Test
    fun testHardLineBreaksExample646() = doTest(
            markdown = "### foo\\\n",
            html = "<h3>foo\\</h3>\n"
    )

    @Test
    fun testHardLineBreaksExample647() = doTest(
            markdown = "### foo  \n",
            html = "<h3>foo</h3>\n"
    )

    @Test
    fun testSoftLineBreaksExample648() = doTest(
            markdown = "foo\nbaz\n",
            html = "<p>foo\nbaz</p>\n"
    )

    @Test
    @Ignore
    fun testSoftLineBreaksExample649() = doTest(
            markdown = "foo \n baz\n",
            html = "<p>foo\nbaz</p>\n"
    )

    @Test
    fun testTextualContentExample650() = doTest(
            markdown = "hello \$.;'there\n",
            html = "<p>hello \$.;'there</p>\n"
    )

    @Test
    fun testTextualContentExample651() = doTest(
            markdown = "Foo χρῆν\n",
            html = "<p>Foo χρῆν</p>\n"
    )

    @Test
    fun testTextualContentExample652() = doTest(
            markdown = "Multiple     spaces\n",
            html = "<p>Multiple     spaces</p>\n"
    )

}
