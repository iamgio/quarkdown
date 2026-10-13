package com.quarkdown.pm.repo

import com.quarkdown.pm.model.PackageRepository

/**
 * Scope of the official package repository, as used in references such as `@atoms/simple-cv:1.0.0`.
 */
const val ATOMS_SCOPE = "atoms"

/**
 * Registry of [PackageRepository]s by reference scope.
 * @param byScope repositories keyed by scope
 */
class PackageRepositories(
    private val byScope: Map<String, PackageRepository>,
) {
    /**
     * @param scope scope part of a reference
     * @return the repository serving [scope], or `null` if none is registered
     */
    fun forScope(scope: String): PackageRepository? = byScope[scope]

    companion object {
        /**
         * @return the registry with the official repositories: `atoms`
         */
        fun default(): PackageRepositories = PackageRepositories(mapOf(ATOMS_SCOPE to AtomsPackageRepository()))
    }
}
