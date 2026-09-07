@param boolean docs = false
@param String mainFile = null
Welcome to [Quarkdown](https://github.com/iamgio/quarkdown)! This is the starting point of your document.

@if(docs)
## Compiling

@endif
- To compile this document, please `cd` to this file's parent directory and run:
  `quarkdown c ${mainFile}.qd`

- To enable live preview, run:
  `quarkdown c ${mainFile}.qd -p -w`

@if(docs)
## Wiki

@endif
For further information and guides, please check out the [official wiki](https://quarkdown.com/wiki).
