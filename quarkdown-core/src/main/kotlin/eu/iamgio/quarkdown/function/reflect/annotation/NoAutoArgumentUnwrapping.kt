package eu.iamgio.quarkdown.function.reflect.annotation

import eu.iamgio.quarkdown.function.reflect.KFunctionAdapter
import eu.iamgio.quarkdown.function.value.Value

/**
 * When invoking a function via [KFunctionAdapter], [Value] arguments are automatically unwrapped to their raw value,
 * unless this annotation is present on the [Value] subclass.
 */
@Target(AnnotationTarget.CLASS)
annotation class NoAutoArgumentUnwrapping
