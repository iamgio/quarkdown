package com.quarkdown.quarkdoc.dokka.util

import org.jetbrains.dokka.model.Annotations
import org.jetbrains.dokka.model.ArrayValue
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.EnumValue
import org.jetbrains.dokka.model.properties.WithExtraProperties

/**
 * @returns the first annotation of type [T] found in the documentable
 */
inline fun <reified T> Documentable.extractAnnotation(): Annotations.Annotation? {
    val annotations =
        (this as? WithExtraProperties<*>)
            ?.extra
            ?.allOfType<Annotations>()
            ?.flatMap { it.directAnnotations.values.flatten() }
            ?: emptyList()

    return annotations.find { it.dri.isOfType<T>() }
}

/**
 * @returns whether the documentable has an annotation of type [T]
 */
inline fun <reified T> Documentable.hasAnnotation(): Boolean = extractAnnotation<T>() != null

/**
 * Converts the parameter of an annotation to a list of enums of type [E] using the provided [valueOf] function.
 * @param paramName the name of the parameter in the annotation
 * @param valueOf a function that converts an enum name to an enum of type [E]
 * @return a list of enums of type [E] corresponding to the parameter value
 * @see EnumValue.toEnum
 */
fun <E : Enum<*>> Annotations.Annotation.parameterToEnumArray(
    paramName: String,
    valueOf: (String) -> E,
): List<E> =
    (this.params[paramName] as? ArrayValue)
        ?.value
        ?.asSequence()
        ?.filterIsInstance<EnumValue>()
        ?.map { it.toEnum(valueOf) }
        ?.toList()
        ?: emptyList()
