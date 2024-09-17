package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.base.block.BlockText
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.context.ScopeContext
import eu.iamgio.quarkdown.function.FunctionParameter
import eu.iamgio.quarkdown.function.SimpleFunction
import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.function.reflect.annotation.Injected
import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.GeneralCollectionValue
import eu.iamgio.quarkdown.function.value.IterableValue
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.Value
import eu.iamgio.quarkdown.function.value.VoidValue
import eu.iamgio.quarkdown.function.value.data.Lambda
import eu.iamgio.quarkdown.function.value.data.Range
import eu.iamgio.quarkdown.function.value.wrappedAsValue

/**
 * `Flow` stdlib module exporter.
 * This module handles the control flow and other statements.
 */
val Flow: Module =
    setOf(
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
 * The current element can be accessed via the lambda argument.
 * @param iterable collection to iterate
 * @param body content, output of each iteration. Accepts 1 parameter (the current element).
 * @return a collection that contains the output of each iteration
 */
@Name("foreach")
fun forEach(
    iterable: Iterable<Value<*>>,
    body: Lambda,
): IterableValue<OutputValue<*>> {
    val values =
        iterable.map {
            body.invokeDynamic(it)
        }

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
 * Arguments can be accessed as a function call with their names.
 * The return type of the function is dynamic, hence it can be used as an input of various types for other function calls.
 *
 * Example:
 * ```
 * .function {greet}
 *     from to:
 *     **Hello .to** from .from
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
            FunctionParameter(name = parameter, type = DynamicValue::class, index)
        }

    // The custom function itself.
    val function =
        SimpleFunction(name, parameters) { bindings ->
            val args = bindings.values.map { it.value }.toTypedArray()

            // The final result is evaluated and returned as a dynamic, hence it can be used as any type.
            body.invokeDynamic(*args)
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

    // Deletes previous values. If any is found, this is an update rather than a declaration.
    val targetContext =
        // Scan contexts upwards until the root.
        // The last one to contain a matching variable name is the owner of the variable.
        (context as? ScopeContext)?.lastParentOrNull {
            it is MutableContext && it.libraries.remove(name)
        } as? MutableContext ?: context // No match = new variable.

    // A variable is just a constant function.
    return function(
        targetContext,
        name,
        Lambda(context, explicitParameters = emptyList()) { _, _ ->
            value
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
fun node(): NodeValue = BlockText.wrappedAsValue()
