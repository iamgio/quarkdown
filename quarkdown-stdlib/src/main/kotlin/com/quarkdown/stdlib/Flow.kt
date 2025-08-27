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
import com.quarkdown.core.function.reflect.annotation.LikelyBody
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
 * Performs a conditional evaluation of content, including the evaluation of body only if the condition is met.
 * The expression is not evaluated if the condition is false.
 *
 * Conditional statements return the evaluation of the body if the condition is met, nothing otherwise,
 * and can be used for different purposes, both for layout and for logic.
 *
 * ```
 * # Shopping list
 *
 * .var {needapples} {yes}
 *
 * .if {.needapples}
 *     I need apples.
 * ```
 *
 * ```
 * .row
 *     A
 *
 *     .if {yes}
 *         B
 *
 *     C
 * ```
 *
 * ```
 * .function {safedivide}
 *     numerator denominator:
 *     .if {.denominator::equals {0}}
 *         0
 *     .ifnot {.denominator::equals {0}}
 *         .numerator::divide by:{.denominator}
 * ```
 *
 * @param condition whether the content should be evaluated
 * @param body content to evaluate if [condition] is verified. Accepts 0 parameters.
 * @return the evaluation of [body] if [condition] is `true`, nothing otherwise
 * @wiki Conditional statements
 */
@Name("if")
@Suppress("ktlint:standard:function-naming")
fun `if`(
    condition: Boolean,
    @LikelyBody body: Lambda,
): OutputValue<*> =
    when (condition) {
        true -> body.invokeDynamic()
        false -> VoidValue
    }

/**
 * Shorthand for `.if {.condition::not}`.
 *
 * @param condition whether the content should *not* be evaluated
 * @param body content to evaluate if [condition] is *not* verified. Accepts 0 parameters.
 * @return [body] if [condition] is false, nothing otherwise
 * @wiki Conditional statements
 */
