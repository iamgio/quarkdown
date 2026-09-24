package com.quarkdown.processor.annotation

/**
 * Overrides the exported name of a native Quarkdown function or parameter.
 * When absent, the Kotlin identifier is used verbatim.
 *
 * - `fun someFunction() = ...` is invoked as `.someFunction`;
 * - `@Name("somefunction") fun someFunction() = ...` is invoked as `.somefunction`;
 * - `fun func(@Name("someparam") someParam: String) = ...` is invoked as `.func someparam:{...}`.
 *
 * Consumed at KSP-round time by the native-library processor, which bakes the exported name
 * directly into the generated wrapper's signature. `SOURCE` retention keeps the annotation
 * out of the runtime class metadata.
 *
 * @param name custom exported name
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Name(
    val name: String,
)
