package com.quarkdown.pm.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Root of a package manifest.
 *
 * Every package, wherever it is stored, carries its manifest as a file named [FILE_NAME] at its root.
 * @param atom metadata of the package, declared in the `[atom]` table
 */
@Serializable
data class PackageManifest(
    val atom: AtomManifest,
) {
    companion object {
        /**
         * Name of the manifest file at the root of every package.
         */
        const val FILE_NAME = "atom.toml"
    }
}

/**
 * Metadata of a package ("atom"), as declared in the `atom` table of its manifest.
 * @param name name of the package, matching its directory in the repository
 * @param version version of the package, matching its version directory in the repository
 * @param entrypoint path, relative to the package root, of the Quarkdown file to include
 * @param description short human-readable description
 * @param authors authors, in the `Name <contact>` form
 * @param minQuarkdownVersion lowest Quarkdown version the package supports, declared as `min-quarkdown`
 * @param categories free-form categories used for discovery
 * @param repository URL of the package's own source repository
 * @param license SPDX license identifier
 */
@Serializable
data class AtomManifest(
    val name: String,
    val version: String,
    val entrypoint: String,
    val description: String? = null,
    val authors: List<String> = emptyList(),
    @SerialName("min-quarkdown") val minQuarkdownVersion: String? = null,
    val categories: List<String> = emptyList(),
    val repository: String? = null,
    val license: String? = null,
)
