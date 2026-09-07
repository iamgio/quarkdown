@param String description = null
@param String mainFile = null
.footer
    .docauthor

    .docname

    .currentpage / .totalpages

# .docname

.docauthors::foreach
    name info:
    .name

@if(description != null)
.whitespace

.text {.docdescription} variant:{smallcaps}

@endif
## First slide

@template.creator.gettingstarted(mainFile = mainFile)
## Second slide

.row alignment:{spacebetween}
    This is a multi-column layout.

    $ \int_{-\infty}^{\infty} e^{-x^2} dx = \sqrt{\pi} $

!(25%)[Quarkdown](image/logo.png)
