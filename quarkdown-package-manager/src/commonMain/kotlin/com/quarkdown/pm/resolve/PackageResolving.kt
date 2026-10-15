package com.quarkdown.pm.resolve

import com.quarkdown.core.packages.PackageResolutionException
import com.quarkdown.pm.model.PackageReference

/**
 * Parses [reference] and runs [action] on it, translating any failure raised along the way
 * (malformed reference, network, manifest, writer) into a [PackageResolutionException]
 * that names the reference and keeps the original cause.
 * @param reference reference as written by the user
 * @param action work to run on the parsed reference
 * @return the result of [action]
 * @throws PackageResolutionException if parsing or [action] fails
 */
internal inline fun <T> resolvingPackage(
    reference: String,
    action: (PackageReference) -> T,
): T =
    try {
        action(PackageReference.parse(reference))
    } catch (e: PackageResolutionException) {
        throw e
    } catch (e: Exception) {
        throw PackageResolutionException(reference, e.message ?: e::class.simpleName.orEmpty(), e)
    }
