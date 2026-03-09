package com.quarkdown.stdlib

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyCitation
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyView
import com.quarkdown.core.bibliography.style.csl.CslBibliographyStyle
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.function.library.module.QuarkdownModule
import com.quarkdown.core.function.library.module.moduleOf
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.LikelyNamed
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.wrappedAsValue

/**
 * `Bibliography` stdlib module exporter.
 * This module handles bibliographies and citations.
 * @see com.quarkdown.core.bibliography.Bibliography
 */
val Bibliography: QuarkdownModule =
    moduleOf(
        ::bibliography,
        ::cite,
    )

/**
 * The default [CSL](https://citationstyles.org) style used when no explicit style is specified.
 */
private const val DEFAULT_CSL_STYLE = "ieee"

/**
 * Generates a bibliography from a bibliography file.
 *
 * Supported formats include [BibTeX](https://www.bibtex.org) (`.bib`),
 * CSL JSON, YAML, EndNote, and RIS.
 *
 * The bibliography is formatted using a [CSL](https://citationstyles.org) style definition,
 * powered by [citeproc-java](https://github.com/michel-kraemer/citeproc-java).
 * This enables support for thousands of citation styles from the
 * [CSL Style Repository](https://github.com/citation-style-language/styles).
 *
 * Example:
 * ```markdown
 * .bibliography {bibliography.bib}
 *     style:{apa}
 * ```
 *
 * @param path path to the bibliography file, with extension
 * @param style [CSL](https://citationstyles.org) style identifier (e.g. `apa`, `ieee`, `chicago-author-date`).
 * @param title title of the bibliography. If unset, the default localized title is used
 * @param decorativeTitle whether the title, if present, should be a decorative heading,
 *                        which does not trigger automatic page breaks.
 * @return a wrapped [BibliographyView] node
 * @see cite to cite bibliography entries
 * @wiki Bibliography
 */
fun bibliography(
    @Injected context: MutableContext,
    path: String,
    @LikelyNamed style: String = DEFAULT_CSL_STYLE,
    @LikelyNamed title: InlineMarkdownContent? = null,
    @Name("decorativetitle") decorativeTitle: Boolean = false,
): NodeValue {
    val file = file(context, path)

    val resolvedStyle = CslBibliographyStyle.from(style, file.inputStream(), file.name)

    return BibliographyView(
        title = title?.children,
        bibliography = resolvedStyle.bibliography,
        style = resolvedStyle,
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
 * @wiki Bibliography#citations
 */
fun cite(key: String) =
    BibliographyCitation(key)
        .wrappedAsValue()
