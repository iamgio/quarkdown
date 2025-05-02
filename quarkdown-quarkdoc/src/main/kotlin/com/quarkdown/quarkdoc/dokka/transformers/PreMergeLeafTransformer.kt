package com.quarkdown.quarkdoc.dokka.transformers

import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.transformers.documentation.PreMergeDocumentableTransformer

/**
 *
 */
abstract class PreMergeLeafTransformer : PreMergeDocumentableTransformer {
    override fun invoke(modules: List<DModule>): List<DModule> = modules.map(::transform)

    private fun transform(module: DModule): DModule =
        module.copy(
            packages = module.packages.map(::transform),
        )

    private fun transform(pkg: DPackage): DPackage =
        pkg.copy(
            functions = pkg.functions.map(::transform),
        )

    abstract fun transform(function: DFunction): DFunction
}
