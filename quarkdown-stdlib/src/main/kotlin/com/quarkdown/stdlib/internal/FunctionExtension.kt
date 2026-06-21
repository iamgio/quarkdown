package com.quarkdown.stdlib.internal

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.function.Function
import com.quarkdown.core.function.SimpleFunction
import com.quarkdown.core.function.call.executeAs
import com.quarkdown.core.function.signatureAsString
import com.quarkdown.core.function.value.data.Lambda
import com.quarkdown.core.function.value.data.LambdaParameter
import com.quarkdown.stdlib.extend

/**
 * Name under which the original function is exposed inside an [extend] wrapper body.
 */
private const val SUPER_NAME = "super"

/**
 * Implementation of [extend]: registers a wrapper around [targetName] in [context],
 * exposing the original function as `.${SUPER_NAME}` within [body].
 *
 * @param context context to register the wrapper in
 * @param targetName name of the existing function to extend
 * @param body wrapper content; its explicit parameters, if any, must match the target's parameter names
 * @throws IllegalArgumentException if no function named [targetName] exists,
 *         or if any explicit body parameter does not match an original parameter
 */
internal fun extendFunction(
    context: MutableContext,
    targetName: String,
    body: Lambda,
) {
    val targetFunction: Function<*> =
        context.getFunctionByName(targetName)
            ?: throw IllegalArgumentException("Cannot extend function $targetName because it does not exist.")

    // The wrapper mirrors the target's non-injected parameters, all marked optional.
    val wrapperParameters =
        targetFunction.parameters
            .filterNot { it.isInjected }
            .map { it.copy(isOptional = true) }

    val lambdaParameters = wrapperParameters.map { LambdaParameter(it.name, isOptional = true) }

    // Every explicit body parameter must match an original parameter by name.
    val targetNames = wrapperParameters.mapTo(mutableSetOf()) { it.name }
    val unresolved = body.explicitParameters.filter { it.name !in targetNames }
    if (unresolved.isNotEmpty()) {
        throw IllegalArgumentException(
            "The following parameters are not part of ${targetFunction.signatureAsString()}: " +
                unresolved.joinToString { it.name },
        )
    }

    val lambda = Lambda(context, lambdaParameters, body.action)

    context.markFunctionAsExtended(targetName)

    declareFunction(context, targetName, wrapperParameters) { call, args, outerBindings ->
        // `super` is exposed as a callable within the body: its explicit arguments override the outer call's bindings.
        val superFunction =
            SimpleFunction(SUPER_NAME, parameters = wrapperParameters) { overrides, _ ->
                // Each merged argument is re-emitted as named so the relative order of named overrides
                // and positional fall-throughs cannot violate the "named arguments come last" rule.
                val mergedArgs =
                    (outerBindings.entries + overrides.entries)
                        .associate { (param, arg) -> param.name to arg.copy(name = param.name) }
                        .values
                        .toList()
                call.executeAs(targetFunction, arguments = mergedArgs)
            }

        lambda.invokeDynamic(args, callingContext = call.context, additionalFunctions = setOf(superFunction))
    }
}
