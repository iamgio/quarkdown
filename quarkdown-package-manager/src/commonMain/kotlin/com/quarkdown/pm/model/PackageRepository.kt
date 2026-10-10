package com.quarkdown.pm.model

import com.quarkdown.core.pipeline.output.OutputResource

/**
 *
 */
interface PackageRepository {
    fun getPackage(
        identifier: PackageIdentifier,
        version: String,
    ): RepositoryPackage?
}

/**
 *
 */
data class RepositoryPackage(
    val manifest: PackageManifest,
    val contents: () -> OutputResource,
)
