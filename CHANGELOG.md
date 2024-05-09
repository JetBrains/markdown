# intellij-markdown Changelog

## [Unreleased]
- Fixed handling of html entities inside mathematical expressions 

## [0.7.1]
- Fixed parsing of code spans with backslashes and spaces
- Updated mathematical expressions in GFM to trim surrounding backticks and whitespaces

## [0.7.0]
- [#133] Added support for mathematical expressions in GFM
- [#140] Added wasmJs target
- [#146] Added linuxArm64 target
- [#149] Fixed parsing of code spans with backslashes and spaces

## 0.6.1
- [#141] Added support for cancellation of the parsing process

## 0.6.0
- [#135] Fixed node creation for zero-length table cells

## 0.5.2
- [#130] Fixed segfault caused by lexer companion object initialization on native targets

## 0.5.1
- [#127] Migrated lexer to the optional BitSet
- [#129] Fixed parsing of auto-link lexing to stop at opening angle bracket

## 0.5.0
- Updated Kotlin version to `1.9.0` and set the project language level to `1.7`
- [#121] Fixed legacy mode warnings from JS artifacts

## 0.4.1
- [#117] Fixed exceptions caused by the table pipes regex in Safari

## 0.4.0
- Added the ability to provide a custom way of obtaining opening info for code fence delimiters

## 0.3.6
- [#114] Added support for HTTPS urls in GFM auto links without explicitly defined schema
- Fixed binary compatibility issues caused by `MarkdownParser(MarkdownFlavourDescriptor)` constructor

## 0.3.5
- [#48] Updated table parsing constraints to allow creating tables with less than 3 dashes in delimiter row

## 0.3.4
- [[IDEA-283181](https://youtrack.jetbrains.com/issue/IDEA-283181)] Fixed incorrect handling of escaped pipes in code spans inside table cells

## 0.3.3
- [#108] Fixed incorrect handling of escaped pipes inside tables
- [#111] Fixed public type of `GFMFlavourDescriptor.markerProcessorFactory`
