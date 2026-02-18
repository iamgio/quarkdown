@param String name = null
@param String initialContent = null

@if(name != null)
.docname {${name}}
@endif
.include {docs}

@if(initialContent != null)
${initialContent}
@endif
