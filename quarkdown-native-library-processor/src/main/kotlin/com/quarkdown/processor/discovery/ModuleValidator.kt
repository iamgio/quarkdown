package com.quarkdown.processor.discovery

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.quarkdown.processor.annotation.QFunction

/**
 * Enforces the structural rules that every `@QFunction` must satisfy before code generation runs.
 *
 * The rules are intentionally narrow so the generator has a uniform shape to emit:
 * - a `@QFunction` must be a top-level function (or a member of an `object`), since the generator
 *   delegates to it through a fully-qualified call and cannot reach instance methods on regular classes;
 * - its enclosing file must be marked `@file:QModule`, since the module index is built per file.
 *
 * Violations are surfaced through [KSPLogger.error], which fails the KSP round and prevents the
 * generator from emitting output for malformed input. Validation is kept separate from
 * [ModuleDescriber] so that it can report *every* offending function in a single round rather
 * than aborting at the first one a describer would encounter.
 */
internal class ModuleValidator(
    private val resolver: Resolver,
    private val logger: KSPLogger,
) {
    /**
     * Runs every validation rule against every `@QFunction` declaration visible in the current round.
     *
     * @param moduleFiles the files already identified as `@file:QModule`; used to detect orphaned
     *   `@QFunction`s that live outside any registered module.
     */
    fun validate(moduleFiles: Set<KSFile>) {
        resolver
            .getSymbolsWithAnnotation(QFunction::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
            .forEach { function -> validateFunction(function, moduleFiles) }
    }

    /**
     * Reports the two failure modes a single `@QFunction` can exhibit:
     * - it is nested inside a non-object class (the generator's FQN delegation cannot reach instance methods);
     * - it lives in a file that is not annotated with `@file:QModule` (no module would own it).
     */
    private fun validateFunction(
        function: KSFunctionDeclaration,
        moduleFiles: Set<KSFile>,
    ) {
        val parent = function.parentDeclaration
        if (parent is KSClassDeclaration && parent.classKind != ClassKind.OBJECT) {
            logger.error(
                "@QFunction must be a top-level function; '${function.simpleName.asString()}' is declared inside ${parent.qualifiedName?.asString()}.",
                function,
            )
            return
        }
        val file = function.containingFile
        if (file != null && file !in moduleFiles) {
            logger.error(
                "@QFunction '${function.simpleName.asString()}' is declared in '${file.fileName}', " +
                    "which is not annotated with @file:QModule.",
                function,
            )
        }
    }
}
