package com.quarkdown.stdlib

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyView
import com.quarkdown.core.bibliography.bibtex.BibTeXBibliographyParser
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.function.library.loader.Module
import com.quarkdown.core.function.library.loader.moduleOf
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.LikelyNamed
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.core.bibliography.style.BibliographyStyle as CoreBibliographyStyle

/**
 * `Bibliography` stdlib module exporter.
 * This module handles bibliographies and citations.
 * @see com.quarkdown.core.bibliography.Bibliography
 */
val Bibliography: Module =
    moduleOf(
        ::bibliography,
    )

/**
 * Bibliography styles supported by [bibliography].
 * See [here](https://www.overleaf.com/learn/latex/Bibtex_bibliography_styles) for examples of each style.
 */
enum class BibliographyStyle(
    internal val style: CoreBibliographyStyle,
) {
    PLAIN(CoreBibliographyStyle.Plain),
    IEEETR(CoreBibliographyStyle.Ieeetr),
}

/**
 * Generates a bibliography from a [BibTeX](https://www.bibtex.org) file.
 * @param path path to the BibTeX file, with extension
 * @param style bibliography style to use
 * @param title title of the bibliography. If unset, the default localized title is used
 * @return a wrapped [BibliographyView] node
 * @wiki Bibliography
 */
fun bibliography(
    @Injected context: MutableContext,
    path: String,
    @LikelyNamed style: BibliographyStyle = BibliographyStyle.PLAIN,
    @LikelyNamed title: InlineMarkdownContent? = null,
): NodeValue {
    val file = file(context, path)
    val bibliography = BibTeXBibliographyParser.parse(file.reader())

    return BibliographyView(
        title = title?.children,
        bibliography = bibliography,
        style = style.style,
    ).wrappedAsValue()
}
