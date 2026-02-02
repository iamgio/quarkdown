package com.quarkdown.quarkdoc.dokka.page

import com.quarkdown.quarkdoc.dokka.util.findDeep
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.doc.CustomTagWrapper
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.plugability.DokkaContext
import java.net.URLEncoder

private const val TAG_NAME = "wiki"

/**
 * The root URL of the Quarkdown wiki to link to.
 */
const val WIKI_ROOT = "https://quarkdown.com/wiki/"

/**
 * Transformer that generates a new section for the `@wiki` documentation tag of a function,
 * with a link to the corresponding wiki page.
 */
class WikiLinkPageTransformer(
    context: DokkaContext,
) : NewSectionDocumentablePageTransformer<DFunction, String>("Wiki page", context) {
    override fun extractDocumentable(documentables: List<Documentable>) = documentables.firstOrNull() as? DFunction

    /**
     * Extracts the wiki page name from the `@wiki` documentation tag, if present.
     * For example: `@wiki home` -> `home`
     */
    override fun extractData(documentable: DFunction): String? {
        val wikiTag: CustomTagWrapper =
            documentable.documentation.values
                .firstOrNull()
                ?.findDeep<CustomTagWrapper> { it.name == TAG_NAME }
                ?: return null

        return wikiTag.root.findDeep<Text>()?.body
    }

    override fun createSection(
        data: String,
        documentable: DFunction,
        builder: PageContentBuilder.DocumentableContentBuilder,
    ) = builder.buildGroup {
        val url = WIKI_ROOT + URLEncoder.encode(data, Charsets.UTF_8)
        link(data, url)
    }
}
