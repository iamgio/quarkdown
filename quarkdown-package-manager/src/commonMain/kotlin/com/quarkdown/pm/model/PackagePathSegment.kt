package com.quarkdown.pm.model

/**
 * Validation of the parts of a package reference that end up as path segments,
 * both in repository URLs and in the local `@<scope>/<name>/` directory.
 */
internal object PackagePathSegment {
    private val forbidden = Regex("""[/\\:\s]""")

    /**
     * Ensures [value] can be used as a single path segment: non-empty, free of separators,
     * colons and whitespace, and not a relative directory marker.
     * @param value text to validate
     * @param role name of the validated part, used in the error message (e.g. `name`)
     * @return [value] itself
     * @throws IllegalArgumentException if [value] is not a valid segment
     */
    fun validate(
        value: String,
        role: String,
    ): String {
        require(
            value.isNotEmpty() &&
                value != "." &&
                value != ".." &&
                !forbidden.containsMatchIn(value),
        ) {
            "Invalid package $role '$value': must be a single path segment without separators, colons or whitespace."
        }
        return value
    }
}
