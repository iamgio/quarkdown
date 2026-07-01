package com.quarkdown.processor.util

import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * FQN of `@com.quarkdown.core.function.reflect.annotation.Name`.
 * Referenced as a string to avoid pulling `quarkdown-core` into the processor's classpath.
 */
private const val QUARKDOWN_NAME_ANNOTATION_FQN = "com.quarkdown.core.function.reflect.annotation.Name"

/** Returns true if this [KSAnnotated] has an annotation of type [A]. */
inline fun <reified A : Annotation> KSAnnotated.hasAnnotation(): Boolean {
    val target = A::class.qualifiedName ?: return false
    return annotations.any {
        it.annotationType
            .resolve()
            .declaration.qualifiedName
            ?.asString() == target
    }
}

/**
 * Returns the string value of Quarkdown's `@Name(...)` annotation on this declaration, or `null`
 * when the annotation is absent. Accepts both positional (`@Name("foo")`) and named
 * (`@Name(name = "foo")`) argument forms.
 */
fun KSAnnotated.quarkdownName(): String? =
    annotations
        .firstOrNull { annotation ->
            annotation.annotationType
                .resolve()
                .declaration.qualifiedName
                ?.asString() == QUARKDOWN_NAME_ANNOTATION_FQN
        }?.arguments
        ?.firstOrNull { it.name?.asString() == "name" || it.name == null }
        ?.value as? String
