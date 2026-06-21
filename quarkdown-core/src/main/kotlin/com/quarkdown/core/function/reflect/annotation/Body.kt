package com.quarkdown.core.function.reflect.annotation

/**
 * When a library function parameter is annotated with `@Body`,
 * it is marked as the body parameter of the function: a body argument from a function call
 * always binds to it, regardless of its position in the signature, and the parameter is
 * excluded from positional and named bindings.
 *
 * @see com.quarkdown.core.function.call.binding.RegularArgumentsBinder
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Body
