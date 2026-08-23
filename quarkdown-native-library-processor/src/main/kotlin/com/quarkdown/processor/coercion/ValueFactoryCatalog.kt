package com.quarkdown.processor.coercion

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType

/**
 * Reads the `@FromDynamicType` dispatch table off `ValueFactory` on the compile classpath.
 *
 * `ValueFactory` stays the single source of truth for which raw types convert to which static
 * types; the processor never hardcodes the table.
 */
object ValueFactoryCatalog {
    private const val VALUE_FACTORY_FQN = "com.quarkdown.core.function.value.factory.ValueFactory"
    private const val ANNOTATION_FQN = "com.quarkdown.core.function.reflect.FromDynamicType"

    /**
     * @param resolver KSP resolver for the current round
     * @param logger sink for reporting annotations that cannot be honored
     * @return one [FactoryCandidate] per usable `@FromDynamicType` annotation found on `ValueFactory`
     * @throws IllegalStateException if `ValueFactory` is not on the classpath, which would
     *         otherwise silently produce wrappers with no conversions at all
     */
    fun read(
        resolver: Resolver,
        logger: KSPLogger,
    ): List<FactoryCandidate> {
        val declaration =
            resolver.getClassDeclarationByName(resolver.getKSNameFromString(VALUE_FACTORY_FQN))
                ?: error(
                    "Cannot resolve $VALUE_FACTORY_FQN. " +
                        "The module running the Quarkdown native library processor must depend on quarkdown-core.",
                )

        return declaration
            .getDeclaredFunctions()
            .flatMap { function ->
                function.annotations
                    .filter {
                        it.annotationType
                            .resolve()
                            .declaration.qualifiedName
                            ?.asString() == ANNOTATION_FQN
                    }.mapNotNull { annotation ->
                        val unwrapped =
                            annotation.arguments
                                .firstOrNull { it.name?.asString() == "unwrappedType" || it.name == null }
                                ?.value as? KSType ?: return@mapNotNull null

                        val requiresContext =
                            annotation.arguments
                                .firstOrNull { it.name?.asString() == "requiresContext" }
                                ?.value as? Boolean ?: false

                        if (!function.hasCallableShape(requiresContext)) {
                            logger.warn(
                                "@FromDynamicType on '${function.simpleName.asString()}' is ignored: a candidate must " +
                                    "take the raw value" + (if (requiresContext) " and the context" else "") +
                                    " and nothing else.",
                                function,
                            )
                            return@mapNotNull null
                        }

                        FactoryCandidate(
                            functionName = function.simpleName.asString(),
                            unwrappedTypeQualifiedName =
                                unwrapped.declaration.qualifiedName?.asString()
                                    ?: return@mapNotNull null,
                            requiresContext = requiresContext,
                        )
                    }
            }.toList()
    }

    /**
     * Whether this function can be called the way the generator emits candidates: with the raw
     * value alone, or with the raw value and the call's context.
     *
     * A candidate of any other shape would produce generated code that does not compile, so it is
     * rejected here rather than at the far end of the build.
     */
    private fun KSFunctionDeclaration.hasCallableShape(requiresContext: Boolean): Boolean =
        this.parameters.size == if (requiresContext) 2 else 1
}
