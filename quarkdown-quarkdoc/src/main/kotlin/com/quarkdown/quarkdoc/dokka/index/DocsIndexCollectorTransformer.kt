package com.quarkdown.quarkdoc.dokka.index

import com.quarkdown.core.function.reflect.annotation.LikelyChained
import com.quarkdown.core.function.toQuarkdownNamingFormat
import com.quarkdown.quarkdoc.dokka.transformers.enumeration.adapters.QuarkdocEnumAdapters
import com.quarkdown.quarkdoc.dokka.transformers.optional.AdditionalParameterPropertiesTransformer.ParameterProperties
import com.quarkdown.quarkdoc.dokka.util.hasAnnotation
import com.quarkdown.quarkdoc.reader.DocsFunction
import com.quarkdown.quarkdoc.reader.DocsParameter
import com.quarkdown.quarkdoc.reader.json.IndexedFunction
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.driOrNull
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.model.doc.Param
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.transformers.documentation.PreMergeDocumentableTransformer

/**
 * Transformer that collects the documentation index data of Quarkdown module functions
 * directly from the Dokka model, into [DocsIndexStorage].
 */
class DocsIndexCollectorTransformer(
    @Suppress("unused") private val context: DokkaContext,
) : PreMergeDocumentableTransformer {
    override fun invoke(modules: List<DModule>): List<DModule> {
        DocsIndexStorage.clear()
        modules
            .asSequence()
            .flatMap { it.packages }
            .forEach { pkg ->
                val moduleName = pkg.quarkdownModuleName ?: return@forEach
                pkg.functions.forEach { collect(it, moduleName) }
            }
        return modules
    }

    /**
     * The Quarkdown module name of a synthetic `<package>.module.<Name>` package, or `null` for regular packages.
     */
    private val DPackage.quarkdownModuleName: String?
        get() =
            packageName
                .split('.')
                .takeIf { it.getOrNull(it.size - 2) == MODULE_PACKAGE_SEGMENT }
                ?.lastOrNull()

    private fun collect(
        function: DFunction,
        moduleName: String,
    ) {
        DocsIndexStorage.add(
            IndexedFunction(
                name = function.name,
                moduleName = moduleName,
                function =
                    DocsFunction(
                        name = function.name,
                        parameters = function.parameters.mapNotNull { it.toDocsParameter(parameterDocumentation(function)) },
                        isLikelyChained = function.hasAnnotation<LikelyChained>() && function.parameters.isNotEmpty(),
                    ),
                contentMarkdown = null,
            ),
        )
    }

    /**
     * @return the documentation tags of each parameter of [function], by parameter name
     */
    private fun parameterDocumentation(function: DFunction): Map<String, List<DocTag>> =
        function.documentation.values
            .flatMap { it.children }
            .filterIsInstance<Param>()
            .associate { it.name to it.root.children }

    private fun DParameter.toDocsParameter(documentation: Map<String, List<DocTag>>): DocsParameter? {
        val name = name ?: return null
        val properties = ParameterProperties.of(this)

        return DocsParameter(
            name = name,
            description = DocTagMarkdownRenderer.render(documentation[name].orEmpty()),
            isOptional = properties.isOptional,
            isLikelyNamed = properties.isLikelyNamed,
            isLikelyBody = properties.isLikelyBody,
            allowedValues =
                type.driOrNull
                    ?.let(QuarkdocEnumAdapters::fromDRI)
                    ?.entries
                    ?.map { it.name.toQuarkdownNamingFormat() },
        )
    }
}
