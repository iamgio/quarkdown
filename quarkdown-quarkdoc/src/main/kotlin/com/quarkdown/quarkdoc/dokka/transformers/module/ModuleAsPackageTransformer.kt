package com.quarkdown.quarkdoc.dokka.transformers.module

import com.quarkdown.core.function.library.module.QuarkdownModule
import com.quarkdown.quarkdoc.dokka.transformers.QuarkdocDocumentableReplacerTransformer
import com.quarkdown.quarkdoc.dokka.util.isOfType
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.DObject
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.model.DProperty
import org.jetbrains.dokka.model.GenericTypeConstructor
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * A Quarkdown-module wrapper as discovered inside a package: the `object <Name>` produced by
 * the KSP native-library processor.
 *
 * @param name the wrapper object's simple name, used as the module's display name
 * @param objectDri DRI of the wrapper object itself, used to drop it from its original package
 * @param functions the wrapper object's public functions, which become the module's functions
 * @param sourceSets the source sets those functions belong to
 */
private data class SyntheticModule(
    val name: String,
    val objectDri: DRI,
    val functions: List<DFunction>,
    val sourceSets: Set<DokkaConfiguration.DokkaSourceSet>,
)

/**
 * Detects whether [classlike] is a Quarkdown-module wrapper: an `object` with a `Module` property
 * typed as [QuarkdownModule]. Returns the matching [SyntheticModule], or `null` when [classlike]
 * is any other classlike.
 */
private fun asSyntheticModule(classlike: DClasslike): SyntheticModule? {
    if (classlike !is DObject) return null
    val moduleProperty = classlike.properties.firstOrNull(::isQuarkdownModuleProperty) ?: return null
    val name = classlike.name ?: return null
    return SyntheticModule(
        name = name,
        objectDri = classlike.dri,
        functions = classlike.functions,
        sourceSets = moduleProperty.sourceSets,
    )
}

private fun isQuarkdownModuleProperty(property: DProperty): Boolean {
    val type = property.type as? GenericTypeConstructor ?: return false
    return type.dri.isOfType<QuarkdownModule>()
}

/**
 * Presents each KSP-generated wrapper object as its own synthetic subpackage of the source
 * package, so the resulting documentation groups a module's functions on the same page under a
 * "Layout", "Text", ... section instead of scattering them across the parent package.
 *
 * The wrapper structure is fixed: `object <Name> { val Module = moduleOf(...); public fun ... }`.
 * We recognize it structurally (an object carrying a `QuarkdownModule`-typed property) and
 * promote its functions into a synthetic `<original.package>.module.<Name>` package. The wrapper
 * object itself is dropped from the original package so it does not appear twice.
 */
class ModuleAsPackageTransformer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    override fun transformModule(module: DModule): AnyWithChanges<DModule> {
        val syntheticsByPackage: Map<DPackage, List<SyntheticModule>> =
            module.packages.associateWith { pkg -> pkg.classlikes.mapNotNull(::asSyntheticModule) }
        if (syntheticsByPackage.values.all { it.isEmpty() }) return module.unchanged()

        val syntheticPackages: List<DPackage> =
            syntheticsByPackage.flatMap { (pkg, modules) -> modules.map { createSyntheticPackage(pkg, it) } }
        val wrapperDris: Set<DRI> =
            syntheticsByPackage.values
                .flatten()
                .map(SyntheticModule::objectDri)
                .toSet()
        val prunedPackages: List<DPackage> =
            module.packages.map { pkg -> pkg.copy(classlikes = pkg.classlikes.filterNot { it.dri in wrapperDris }) }

        return module.copy(packages = syntheticPackages + prunedPackages).changed()
    }

    private fun createSyntheticPackage(
        parentPackage: DPackage,
        syntheticModule: SyntheticModule,
    ): DPackage =
        parentPackage.copy(
            dri = DRI(packageName = parentPackage.packageName + ".module." + syntheticModule.name),
            functions = syntheticModule.functions,
            properties = emptyList(),
            classlikes = emptyList(),
            typealiases = emptyList(),
        )
}
