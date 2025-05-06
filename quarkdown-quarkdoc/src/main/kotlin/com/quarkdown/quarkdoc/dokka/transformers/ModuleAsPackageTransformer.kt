package com.quarkdown.quarkdoc.dokka.transformers

import com.quarkdown.core.util.filterNotNullEntries
import com.quarkdown.quarkdoc.dokka.storage.QuarkdownModulesStorage
import com.quarkdown.quarkdoc.dokka.util.difference
import com.quarkdown.quarkdoc.dokka.util.sourcePaths
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * Transformer that transforms a Quarkdown [com.quarkdown.core.function.library.loader.Module]
 * into a Dokka package containing the module's functions.
 */
class ModuleAsPackageTransformer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    private data class SyntheticModule(
        val name: String,
        val dri: DRI,
        val functions: List<DFunction>,
        val sourceSets: Set<DokkaConfiguration.DokkaSourceSet>,
    )

    private fun getSyntheticModules(functions: List<DFunction>): List<SyntheticModule> =
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

    override fun transformModule(module: DModule): AnyWithChanges<DModule> {
        val syntheticModules: Map<DPackage, List<SyntheticModule>> =
            module.packages.associateWith { getSyntheticModules(it.functions) }

        val newPackages =
            syntheticModules.flatMap { (pkg, modules) ->
                modules.map { module ->
                    pkg.copy(
                        dri = DRI(packageName = pkg.packageName + "." + module.name),
                        functions = module.functions,
                        properties = emptyList(),
                        classlikes = emptyList(),
                        typealiases = emptyList(),
                    )
                }
            }

        return module
            .copy(
                packages = newPackages + module.packages.difference(newPackages),
            ).changed()
    }
}
