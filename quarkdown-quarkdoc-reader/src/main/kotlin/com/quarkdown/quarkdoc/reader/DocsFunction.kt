package com.quarkdown.quarkdoc.reader

/**
 * A function in a documentation file.
 * @param name the name of the function
 * @param parameters the parameters of the function
 * @param isLikelyChained whether the function is likely to be chained
 */
data class DocsFunction(
    val name: String,
    val parameters: List<DocsParameter>,
    val isLikelyChained: Boolean,
)

/**
 * A function parameter in a documentation file.
 * @param name the name of the parameter
 * @param description the description of the parameter, possibly in HTML format
 * @param isOptional whether the parameter is optional
 * @param isLikelyNamed whether the parameter is likely to be passed as a named parameter
 * @param isLikelyBody whether the parameter is likely to be passed as a body parameter
 */
data class DocsParameter(
    val name: String,
    val description: String,
    val isOptional: Boolean,
    val isLikelyNamed: Boolean,
    val isLikelyBody: Boolean,
)
