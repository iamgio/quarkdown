package com.quarkdown.interaction.executable

/**
 * Abstraction of a Node.js module, which can be installed and linked to a [NodeJsWrapper] through a [NpmWrapper].
 * @param name name of the module, which matches the name in the NPM registry
 */
open class NodeModule(
    val name: String,
)
