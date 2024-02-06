package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.log.Log
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        Log.error("No source file passed.")
        exitProcess(NO_SOURCE_FILE_EXIT_CODE)
    }

    val sourceFile = File(args.first())
}
