package com.quarkdown.processor.util

import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * Returns true if this [KSAnnotated] has an annotation of type [A].
 */
inline fun <reified A : Annotation> KSAnnotated.hasAnnotation(): Boolean {
    val target = A::class.qualifiedName ?: return false
    return annotations.any {
        it.annotationType
            .resolve()
            .declaration.qualifiedName
            ?.asString() == target
    }
}
