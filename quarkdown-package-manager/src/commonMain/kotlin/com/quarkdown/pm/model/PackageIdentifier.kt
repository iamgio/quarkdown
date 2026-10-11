package com.quarkdown.pm.model

/**
 * Identifies a package independently of its version.
 * @param scope alias of the repository the package is published to, e.g. `atoms` for the official repository
 * @param name name of the package within its repository, e.g. `cv`
 * @throws IllegalArgumentException if [scope] or [name] is not a single path segment
 */
data class PackageIdentifier(
    val scope: String,
    val name: String,
) {
    init {
        PackagePathSegment.validate(scope, role = "scope")
        PackagePathSegment.validate(name, role = "name")
    }
}
