@param String name = null
@param boolean docs = false
@param String initialContent = null
@param String mainFile = null

@if(!docs && name != null)
# ${name}

@endif

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

For further information and guides, please check out the [official wiki](https://github.com/iamgio/quarkdown/wiki).

@if(!docs)
!(50%)[Quarkdown](image/logo.png)
@else
## Structure

- `_setup.qd`: global setup, included in each subdocument.
- `_nav.qd`: navigation tree, displayed in the sidebar.
@endif
