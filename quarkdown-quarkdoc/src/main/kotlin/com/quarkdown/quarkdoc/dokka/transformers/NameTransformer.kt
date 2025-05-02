package com.quarkdown.quarkdoc.dokka.transformers

import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.quarkdoc.dokka.util.extractAnnotation
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.Documentable

class NameTransformer : PreMergeLeafTransformer() {
    override fun transform(function: DFunction): DFunction =
        function.copy(
            name = overrideNameIfAnnotated(function).name,
            parameters = function.parameters.map(::overrideNameIfAnnotated),
        )

    private fun <D : Documentable> overrideNameIfAnnotated(documentable: D): D {
        val nameAnnotation = documentable.extractAnnotation<Name>()
        val newName = nameAnnotation?.params["name"]?.toString() ?: return documentable

        @Suppress("UNCHECKED_CAST")
        return when (documentable) {
            is DFunction -> documentable.copy(name = newName)
            is DParameter -> documentable.copy(name = newName)
            else -> documentable
        } as D
    }
}
