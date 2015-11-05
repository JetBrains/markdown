Colons can be used to align columns.

| Tables        | Are           | Cool  |
| ------------- |:-------------:| -----:|
| col 3 is      | right-aligned | $1600 |
| col 2 is      | centered      |   $12 |
| zebra stripes | are neat      |    $1 |

There must be at least 3 dashes separating each header cell.
The outer pipes (|) are optional, and you don't need to make the
raw Markdown line up prettily. You can also use inline Markdown.

Markdown | Less | Pretty
--- | --- | ---
*Still* | `renders` | **nicely**
1 | 2 | 3

Here we'll test unexpected numbers of columns in data cells:

| Header 1 | Header 2 | Header 3 |
|----------|----------|----------|
|  Data 1  |

| Header 1 | Header 2 | Header 3 |
|----------|----------|----------|
|  Data 1  | Data 2   | Data 3   | Data 4 |

End.