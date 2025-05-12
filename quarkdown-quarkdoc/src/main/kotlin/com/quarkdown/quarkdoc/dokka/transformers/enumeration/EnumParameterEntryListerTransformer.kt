package com.quarkdown.quarkdoc.dokka.transformers.enumeration

import com.quarkdown.core.function.toQuarkdownNamingFormat
import com.quarkdown.core.util.filterNotNullEntries
import com.quarkdown.quarkdoc.dokka.kdoc.mapDocumentation
import com.quarkdown.quarkdoc.dokka.transformers.QuarkdocDocumentableReplacerTransformer
import com.quarkdown.quarkdoc.dokka.util.tryCopy
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.driOrNull
import org.jetbrains.dokka.model.DEnum
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.doc.CodeInline
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.model.doc.DocumentationLink
import org.jetbrains.dokka.model.doc.H4
import org.jetbrains.dokka.model.doc.Li
import org.jetbrains.dokka.model.doc.Param
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.model.doc.Ul
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * A transformer that, given a parameter that expects an enum value,
 * lists the enum entries in its documentation.
 */
class EnumParameterEntryListerTransformer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    /**
     * @return the parameters, among [parameters], that expect an enum value, associated with their enum declaration.
     */
    private fun associateEnumParameters(parameters: List<DParameter>): Map<String, DEnum> =
        parameters
            .asSequence()
            .map { it.name to it.type.driOrNull?.let(EnumStorage::getByDri) }
            .filterNotNullEntries()
            .toMap()

    /**
     * @return the documentation content to add to the parameter documentation,
     * which lists the enum entries of the given [enum].
     */
    private fun createNewDocumentationContent(enum: DEnum): List<DocTag> =
        listOf(
            H4(
                listOf(
                    Text("Values"),
                ),
            ),
            Ul(
                enum.entries.map { entry ->
                    Li(
                        listOf(
                            DocumentationLink(
                                dri = entry.dri,
                                listOf(
                                    CodeInline(
                                        listOf(
                                            Text(entry.name.toQuarkdownNamingFormat()),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    )
                },
            ),
        )

    override fun transformFunction(function: DFunction): AnyWithChanges<DFunction> {
        val enumParameters: Map<String, DEnum> =
            associateEnumParameters(function.parameters)
                .takeIf { it.isNotEmpty() }
                ?: return function.unchanged()

        // Updates the documentation of the parameters to include the enum entries.
        val documentation =
            mapDocumentation(function.documentation) {
                register(Param::class) { param ->
                    val enum = enumParameters[param.name] ?: return@register param
                    val root = param.root
                    param.copy(
                        root = root.tryCopy(newChildren = root.children + createNewDocumentationContent(enum)),
                    )
                }
            }

        return function.copy(documentation = documentation).changed()
    }
}
