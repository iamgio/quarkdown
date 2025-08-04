package com.quarkdown.lsp.subservices

import org.eclipse.lsp4j.services.TextDocumentService

/**
 * Represents a single operation that is part of a [TextDocumentService].
 *
 * This is to ensure the main service does not break single-responsibility principles.
 * @param P type of the parameters
 * @param O type of the output of the operation
 */
interface TextDocumentSubservice<P, O> {
    /**
     * Processes the given parameters and text to produce an output.
     * @param params the parameters for the operation
     * @param text the text of the current document
     * @return the output of the operation
     */
    fun process(
        params: P,
        text: String,
    ): O
}
