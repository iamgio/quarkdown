package com.quarkdown.core.function.reflect.annotation

import com.quarkdown.core.function.reflect.KFunctionAdapter
import com.quarkdown.core.function.value.Value

/**
 * When invoking a function via [KFunctionAdapter], [Value] arguments are automatically unwrapped to their raw value,
 * unless this annotation is present on the [Value] subclass.
 */
@Target(AnnotationTarget.CLASS)
annotation class NoAutoArgumentUnwrapping
