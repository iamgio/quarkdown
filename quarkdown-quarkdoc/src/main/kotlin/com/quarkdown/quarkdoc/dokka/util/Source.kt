package com.quarkdown.quarkdoc.dokka.util

import org.jetbrains.dokka.model.WithSources

/**
 * The paths to the source files of a documentable object.
 */
val WithSources.sourcePaths: List<String>
    get() = sources.values.map { it.path }
