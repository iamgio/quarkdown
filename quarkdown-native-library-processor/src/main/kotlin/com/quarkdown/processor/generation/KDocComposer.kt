package com.quarkdown.processor.generation

import com.quarkdown.processor.model.FunctionDescriptor
import com.quarkdown.processor.model.ParameterDescriptor

/**
 * Assembles the KDoc block that sits above a wrapper.
 */
internal object KDocComposer {
    /**
     * Returns the KDoc block for a wrapper, or null if the wrapper has no KDoc to emit.
     *
     * The wrapper's KDoc is composed of:
     * 1. The source function's KDoc, with `@param` tags for spread outer parameters stripped (they'd
     *    double up once the spread's own component tags are added) and identifiers rewritten to
     *    exported form.
     * 2. Per-spread `@param` tags, extracted from each spread's data class KDoc and rewritten.
     */
    fun compose(function: FunctionDescriptor): String? {
        val spreads = function.parameters.filterIsInstance<ParameterDescriptor.Spread>()
        val functionKDoc = rewriteFunctionKDoc(function, spreads)
        val spreadTags = rewriteSpreadParamTags(spreads)

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
    ): String? {
        val spreadOuterNames = spreads.mapTo(mutableSetOf()) { it.originalName }
        val renames = collectPlainRenames(function)
        return function.kdoc
            ?.let { KDocRewriter.stripParamTags(it, spreadOuterNames) }
            ?.let { KDocRewriter.rewrite(it, renames) }
    }

    /**
     * Per-spread `@param` tag list, extracted from each spread's data class KDoc and rewritten
     * against that spread's own component rename map.
     */
    private fun rewriteSpreadParamTags(spreads: List<ParameterDescriptor.Spread>): List<String> =
        spreads.flatMap { spread ->
            val renames = collectSpreadComponentRenames(spread)
            spread.dataClassKdoc
                ?.let(KDocRewriter::extractParamTags)
                ?.map { KDocRewriter.rewrite(it, renames) }
                .orEmpty()
        }

    /** Original-name -> exported-name map covering the function's plain parameters only. */
    private fun collectPlainRenames(function: FunctionDescriptor): Map<String, String> =
        function.parameters
            .filterIsInstance<ParameterDescriptor.Plain>()
            .associate { it.originalName to it.exportedName }

    /** Original-name -> exported-name map covering a single spread's own components only. */
    private fun collectSpreadComponentRenames(spread: ParameterDescriptor.Spread): Map<String, String> =
        spread.components.associate { it.originalName to it.exportedName }
}
