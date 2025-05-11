package com.quarkdown.quarkdoc.dokka.page

import com.quarkdown.quarkdoc.dokka.util.documentableContentBuilder
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.pages.WithDocumentables
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.transformers.pages.PageTransformer

/**
 * A [PageTransformer] that features a ready-to-use content builder for building documentation pages for [Documentable]s of type [D].
 * @param D the type of [Documentable] that should own the page
 */
abstract class DocumentablePageTransformer<D : Documentable>(
    private val context: DokkaContext,
) : PageTransformer {
    /**
     * Extracts the [Documentable] from the list of documentables featured in the page.
     * @param documentables the list of documentables of the page
     * @return the extracted [Documentable] of type [D], if any. Transformation is stopped if `null`.
     */
    protected abstract fun extractDocumentable(documentables: List<Documentable>): D?

    /**
     * Creates the content for the page based on the extracted documentable via [extractDocumentable].
     * @param page the page to create content for
     * @param documentable the [Documentable] of type [D] that owns the page
     * @param builder the content builder to use
     * @return the created content, if any. Transformation is stopped if `null`.
     */
    protected abstract fun createContent(
        page: ContentPage,
        documentable: D,
        builder: PageContentBuilder.DocumentableContentBuilder,
    ): ContentNode?

    override fun invoke(input: RootPageNode): RootPageNode {
        return input.transformContentPagesTree { page ->
            if (page !is WithDocumentables) return@transformContentPagesTree page

            val documentable =
                extractDocumentable(page.documentables)
                    ?: return@transformContentPagesTree page

            val builder =
                context.documentableContentBuilder(
                    documentable,
                    page.dri,
                )

            createContent(page, documentable, builder)
                ?.let { page.modified(content = it) }
                ?: page
        }
    }
}
