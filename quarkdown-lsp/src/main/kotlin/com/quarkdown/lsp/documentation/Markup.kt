package com.quarkdown.lsp.documentation

import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.MarkupKind

/**
 * @return [this] Markdown content wrapped as LSP [MarkupContent]
 */
fun String.markdownToMarkup(): MarkupContent = MarkupContent(MarkupKind.MARKDOWN, this)
