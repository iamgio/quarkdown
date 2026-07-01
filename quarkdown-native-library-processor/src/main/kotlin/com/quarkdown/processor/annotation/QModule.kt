package com.quarkdown.processor.annotation

/**
 * Marks a Kotlin source file as a Quarkdown native library module.
 *
 * Each `@file:QModule` source becomes a `QuarkdownModule` exported to Quarkdown scripts,
 * grouping the [QFunction]-annotated declarations defined in the same file.
 */
@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
annotation class QModule
