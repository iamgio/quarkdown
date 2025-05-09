package com.quarkdown.quarkdoc.dokka.transformers

import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.function.reflect.annotation.OnlyForDocumentType
import com.quarkdown.quarkdoc.dokka.util.extractAnnotation
import com.quarkdown.quarkdoc.dokka.util.parameterToEnumArray
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.properties.ExtraProperty
import org.jetbrains.dokka.model.properties.PropertyContainer
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * Extra property that stores the document types a function supports, if specified.
 * @param targets the list of document types the function supports
 */
data class DocumentTarget(
    val targets: List<DocumentType>,
) : ExtraProperty<DFunction> {
    companion object : ExtraProperty.Key<DFunction, DocumentTarget>

    override val key = DocumentTarget
}

/**
 * Given a function annotated with `@OnlyForDocumentType` which defines constraints
 * about the document type the function supports, this transformer
 * adds a [DocumentTarget] extra property.
 * @see com.quarkdown.quarkdoc.dokka.page.DocumentTypeConstraintsPageTransformer
 */
class DocumentTypeConstraintsTransformer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    override fun transformFunction(function: DFunction): AnyWithChanges<DFunction> {
        val types: List<DocumentType> =
            function
                .extractAnnotation<OnlyForDocumentType>()
                ?.parameterToEnumArray(OnlyForDocumentType::types.name, DocumentType::valueOf)
                ?: return function.unchanged()

        val extras = PropertyContainer.withAll(DocumentTarget(types))

        return function
            .withNewExtras(extras)
            .changed()
    }
}
