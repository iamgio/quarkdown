package eu.iamgio.quarkdown.interaction.executable

import java.io.File

/**
 * Wrapper for invoking the Node Package Manager,
 * with functionalities for installing and linking [NodeModule]s.
 * @param path path to the NPM executable
 */
class NpmWrapper(
    override val path: String = DEFAULT_PATH,
) : ExecutableWrapper() {
    override val workingDirectory: File? = null

    override val isValid: Boolean
        get() =
            try {
                launchAndGetOutput("--version").trim().let {
                    it.isNotBlank() && it.lines().size == 1
                }
            } catch (e: Exception) {
                false
            }

    init {
        validate()
    }

    /**
     * Installs a Node.js module globally, overwriting it if it already exists.
     * @param module the module to install
     */
    fun install(module: NodeModule) {
        launchAndGetOutput("install", "-g", module.name)
    }

    /**
     * @return whether the given [module] is installed globally
     */
    fun isInstalled(module: NodeModule): Boolean =
        try {
            launchAndGetOutput("list", "-g", module.name).contains(module.name)
        } catch (e: Exception) {
            false
        }

    /**
     * Links a globally installed Node.js module to the project located in [node]'s working directory.
     * @param node the Node.js wrapper
     * @param module the module to link
     */
    fun link(
        node: NodeJsWrapper,
        module: NodeModule,
    ) {
        launchAndGetOutput("link", module.name, workingDirectory = node.workingDirectory)
    }

    companion object {
        /**
         * Default path to the NPM executable.
         */
        const val DEFAULT_PATH = "npm"
    }
}
