package com.quarkdown.pm.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Root of a package manifest.
 *
 * Every package, wherever it is stored, carries its manifest as a file named [FILE_NAME] at its root.
 * @param atom metadata of the package, declared in the `[atom]` table
 * @param template project-template declaration from the `[template]` table, or `null` if the package is not a template
 */
@Serializable
data class PackageManifest(
    val atom: AtomManifest,
    val template: TemplateManifest? = null,
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

/**
 * Declaration that makes a package usable as a project template, as declared in the `template` table of its manifest.
 * @param doctype document type of projects created from this template, as a Quarkdown document type name
 *                (`plain`, `paged`, `slides`, `docs`), or `null` to let the user choose
 * @param content path, relative to the package root, of the Quarkdown file whose text becomes the initial content
 *                of the new project's main file
 * @param additionalFiles paths, relative to the package root, of files copied as-is into the new project at the same
 *                        relative path, declared as `additional-files`
 * @param enforceTheme whether the template's own theme must be kept, declared as `enforce-theme`:
 *                     when `true`, the project creator never emits a `.theme` call
 */
@Serializable
data class TemplateManifest(
    val doctype: String? = null,
    val content: String,
    @SerialName("additional-files") val additionalFiles: List<String> = emptyList(),
    @SerialName("enforce-theme") val enforceTheme: Boolean = false,
)
