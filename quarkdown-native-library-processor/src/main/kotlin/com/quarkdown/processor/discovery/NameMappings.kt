package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter

/**
 * Round-scoped registry of `@Name` exports for functions and value parameters.
 *
 * Populated during discovery: every `@QFunction` and every one of its value parameters gets
 * recorded here with the name it is exported under (the value of `@Name`, or the source name
 * when no `@Name` is attached). The registry is the single source of truth for anything
 * downstream that needs to translate between source and exported names:
 *
 * - [DefaultValueExtractor] consumes [parameterRenames] to rewrite identifier references in
 *   default expressions to the wrapper's parameter names;
 * - future stages (documentation, annotation propagation, cross-reference validation) can query
 *   [exportedName] to look up a declaration's exported identity without re-scanning annotations.
 *
 * Keys are derived from stable string identifiers (qualified names for functions, an
 * enclosing-function-plus-parameter-name compound for parameters). This avoids relying on
 * KSDeclaration instance identity, which KSP2 does not guarantee across resolver traversals.
 */
internal class NameMappings {
    private val functionExports: HashMap<String, String> = HashMap()
    private val parameterExports: HashMap<String, String> = HashMap()

    /**
     * Records that [function] is exported under [exportedName].
     */
    fun record(
        function: KSFunctionDeclaration,
        exportedName: String,
    ) {
        functionExports[keyOf(function)] = exportedName
    }

    /**
     * Records that [parameter] is exported under [exportedName].
     */
    fun record(
        parameter: KSValueParameter,
        exportedName: String,
    ) {
        keyOf(parameter)?.let { parameterExports[it] = exportedName }
    }

    /**
     * Returns the recorded exported name of [function], or `null` when [function] was never recorded.
     */
    fun exportedName(function: KSFunctionDeclaration): String? = functionExports[keyOf(function)]

    /**
     * Returns the recorded exported name of [parameter], or `null` when [parameter] was never recorded.
     */
    fun exportedName(parameter: KSValueParameter): String? = keyOf(parameter)?.let { parameterExports[it] }

    /**
     * Returns an original-name -> exported-name map for every parameter of [function] whose
     * export renamed it. Parameters that are unchanged (or unrecorded) are omitted so callers
     * can use `Map.get` semantics without post-filtering.
     */
    fun parameterRenames(function: KSFunctionDeclaration): Map<String, String> =
        buildMap {
            function.parameters.forEach { param ->
                val original = param.name?.asString() ?: return@forEach
                val exported = keyOf(param)?.let { parameterExports[it] } ?: return@forEach
                if (original != exported) {
                    put(original, exported)
                }
            }
        }

    /**
     * Returns [parameterRenames] for the function that owns [parameter], or an empty map when
     * the parent is not a resolvable [KSFunctionDeclaration]. Convenience for extractors that
     * only hold a parameter reference and would otherwise walk up to the containing function
     * themselves.
     */
    fun parameterRenames(parameter: KSValueParameter): Map<String, String> {
        val function = parameter.parent as? KSFunctionDeclaration ?: return emptyMap()
        return parameterRenames(function)
    }

    private fun keyOf(function: KSFunctionDeclaration): String = function.qualifiedName?.asString() ?: function.simpleName.asString()

    private fun keyOf(parameter: KSValueParameter): String? {
        val function = parameter.parent as? KSFunctionDeclaration ?: return null
        val paramName = parameter.name?.asString() ?: return null
        return keyOf(function) + PARAMETER_KEY_SEPARATOR + paramName
    }

    private companion object {
        private const val PARAMETER_KEY_SEPARATOR = "#"
    }
}
