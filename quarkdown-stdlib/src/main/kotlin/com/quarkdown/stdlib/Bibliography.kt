package com.quarkdown.stdlib

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyCitation
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyView
import com.quarkdown.core.bibliography.bibtex.BibTeXBibliographyParser
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.function.library.loader.Module
import com.quarkdown.core.function.library.loader.moduleOf
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.LikelyNamed
import com.quarkdown.core.function.reflect.annotation.Name
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
        ::cite,
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
 * @param decorativeTitle whether the title, if present, should be a decorative heading,
 *                        which does not trigger automatic page breaks.
 * @return a wrapped [BibliographyView] node
 * @wiki Bibliography
 */
fun bibliography(
    @Injected context: MutableContext,
    path: String,
    @LikelyNamed style: BibliographyStyle = BibliographyStyle.PLAIN,
    @LikelyNamed title: InlineMarkdownContent? = null,
    @Name("decorativetitle") decorativeTitle: Boolean = false,
): NodeValue {
    val file = file(context, path)
    val bibliography = BibTeXBibliographyParser.parse(file.reader())

    return BibliographyView(
        title = title?.children,
        bibliography = bibliography,
        style = style.style,
        isTitleDecorative = decorativeTitle,
    ).wrappedAsValue()
}

/**
 * Creates a citation to a bibliography entry.
 *
 * The result is a label that matches with that of the bibliography entry with the given [key].
 *
 * Example:
 *
 * `bibliography.bib`
 *
 * ```bibtex
 * @article{einstein,
 *   ...
 * }
 *
 * ...
 * ```
 *
 * Quarkdown:
 *
 * ```markdown
 * Einstein's work .cite {einstein} is fundamental to modern physics.
 *
 * .bibliography {bibliography.bib}
 * ```
 *
 * Result:
 * ```text
 * Einstein's work [1] is fundamental to modern physics.
 * ```
 * @param key the key of the bibliography entry to cite
 * @return a wrapped [BibliographyCitation] node
 * @wiki Bibliography#citing
 */
fun cite(key: String) =
    BibliographyCitation(key)
        .wrappedAsValue()
