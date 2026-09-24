package com.quarkdown.processor.annotation

/**
 * Marks a top-level Kotlin function as an exportable Quarkdown function.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class QFunction
