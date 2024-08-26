package eu.iamgio.quarkdown.function.reflect.annotation

/**
 * If a library member is annotated with this, a custom name is set instead of its original name.
 * For example:
 * - `fun someFunction() = ...`
 *   can be invoked in Quarkdown via .someFunction.
 * - `@Name("somefunction") fun someFunction() = ...`
 *   can be invoked in Quarkdown via .somefunction.
 * - `fun func(@Name("someparam") someParam: String) = ...`
 *    can be invoked in Quarkdown via `.func someparam:{...}`
 *
 * @param name custom function name
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class Name(val name: String)
