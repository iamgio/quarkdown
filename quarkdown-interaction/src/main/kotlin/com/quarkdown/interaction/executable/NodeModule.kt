package com.quarkdown.interaction.executable

/**
 * Abstraction of a Node.js module, which can be installed and linked to a [NodeJsWrapper] through a [NpmWrapper].
 * @param name name of the module, which matches the name in the NPM registry
 */
open class NodeModule(
    val name: String,
)

/**
 * Exception thrown when a required Node.js module is not installed globally.
 * @param module the module that is not installed
 */
class NodeModuleNotInstalledException(
    module: NodeModule,
) : IllegalStateException(
        """
        Module '${module.name}' is not installed. Please install it via `npm install -g ${module.name}` and retry.
        Make sure Quarkdown has the permissions to link the module correctly.
        If your NPM package directory requires root privileges, please consider updating your NPM configuration.
        For more information, see https://stackoverflow.com/questions/18088372/how-to-npm-install-global-not-as-root
        
        Note: installing Quarkdown via a package manager is suggested, as it sets up the required dependencies automatically.
        """.trimIndent(),
    )
