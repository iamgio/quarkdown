package com.quarkdown.cli.creator.content

import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.filesystem.FsEntry
import com.quarkdown.core.function.quarkdownName
import com.quarkdown.core.packages.PackageResolutionException
import com.quarkdown.pm.model.PackageFile
import com.quarkdown.pm.model.RepositoryPackage
import com.quarkdown.pm.model.TemplateManifest
import com.quarkdown.pm.model.download
import com.quarkdown.pm.net.RemoteFetchException
import com.quarkdown.pm.repo.PackageRepositories

/**
 * A package that declares itself a project template, resolved and downloaded into what the project creator needs from it.
 * @param reference the reference as typed by the user, re-emitted as `.include {reference}` in the new project
 * @param doctype document type the template declares, or `null` to let the user choose
 * @param content text that becomes the main file's initial content
 * @param additionalFiles root of an in-memory directory holding the files to copy into the project,
 *                        each at its package-relative path
 * @see PackageTemplateInitialContentSupplier
 */
class PackageTemplate(
    val reference: String,
    val doctype: DocumentType?,
    val content: String,
    val additionalFiles: FsEntry,
) {
    companion object {
        private fun RepositoryPackage.templateManifest(reference: String): TemplateManifest =
            manifest.template
                ?: throw PackageResolutionException(reference, "exists but is not a template")

        private fun RepositoryPackage.templateFile(
            reference: String,
            path: String,
        ): PackageFile =
            file(path)
                ?: throw PackageResolutionException(reference, "declares '$path' as a template file, but the package has no such file")

        private fun documentType(
            reference: String,
            name: String,
        ): DocumentType =
            DocumentType.entries.firstOrNull { it.quarkdownName == name }
                ?: throw PackageResolutionException(reference, "declares unknown document type '$name'")

        private inline fun <T> downloading(
            reference: String,
            action: () -> T,
        ): T =
            try {
                action()
            } catch (e: RemoteFetchException) {
                throw PackageResolutionException(reference, e.message.orEmpty(), e)
            }

        /**
         * Fetches a package, validates its template manifest and downloads the template's files.
         * @param repositories repositories by scope
         * @param reference reference to the template package, as typed by the user
         * @return the resolved template
         * @throws PackageResolutionException if the reference is malformed, the package cannot be fetched,
         *         it doesn't have a template manifest, a declared file is missing from the package,
         *         the declared document type is unknown, or a file cannot be downloaded
         */
        fun resolve(
            repositories: PackageRepositories,
            reference: String,
        ): PackageTemplate {
            val pkg = repositories.fetch(reference)
            val template = pkg.templateManifest(reference)
            val content = pkg.templateFile(reference, template.content)
            val additionalFiles = template.additionalFiles.map { pkg.templateFile(reference, it) }

            return downloading(reference) {
                PackageTemplate(
                    reference,
                    doctype = template.doctype?.let { documentType(reference, it) },
                    content = content.content().decodeToString(),
                    additionalFiles = additionalFiles.download().resolve("/"),
                )
            }
        }
    }
}
