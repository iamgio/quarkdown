package com.quarkdown.pm.repo

import com.quarkdown.pm.model.PackageIdentifier
import com.quarkdown.pm.model.PackageRepository
import com.quarkdown.pm.model.RepositoryPackage

const val ATOMS_REPO_URL = "https://raw.githubusercontent.com/quarkdown-labs/atoms"

/**
 *
 */
class AtomsPackageRepository : PackageRepository {
    override fun getPackage(
        identifier: PackageIdentifier,
        version: String,
    ): RepositoryPackage? {
        val name = identifier.name
        return null
    }
}
