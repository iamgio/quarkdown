package com.quarkdown.pm.model

import io.github.z4kn4fein.semver.toVersionOrNull

/**
 * Picks the newest of [versions] by semantic-version precedence.
 * @param versions version names, e.g. the version directories of a package
 * @return the newest version as it was given, or null when none is a semantic version
 */
fun latestVersion(versions: Iterable<String>): String? =
    versions
        .mapNotNull { name -> name.toVersionOrNull()?.let { parsed -> parsed to name } }
        .maxByOrNull { (parsed, _) -> parsed }
        ?.second
