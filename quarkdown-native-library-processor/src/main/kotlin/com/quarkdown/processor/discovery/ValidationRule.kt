package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

/**
 * Structural rule applied to a single `@QFunction` declaration during validation.
 *
 * Modeled as a functional interface so future lint-style checks (e.g. "body parameters must be
 * last", "@Injected only on Context", "no ambiguous positional defaults") plug in as another
 * value in [ModuleValidator]'s rules list rather than as another branch inside `validateFunction`.
 *
 * Each rule reports its own violations directly to `ctx.logger` and returns a short-circuit
 * signal: `true` to keep checking remaining rules for the same function, `false` to stop.
 * The short-circuit protocol lets a rule that already failed a structural precondition
 * suppress noisy downstream diagnostics without coupling the rules to each other.
 */
internal fun interface ValidationRule {
    /**
     * Checks [function] under [ctx]. Returns `true` to let remaining rules run on the same
     * target, `false` to short-circuit.
     */
    fun check(
        function: KSFunctionDeclaration,
        ctx: DiscoveryContext,
    ): Boolean
}

/**
 * Requires every `@QFunction` to be either top-level or a member of an `object`, because the
 * generator delegates to the source function through its fully qualified name and cannot reach
 * instance methods on regular classes, and local/nested functions have no qualified name at all.
 *
 * Short-circuits when it fails: any downstream file-scoped check would produce a duplicate
 * diagnostic for what is really the same structural violation.
 */
internal object TopLevelOrObjectRule : ValidationRule {
    override fun check(
        function: KSFunctionDeclaration,
        ctx: DiscoveryContext,
    ): Boolean {
        val parent = function.parentDeclaration
        val isTopLevelOrObjectMember =
            parent == null || (parent is KSClassDeclaration && parent.classKind == ClassKind.OBJECT)
        if (!isTopLevelOrObjectMember) {
            val parentDescription =
                (parent as? KSClassDeclaration)?.qualifiedName?.asString()
                    ?: parent.simpleName.asString()
            ctx.logger.error(
                "@QFunction must be a top-level function or an object member; " +
                    "'${function.simpleName.asString()}' is declared inside $parentDescription.",
                function,
            )
            return false
        }
        return true
    }
}

/**
 * Requires the containing file to be a registered `@file:QModule`, since the module index is
 * built per file and an orphaned `@QFunction` would have no module to belong to.
 */
internal object InModuleFileRule : ValidationRule {
    override fun check(
        function: KSFunctionDeclaration,
        ctx: DiscoveryContext,
    ): Boolean {
        val file = function.containingFile
        if (file != null && file !in ctx.moduleFiles) {
            ctx.logger.error(
                "@QFunction '${function.simpleName.asString()}' is declared in '${file.fileName}', " +
                    "which is not annotated with @file:QModule.",
                function,
            )
            return false
        }
        return true
    }
}

/**
 * Rejects generic `@QFunction`s. The generated wrapper delegates to the source function through
 * its fully qualified name and would reference bare type variables (`T`, `E`, ...) without a
 * corresponding `<T>` declaration on the wrapper itself, producing invalid Kotlin.
 */
internal object NoTypeParametersRule : ValidationRule {
    override fun check(
        function: KSFunctionDeclaration,
        ctx: DiscoveryContext,
    ): Boolean {
        if (function.typeParameters.isNotEmpty()) {
            ctx.logger.error(
                "@QFunction '${function.simpleName.asString()}' declares type parameters, " +
                    "which the generated wrapper cannot forward.",
                function,
            )
            return false
        }
        return true
    }
}

/**
 * Requires every `@QFunction` to have an explicit return type in its source declaration.
 * KSP models an implicit return as `KSFunctionDeclaration.returnType == null`, and the code
 * generator has no meaningful type to emit for the wrapper's return type in that case.
 */
internal object ExplicitReturnTypeRule : ValidationRule {
    override fun check(
        function: KSFunctionDeclaration,
        ctx: DiscoveryContext,
    ): Boolean {
        if (function.returnType == null) {
            ctx.logger.error(
                "@QFunction '${function.simpleName.asString()}' must declare an explicit return type.",
                function,
            )
            return false
        }
        return true
    }
}

/**
 * Requires every value parameter of a `@QFunction` to carry a source name. Destructured
 * lambda parameters and similar unnamed forms leave `KSValueParameter.name == null`, which the
 * code generator cannot render into a wrapper signature.
 */
internal object NamedParametersRule : ValidationRule {
    override fun check(
        function: KSFunctionDeclaration,
        ctx: DiscoveryContext,
    ): Boolean {
        if (function.parameters.any { it.name == null }) {
            ctx.logger.error(
                "@QFunction '${function.simpleName.asString()}' declares an unnamed parameter, " +
                    "which the generated wrapper cannot forward.",
                function,
            )
            return false
        }
        return true
    }
}
