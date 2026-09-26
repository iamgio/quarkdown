package com.quarkdown.lsp.model

/**
 * The role of a [Completion], mapped to a protocol-specific kind at the transport boundary.
 */
enum class CompletionKind {
    /** Completion of a function name. */
    FUNCTION,

    /** Completion of a function parameter name. */
    PARAMETER,

    /** Completion of a parameter value. */
    VALUE,
}

/**
 * A completion proposal.
 * @param label the text displayed in the completion list
 * @param kind the role of the completion
 * @param detail a short additional note (e.g. the module name, or `required`)
 * @param documentationMarkdown the documentation of the completed element, in Markdown
 * @param insertionSnippet the snippet to insert when the completion is accepted,
 *        with tab-stop placeholders, or `null` to insert [label]
 */
data class Completion(
    val label: String,
    val kind: CompletionKind,
    val detail: String? = null,
    val documentationMarkdown: String? = null,
    val insertionSnippet: String? = null,
)
