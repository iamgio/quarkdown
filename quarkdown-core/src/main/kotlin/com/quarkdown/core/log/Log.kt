package com.quarkdown.core.log

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.mutableLoggerConfigInit
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Tag that marks success messages, granting them a highlighted format.
 */
private const val SUCCESS_TAG = "SUCCESS"

/**
 * Kermit [LogWriter] that mirrors Quarkdown's logging behavior.
 */
private object QuarkdownLogWriter : LogWriter() {
    private const val ANSI_WHITE = "\u001B[37m"
    private const val ANSI_GREEN = "\u001B[32m"
    private const val ANSI_RESET = "\u001B[0m"

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?,
    ) {
        if (message.isNotEmpty()) {
            when {
                severity == Severity.Error -> {
                    System.err.println(message)
                }

                tag == SUCCESS_TAG -> {
                    val time = LocalTime.now().format(timeFormatter)
                    println("$ANSI_WHITE[$time]$ANSI_RESET ${ANSI_GREEN}Success$ANSI_RESET $message")
                }

                severity == Severity.Warn -> {
                    println("[!] $message")
                }

                severity == Severity.Debug -> {
                    println("[DEBUG] $message")
                }

                else -> {
                    println(message)
                }
            }
        }
        throwable?.printStackTrace()
    }
}

/**
 * Bridge for logging utilities, backed by Kermit.
 * The minimum severity is read from the `loglevel` system property
 * (`debug`, `info`, `warn`, `error`), defaulting to `info`.
 */
object Log {
    private val config =
        mutableLoggerConfigInit(listOf(QuarkdownLogWriter)).apply {
            minSeverity = severityFromProperty()
        }

    private val logger = Logger(config)

    /**
     * @return the minimum severity set via the `loglevel` system property, or [Severity.Info] by default
     */
    private fun severityFromProperty(): Severity =
        when (System.getProperty("loglevel")?.lowercase()) {
            "debug" -> Severity.Debug
            "warn" -> Severity.Warn
            "error" -> Severity.Error
            else -> Severity.Info
        }

    fun debug(message: Any) = logger.d { message.toString() }

    /**
     * Logs the result of [message] lazily, only if the logger is set at debug level.
     */
    fun debug(message: () -> Any) = logger.d { message().toString() }

    fun debug(throwable: Throwable) = logger.d(throwable) { "" }

    fun info(message: Any) = logger.i { message.toString() }

    fun success(message: Any) = logger.withTag(SUCCESS_TAG).i { message.toString() }

    fun warn(message: Any) = logger.w { message.toString() }

    fun error(message: Any) = logger.e { message.toString() }

    /**
     * Disables all logging.
     */
    fun disableLogging() {
        config.minSeverity = Severity.Assert
    }
}
