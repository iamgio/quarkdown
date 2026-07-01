package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

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
 * Uses identity semantics (KSP declarations compare by identity in a round), which is the same
 * contract the underlying `HashMap` provides.
 */
internal class NameMappings {
    private val exports: HashMap<KSAnnotated, String> = HashMap()

    /**
     * Records that [declaration] is exported under [exportedName]. Later calls overwrite earlier
     * ones for the same declaration; in practice this is unused because every declaration is
     * recorded once during its `describe(...)` visit.
     */
    fun record(
        declaration: KSAnnotated,
        exportedName: String,
    ) {
        exports[declaration] = exportedName
    }

    /**
     * Returns the recorded exported name of [declaration], or `null` when [declaration] was
     * never recorded.
     */
    fun exportedName(declaration: KSAnnotated): String? = exports[declaration]

    /**
     * Returns an original-name -> exported-name map for every parameter of [function] whose
     * export renamed it. Parameters that are unchanged (or unrecorded) are omitted so callers
     * can use `Map.get` semantics without post-filtering.
     */
    fun parameterRenames(function: KSFunctionDeclaration): Map<String, String> =
        buildMap {
            function.parameters.forEach { param ->
                val original = param.name?.asString() ?: return@forEach
                val exported = exports[param] ?: return@forEach
                if (original != exported) {
                    put(original, exported)
                }
            }
        }
}
