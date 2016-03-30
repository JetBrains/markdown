* [implicit current directory relative inline link](baz.html)
* [explicit current directory relative inline link](./baz.html)
* [parent directory relative inline link](../quux.html)
* [absolute inline link](https://google.com/)
* [root inline link](/root)

+ ![implicit current directory relative inline image](baz-image.png)
+ ![explicit current directory relative inline image](./baz-image.png)
+ ![parent directory relative inline image](../quux-image.png)
+ ![absolute inline image](http://example.com/not-real.png)
+ ![root inline image](/root)

<!--autolinks require absolute URI by definition-->
- <https://google.com/>

* [implicit current directory relative link definition][implicit current directory relative link definition]
* [explicit current directory relative link definition][explicit current directory relative link definition]
* [parent directory relative link definition][parent directory relative link definition]
* [absolute link definition][absolute link definition]
* [root link definition][root link definition]

+ [implicit current directory relative link definition]
+ [explicit current directory relative link definition]
+ [parent directory relative link definition]
+ [absolute link definition]
+ [root link definition]


[implicit current directory relative link definition]: baz.html
[explicit current directory relative link definition]: ./baz.html
[parent directory relative link definition]: ../quux.html
[absolute link definition]: https://google.com/
[root link definition]: /root
