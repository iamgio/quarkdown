@param String name = null
@param boolean docs = false
@param String initialContent = null
@param String mainFile = null

@if(!docs && name != null)
# ${name}

@endif

@template.creator.gettingstarted(docs = docs, mainFile = mainFile)
@if(!docs)
!(50%)[Quarkdown](image/logo.png)
@else
## Structure

- `_setup.qd`: global setup, included in each subdocument.
- `_nav.qd`: navigation tree, displayed in the sidebar.
@endif
