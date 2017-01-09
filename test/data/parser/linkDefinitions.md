[foo]: /url
"title" ok

[foo]: /url\bar\*baz "foo\"bar\baz"

[foo]: /url "title"

   [foo]:
      /url
           'the title'

[Foo*bar\]]:my_(url) 'title (with parens)'

[Foo bar]:
<my url>
'title'

[foo]:
/url

[foo]:

[foo]

[ΑΓΩ]: /φου

[foo]: /url "title" ok

    [foo]: /url "title"

Foo
[bar]: /baz

# [Foo]
[foo]: /url
> bar

[foo]: /foo-url "foo"
[bar]: /bar-url
  "bar"
[baz]: /baz-url

> [foo]: /url

[*foo*]: /url "title"

[foo`]: /url "tit`le"

[Foo
  bar]: /url 'tit
            le
            le
            le'


[Foo
  bar]: /url 'tit
            le
            le

            le'

[foo]: <bar>

[foo]: <bar> 'title'

[foo]: <bar baz>

[foo]: <bar
baz>
