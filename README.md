intellij-markdown [![Build Status](https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:IntelliJMarkdownParser_BuildForIdeaPlugin)/statusIcon.svg?guest=1)](https://teamcity.jetbrains.com/viewType.html?buildTypeId=IntelliJMarkdownParser_BuildForIdeaPlugin&guest=1) [![Download](https://api.bintray.com/packages/jetbrains/markdown/markdown/images/download.svg) ](https://bintray.com/jetbrains/markdown/markdown/_latestVersion) [![official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
=================

## Multiplatform Markdown processor written in Kotlin

Introduction
-------------

[intellij-markdown][self] is an extensible markdown processor.
It aims to suit the following needs:
- Use one code base for both client and server-side processing;
- Produce consistent output on different platforms;
- Support different flavours;
- Be easily extensible.

The processor is written in pure [Kotlin] (with a little [flex][jflex]) so it can be compiled to both JS and Java bytecode
and thus can be used everywhere.

Usage
-----

### Adding `intellij-markdown` as a dependency

The library is hosted in jcenter and bintray, so in order to add it you need to set up one of repositories:
```groovy
repositories {
    // other repos...
    
    // select one of these
    jcenter()
    maven { url "https://dl.bintray.com/jetbrains/markdown" }
}
```

This is a [Kotlin Multiplatform](https://kotlinlang.org/docs/reference/multiplatform.html) project
and hence is better served under gradle. If you have fresh enough gradle (`>= 5.4`), you can just
add the main artifact as a dependency: 
```groovy
dependencies {
    // other deps...

    implementation "org.jetbrains:markdown:0.2.0.pre-55"
}
```

Gradle should resolve your target platform and decide which artifact (jvm or js) to download.

If you are writing **multiplatform** project as well, you can add one dependency for `commonMain`:
```groovy
commonMain {
     dependencies {
         implementation "org.jetbrains:markdown:0.2.0.pre-55"
     }
 }
```

If you are using Maven or older gradle, you should specify the respective artifact for your platform,
e.g.
* `org.jetbrains:markdown-jvm:0.2.0.pre-55` for jvm version
* `org.jetbrains:markdown-js:0.2.0.pre-55` for js version

### Using `intellij-markdown` for parsing and generating html

One of the goals of this project is to provide flexibility in terms of the tasks being solved.
[Markdown plugin] for JetBrains IDEs is an example of usage when markdown processing is done
in several stages:

* Parse block structure without parsing inlines to provide lazy parsable blocks for IDE;
* Quickly parse inlines of a given block to provide faster syntax highlighting update;
* Generate HTML for preview.

These tasks may be completed independently according to the current needs. 

#### Simple html generation (Kotlin)

```kotlin
val src = "Some *Markdown*"
val flavour = CommonMarkFlavourDescriptor()
val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(src)
val html = HtmlGenerator(src, parsedTree, flavour).generateHtml()
```

#### Simple html generation (Java)

```java
final String src = "Some *Markdown*";
final MarkdownFlavourDescriptor flavour = new GFMFlavourDescriptor();
final ASTNode parsedTree = new MarkdownParser(flavour).buildMarkdownTreeFromString(text);
final String html = new HtmlGenerator(src, parsedTree, flavour, false).generateHtml()
```

Development gotchas
-------------------

1. The currently used CI is [TeamCity](https://teamcity.jetbrains.com/project/IntelliJMarkdownParser?mode=builds).

   Incoming pull requests will be tested there, you can check the build status (manually) via the link above.


2. The only non-Kotlin files are `.flex` lexer definitions. They are used for generating lexers which are
   the first stage of inlines parsing. Unfortunately, due to bugs, native `java->kt` conversion crashes for these files.  
   
   So, conversion from `.flex` to respective Kotlin files deserves a special instruction (use IntelliJ please):
   1. Install `grammar-kit` plugin, as will be suggested on `.flex` files opening.
   1. Install [`jflexToKotlin`](https://github.com/valich/jflexToKotlin) plugin (manually, via settings). 
   1. Run `Run JFlex Generator` action while having `.flex` file opened.
        * On the first run a dialog will open, suggesting to place to download jflex. Select project root,
          then delete excessively downloaded `.skeleton` file.
   1. A respective `_<SomeName>Lexer.java` will be generated somewhere. Move it near existing `_<SomeName>Lexer.kt`.
   1. Delete `.kt` lexer.
   1. Run `Convert JFlex Lexer to Kotlin` action while having the new `.java` file opened.
   1. Fix the small problems such as imports in kt file. There should be no major issues; if there are,
      feel free to complain :)

Parsing algorithm
-----------------

The parsing process is held in two logical parts:

1. Splitting the document into the blocks of logical structure (lists, blockquotes, paragraphs, etc.);
2. Parsing the inline structure of the resulted blocks.

This is the same way as the one being proposed in [Commonmark spec](http://spec.commonmark.org/0.16/#appendix-a-a-parsing-strategy).

### Building the logical structure

Each (future) node (list, list item, blockquote, etc.) is associated with the so-called _[marker block]_.
The rollback-free parsing algorithm is processing every token in the file, one by one.
Tokens are passed to the opened marker blocks, and each block chooses whether to:

- do nothing
- drop itself
- complete itself

The _[marker processor]_ stores the blocks, executes the actions chosen by the blocks, and, possibly, adds some new ones.

### Parsing inlines

For the sake of speed and parsing convenience the text is passed to the [lexer] at first. Then the resulting
set of tokens is processed in the special way.

Since the inline constructs in markdown have some priorities
(i.e. if two different ones overlap, the parsing result depends on the their types, not their positions,
e.g. ``` *code, `not* emph` ``` and ``` `code, *not` emph* ``` are both code spans + literal asterisks), normal
recursive parsing is inapplicable.

Still the parsing of the inlines is quite straightforward. For each inline construct there is a 
particular _[sequential parser]_ which accepts some input text and returns:

1. The parsed ranges found in this text;
2. The sub-text(s), which are to be passed to the subsequent inline parsers.

### Building AST

After building logical structure and parsing inlines a set of ranges corresponding to some markdown
entities (i.e. nodes) is given. In order to work with the results effectively it ought to be converted to
the [AST].

As a result, a root [AST node] corresponding to the parsed markdown document is returned. Each AST node has
own type which is called [IElementType](src/org/intellij/markdown/IElementType.kt) as in IntelliJ Platform.

### Generating HTML

For a given AST root a special [visitor][visitor pattern] to generate the resulting HTML is created. Using a 
given mapping from IElementType to the [_HTML generating provider_][generating provider] it processes the
parsed tree in [Depth-First order][DFS], generating HTML pieces for on each node visit. 

Extending the parser
-------------------

Many routines in the above process can be extended or redefined by creating a different markdown flavours.
The minimal default is [CommonMark] which is implemented in this project. 
Github Flavoured Markdown (which is also implemented) is an example of extending CommonMark flavour. It can be
used as a [reference](src/org/intellij/markdown/flavours/gfm/GFMFlavourDescriptor.kt) for implementing your own
markdown features.

Below is a tree representing a big part of the API.

API
---

* [`MarkdownFlavourDescriptor`](src/org/intellij/markdown/flavours/MarkdownFlavourDescriptor.kt)
    is a base class for extending markdown parser.

    * [`markerProcessorFactory`](src/org/intellij/markdown/parser/MarkerProcessorFactory.kt)
        is responsible for block structure customization.

        * `stateInfo` value allows to use a state during document parsing procedure.

          `updateStateInfo(pos: LookaheadText.Position)` is called at the beginning of each position processing

        * `populateConstraintsTokens` is called to create nodes for block structure markers at the beginning
          of the lines (for example, `>` characters constituting blockquotes)

        * [`getMarkerBlockProviders`](src/org/intellij/markdown/parser/markerblocks/MarkerBlockProvider.kt)
            is a place to (re)define types of block structures

    * [`sequentialParserManager`](src/org/intellij/markdown/parser/sequentialparsers/SequentialParserManager.kt)

        `getParserSequence` defines inlines parsing procedure. The method must return a list of SequentialParsers
        where the earliest parsers have the biggest operation precedence. For example, to parse code spans and emph
        with correct priority, the list should be [CodeSpanParser, EmphParser] but not the opposite.

        [`SequentialParser`](src/org/intellij/markdown/parser/sequentialparsers/SequentialParser.kt) has only one method:

        `parse(tokens: TokensCache, rangesToGlue: List<IntRange>): ParsingResult`
        * `tokens` is a special holder for the tokens returned by lexer
        * `rangesToGlue` is a list of ranges in the document which are to be searched for the structures in question.

          Considering the input: ``A * emph `code * span` b * c `` for the emph parser ranges
          [`A * emph `, ` b * c`] mean that emph must be searched in the input `A * emph | b * c`.

          The method must essentially return the parsing result (nodes for the found structures) and the parts
          of the text to be given to the next parsers.

          Considering the same input for the code span parser the result would be `` `code * span` ``
          of the type "code span" and the delegate pieces would be [`A * emph `, ` b * c`].

    * [`createInlinesLexer`](src/org/intellij/markdown/lexer/MarkdownLexer.kt) should return the lexer to split the text
      to the tokens before inline parsing procedure run.

    * [`createHtmlGeneratingProviders(linkMap: LinkMap, baseURI: URI?)`](src/org/intellij/markdown/html/GeneratingProvider.kt)
      is the place where generated HTML is customized. This method should return a map which defines how to handle
      the particular kinds of the nodes in the resulting tree.

      `linkMap` here is a precalculated information about the links defined in the document with the means of
      link definition. `baseURI` is the URI to be considered the base path for the relative links resolving.
      For example, given `baseUri='/user/repo-name/blob/master'` the link `foo/bar.png` should be transformed to
      the `/user/repo-name/blob/master/foo/bar.png`.

      Each returned provider must implement `processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode)`
      where 
      * `text` is the whole document being processed,
      * `node` is the node being given to the provider,
      * `visitor` is a special object responsible for the HTML generation.
        See [`GeneratingProviders.kt`](src/org/intellij/markdown/html/GeneratingProviders.kt) for the samples.

[self]: https://github.com/valich/intellij-markdown
[Kotlin]: https://github.com/JetBrains/kotlin
[jflex]: https://github.com/jflex-de/jflex
[markdown plugin]: https://github.com/JetBrains/intellij-plugins/tree/master/markdown

[AST]: https://en.wikipedia.org/wiki/Abstract_syntax_tree 'Wikipedia reference'
[visitor pattern]: https://en.wikipedia.org/wiki/Visitor_pattern 'Wikipedia reference'
[DFS]: https://en.wikipedia.org/wiki/Depth-first_search 'Wikipedia reference'
[CommonMark]: http://commonmark.org

[marker processor]: src/org/intellij/markdown/parser/MarkerProcessor.kt
[marker block]: src/org/intellij/markdown/parser/markerblocks/MarkerBlock.kt
[sequential parser]: src/org/intellij/markdown/parser/sequentialparsers/SequentialParser.kt
[ast node]: src/org/intellij/markdown/ast/ASTNode.kt
[generating provider]: src/org/intellij/markdown/html/GeneratingProvider.kt
[lexer]: src/org/intellij/markdown/lexer/MarkdownLexer.kt
