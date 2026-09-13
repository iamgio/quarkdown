package com.quarkdown.core.log

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.mutableLoggerConfigInit
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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

    private val timeFormat =
        LocalTime.Format {
            hour()
            char(':')
            minute()
        }

    @OptIn(ExperimentalTime::class)
    private fun currentTime(): String =
        timeFormat.format(
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .time,
        )

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
                    println("$ANSI_WHITE[${currentTime()}]$ANSI_RESET ${ANSI_GREEN}Success$ANSI_RESET $message")
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
 * The minimum [level] defaults to [LogLevel.INFO] and can be adjusted by the launcher
 * (e.g. the CLI, which reads it from the `QD_LOG_LEVEL` environment variable).
 */
object Log {
    private val config =
        mutableLoggerConfigInit(listOf(QuarkdownLogWriter)).apply {
            minSeverity = LogLevel.INFO.severity
        }

    private val logger = Logger(config)

    /**
     * The minimum level a message must have in order to be logged.
     */
    var level: LogLevel = LogLevel.INFO
        set(value) {
            field = value
            config.minSeverity = value.severity
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
}
