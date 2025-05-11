package com.quarkdown.quarkdoc.dokka.util

import org.jetbrains.dokka.links.DRI

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
