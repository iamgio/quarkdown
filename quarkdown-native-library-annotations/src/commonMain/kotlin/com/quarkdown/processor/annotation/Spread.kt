package com.quarkdown.processor.annotation

/**
 * Marks a parameter of a [QFunction] as a *spread* of a class type: the processor unpacks the
 * class's primary-constructor parameters into individual wrapper parameters and reconstructs
 * an instance at the delegation call site via named-argument constructor invocation.
 *
 * `@Name` on a component of the spread class is honored the same way it is on top-level
 * `@QFunction` parameters, so exported names are consistent across the wrapper regardless of
 * whether a parameter came from the source function directly or from a spread component.
 *
 * Example:
 *
 * ```
 * data class Style(
 *     @Name("foreground") val foregroundColor: String,
 *     @Name("background") val backgroundColor: String = "white",
 * )
 *
 * @QFunction
 * fun container(text: String, @Spread style: Style) = ...
 * ```
 *
 * generates:
 *
 * ```
 * public fun container(
 *     text: String,
 *     foreground: String,
 *     background: String = "white",
 * ): ... =
 *     container(text = text, style = Style(foregroundColor = foreground, backgroundColor = background))
 * ```
 *
 * Components listed in [exclude] are left out of the wrapper and take their constructor default
 * in the reconstruction. This resolves name clashes between a spread component and a parameter
 * the function declares itself:
 *
 * ```
 * @QFunction
 * fun row(alignment: Alignment, @Spread(exclude = ["alignment"]) style: Style) = ...
 * ```
 *
 * @param exclude Kotlin names of the spread class's primary-constructor parameters to leave out of the wrapper.
 *                Each must have a default value.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Spread(
    val exclude: Array<String> = [],
)
