@param String name = null
@param String description = null
@param java.util.List<String> keywords = java.util.Collections.emptyList()
@param java.util.List<String> authors = java.util.Collections.emptyList()
@param String type = null
@param boolean docs = false
@param String lang = null
@param boolean theme = false
@param String colorTheme = null
@param String layoutTheme = null
@param boolean pageCounter = false
@param String initialContent = null

@if(!docs && name != null)
.docname {${name}}
@endif
@if(description != null)
.docdescription {${description}}
@endif
@if(!docs && type != null)
.doctype {${type}}
@endif
@if(lang != null)
.doclang {${lang}}
@endif
@if(theme)
.theme@if(colorTheme != null) {${colorTheme}}@endif@if(layoutTheme != null) layout:{${layoutTheme}}@endif
@endif
@if(!keywords.isEmpty())
${""}
.dockeywords
@for(String item : keywords)
${"  "}- ${item}
@endfor
@endif
@if(!authors.isEmpty())
${""}
.docauthors
@for(String item : authors)
${"  "}- ${item}
@endfor
@endif
@if(pageCounter)
${""}
.pagemargin {bottomcenter}
    .currentpage
@endif
@if(docs && name != null)
${""}
.pagemargin {topleft}
    ${name}
@endif
@if(!docs && initialContent != null)
${""}
${initialContent}
@endif
