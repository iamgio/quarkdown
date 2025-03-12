package eu.iamgio.quarkdown.pdf.html.executable

/**
 * Wrapper for invoking the Node Package Manager.
 * @param path path to the NPM executable
 */
class NpmWrapper(
    override val path: String = DEFAULT_PATH,
) : ExecutableWrapper() {
    override val isValid: Boolean
        get() =
            try {
                getCommandOutput("--version").trim().let {
                    it.isNotBlank() && it.lines().size == 1
                }
            } catch (e: Exception) {
                false
            }

    fun install(module: NodeModule) {
        getCommandOutput("install", "-g", module.name)
    }

    fun uninstall(module: NodeModule) {
        getCommandOutput("uninstall", "-g", module.name)
    }

    fun link(
        node: NodeJsWrapper,
        module: NodeModule,
    ) {
        getCommandOutput("link", module.name, workingDirectory = node.workingDirectory)
    }

    fun isInstalled(module: NodeModule): Boolean =
        try {
            getCommandOutput("list", "-g", module.name).contains(module.name)
        } catch (e: Exception) {
            false
        }

    companion object {
        const val DEFAULT_PATH = "npm"
    }
}
