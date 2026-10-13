package com.quarkdown.pm.manifest

import com.quarkdown.pm.model.PackageManifest

/**
 * Strategy that turns the textual content of a manifest file into a [PackageManifest].
 * @see TomlManifestParser for the `atom.toml` format
 */
fun interface ManifestParser {
    /**
     * Parses a manifest.
     * @param source raw content of the manifest file
     * @return the parsed manifest
     * @throws ManifestParseException if the content is malformed or misses required fields
     */
    fun parse(source: String): PackageManifest
}

/**
 * Raised when a manifest cannot be parsed into a [PackageManifest].
 * @param message description of the problem
 * @param cause underlying parser failure, if any
 */
class ManifestParseException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
