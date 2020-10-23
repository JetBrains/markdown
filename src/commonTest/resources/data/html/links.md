[link](<foo(and(bar))>)

[link](foo%20b&auml;)

[link](/url "title")
[link](/url 'title')
[link](/url (title))

[link](/url 'title "and" title')

[link *foo **bar** `#`*](/uri)

-------------

[link [foo [bar]]][ref]

[ref]: /uri

[foo][bar]

[bar]: /url "title"

[link *foo **bar** `#`*][ref]

[foo [bar](/uri)][ref]

[ref]: /uri

[foo][BaR]

[Foo
  bar]: /url

[Baz][Foo bar]

[Foo][]

[foo]: /url "title"

[Foo]

[fooe]: /url 'title

with blank line'

[fooe]

[Link nOrmalization and order][]

[Link Normalization AND order]: https://vk.com "first alt"
[link normalization and order]: http://google.com "second alt"
