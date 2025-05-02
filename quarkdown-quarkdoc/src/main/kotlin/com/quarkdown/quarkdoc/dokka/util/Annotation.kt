package com.quarkdown.quarkdoc.dokka.util

import org.jetbrains.dokka.model.Annotations
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.properties.WithExtraProperties

inline fun <reified T> Documentable.extractAnnotation(): Annotations.Annotation? {
    val annotations =
        (this as? WithExtraProperties<*>)
            ?.extra
            ?.allOfType<Annotations>()
            ?.flatMap { it.directAnnotations.values.flatten() }
            ?.also { println(it) }
            ?: emptyList()

    return annotations.firstOrNull {
        it.dri.packageName == T::class.qualifiedName?.substringBeforeLast(".") &&
            it.dri.classNames == T::class.simpleName
    }
}
