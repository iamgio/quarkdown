package com.quarkdown.processor.util

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation

/**
 * FQN of `@com.quarkdown.core.function.reflect.annotation.Name`.
 * Referenced as a string to avoid pulling `quarkdown-core` into the processor's classpath.
 */
private const val QUARKDOWN_NAME_ANNOTATION_FQN = "com.quarkdown.core.function.reflect.annotation.Name"

/**
 * Returns the first [KSAnnotation] on this declaration whose declaration FQN matches [fqn],
 * or `null` when no such annotation is present.
 *
 * This is the primitive on top of which every other annotation query in this module is built,
 * so that resolution logic (annotation type resolve + declaration FQN check) lives in one place.
 */
fun KSAnnotated.getAnnotation(fqn: String): KSAnnotation? =
    annotations.firstOrNull {
        it.annotationType
            .resolve()
            .declaration.qualifiedName
            ?.asString() == fqn
    }

/** True when this declaration carries the annotation whose FQN is [fqn]. */
fun KSAnnotated.hasAnnotation(fqn: String): Boolean = getAnnotation(fqn) != null

/** True when this declaration carries an annotation of type [A]. */
inline fun <reified A : Annotation> KSAnnotated.hasAnnotation(): Boolean {
    val fqn = A::class.qualifiedName ?: return false
    return hasAnnotation(fqn)
}

/**
 * Returns the string value of Quarkdown's `@Name(...)` annotation on this declaration, or `null`
 * when the annotation is absent. Accepts both positional (`@Name("foo")`) and named
 * (`@Name(name = "foo")`) argument forms.
 */
fun KSAnnotated.quarkdownName(): String? =
    getAnnotation(QUARKDOWN_NAME_ANNOTATION_FQN)
        ?.arguments
        ?.firstOrNull { it.name?.asString() == "name" || it.name == null }
        ?.value as? String
