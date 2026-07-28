package com.quarkdown.processor.generation

import com.quarkdown.processor.model.FunctionDescriptor
import com.quarkdown.processor.model.ParameterDescriptor

/**
 * Assembles the KDoc block that sits above a wrapper.
 *
 * A wrapper's documentation is stitched together from two source-side pieces:
 * - the source function's own KDoc, with every identifier rewritten to its exported form;
 * - the `@param` tags lifted from each `@Spread` parameter's data class, so the spread's
 *   components appear in the wrapper's KDoc as first-class parameters instead of being hidden
 *   under the outer parameter's own documentation.
 *
 * Returns `null` when neither source contributes anything renderable, letting the generator skip
 * the `/** */` block entirely.
 */
internal object KDocComposer {
    fun compose(function: FunctionDescriptor): String? {
        val renames = collectRenames(function)
        val spreads = function.parameters.filterIsInstance<ParameterDescriptor.Spread>()
        val functionKDoc = rewriteFunctionKDoc(function, spreads, renames)
        val spreadTags = rewriteSpreadParamTags(spreads, renames)

        if (functionKDoc.isNullOrBlank() && spreadTags.isEmpty()) return null

        return buildList {
            functionKDoc?.takeIf { it.isNotBlank() }?.let(::add)
            addAll(spreadTags)
        }.joinToString("\n")
    }

    /**
     * Function KDoc with `@param` tags for spread outer parameters stripped (they'd double up
     * once the spread's own component tags are added) and identifiers rewritten to exported form.
     */
    private fun rewriteFunctionKDoc(
        function: FunctionDescriptor,
        spreads: List<ParameterDescriptor.Spread>,
        renames: Map<String, String>,
    ): String? {
        val spreadOuterNames = spreads.mapTo(mutableSetOf()) { it.originalName }
        return function.kdoc
            ?.let { KDocRewriter.stripParamTags(it, spreadOuterNames) }
            ?.let { KDocRewriter.rewrite(it, renames) }
    }

    /** Per-spread `@param` tag list, extracted from each spread's data class KDoc and rewritten. */
    private fun rewriteSpreadParamTags(
        spreads: List<ParameterDescriptor.Spread>,
        renames: Map<String, String>,
    ): List<String> =
        spreads
            .mapNotNull { it.dataClassKdoc?.let(KDocRewriter::extractParamTags) }
            .flatten()
            .map { KDocRewriter.rewrite(it, renames) }

    /**
     * Original-name -> exported-name map covering every parameter that appears in the wrapper's
     * signature (plain parameters and spread components alike).
     */
    private fun collectRenames(function: FunctionDescriptor): Map<String, String> =
        function.parameters.flatParameters.associate { it.originalName to it.exportedName }
}
