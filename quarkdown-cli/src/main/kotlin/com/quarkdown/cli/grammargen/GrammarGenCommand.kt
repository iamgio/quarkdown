package com.quarkdown.cli.grammargen

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.quarkdown.core.log.Log
import com.quarkdown.grammargen.GrammarFormat
import com.quarkdown.grammargen.GrammarGen
import com.quarkdown.grammargen.textmate.TextMateFormat

/**
 * Internal utility command to generate a grammar file for Quarkdown-powered editors and tools.
 */
class GrammarGenCommand : CliktCommand("grammar-gen") {
    private val outputFile by argument(help = "Output file for the generated grammar.")
        .file(
            mustExist = false,
            canBeDir = false,
        )

    private val format: GrammarFormat<*> by option(
        "--format",
        help = "Format of the grammar file to generate.",
    ).convert {
        when (it.lowercase()) {
            "vscode", "textmate" -> TextMateFormat()
            else -> throw IllegalArgumentException("Unsupported format: $it.")
        }
    }.required()

    override fun run() {
        val out = GrammarGen.generate(format)
        outputFile.parentFile?.mkdirs()
        outputFile.writeText(out)
        Log.info("Grammar file generated at ${outputFile.absolutePath}")
    }

    override fun help(context: Context): String = "(Internal) Generates a grammar file for Quarkdown-powered editors and tools."
}
