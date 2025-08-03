package com.quarkdown.quarkdoc.dokka.page

import com.quarkdown.core.function.reflect.annotation.LikelyChained
import com.quarkdown.quarkdoc.dokka.signature.QuarkdownSignatureProvider
import com.quarkdown.quarkdoc.dokka.util.hasAnnotation
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.plugability.DokkaContext

private const val CHAINING_WIKI_PAGE = "syntax-of-a-function-call#chaining-calls"

/**
 * Transformer that generates a new section for the `@wiki` documentation tag of a function,
 * with a link to the corresponding wiki page.
 */
class LikelyChainedPageTransformer(
    context: DokkaContext,
) : NewSectionDocumentablePageTransformer<DFunction, Boolean>("Chaining", context) {
    private val signatureProvider by lazy {
        QuarkdownSignatureProvider(
            context,
            requireModule = false,
            defaultValues = false,
            withChaining = true,
        )
    }

    override fun extractDocumentable(documentables: List<Documentable>) = documentables.firstOrNull() as? DFunction

    override fun extractData(documentable: DFunction): Boolean? =
        true
            .takeIf { documentable.hasAnnotation<LikelyChained>() }
            ?.takeIf { documentable.parameters.isNotEmpty() }

    override fun createSection(
        data: Boolean,
        documentable: DFunction,
        builder: PageContentBuilder.DocumentableContentBuilder,
    ) = builder.buildGroup {
        text("This function is designed to be ")
        link(
            "chained",
            WIKI_ROOT + CHAINING_WIKI_PAGE,
        )
        text(" with other function calls:")
        +signatureProvider.signature(documentable)
    }
}
