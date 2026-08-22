package com.quarkdown.processor.coercion

/**
 * Decides, at build time, how each `@QFunction` parameter is filled at call time.
 *
 * This mirrors what the deleted runtime `DynamicValueConverter` used to do reflectively on every
 * call, with two deliberate differences: enum entries are baked into the emitted source instead of
 * being looked up through `values()`, and an unmappable type fails the build instead of the render.
 */
object CoercionPlanner {
    private const val VALUE_FACTORY = "com.quarkdown.core.function.value.factory.ValueFactory"
    private const val PARAMETER_TYPE = "com.quarkdown.core.function.ParameterType"
    private const val INJECTION_KIND = "com.quarkdown.core.function.InjectionKind"

    private const val DYNAMIC_VALUE = "com.quarkdown.core.function.value.DynamicValue"
    private const val LAMBDA = "com.quarkdown.core.function.value.data.Lambda"
    private const val VALUE = "com.quarkdown.core.function.value.Value"

    private const val CONTEXT = "com.quarkdown.core.context.Context"
    private const val MUTABLE_CONTEXT = "com.quarkdown.core.context.MutableContext"
    private const val FUNCTION_CALL = "com.quarkdown.core.function.call.FunctionCall"
    private const val FUNCTION_CALL_NODE = "com.quarkdown.core.ast.quarkdown.FunctionCallNode"

    /**
     * @param type resolved shape of the parameter's type
     * @param isInjected whether the parameter carries `@Injected`
     * @param candidates factory functions available on `ValueFactory`
     * @return how to fill the parameter, or [CoercionPlan.Unsupported] when nothing matches
     */
    fun plan(
        type: TypeShape,
        isInjected: Boolean,
        candidates: List<FactoryCandidate>,
    ): CoercionPlan =
        when {
            isInjected -> planInjection(type)
            type.qualifiedName == DYNAMIC_VALUE ->
                CoercionPlan.ViaFactory("$PARAMETER_TYPE.Dynamic", "$DYNAMIC_VALUE(it)")

            type.qualifiedName == LAMBDA ->
                CoercionPlan.ViaFactory(
                    "$PARAMETER_TYPE.LambdaBlock",
                    "$VALUE_FACTORY.lambda(it, call.requireContext())",
                )

            type.isValue -> planValuePassthrough(type)
            type.isEnum -> planEnum(type)
            else -> planFactory(type, candidates)
        }

    /**
     * Whether the parameter already expects a wrapped value, e.g. `OutputValue<*>`.
     */
    private val TypeShape.isValue: Boolean
        get() = qualifiedName == VALUE || VALUE in supertypeQualifiedNames

    /**
     * A parameter that expects a wrapped value takes any argument unchanged: coercion returns it at
     * its first check, before any conversion is attempted. The emitted factory is only a fallback
     * for a raw, unwrapped argument, and yields `null` there so the failure is reported as a normal
     * type mismatch rather than a cast error.
     */
    private fun planValuePassthrough(type: TypeShape): CoercionPlan = CoercionPlan.ViaFactory(staticType(type), "it as? $VALUE<*>")

    /**
     * Injected parameters are read off the call itself, so they need no conversion, only a local.
     */
    private fun planInjection(type: TypeShape): CoercionPlan {
        val (kind, local) =
            when (type.qualifiedName) {
                CONTEXT, MUTABLE_CONTEXT -> "CONTEXT" to "call.requireContext() as ${type.qualifiedName}"
                FUNCTION_CALL -> "CALL" to "call"
                FUNCTION_CALL_NODE -> "SOURCE_NODE" to "call.sourceNode"
                else ->
                    return CoercionPlan.Unsupported(
                        "type '${type.qualifiedName}' is not injectable; " +
                            "@Injected accepts Context, FunctionCall or FunctionCallNode",
                    )
            }
        return CoercionPlan.ViaInjection("$PARAMETER_TYPE.Injected($INJECTION_KIND.$kind)", local)
    }

    /**
     * Enum entries are emitted as a literal array, replacing the reflective `values()` lookup.
     */
    private fun planEnum(type: TypeShape): CoercionPlan =
        CoercionPlan.ViaFactory(
            staticType(type),
            "$VALUE_FACTORY.enum(it, ${type.qualifiedName}.entries.toTypedArray())",
        )

    /**
     * Picks the `ValueFactory` conversion whose accepted type this parameter satisfies.
     *
     * An exact match always wins. Otherwise exactly one supertype match must remain: the runtime
     * converter used to break ties by declaration order, which neither reflection nor KSP
     * guarantees for a compiled class, so an ambiguity fails the build instead of silently
     * picking one.
     */
    private fun planFactory(
        type: TypeShape,
        candidates: List<FactoryCandidate>,
    ): CoercionPlan {
        val exact = candidates.firstOrNull { it.unwrappedTypeQualifiedName == type.qualifiedName }
        val matches =
            exact?.let(::listOf)
                ?: candidates.filter { it.unwrappedTypeQualifiedName in type.supertypeQualifiedNames }

        val match =
            when (matches.size) {
                1 -> matches.single()
                0 ->
                    return CoercionPlan.Unsupported(
                        "no ValueFactory conversion accepts type '${type.qualifiedName}'",
                    )

                else ->
                    return CoercionPlan.Unsupported(
                        "ambiguous ValueFactory conversions for type '${type.qualifiedName}': " +
                            matches.joinToString { it.functionName },
                    )
            }

        val arguments = if (match.requiresContext) "it, call.requireContext()" else "it"
        return CoercionPlan.ViaFactory(staticType(type), "$VALUE_FACTORY.${match.functionName}($arguments)")
    }

    private fun staticType(type: TypeShape): String = "$PARAMETER_TYPE.Static(\"${type.simpleName}\")"
}
