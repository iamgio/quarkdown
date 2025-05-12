package com.quarkdown.quarkdoc.dokka.page

import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.function.quarkdownName
import com.quarkdown.quarkdoc.dokka.transformers.DocumentTargetProperty
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.TextStyle
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * Given a function that is constrained to specific document types,
 * this page transformer explains these constraints in a new section of the documentation page.
 * @see com.quarkdown.quarkdoc.dokka.transformers.DocumentTypeConstraintsTransformer
 */
class DocumentTypeConstraintsPageTransformer(
    context: DokkaContext,
) : NewSectionDocumentablePageTransformer<DFunction, List<DocumentType>>("Target", context) {
    override fun extractDocumentable(documentables: List<Documentable>) = documentables.firstOrNull() as? DFunction

    override fun extractData(documentable: DFunction): List<DocumentType>? = documentable.extra[DocumentTargetProperty]?.targets

    override fun createSection(
        data: List<DocumentType>,
        documentable: DFunction,
        builder: PageContentBuilder.DocumentableContentBuilder,
    ) = builder.buildGroup {
        text("This function is ")
        text("only available", styles = setOf(TextStyle.Bold))
        text(" for the following document types: ")
        data.forEachIndexed { index, target ->
            codeInline { text(target.quarkdownName) }
            text(if (index < data.size - 1) ", " else ".")
        }
    }
}
