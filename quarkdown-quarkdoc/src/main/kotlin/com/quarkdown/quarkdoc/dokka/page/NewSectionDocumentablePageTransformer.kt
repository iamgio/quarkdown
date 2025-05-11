package com.quarkdown.quarkdoc.dokka.page

import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentDivergentInstance
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentStyle
import org.jetbrains.dokka.pages.recursiveMapTransform
import org.jetbrains.dokka.plugability.DokkaContext

private const val KDOC_TAG_HEADER_LEVEL = 4

/**
 * A [org.jetbrains.dokka.transformers.pages.PageTransformer] that creates a new titled section in the documentation page of a [Documentable].
 * For instance, *Parameters*, *Return*, *See also*, etc. are titled sections.
 * @param D the type of [Documentable] that owns the page
 * @param T the type of data to be extracted from the [Documentable] and displayed in the section
 */
abstract class NewSectionDocumentablePageTransformer<D : Documentable, T>(
    private val title: String,
    context: DokkaContext,
) : DocumentablePageTransformer<D>(context) {
    /**
     * Extracts the data from the [Documentable] to be displayed in the section content.
     * @return the extracted data, if any. Transformation is stopped if `null`.
     */
    protected abstract fun extractData(documentable: D): T?

    /**
     * Creates the content for the section based on the data extracted from [extractData].
     * @param data the extracted data
     * @param documentable the [Documentable] that owns the page
     * @param builder the content builder to use
     * @return the created content, without the header
     */
    protected abstract fun createSection(
        data: T,
        documentable: D,
        builder: PageContentBuilder.DocumentableContentBuilder,
    ): ContentNode

    /**
     * Prepends a section header to the content created by [createSection].
     */
    private fun createTitledSection(
        data: T,
        documentable: D,
        builder: PageContentBuilder.DocumentableContentBuilder,
    ): ContentNode =
        builder.buildGroup(styles = setOf(ContentStyle.KDocTag)) {
            header(level = KDOC_TAG_HEADER_LEVEL, title)
            +createSection(data, documentable, builder).children
        }

    override fun createContent(
        page: ContentPage,
        documentable: D,
        builder: PageContentBuilder.DocumentableContentBuilder,
    ): ContentNode? {
        val data = extractData(documentable) ?: return null
        val section = createTitledSection(data, documentable, builder)
        // The original page content to update lies in a ContentDivergentInstance node.
        return page.content.recursiveMapTransform<ContentDivergentInstance, ContentNode> { node ->
            when (val after = node.after) {
                is ContentGroup -> node.copy(after = after.copy(children = after.children + listOf(section)))
                null -> node.copy(after = section)
                else -> node
            }
        }
    }
}
