package com.quarkdown.quarkdoc.dokka.util

import org.jetbrains.dokka.model.WithChildren
import org.jetbrains.dokka.model.doc.DocTag
import kotlin.reflect.full.instanceParameter

/**
 * Attempts to copy the current instance of [this] with the new children provided.
 * @param newChildren the new children to set in the copied instance
 * @return a new instance of the same type as [this] with the new children set, or [this] if the copy operation fails
 */
fun WithChildren<*>.tryCopy(newChildren: List<DocTag>): WithChildren<*> {
    // This implementation via reflection is a terrible workaround, yet the most convenient one.
    val copyMethod = this::class.members.find { it.name == "copy" } ?: return this
    val parameters = copyMethod.parameters
    val args =
        parameters
            .associateWith { parameter ->
                when (parameter.name) {
                    "children" -> newChildren
                    "child", "root" -> newChildren.firstOrNull()
                    else -> null
                }
            }.filterValues { it != null }
            .plus(copyMethod.instanceParameter!! to this)

    return copyMethod.callBy(args) as WithChildren<*>
}
