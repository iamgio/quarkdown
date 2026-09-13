package com.quarkdown.core.log

import co.touchlab.kermit.Severity

/**
 * Verbosity levels for [Log], from most to least verbose.
 * @param severity the corresponding Kermit severity
 */
enum class LogLevel(
    internal val severity: Severity,
) {
    /** Diagnostic messages, useful for troubleshooting. */
    DEBUG(Severity.Debug),

    /** Regular progress messages. */
    INFO(Severity.Info),

    /** Warnings only. */
    WARN(Severity.Warn),

    /** Errors only. */
    ERROR(Severity.Error),

    /** Disables logging entirely. */
    NONE(Severity.Assert),
}
