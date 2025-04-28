package eu.iamgio.quarkdown.function.reflect.annotation

/**
 * When a library function parameter is annotated with `@Injected`, its value is not supplied by a function call
 * but rather automatically injected by [eu.iamgio.quarkdown.function.call.binding.InjectedArgumentsBinder].
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Injected
