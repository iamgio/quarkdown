package com.quarkdown.core.function.call.binding

import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.ParameterType
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.call.FunctionCallArgument
import com.quarkdown.core.function.error.InvalidArgumentCountException
import com.quarkdown.core.function.error.ParameterAlreadyBoundException
import com.quarkdown.core.function.error.UnnamedArgumentAfterNamedException
import com.quarkdown.core.function.error.UnresolvedParameterException
import com.quarkdown.core.function.expression.Expression
import com.quarkdown.core.function.expression.RawInlineExpression
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.factory.ValueFactory

/**
 * Builder of bindings for the regular (not injected) argument subset of a function call.
 * @param call function call to bind arguments for
 */
class RegularArgumentsBinder(
    private val call: FunctionCall<*>,
) : ArgumentsBinder {
    // As soon as a named argument is encountered, all following arguments must be named too.
    private var encounteredNamedArgument = false

    /**
     * Binds an argument to its corresponding parameter.
     * @param argument argument to bind
     * @param argumentIndex index of the argument in the call
     * @param bindable parameters available for positional and named binding (excludes any parameter reserved for the body argument)
     * @param byName name-indexed view of [bindable] for O(1) named-argument lookup
     * @param bodyParameter parameter reserved for the body argument, if any
     * @return the parameter bound to the given argument
     * @throws InvalidArgumentCountException if the number of arguments exceeds the number of parameters
     * @throws UnresolvedParameterException if the argument is named and refers to a non-existent parameter
     * @throws UnnamedArgumentAfterNamedException if an unnamed argument appears after a named one
     */
    private fun findParameter(
        argument: FunctionCallArgument,
        argumentIndex: Int,
        bindable: List<FunctionParameter>,
        byName: Map<String, FunctionParameter>,
        bodyParameter: FunctionParameter?,
    ): FunctionParameter =
        when {
            // A body argument binds to its reserved parameter, or falls back to the last one in the signature.
            argument.isBody -> {
                bodyParameter ?: bindable.lastOrNull()
            }

            // A non-body parameter that refers to a parameter by its name.
            argument.isNamed -> {
                encounteredNamedArgument = true
                byName[argument.name]
                    ?: throw UnresolvedParameterException(argument, call)
            }

            // Non-body, unnamed parameters follow the index and cannot appear after a named argument has been encountered.
            !encounteredNamedArgument -> {
                bindable.getOrNull(argumentIndex)
            }

            // Unnamed arguments cannot appear after a named one.
            else -> {
                throw UnnamedArgumentAfterNamedException(call)
            }
        } ?: throw InvalidArgumentCountException(call) // Error if args count > params count.

    /**
     * Prepares an argument for its parameter: the two adjustments that depend on how the argument
     * was written, then conversion to the parameter's static type.
     *
     * @param parameter parameter bound to the argument
     * @param argument argument to prepare
     * @return the argument, converted and adjusted for the parameter
     */
    private fun prepare(
        parameter: FunctionParameter,
        argument: FunctionCallArgument,
    ): FunctionCallArgument {
        val expression = argument.expression

        // A raw inline argument targeting a lambda parameter is parsed as a lambda directly,
        // bypassing the eager expression pipeline. This matches how body arguments are handled.
        if (expression is RawInlineExpression && call.context != null && parameter.type == ParameterType.LambdaBlock) {
            return argument.copy(expression = ValueFactory.lambda(expression.raw, call.context))
        }

        // A dynamic parameter takes the value as-is, wrapped so that its rawness stays explicit.
        if (parameter.type == ParameterType.Dynamic) {
            return argument.copy(expression = DynamicValue(argument.value.unwrappedValue))
        }

        // Native parameters carry the conversion the processor chose for them at build time.
        val converted = parameter.convert?.invoke(argument.value, parameter, call) ?: return argument
        return when (converted) {
            is Expression -> argument.copy(expression = converted)
            else -> argument
        }
    }

    override fun createBindings(parameters: List<FunctionParameter>): ArgumentBindings {
        // A parameter is reserved for the body argument when it is explicitly marked as such  and the call provides a body argument.
        val bodyParameter: FunctionParameter? =
            when {
                call.arguments.any { it.isBody } -> parameters.firstOrNull { it.isExplicitlyBody }
                else -> null
            }

        // Parameters available for binding non-body arguments.
        val bindable = if (bodyParameter == null) parameters else parameters - bodyParameter

        // Name-indexed view of bindable parameters, for O(1) named-argument lookup.
        val byName = bindable.associateBy { it.name }

        // Parameters that have been already bound to arguments.
        val boundParameters = mutableSetOf<FunctionParameter>()

        return call.arguments
            .withIndex()
            .associate { (index, argument) ->
                // Corresponding parameter.
                val parameter = findParameter(argument, index, bindable, byName, bodyParameter)

                // Check if the parameter is already bound.
                when {
                    parameter in boundParameters -> throw ParameterAlreadyBoundException(call, parameter, argument)
                    else -> boundParameters += parameter
                }

                // Arguments are prepared here, then converted by the called function itself.
                val preparedArgument = prepare(parameter, argument)

                // Push binding.
                parameter to preparedArgument
            }
    }
}
