package eu.iamgio.quarkdown.interaction.executable

import eu.iamgio.quarkdown.log.Log
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

/**
 * Helper class for easily launching Node.js scripts with required NPM modules.
 * @param node the Node.js wrapper
 * @param npm the NPM wrapper
 */
class NodeNpmHelper(
    private val node: NodeJsWrapper,
    private val npm: NpmWrapper,
) {
    private val linkedModules = mutableSetOf<NodeModule>()

    private fun checkWrapper(
        wrapper: ExecutableWrapper,
        name: String,
    ) {
        check(wrapper.isValid) {
            "$name executable cannot be found at '${wrapper.path}'"
        }
    }

    private fun linkModule(module: NodeModule) {
        if (!npm.isInstalled(module)) {
            Log.info("Module '${module.name}' is not installed. Installing...")
            npm.install(module)
        }
        npm.link(node, module)
        linkedModules += module
    }

    @OptIn(ExperimentalPathApi::class)
    private fun cleanup() {
        linkedModules.forEach { npm.unlink(node, it) }
        // Path#deleteRecursively does not follow symlinks, while File#deleteRecursively does.
        // The symlinks contained in the node_modules directory point to the global packages,
        // and should not be deleted.
        sequenceOf("package.json", "package-lock.json", "node_modules")
            .map { File(node.workingDirectory, it).toPath() }
            .forEach { it.deleteRecursively() }
    }

    /**
     * Checks if the Node.js and NPM wrappers are present and valid,
     * links, and optionally installs, the required modules,
     * runs the given action and cleans up the working directory from generated files.
     * @param requiredModules the modules to link
     * @param action the action to run after the executables are checked and the modules are linked
     * @throws IllegalStateException if the Node.js or NPM wrappers are not valid
     */
    fun launch(
        vararg requiredModules: NodeModule,
        action: () -> Unit,
    ) {
        checkWrapper(node, "Node.js")
        checkWrapper(npm, "NPM")
        requiredModules.forEach(::linkModule)

        try {
            action()
        } catch (e: Exception) {
            throw e
        } finally {
            cleanup()
        }
    }
}
