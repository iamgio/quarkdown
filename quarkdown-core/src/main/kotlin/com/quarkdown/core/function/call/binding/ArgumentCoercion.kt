package com.quarkdown.core.function.call.binding

import com.quarkdown.core.context.Context
import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.error.FunctionCallRuntimeException
import com.quarkdown.core.function.error.InvalidFunctionCallException
import com.quarkdown.core.function.error.MismatchingArgumentTypeException
import com.quarkdown.core.function.value.AdaptableValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.function.value.isNone
import com.quarkdown.core.pipeline.error.PipelineException

/**
 * Upper bound on how many times [coerce] follows an [AdaptableValue] chain before giving up,
 * so that a cyclic adaptation cannot hang the compiler.
 */
const val MAX_ADAPTATION_DEPTH = 8

/**
 * Marks an argument the call did not supply, as distinct from an explicit `.none`.
 *
 * Generated function bodies receive this sentinel for every omitted optional parameter and
 * substitute the parameter's declared default. `null` cannot play this role, because a nullable
 * parameter legitimately accepts `.none` from the call site.
 */
data object Unbound

/**
 * @param parameter parameter to look the bound argument up for
 * @return the [Value] bound to [parameter], or [Unbound] when the call omitted it
 */
fun ArgumentBindings.rawOf(parameter: FunctionParameter): Any = this[parameter]?.value ?: Unbound

/**
 * @return the context attached to this call
 * @throws InvalidFunctionCallException if the call has no attached context
 */
fun FunctionCall<*>.requireContext(): Context =
    context
        ?: throw InvalidFunctionCallException(
            this,
            reason = "the call has no attached context, which this function's arguments require",
            includeArguments = false,
        )

/**
 * Reports that [found] cannot satisfy [parameter].
 * Public because [coerce] is inline and has to reach it from generated call sites.
 * @param call the call being executed
 * @param parameter the parameter that could not accept the value
 * @param found the offending value
 */
fun argumentTypeMismatch(
    call: FunctionCall<*>,
    parameter: FunctionParameter,
    found: Any?,
): Nothing = throw MismatchingArgumentTypeException(call, parameter, found)

/**
 * Converts a function call argument to the static type a parameter expects, keeping it wrapped.
 *
 * This is the single conversion entry point. The choice of [factory] is made at build time by the
 * native library processor, so no type-to-factory lookup happens here.
 *
 * @param raw the bound argument, normally a [Value]
 * @param parameter the parameter being filled, used for nullability and error reporting
 * @param call the call being executed, used for error reporting
 * @param factory the build-time-selected conversion, invoked only when no cheaper path applies
 * @param T the static type to produce
 * @return a value wrapping a [T]
 * @throws MismatchingArgumentTypeException if no path produces a [T]
 */
inline fun <reified T> convertArgument(
    raw: Any,
    parameter: FunctionParameter,
    call: FunctionCall<*>,
    noinline factory: (Any) -> Value<*>?,
): Value<*> {
    require(raw !== Unbound) {
        "Unbound argument reached conversion for parameter '${parameter.name}'. This is a code generation bug."
    }

    val wrapped: Value<*>? = raw as? Value<*>
    if (wrapped != null) {
        // Already the expected type. Covers dynamic parameters, which keep their DynamicValue intact.
        if (wrapped.satisfies<T>()) return wrapped

        // Quarkdown's `.none` is Kotlin's null, but only where null is accepted.
        if (parameter.isNullable && wrapped.isNone()) return NoneValue

        wrapped.adaptedToSatisfy<T>()?.let { return it }
    }

    val source = (if (wrapped != null) wrapped.unwrappedValue else raw) ?: argumentTypeMismatch(call, parameter, raw)
    return convertThroughFactory(source, call, factory) ?: argumentTypeMismatch(call, parameter, raw)
}

/**
 * Whether this value is a [T], either directly or once unwrapped.
 *
 * The two cases are equivalent for the caller: a parameter expecting the wrapper and one expecting
 * the wrapped type are both satisfied by this value as it stands, with no conversion needed.
 */
@PublishedApi
internal inline fun <reified T> Value<*>.satisfies(): Boolean = this is T || this.unwrappedValue is T

/**
 * Follows this value's adaptation chain, e.g. `NodeValue` to `MarkdownContentValue` to
 * `InlineMarkdownContentValue`, looking for a form that satisfies [T].
 *
 * @return the first adapted value that satisfies [T], or `null` if the chain ends or runs longer
 *         than [MAX_ADAPTATION_DEPTH] without producing one
 */
@PublishedApi
internal inline fun <reified T> Value<*>.adaptedToSatisfy(): Value<*>? {
    var current: Any? = this
    var depth = 0
    while (current is AdaptableValue<*> && depth++ < MAX_ADAPTATION_DEPTH) {
        val adapted = current.adapt()
        if (adapted.satisfies<T>()) return adapted
        current = adapted
    }
    return null
}

/**
 * Runs the build-time-selected conversion, attributing its failure to the call being executed.
 *
 * @param source the raw value to convert
 * @param call the call being executed, used for error reporting
 * @param factory the conversion to run
 * @return the converted value, or `null` when the conversion declines to produce one
 * @throws InvalidFunctionCallException if the conversion rejects [source]
 */
@PublishedApi
internal fun convertThroughFactory(
    source: Any,
    call: FunctionCall<*>,
    factory: (Any) -> Value<*>?,
): Value<*>? =
    try {
        factory(source)
    } catch (e: PipelineException) {
        // A conversion that rejected the value explains why better than a generic type mismatch
        // would, so its message is kept and simply attributed to this call.
        throw InvalidFunctionCallException(call, e.message)
    }

/**
 * Same as [convertArgument], unwrapped to the static type itself.
 *
 * Generated function bodies call this. When binding already converted the argument, every step
 * before the factory short-circuits, so the conversion does not run twice.
 *
 * @param T the static type to produce
 * @return [raw] as a [T]
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T> coerce(
    raw: Any,
    parameter: FunctionParameter,
    call: FunctionCall<*>,
    noinline factory: (Any) -> Value<*>?,
): T {
    if (raw is T) return raw
    val converted = convertArgument<T>(raw, parameter, call, factory)
    if (converted is T) return converted
    if (converted is NoneValue && parameter.isNullable) return null as T
    val result = converted.unwrappedValue
    return if (result is T) result else argumentTypeMismatch(call, parameter, raw)
}

/**
 * Runs a generated native function body, attributing any error raised inside it to [call].
 *
 * Errors that already carry pipeline semantics, including those bubbling up from a nested function
 * call, propagate untouched; anything else, such as an `IllegalArgumentException` from a `require`
 * check, is wrapped so the reader sees which call failed.
 *
 * @param call the call being executed
 * @param body the generated invocation
 * @param T the function's output type
 * @return the function's output
 */
inline fun <T> invokeGuarded(
    call: FunctionCall<*>,
    body: () -> T,
): T =
    try {
        body()
    } catch (e: PipelineException) {
        throw e
    } catch (e: Exception) {
        throw FunctionCallRuntimeException(call, e)
    }
