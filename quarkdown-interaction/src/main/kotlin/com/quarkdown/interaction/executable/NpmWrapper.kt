package com.quarkdown.interaction.executable

import com.quarkdown.interaction.Env
import com.quarkdown.interaction.os.OsUtils
import java.io.File

/**
 * Wrapper for invoking the Node Package Manager,
 * with functionalities for installing and linking [NodeModule]s.
 *
 * The following environment variable can affect the behavior of this wrapper:
 * - [com.quarkdown.interaction.Env.QD_NPM_PREFIX]: if set, determines the global prefix for NPM operations.
 * - [com.quarkdown.interaction.Env.NODE_PATH]: if set, determines the path to the Node.js modules.
 *
 * @param path path to the NPM executable
 */
class NpmWrapper(
    override val path: String,
) : ExecutableWrapper() {
    override val workingDirectory: File? = null

    /**
     * If the `QD_NPM_PREFIX` environment variable is set,
     * returns the `--prefix` argument with its value.
     */
    private val globalPrefixArgs: Array<String>
        get() =
            when (val prefix = Env.npmPrefix) {
                null -> emptyArray()
                else -> arrayOf("--prefix", prefix)
            }

    override val isValid: Boolean
        get() =
            try {
                launchAndGetOutput("--version").isNotBlank()
            } catch (_: Exception) {
                false
            }

    init {
        validate()
    }

    /**
     * @return whether the given [module] is installed in [node]'s working directory.
     */
    fun isInstalled(
        node: NodeJsWrapper,
        module: NodeModule,
    ): Boolean =
        try {
            launchAndGetOutput(
                "list",
                module.name,
                *globalPrefixArgs,
                workingDirectory = node.workingDirectory,
            ).contains(module.name)
        } catch (_: Exception) {
            false
        }

    /**
     * Links a globally installed Node.js module to the project located in [node]'s working directory.
     * @param node the Node.js wrapper
     * @param module the module to link
     */
    @Deprecated("Not used anymore since v1.6.0")
    fun link(
        node: NodeJsWrapper,
        module: NodeModule,
    ) {
        launchAndGetOutput("link", module.name, *globalPrefixArgs, workingDirectory = node.workingDirectory)
    }

    /**
     * Unlinks a linked Node.js module from the project located in [node]'s working directory.
     * @param node the Node.js wrapper
     * @param module the module to unlink
     */
    @Deprecated("Not used anymore since v1.6.0")
    fun unlink(
        node: NodeJsWrapper,
        module: NodeModule,
    ) {
        launchAndGetOutput("unlink", module.name, *globalPrefixArgs, workingDirectory = node.workingDirectory)
    }

    companion object : WithDefaultPath {
        /**
         * Default base path to the NPM executable.
         */
        private const val DEFAULT_PATH = "npm"

        /**
         * Default path to the NPM executable, OS-dependent.
         * @see [com.quarkdown.interaction.os.OsUtils.cmdBasedExecutablePath]
         */
        private val osDependentDefaultPath: String
            get() = OsUtils.cmdBasedExecutablePath(DEFAULT_PATH)

        override val defaultPath: String
            get() = osDependentDefaultPath
    }
}
