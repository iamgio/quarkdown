package com.quarkdown.core.bibliography

import java.io.Reader

/**
 * Parser for bibliographies from various formats.
 * @see com.quarkdown.core.bibliography.bibtex.BibTeXBibliographyParser
 */
interface BibliographyParser {
    /**
     * Parses a [Bibliography] from the given [reader].
     * @param reader the reader to read the bibliography from
     * @return the parsed bibliography
     */
    fun parse(reader: Reader): Bibliography
}
