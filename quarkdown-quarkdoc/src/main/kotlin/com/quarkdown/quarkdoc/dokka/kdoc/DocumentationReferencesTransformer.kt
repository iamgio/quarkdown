package com.quarkdown.quarkdoc.dokka.kdoc

import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.doc.DocumentationLink
import org.jetbrains.dokka.model.doc.Param
import org.jetbrains.dokka.model.doc.See

/**
 * Entity that transforms references of [org.jetbrains.dokka.model.Documentable]s within a KDoc documentation.
 */
interface DocumentationReferencesTransformer {
    /**
     * Transforms a link, in the form of `[link]`, within a KDoc documentation.
     * @param the original link
     * @return the transformed link
     */
    fun onLink(link: DocumentationLink): DocumentationLink

    /**
     * Transforms a parameter reference, in the form of `@param paramName`, within a KDoc documentation.
     * @param param the original parameter reference
     * @param actualParameter the parameter that the reference points to
     * @return the transformed parameter reference
     */
    fun onParam(
        param: Param,
        actualParameter: DParameter,
    ): Param

    /**
     * Transforms a "see" reference, in the form of `@see link`, within a KDoc documentation.
     * @param see the original "see" reference
     * @return the transformed "see" reference
     */
    fun onSee(see: See): See

    /**
     * Transforms references within a KDoc documentation.
     * @param documentation the original documentation
     * @param parameters the list of arameters to be looked up from `@param` references
     * @return the transformed documentation
     */
    fun transformReferences(
        documentation: DokkaDocumentation,
        parameters: List<DParameter>,
    ): DokkaDocumentation =
        mapDocumentation(documentation) {
            register(DocumentationLink::class, ::onLink)
            register(Param::class) { param ->
                val actualParameter = parameters.find { it.name == param.name } ?: return@register param
                onParam(param, actualParameter)
            }
            register(See::class, ::onSee)
        }
}
