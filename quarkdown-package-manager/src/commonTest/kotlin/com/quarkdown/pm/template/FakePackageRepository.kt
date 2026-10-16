package com.quarkdown.pm.template

import com.quarkdown.pm.model.AtomManifest
import com.quarkdown.pm.model.PackageFile
import com.quarkdown.pm.model.PackageManifest
import com.quarkdown.pm.model.PackageReference
import com.quarkdown.pm.model.PackageRepository
import com.quarkdown.pm.model.RepositoryPackage
import com.quarkdown.pm.model.TemplateManifest
import com.quarkdown.pm.repo.ATOMS_SCOPE
import com.quarkdown.pm.repo.PackageRepositories

/**
 * In-memory [PackageRepository] serving canned packages, for tests.
 * @param packages packages by reference string, e.g. `@atoms/cv:1.0.0`
 */
class FakePackageRepository(
    private val packages: Map<String, RepositoryPackage>,
) : PackageRepository {
    override fun getPackage(reference: PackageReference): RepositoryPackage? = packages[reference.toString()]

    /**
     * @return a registry mapping the `atoms` scope to this repository
     */
    fun asAtoms(): PackageRepositories = PackageRepositories(mapOf(ATOMS_SCOPE to this))
}

/**
 * Builds a package of the `atoms` scope with text files.
 * @param name package name
 * @param version package version
 * @param template the `[template]` table, or `null` for a plain package
 * @param files file text by package-relative path; the manifest file is always present
 */
fun templatePackage(
    name: String,
    version: String = "1.0.0",
    template: TemplateManifest? = null,
    files: Map<String, String> = emptyMap(),
): RepositoryPackage =
    RepositoryPackage(
        PackageManifest(AtomManifest(name = name, version = version, entrypoint = "main.qd"), template),
        (files + (PackageManifest.FILE_NAME to "[atom]")).map { (path, text) -> PackageFile(path) { text.encodeToByteArray() } },
    )
