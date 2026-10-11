package com.quarkdown.pm.model

/**
 * A reference to an exact version of a package, as written in Quarkdown source: `@<scope>/<name>:<version>`.
 * @param identifier the referenced package
 * @param version exact version of the package
 * @throws IllegalArgumentException if [version] is not a single path segment
 */
data class PackageReference(
    val identifier: PackageIdentifier,
    val version: String,
) {
    init {
        PackagePathSegment.validate(version, role = "version")
    }

    override fun toString(): String = "$PREFIX${identifier.scope}/${identifier.name}:$version"

    companion object {
        /**
         * Character that opens a package reference and distinguishes it from a file path.
         */
        const val PREFIX = '@'

        private val syntax = Regex("""^$PREFIX([^/:]*)/([^/:]*):(.*)$""")

        /**
         * @param string text to inspect
         * @return whether [string] is meant as a package reference, regardless of its validity
         */
        fun isReference(string: String): Boolean = string.startsWith(PREFIX)

        /**
         * Parses a reference in the `@<scope>/<name>:<version>` form.
         * @param reference text to parse
         * @return the parsed reference
         * @throws IllegalArgumentException if [reference] does not match the syntax,
         *                                  or if any of its parts is not a valid path segment
         */
        fun parse(reference: String): PackageReference {
            val match =
                requireNotNull(syntax.matchEntire(reference)) {
                    "Malformed package reference '$reference': expected ${PREFIX}scope/name:version."
                }
            val (scope, name, version) = match.destructured
            return PackageReference(PackageIdentifier(scope, name), version)
        }
    }
}
