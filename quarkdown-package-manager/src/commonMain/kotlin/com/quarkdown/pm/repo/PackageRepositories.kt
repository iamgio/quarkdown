package com.quarkdown.pm.repo

import com.quarkdown.core.packages.PackageResolutionException
import com.quarkdown.pm.model.PackageReference
import com.quarkdown.pm.model.PackageRepository
import com.quarkdown.pm.model.RepositoryPackage
import com.quarkdown.pm.resolve.resolvingPackage

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

    /**
     * Fetches the package a reference points to from the repository registered for its scope.
     * @param reference exact package version to fetch
     * @return the package
     * @throws PackageResolutionException if no repository is registered for the scope, or the package is not found
     * @throws com.quarkdown.pm.net.RemoteFetchException if the repository cannot be reached
     * @throws com.quarkdown.pm.manifest.ManifestParseException if the package's manifest is malformed
     */
    fun fetch(reference: PackageReference): RepositoryPackage {
        val scope = reference.identifier.scope
        val repository =
            forScope(scope)
                ?: throw PackageResolutionException(reference.toString(), "no package repository is registered for scope '$scope'")

        return repository.getPackage(reference)
            ?: throw PackageResolutionException(reference.toString(), "not found in repository '$scope'")
    }

    /**
     * Parses [reference] and fetches the package it points to.
     * @param reference reference as written by the user, in the `@<scope>/<name>:<version>` form
     * @return the package
     * @throws PackageResolutionException if the reference is malformed, or any failure of [fetch] occurs;
     *         the original failure is kept as the cause
     */
    fun fetch(reference: String): RepositoryPackage = resolvingPackage(reference, ::fetch)

    companion object {
        /**
         * @return the registry with the official repositories: `atoms`
         */
        fun default(): PackageRepositories = PackageRepositories(mapOf(ATOMS_SCOPE to AtomsPackageRepository()))
    }
}
