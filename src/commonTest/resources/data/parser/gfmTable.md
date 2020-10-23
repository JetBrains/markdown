First Header | Second Header
------------ | -------------
Content from cell 1 | Content from cell 2
Content in the first column | Content in the second column

|aaa
|---
|bbb
ccc

aaa|
---|
bbb|

|aa
|--
|bb

|aaa
|---

aaa|
---|

aaa
---|

aaa|
---

foo|bar


Alignment
---------

|aaa
|:---

|aaa
|:---:

|aaa
|---:

|aaa
|::---

|aaa
|-:--

|a*a*
|---
|b*b*

_The same number of cells should be present in header and separator line_

|foo|bar|baz|
-------------

|foo|bar|baz|
|-----------

|foo|bar|baz|
|---|---|---|---

|foo|bar|baz|
---|---|---

|||||
---|---|---|---

_The number of row cells may be different:_

|foo|bar|baz|
---|---|---
|foo

|foo|bar|baz|
---|---|---
|foo|baz|maaz|quiz

(RUBY-18313) | Table | With trailing spaces |  
-------------------|-------------|-----------------------|  
In the second      | line | (with dashes)    |  
