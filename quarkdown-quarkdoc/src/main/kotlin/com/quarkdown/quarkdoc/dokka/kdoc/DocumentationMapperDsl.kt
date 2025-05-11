package com.quarkdown.quarkdoc.dokka.kdoc

/**
 * DSL for [DocumentationMapper].
 */
fun mapDocumentation(
    documentation: DokkaDocumentation,
    deep: Boolean = true,
    block: DocumentationMapper.() -> Unit,
): DokkaDocumentation {
    val mapper =
        if (deep) {
            DeepDocumentationMapper()
        } else {
            SimpleDocumentationMapper()
        }
    return mapper.also(block).map(documentation)
}
