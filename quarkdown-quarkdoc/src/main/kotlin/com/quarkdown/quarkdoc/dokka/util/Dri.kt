package com.quarkdown.quarkdoc.dokka.util

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.links.sureClassNames

fun DRI.isOfType(
    packageName: String,
    className: String,
): Boolean = this.packageName == packageName && this.classNames == className

/**
 * @return whether the [DRI] points to the type [T]
 */
inline fun <reified T> DRI.isOfType(): Boolean {
    val packageName = T::class.java.`package`.name
    val className = T::class.simpleName
    return isOfType(packageName, className ?: "")
}

/**
 * @return the fully qualified name of the class represented, ready to be passed to [Class.forName]
 * @throws IllegalStateException if the DRI does not represent a class
 */
val DRI.fullyQualifiedReflectionName: String
    get() = "$packageName.${sureClassNames.replace('.', '$')}"
