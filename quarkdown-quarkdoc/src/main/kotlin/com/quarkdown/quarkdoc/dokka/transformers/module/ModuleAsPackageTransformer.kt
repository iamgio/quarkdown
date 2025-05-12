package com.quarkdown.quarkdoc.dokka.transformers.module

import com.quarkdown.core.util.filterNotNullEntries
import com.quarkdown.quarkdoc.dokka.storage.QuarkdownModulesStorage
import com.quarkdown.quarkdoc.dokka.transformers.QuarkdocDocumentableReplacerTransformer
import com.quarkdown.quarkdoc.dokka.util.difference
import com.quarkdown.quarkdoc.dokka.util.sourcePaths
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * A synthetic module is a Quarkdown [com.quarkdown.core.function.library.loader.Module] that is defined in a source file.
 * The synthetic module contains the functions that are defined in the source file.
 * @param name the name of the synthetic module.
 * @param dri the DRI to the [com.quarkdown.core.function.library.loader.Module] property definition.
 * @param functions the functions that are defined in the source file of the module
 * @param sourceSets the source sets that are associated with the functions.
 */
private data class SyntheticModule(
    val name: String,
    val dri: DRI,
    val functions: List<DFunction>,
    val sourceSets: Set<DokkaConfiguration.DokkaSourceSet>,
)

/**
 * Given a list of functions, extracts the Quarkdown modules that they belong to, according to [QuarkdownModulesStorage].
 * @param functions functions to extract the synthetic modules from.
 * @return the list of synthetic modules.
 */
private fun extractSyntheticModules(functions: List<DFunction>): List<SyntheticModule> =
    functions
        .groupBy { function ->
            val sourcePath = function.sourcePaths.singleOrNull() ?: return@groupBy null
            QuarkdownModulesStorage[sourcePath]
        }.asSequence()
        .map { it.toPair() }
        .filterNotNullEntries()
        .map { (module, functions) ->
            SyntheticModule(module.name, module.dri, functions, functions.flatMap { it.sourceSets }.toSet())
        }.toList()

/**
 * Creates a synthetic subpackage of the given [parentPackage] for each module of [syntheticModules].
 * @param parentPackage the package to create the synthetic subpackages for
 * @param syntheticModules the list of synthetic modules to create the subpackages for
 * @return the new synthetic packages
 */
private fun createSyntheticPackages(
    parentPackage: DPackage,
    syntheticModules: List<SyntheticModule>,
): List<DPackage> =
    syntheticModules.map { module ->
        parentPackage.copy(
            dri = DRI(packageName = parentPackage.packageName + ".module." + module.name),
            functions = module.functions,
            properties = emptyList(),
            classlikes = emptyList(),
            typealiases = emptyList(),
        )
    }

/**
 * Transformer that transforms a Quarkdown [com.quarkdown.core.function.library.loader.Module]
 * into a Dokka package containing the module's functions.
 */
class ModuleAsPackageTransformer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    override fun transformModule(module: DModule): AnyWithChanges<DModule> {
        val newPackages: List<DPackage> =
            module.packages
                .associateWith { extractSyntheticModules(it.functions) }
                .flatMap { (pkg, modules) ->
                    createSyntheticPackages(pkg, modules)
                }

        return module
            .copy(
                packages = newPackages + module.packages.difference(newPackages),
            ).changed()
    }
}
