package com.quarkdown.quarkdoc.reader.dokka

import com.quarkdown.quarkdoc.reader.DocsWalker
import java.io.File

/**
 * A directory with this name is a Quarkdown module.
 */
private const val MODULE_DIR_NAME = "module"

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
                .takeIf { it.getOrNull(it.size - 2) == MODULE_DIR_NAME }
                ?.lastOrNull()

    /**
     * Recursively scans Dokka HTML files in the given root directory.
     */
    override fun walk(): Sequence<DocsWalker.Result<DokkaHtmlContentExtractor>> =
        root
            .walkTopDown()
            .asSequence()
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
