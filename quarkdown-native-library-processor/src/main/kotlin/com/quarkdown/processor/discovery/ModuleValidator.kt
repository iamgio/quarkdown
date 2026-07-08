package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.quarkdown.processor.annotation.QFunction

/**
 * Enforces the structural rules that every `@QFunction` must satisfy before code generation runs.
 *
 * The rules themselves live in [ValidationRule] implementations; this class only orchestrates:
 * it walks every `@QFunction` in the current round and applies each rule until one returns
 * `false` (short-circuit). Keeping the loop free of rule bodies means adding a new rule is a
 * one-line list edit rather than a `if / else if / else` fork inside the validator.
 *
 * Errors reported by rules go through [com.google.devtools.ksp.processing.KSPLogger.error], which fails the KSP round and prevents
 * the generator from emitting output for malformed input.
 */
internal class ModuleValidator(
    private val rules: List<ValidationRule> = DEFAULT_RULES,
) {
    /**
     * Runs every rule against every `@QFunction` declaration visible in the current round.
     */
    fun validate(ctx: DiscoveryContext) {
        ctx.resolver
            .getSymbolsWithAnnotation(QFunction::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
            .forEach { function -> rules.all { it.check(function, ctx) } }
    }

    companion object {
        /**
         * Default rule ordering: the structural rule ([TopLevelOrObjectRule]) runs first because
         * a violation there also invalidates any file-level assumption, so short-circuiting it
         * keeps diagnostics focused on the primary defect.
         */
        val DEFAULT_RULES: List<ValidationRule> =
            listOf(
                TopLevelOrObjectRule,
                InModuleFileRule,
                NoTypeParametersRule,
                ExplicitReturnTypeRule,
                NamedParametersRule,
            )
    }
}
