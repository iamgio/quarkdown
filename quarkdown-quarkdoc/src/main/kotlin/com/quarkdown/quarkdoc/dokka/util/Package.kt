package com.quarkdown.quarkdoc.dokka.util

import org.jetbrains.dokka.model.DPackage

/**
 * @param others the list of packages to subtract from [this] collection of packages.
 * @return a new list of packages with the functions, properties, and classlikes from [this] collection of packages
 * excluding those that are also in [others].
 */
fun Iterable<DPackage>.difference(others: Iterable<DPackage>): List<DPackage> =
    map { pkg ->
        pkg.copy(
            functions = pkg.functions - others.flatMap { it.functions },
            properties = pkg.properties - others.flatMap { it.properties },
            classlikes = pkg.classlikes - others.flatMap { it.classlikes },
        )
    }
