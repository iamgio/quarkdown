package com.quarkdown.stdlib.internal

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.function.Function
import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.SimpleFunction
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.call.FunctionCallArgument
import com.quarkdown.core.function.call.binding.ArgumentBindings
import com.quarkdown.core.function.call.executeAs
import com.quarkdown.core.function.call.validate.FunctionCallValidator
import com.quarkdown.core.function.signatureAsString
import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.function.value.data.Lambda
import com.quarkdown.core.function.value.data.LambdaParameter
import com.quarkdown.stdlib.extend

/**
 * Name under which the target of an [extend] wrapper is exposed inside its body.
 */
private const val SUPER_NAME = "super"

/**
 * Function produced by an [extend] call. It exposes its body as the callable target and
 * a rewireable [superTarget] as the function invoked by `.${SUPER_NAME}` within the body.
 *
 * @param name name of the function being extended
 * @param parameters parameters of the function being extended, all marked optional
 * @param body extension body invoked when the target is called
 * @param condition optional predicate; when it returns `false`, the call transparently
 *                  delegates to [superTarget] instead of running [body]
 * @param initialSuperTarget target of `.${SUPER_NAME}` at construction time (usually the original function)
 */
private class ExtensionFunction(
    override val name: String,
    override val parameters: List<FunctionParameter<*>>,
    private val body: Lambda,
    private val condition: Lambda?,
    initialSuperTarget: Function<*>,
) : Function<OutputValue<*>> {
    override val validators: List<FunctionCallValidator<OutputValue<*>>> = emptyList()

    /**
     * Function that `.${SUPER_NAME}` inside [body] delegates to. Mutated when a new extension
     * is chained after this one, so `.super` transparently walks down toward the original.
     */
    var superTarget: Function<*> = initialSuperTarget

    override val invoke: (ArgumentBindings, FunctionCall<OutputValue<*>>) -> OutputValue<*> = { outerBindings, call ->
        val args: List<Value<*>> = parameters.map { outerBindings[it]?.value ?: NoneValue }

        val superFunction =
            SimpleFunction(SUPER_NAME, parameters = parameters) { overrides, _ ->
                call.executeAs(superTarget, arguments = mergeNamedArguments(outerBindings, overrides))
            }

        when {
            // Condition not met: transparently fall through to the current `.super`.
            condition != null && !condition.invoke<Boolean, BooleanValue>(args).unwrappedValue -> {
                call.executeAs(superFunction, arguments = emptyList())
            }

            else -> {
                body.invokeDynamic(
                    args,
                    callingContext = call.context,
                    additionalFunctions = setOf(superFunction),
                )
            }
        }
    }
}

/**
 * Follows any [ExtensionFunction] chain rooted at this function until it reaches a
 * non-extension function.
 *
 * @return the innermost [ExtensionFunction] (whose `superTarget` is the original), paired
 *         with that original function; or `null to this` if this function is not extended.
 */
private fun Function<*>.followExtensionChain(): Pair<ExtensionFunction?, Function<*>> {
    val innermost =
        generateSequence(this as? ExtensionFunction) { it.superTarget as? ExtensionFunction }
            .lastOrNull()
            ?: return null to this
    return innermost to innermost.superTarget
}

/**
 * Merges [outerBindings] with [overrides] into a list of named arguments, with [overrides]
 * winning for parameters present in both. Each argument is re-emitted as named to keep the
 * "named arguments come last" rule intact regardless of the relative position of overrides
 * and positional fall-throughs.
 */
private fun mergeNamedArguments(
    outerBindings: ArgumentBindings,
    overrides: ArgumentBindings,
): List<FunctionCallArgument> {
    val merged = mutableMapOf<String, FunctionCallArgument>()
    for ((param, arg) in outerBindings) {
        merged[param.name] = arg.copy(name = param.name)
    }
    for ((param, arg) in overrides) {
        merged[param.name] = arg.copy(name = param.name)
    }
    return merged.values.toList()
}

/**
 * Implementation of [extend]: adds a wrapper around [targetName] in [context],
 * exposing the target function as `.${SUPER_NAME}` within [body].
 *
 * Multiple extensions on the same name compose in declaration order:
 * the first-declared extension is the outermost caller, and each subsequent
 * `.extend` inserts itself just above the original. This means the last-declared
 * extension's `.super X` overrides are what the original ultimately sees.
 *
 * @param context context to register the wrapper in
 * @param targetName name of the existing function to extend
 * @param condition optional condition to meet. Takes the same parameters as [body].
 *                  If the condition is not met, the call falls through to `.super`,
 *                  which resolves to the next extension or the original function.
 * @param body wrapper content; its explicit parameters, if any, must match the target's parameter names
 * @throws IllegalArgumentException if no function named [targetName] exists,
 *         or if any explicit body parameter does not match an original parameter
 */
internal fun extendFunction(
    context: MutableContext,
    targetName: String,
    condition: Lambda?,
    body: Lambda,
) {
    val existing =
        context.getFunctionByName(targetName)
            ?: throw IllegalArgumentException("Cannot extend function $targetName because it does not exist.")

    val (innermost, originalFunction) = existing.followExtensionChain()

    // The wrapper mirrors the original's non-injected parameters, all marked optional.
    val wrapperParameters =
        originalFunction.parameters
            .filterNot { it.isInjected }
            .map { it.copy(isOptional = true) }
    val lambdaParameters = wrapperParameters.map { LambdaParameter(it.name, isOptional = true) }

    // Every explicit body parameter must match an original parameter by name.
    val targetNames = wrapperParameters.mapTo(mutableSetOf()) { it.name }
    val unresolved = body.explicitParameters.filter { it.name !in targetNames }
    if (unresolved.isNotEmpty()) {
        throw IllegalArgumentException(
            "The following parameters are not part of ${originalFunction.signatureAsString()}: " +
                unresolved.joinToString { it.name },
        )
    }

    val bodyLambda = Lambda(context, lambdaParameters, body.action)
    val conditionLambda = condition?.let { Lambda(context, lambdaParameters, it.action) }

    val newExtension =
        ExtensionFunction(
            name = targetName,
            parameters = wrapperParameters,
            body = bodyLambda,
            condition = conditionLambda,
            initialSuperTarget = originalFunction,
        )

    context.markFunctionAsExtended(targetName)

    if (innermost == null) {
        // First extension for this name: register the wrapper as the callable target.
        declareFunction(context, newExtension)
    } else {
        // Splice the new extension between the previous innermost wrapper and the original,
        // so its `.super` overrides land on the original last.
        innermost.superTarget = newExtension
    }
}
