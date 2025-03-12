package eu.iamgio.quarkdown.pdf.html.executable

import java.io.File

/**
 * Wrapper for invoking the Node Package Manager.
 * @param path path to the NPM executable
 */
class NpmWrapper(
    override val path: String = DEFAULT_PATH,
) : ExecutableWrapper() {
    override val workingDirectory: File? = null

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

    fun isInstalled(module: NodeModule): Boolean =
        try {
            getCommandOutput("list", "-g", module.name).contains(module.name)
        } catch (e: Exception) {
            false
        }

    fun link(
        node: NodeJsWrapper,
        module: NodeModule,
    ) {
        getCommandOutput("link", module.name, workingDirectory = node.workingDirectory)
    }

    companion object {
        const val DEFAULT_PATH = "npm"
    }
}
