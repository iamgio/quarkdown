@file:QModule

package com.quarkdown.stdlib

import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.core.log.Log
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/**
 * Logs a message (info level) to the standard output.
 * @param message message to log
 */
@QFunction
fun log(message: String) = VoidValue.also { Log.info(message) }

/**
 * Logs a message (debug level) to the standard output.
 * Note that `-Dloglevel=debug` must be enabled to see debug messages.
 * @param message message to log
 */
@QFunction
fun debug(message: String) = VoidValue.also { Log.debug(message) }

/**
 * Throws an [Exception] with the given message.
 * The result depends on the pipeline's error handler.
 * By default, the message is logged (error level) to the standard output and an error box is rendered on the document.
 * If the program is run on strict mode, the stack trace is printed and the process will be stopped.
 * @param message error message
 */
@QFunction
fun error(message: String) = VoidValue.also { throw Exception(message) }
