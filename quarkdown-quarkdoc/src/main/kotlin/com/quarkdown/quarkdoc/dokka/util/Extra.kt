package com.quarkdown.quarkdoc.dokka.util

import org.jetbrains.dokka.model.properties.ExtraProperty
import org.jetbrains.dokka.model.properties.WithExtraProperties

/**
 * Appends the given [extra] property to the given object.
 * @param extra the extra properties to add
 * @return a new instance of [this] object with the added extra properties
 */
fun <T : Any, W : WithExtraProperties<T>> W.withAddedExtra(vararg extra: ExtraProperty<T>) =
    this.withNewExtras(
        this.extra.addAll(listOf(*extra)),
    )
