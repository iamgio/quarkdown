package com.quarkdown.quarkdoc.dokka.index

import com.quarkdown.quarkdoc.reader.DocsWalker
import java.io.File

/**
 * A package/directory segment with this name marks a Quarkdown module:
 * module functions live in `<package>.module.<Name>` synthetic packages.
 */
const val MODULE_PACKAGE_SEGMENT = "module"

/**
 * Recursive walker of Dokka HTML files.
 */
class DokkaHtmlWalker(
    private val root: File,
) : DocsWalker<DokkaHtmlContentExtractor> {
    // e.g. com.quarkdown.stdlib.module.String/lowercase.html => String
    private val File.quarkdownModuleName: String?
        get() =
            parentFile.name
                .split('.')
                .takeIf { it.getOrNull(it.size - 2) == MODULE_PACKAGE_SEGMENT }
                ?.lastOrNull()

    /**
     * Recursively scans Dokka HTML files in the given root directory.
     */
    override fun walk(): Sequence<DocsWalker.Result<DokkaHtmlContentExtractor>> =
        root
            .walkTopDown()
            .filter { it.isFile }
            .filter { it.extension == "html" }
            .filterNot { it.name == "index.html" }
            .map { file ->
                DocsWalker.Result(
                    name = file.nameWithoutExtension,
                    moduleName = file.quarkdownModuleName,
                    extractor = { DokkaHtmlContentExtractor(file.readText()) },
                )
            }
}
