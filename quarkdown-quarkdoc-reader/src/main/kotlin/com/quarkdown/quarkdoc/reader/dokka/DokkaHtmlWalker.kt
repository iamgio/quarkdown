package com.quarkdown.quarkdoc.reader.dokka

import com.quarkdown.quarkdoc.reader.DocsWalker
import java.io.File

/**
 * Recursive walker of Dokka HTML files.
 */
class DokkaHtmlWalker(
    private val root: File,
) : DocsWalker<DokkaHtmlContentExtractor> {
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
                    moduleName = file.parentFile.name,
                    extractor = { DokkaHtmlContentExtractor(file.readText()) },
                )
            }
}
