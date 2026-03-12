package com.quarkdown.stdlib

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.block.createSectionHeading
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyCitation
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyView
import com.quarkdown.core.bibliography.style.csl.CslBibliographyStyle
import com.quarkdown.core.context.Context
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
 * @param headingDepth depth of the heading preceding the bibliography
 * @param includeHeadingInToc whether the heading preceding the bibliography should itself be indexed
 *                            in the document's table of contents.
 *                            Cannot be enabled together with [decorativeHeading]
 * @param decorativeHeading whether the heading, if present, should be decorative,
 *                          which means it does not trigger automatic page breaks and is not numbered.
 *                          Cannot be enabled together with [includeHeadingInToc]
 * @return an [AstRoot] containing an optional heading and a [BibliographyView]
 * @see cite to cite bibliography entries
 * @throws java.io.IOException if the bibliography file cannot be read or parsed
 * @throws IllegalArgumentException if the specified style does not exist or is invalid,
 *         or if both [includeHeadingInToc] and [decorativeHeading] are enabled
 * @wiki Bibliography
 */
fun bibliography(
    @Injected context: Context,
    path: String,
    @LikelyNamed style: String = DEFAULT_CSL_STYLE,
    @LikelyNamed title: InlineMarkdownContent? = null,
    @Name("headingdepth") headingDepth: Int = 1,
    @Name("indexheading") includeHeadingInToc: Boolean = false,
    @Name("decorativeheading") decorativeHeading: Boolean = false,
): NodeValue {
    val file = file(context, path)
    val resolvedStyle = CslBibliographyStyle.from(style, file.inputStream(), file.name, context.documentInfo.locale)

    val heading =
        Heading.createSectionHeading(
            title?.children,
            localizationKey = "bibliography",
            context,
            depth = headingDepth,
            isDecorative = decorativeHeading,
            includeInTableOfContents = includeHeadingInToc,
        )

    return AstRoot(
        listOfNotNull(
            heading,
            BibliographyView(
                bibliography = resolvedStyle.bibliography,
                style = resolvedStyle,
            ),
        ),
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
