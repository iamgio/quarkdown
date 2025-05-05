package com.quarkdown.stdlib

import com.quarkdown.core.ast.base.block.BlankNode
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.ScopeContext
import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.SimpleFunction
import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.library.loader.Module
import com.quarkdown.core.function.library.loader.moduleOf
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.GeneralCollectionValue
import com.quarkdown.core.function.value.IterableValue
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.core.function.value.data.Lambda
import com.quarkdown.core.function.value.data.LambdaParameter
import com.quarkdown.core.function.value.data.Range
import com.quarkdown.core.function.value.factory.ValueFactory
import com.quarkdown.core.function.value.wrappedAsValue

/**
 * `Flow` stdlib module exporter.
 * This module handles the control flow and other statements.
 */
val Flow: Module =
    moduleOf(
        ::`if`,
        ::ifNot,
        ::forEach,
        ::repeat,
        ::function,
        ::variable,
        ::let,
        ::node,
    )

/**
 * @param condition whether the content should be added to the document
 * @param body content to append if [condition] is verified. Accepts 0 parameters.
 * @return the evaluation of [body] if [condition] is `true`, otherwise nothing
 */
@Name("if")
@Suppress("ktlint:standard:function-naming")
fun `if`(
    condition: Boolean,
    body: Lambda,
): OutputValue<*> =
    when (condition) {
        true -> body.invokeDynamic()
        false -> VoidValue
    }

/**
 * @param condition whether the content should be added to the document
 * @param body content to append if [condition] is not verified. Accepts 0 parameters.
 * @return [body] if [condition] is `false`, otherwise nothing
 */
@Name("ifnot")
fun ifNot(
    condition: Boolean,
    body: Lambda,
): OutputValue<*> = `if`(!condition, body)

/**
 * Repeats content for each element of an iterable collection.
 * The current element can be accessed via the lambda argument, which may be either explicit or implicit.
 *
 * ```
 * .var {collection}
 *   - A
 *   - B
 *   - C
 *
 * .foreach {.collection}
 *   element:
 *   The current element is **.element**
 * ```
 *
 * In implicit form:
 *
 * ```
 * .foreach {.collection}
 *   The current element is **.1**
 * ```
 *
 * In case the iterable is destructurable (e.g. a [dictionary]) and the lambda body has more than 1 explicit parameter,
 * the value is destructured into components.
 *
 * ```
 * .var {x}
 *   .dictionary
 *     - a: 1
 *     - b: 2
 *     - c: 3
 *
 * .foreach {.x}
 *     key value:
 *     **.key** has value **.value**
 * ```
 *
 * @param iterable collection to iterate
 * @param body content, output of each iteration. Accepts 1 parameter (the current element).
 * @return a collection that contains the output of each iteration
 */
@Name("foreach")
fun forEach(
    iterable: Iterable<Value<*>>,
    body: Lambda,
): IterableValue<OutputValue<*>> {
    val values = iterable.map { value -> body.invokeDynamic(value) }
    return GeneralCollectionValue(values)
}

/**
 * Repeats content `N` times.
 * The current index (starting from 1) can be accessed via the lambda argument.
 * This is shorthand for `foreach {..N} {body}`.
 * @param times amount of times to repeat the content
 * @param body content, output of each iteration. Accepts 1 parameter (the current element).
 * @return a collection that contains the output of each iteration
 */
fun repeat(
    times: Int,
    body: Lambda,
): IterableValue<OutputValue<*>> = forEach(Range(1, times), body)

/**
 * Custom functions (via [function]) and variables (via [variable]) are saved in a [Library]
 * whose name begins by this string.
 */
private const val CUSTOM_FUNCTION_LIBRARY_NAME_PREFIX = "__func__"

/**
 * Defines a custom function that can be called later in the document.
 * The amount of parameters (thus of expected arguments) is determined by the amount of **explicit** lambda parameters.
 * Arguments can be accessed as a function call via their names.
 * The return type of the function is dynamic, hence it can be used as an input of various types for other function calls.
 *
 * Example:
 * ```
 * .function {greet}
 *     from to:
 *     **Hello .to** from .from
 * ```
 *
 * The function defined in the previous example can be called normally, even with named arguments:
 * ```
 * .greet {John} {world}
 * ```
 * ```
 * .greet from:{John} to:{world}
 * ```
 *
 * A parameter might also be optional. In this case, if the corresponding argument is not provided, it will be `none`:
 * ```
 * .function {greet}
 *    from to?:
 *    **Hello .to** from .from
 * ```
 *
 * @param name name of the function
 * @param body content of the function. Function parameters must be **explicit** lambda parameters
 */
