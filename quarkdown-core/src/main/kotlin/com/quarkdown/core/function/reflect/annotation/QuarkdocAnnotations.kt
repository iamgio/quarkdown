package com.quarkdown.core.function.reflect.annotation

/**
 * When a library function parameter is annotated with `@LikelyBody`,
 * it is marked as a body parameter that usually expects a body argument ([wiki](https://github.com/iamgio/quarkdown/wiki/syntax-of-a-function-call#block-vs-inline-function-calls)).
 * This does not have any runtime effect, but is rather used for documentation purposes (see Quarkdoc).
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class LikelyBody

/**
 * When a library function parameter is annotated with `@LikelyNamed`,
 * it is marked as a parameter that usually expects a named argument ([wiki](https://github.com/iamgio/quarkdown/wiki/syntax-of-a-function-call)).
 * This does not have any runtime effect, but is rather used for documentation purposes (see Quarkdoc).
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class LikelyNamed
