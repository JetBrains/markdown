intellij-markdown [![Build Status](https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:IntelliJMarkdownParser_BuildForIdeaPlugin)/statusIcon.svg?guest=1)](https://teamcity.jetbrains.com/viewType.html?buildTypeId=IntelliJMarkdownParser_BuildForIdeaPlugin&guest=1) [![Download](https://api.bintray.com/packages/jetbrains/markdown/markdown/images/download.svg) ](https://bintray.com/jetbrains/markdown/markdown/_latestVersion)
=================

**Markdown parser written in kotlin**

Introduction
-------------

This parser is an attempt to create a markdown processor that would:

- Use one code base for both client and server-side processing;
- Support different flavours;
- Be easily extensible.

Since the parser is written in kotlin, it can be compiled in both JS and Java bytecode
and so can be used elsewhere.

Parsing algorithm
-----------------

The parsing process is held in two logical parts:

1. Splitting the document into the blocks of logical structure (lists, blockquotes, paragraphs, etc.);
2. Parsing the inline structure of the resulted blocks.

This is the same way as the one currently being proposed in [Commonmark spec](http://spec.commonmark.org/0.16/#appendix-a-a-parsing-strategy).

### Retrieving the logical structure

Each (future) node (list, list item, blockquote, etc.) is associated with the so-called _[marker block]_.
The rollback-free parsing algorithm is processing every token in the file, one by one.
Tokens are passed to the opened marker blocks, and each block chooses whether to:

- do nothing
- drop itself
- complete itself

The [marker processor] stores the blocks, executes the actions chosen by the blocks, and, probably, adds some new ones.

### Parsing inlines

Since the inline constructs in markdown have some priorities
(i.e. if two different ones are interlapping, the parsing result depends on the their types, not their positions,
e.g. ``` *code, `not* emph` ``` and ``` `code, *not` emph* ``` are both code spans + literal backticks).

The parsing of the inlines hence is very natural. For each inline construct there is a particular parser which
accepts some input text and returns:

1. The parsed nodes found in this text;
2. The sub-text(s), which can be passed to the further inline parsers.

See [sequential parser].

Extending the parser
-------------------

From the previous section one may imply that there are four points where parser could be customized:

1. Defining the own way (priorities) to create new marker blocks;

  (This is made with the own marker processor)
2. Creating own marker blocks;
3. Defining the own sequence in which sequential parsers are run;

  (See [sequential parser manager](https://github.com/valich/intellij-markdown/blob/master/src/org/intellij/markdown/parser/sequentialparsers/SequentialParserManager.kt))
4. Defining own sequential parsers to parse own inline structs.

Unfortunately, currently the code is not ready for choosing the dialects in runtime, but this is a routine task
that is planned for the nearest future.

[marker processor]: https://github.com/valich/intellij-markdown/blob/master/src/org/intellij/markdown/parser/MarkerProcessor.kt
[marker block]: https://github.com/valich/intellij-markdown/blob/master/src/org/intellij/markdown/parser/markerblocks/MarkerBlock.kt
[sequential parser]: https://github.com/valich/intellij-markdown/blob/master/src/org/intellij/markdown/parser/sequentialparsers/SequentialParser.kt