fun function(
    @Injected context: MutableContext,
    name: String,
    body: Lambda,
): VoidValue {
    // Function parameters.
    val parameters =
        body.explicitParameters.mapIndexed { index, parameter ->
            FunctionParameter(parameter.name, type = DynamicValue::class, index, parameter.isOptional)
        }

    // The custom function itself.
    val function =
        SimpleFunction(name, parameters) { bindings ->
            // Retrieving arguments from the function call.
            // `None` is used as a default value if the argument for an optional parameter is not provided.
            val args: List<Value<*>> = parameters.map { bindings[it]?.value ?: NoneValue }

            // The final result is evaluated and returned as a dynamic, hence it can be used as any type.
            body.invokeDynamic(args)
        }

    // The function is registered and ready to be called.
    context.libraries += Library(CUSTOM_FUNCTION_LIBRARY_NAME_PREFIX + name, setOf(function))

    return VoidValue
}

/**
 * Defines a new variable or overwrites an existing one.
 * Variables can be referenced just like functions, via `.variablename`.
 * @param name name of the variable
 * @param value value to assign
 */
@Name("var")
fun variable(
    @Injected context: MutableContext,
    name: String,
    value: DynamicValue,
): VoidValue {
    /**
     * @return whether a variable called [name] was removed from [this] set of libraries.
     */
    fun MutableSet<Library>.remove(name: String) = this.removeIf { it.name == CUSTOM_FUNCTION_LIBRARY_NAME_PREFIX + name }

    // Attempt to find an existing owner context that already contains the variable,
    // in case `context` is nested and the owner is up the hierarchy.
    // If null, either the variable is new or the owner is the root context (because `context` would not be a ScopeContext).
    val potentialOwnerContext: MutableContext? =
        // Scan contexts upwards until the root.
        // The last one to contain a matching variable name is the owner of the variable.
        (context as? ScopeContext)?.lastParentOrNull {
            it is MutableContext && it.libraries.remove(name)
        } as? MutableContext

    // If an owner has been found, that context is the target context.
    // Otherwise, it is `context`. Any reference is also removed in case it already exists.
    val targetContext: MutableContext = potentialOwnerContext ?: context.also { it.libraries.remove(name) }

    // In case the value contains function calls, it is evaluated to a value.
    val evaluated: OutputValue<*> = ValueFactory.eval(value, targetContext)

    // A variable can be seen as two functions:
    // - A parameter-less getter that returns the value
    // - A one-parameter setter that assigns a new value
    // These two functions are merged into a single one that works via an optional argument, acting both as getter and setter.
    return function(
        targetContext,
        name,
        Lambda(context, explicitParameters = listOf(LambdaParameter("value", isOptional = true))) { args, _ ->
            if (args.isEmpty() || args.first() is NoneValue) {
                // Getter
                evaluated
            } else {
                // Setter
                val newValue =
                    args.first().let { it as? DynamicValue ?: DynamicValue(it) } // Wrapping the value if needed.
                variable(targetContext, name, newValue)
            }
        },
    )
}

/**
 * Defines a temporary variable that can be used in the lambda body.
 * Example:
 * ```
 * .let {world}
 *     Hello, **.1**!
 * ```
 * @param value value to use as a temporary variable
 * @param body content to evaluate with the temporary variable. Accepts 1 parameter ([value] itself)
 * @return the evaluation of [body] with [value] as a parameter
 */
fun let(
    value: DynamicValue,
    body: Lambda,
): OutputValue<*> = body.invokeDynamic(value)

/**
 * Creates a null invisible node that forces the expression it lies in to be evaluated as Markdown content.
 * This is a workaround that can be used at the beginning of lambda blocks (e.g. in a `.function`, `.if` or `.foreach` call)
 * in case the visible output does not match the expected one.
 * @return an invisible mock node
 */
fun node(): NodeValue = BlankNode.wrappedAsValue()
