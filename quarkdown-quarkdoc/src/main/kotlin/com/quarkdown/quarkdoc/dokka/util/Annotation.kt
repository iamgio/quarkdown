package com.quarkdown.quarkdoc.dokka.util

import org.jetbrains.dokka.model.Annotations
import org.jetbrains.dokka.model.Documentable
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

    return annotations.firstOrNull {
        it.dri.packageName == T::class.java.`package`.name &&
            it.dri.classNames == T::class.simpleName
    }
}

/**
 * @returns whether the documentable has an annotation of type [T]
 */
inline fun <reified T> Documentable.hasAnnotation(): Boolean = extractAnnotation<T>() != null
