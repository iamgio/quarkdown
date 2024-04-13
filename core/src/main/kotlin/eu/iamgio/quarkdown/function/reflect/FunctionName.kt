package eu.iamgio.quarkdown.function.reflect

/**
 * If a library function is annotated with this, a custom name is set instead of the function's original name.
 * For example:
 * - `fun someFunction() = ...`
 *   can be invoked in Quarkdown via .someFunction.
 * - `@FunctionName("somefunction") fun someFunction() = ...`
 *   can be invoked in Quarkdown via .somefunction.
 *
 * @param name custom function name
 */
annotation class FunctionName(val name: String)
