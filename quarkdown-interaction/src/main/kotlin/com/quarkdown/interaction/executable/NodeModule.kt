package com.quarkdown.interaction.executable

import com.quarkdown.interaction.Env

/**
 * Abstraction of a Node.js module, which can be installed and linked to a [NodeJsWrapper] through a [NpmWrapper].
 * @param name name of the module, which matches the name in the NPM registry
 */
open class NodeModule(
    val name: String,
)

/**
 * Exception thrown when a required Node.js module is not installed.
 * @param module the module that is not installed
 */
class NodeModuleNotInstalledException(
    module: NodeModule,
) : IllegalStateException(
        """
        Module '${module.name}' is not installed. Please install it via `npm install ${module.name} --prefix $${Env.QUARKDOWN_NPM_PREFIX}` and retry.
        Make sure ${Env.QUARKDOWN_NPM_PREFIX} is an environment variable pointing to Quarkdown's `lib` directory or any other installation directory,
        and it must be available at Quarkdown's launch. The current value is '${Env.npmPrefix}'. Note that '/node_modules' is automatically appended
        to this path, so if your system has modules installed in '/usr/lib/node_modules', use '/usr/lib'.
        
        For more information, see: https://github.com/iamgio/quarkdown/wiki/pdf-export
        
        Note: installing Quarkdown via a package manager is suggested, as it sets up the required dependencies automatically.
        """.trimIndent(),
    )
