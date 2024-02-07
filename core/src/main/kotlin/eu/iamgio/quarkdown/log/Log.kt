package eu.iamgio.quarkdown.log

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Bridge for logging utilities.
 */
object Log {
    /**
     * The standard text logger.
     */
    private val logger: Logger by lazy { LogManager.getLogger(this.javaClass.name) }

    // Log4J wrapper functions

    fun debug(message: Any) = logger.debug(message)

    fun info(message: Any) = logger.info(message)

    fun warn(message: Any) = logger.warn(message)

    fun error(message: Any) = logger.error(message)
}