@Name("ifnot")
fun ifNot(
    condition: Boolean,
    @LikelyBody body: Lambda,
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
 *     element:
 *     The current element is **.element**
 * ```
 *
 * In implicit form:
 *
 * ```
 * .foreach {.collection}
 *     The current element is **.1**
 * ```
 *
 * In case the iterable is destructurable (e.g. a [dictionary] or [pair]) and the lambda body has more than 1 explicit parameter,
 * the value is destructured into components.
 *
 * ```
 * .var {x}
 *     .dictionary
 *         - a: 1
 *         - b: 2
 *         - c: 3
 *
 * .foreach {.x}
 *     key value:
 *     **.key** has value **.value**
 * ```
 *
 * The output is a collection containing the output of each iteration (mapping), so, if used as a value,
 * this function has a meaning similar to `map` in many languages.
 *
 * ```
 * .var {collection}
 *   - A
 *   - B
 *   - C
 *
 * .var {mappedcollection}
 *     .foreach {.collection}
 *         item:
 *         .item::lowercase
 *
 * .mappedcollection::first
 * ```
 *
 * > Output: `a`
 *
 * Note that, like any lambda, its content can be inlined by means of the `@lambda` annotation.
 * The previous snippet can be simplified as follows:
 *
 * ```
 * .foreach {.collection} {@lambda item: .item::lowercase}::first
 * ```
 *
 * @param iterable collection to iterate
 * @param body the output of each iteration. Accepts 1 parameter (the current element).
 * @return a collection that contains the output of each iteration
 * @wiki Loops
 */
@Name("foreach")
fun forEach(
    iterable: Iterable<Value<*>>,
    @LikelyBody body: Lambda,
): IterableValue<OutputValue<*>> {
    val values = iterable.map { value -> body.invokeDynamic(value) }
    return GeneralCollectionValue(values)
}

/**
 * Repeats content `N` times. This is shorthand for `.foreach {..N}`.
 *
 * The current index (starting from 1) can be accessed via the lambda argument.
 *
 * ```
 * .repeat {5}
 *     index:
 *     Iteration number .index
 * ```
 *
 * In implicit form:
 *
 * ```
 * .repeat {5}
 *     Iteration number .1
 * ```
 *
 * As with [forEach], the output is a mapping from `[1, N]` to another collection of values.
 * See [forEach]'s documentation for further details.
 *
 * @param times number of times to repeat the content
 * @param body the output of each iteration. Accepts 1 parameter (the current repetition index, starting from 1).
 * @return a collection that contains the output of each iteration
 * @wiki Loops
 */
fun repeat(
    times: Int,
    @LikelyBody body: Lambda,
): IterableValue<OutputValue<*>> = forEach(Range(1, times), body)

/**
 * Custom functions (via [function]) and variables (via [variable]) are saved in a [Library]
 * whose name begins by this string.
 */
private const val CUSTOM_FUNCTION_LIBRARY_NAME_PREFIX = "__func__"

/**
 * Defines a custom function that can be called later in the document.
 *
 * The function is saved in the current context, and can be shared via mechanisms such as [include] or subdocuments.
 *
 * ```
 * .function {myfunction}
 *     You have called this function!
 * ```
 *
 * The function can be called normally:
 *
 * ```
 * .myfunction
 * ```
 *
 * The amount of parameters is determined by the amount of **explicit** lambda parameters.
 *
 * ```
 * .function {add}
 *     a b:
 *     This function has two parameters, `a` and `b`.
 * ```
 *
 * Arguments can be accessed as in a function call by name:
 *
 * ```
 * .function {greet}
 *     from to:
 *     **Hello .to** from .from
 * ```
 *
 * When calling the function, argoments can be positional, named, or a mix of both:
 *
 * ```
 * .greet {John} {world}
 * ```
 *
 * ```
 * .greet from:{John} to:{world}
 * ```
 *
 * A parameter might also be optional. In this case, if the corresponding argument is not provided, it will be `none`:
 *
 * ```
 * .function {greet}
 *    from to?:
 *    **Hello .to** from .from
 * ```
 *
 * As with any [none] value, operations defined in the `Optionality` stdlib module help dealing with it,
 * including simulating default values:
 *
 * ```
 * .function {greet}
 *    from to?:
 *    **Hello .to::otherwise {world}** from .from
 * ```
 *
 * @param name name of the function
 * @param body content of the function. Function parameters must be **explicit** lambda parameters
 * @wiki Declaring functions
 */
fun function(
    @Injected context: MutableContext,
    name: String,
    @LikelyBody body: Lambda,
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
 *
 * ```
 * .var {myvar} {0}
 * ```
 *
 * Variables can be referenced just like functions:
 *
 * ```
 * The variable has value .myvar
 * ```
 *
 * Variables can be reassigned in two ways:
 *
 * - By calling the variable as a function with one argument, which is the new value to assign:
 *   ```
 *   .myvar {42}
 *   ```
 *
 * - By calling this [variable] function again:
 *   ```
 *   .var {myvar} {42}
 *   ```
 *
 * @param name name of the variable
 * @param value value to assign
 * @wiki Variables
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
 * Defines a temporary variable that lives only inside the lambda body.
 *
 * ```
 * .let {world}
 *     item:
 *     Hello, .item
 * ```
 *
 * In implicit form:
 *
 * ```
 * .let {world}
 *    Hello, .1
 * ```
 *
 * @param value value to use as a temporary variable
 * @param body content to evaluate with the temporary variable. Accepts 1 parameter ([value] itself)
 * @return the evaluation of [body] with [value] as a parameter
 * @wiki Let
 */
fun let(
    value: DynamicValue,
    @LikelyBody body: Lambda,
): OutputValue<*> = body.invokeDynamic(value)

/**
 * Creates a null invisible node that forces the expression it lies in to be evaluated as Markdown content.
 *
 * This is a workaround that can be used at the beginning of lambda blocks (e.g. in a `.function`, `.if` or `.foreach` call)
 * in case the visible output does not match the expected one.
 *
 * @return an invisible node
 */
fun node(): NodeValue = BlankNode.wrappedAsValue()
