package com.quarkdown.quarkdoc.dokka.transformers.module

import com.quarkdown.core.function.library.loader.MultiFunctionLibraryLoader
import com.quarkdown.quarkdoc.dokka.transformers.QuarkdocDocumentableReplacerTransformer
import com.quarkdown.quarkdoc.dokka.util.isOfType
import com.quarkdown.quarkdoc.dokka.util.sourcePaths
import org.jetbrains.dokka.model.DProperty
import org.jetbrains.dokka.model.GenericTypeConstructor
import org.jetbrains.dokka.model.TypeAliased
import org.jetbrains.dokka.plugability.DokkaContext

// Cannot retrieve Module's class as it's typealiased.
private const val MODULE_CLASS_NAME = "Module"
private val MODULE_PACKAGE_NAME = MultiFunctionLibraryLoader::class.java.`package`.name

/**
 * Transformer that, instead of performing transformations,
 * stores the [com.quarkdown.core.function.library.loader.Module] declarations associated with their source files.
 * @see QuarkdownModulesStorage
 */
class ModulesStorer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    private fun isModuleDefinition(property: DProperty): Boolean {
        val typeAlias = property.type as? TypeAliased ?: return false
        val type = typeAlias.typeAlias as? GenericTypeConstructor ?: return false
        return type.dri.isOfType(MODULE_PACKAGE_NAME, MODULE_CLASS_NAME)
    }

    override fun transformProperty(property: DProperty): AnyWithChanges<DProperty> {
        if (!isModuleDefinition(property)) return property.unchanged()

        val dri = property.dri

        property.sourcePaths.singleOrNull()?.let { sourceFile ->
            QuarkdownModulesStorage[sourceFile] = StoredModule(name = property.name, dri = dri)
        }
        return property.unchanged()
    }
}
