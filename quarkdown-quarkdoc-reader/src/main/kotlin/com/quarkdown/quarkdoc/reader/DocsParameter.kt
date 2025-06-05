package com.quarkdown.quarkdoc.reader

/**
 * A function parameter in a documentation file.
 * @param name the name of the parameter
 * @param description the description of the parameter, possibly in HTML format
 * @param isOptional whether the parameter is optional
 * @param isLikelyBody whether the parameter is likely to be passed as a body parameter
 */
data class DocsParameter(
    val name: String,
    val description: String,
    val isOptional: Boolean,
    val isLikelyBody: Boolean,
)
