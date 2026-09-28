package com.quarkdown.core

import com.quarkdown.core.flavor.InlineLexerVariant
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.lexer.Lexer
import com.quarkdown.core.util.normalizeLineSeparators
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pins the tokenization of every edge case the scan-based lexers were built against.
 *
 * A snapshot the repository does not carry is a failure, so a lost file cannot make this test vacuous. To
 * accept a deliberate change, run the test with `QD_UPDATE_LEXER_SNAPSHOTS=1` in the environment, which
 * rewrites the snapshots, then read the diff before committing it. Any other value leaves the assertions in
 * charge, so a stray `=0` cannot disable them.
 */
class LexerSnapshotTest {
    private companion object {
        const val UPDATE_VARIABLE = "QD_UPDATE_LEXER_SNAPSHOTS"

        const val UPDATE_VALUE = "1"
    }

    private fun String.escaped() = replace("\\", "\\\\").replace("\n", "\\n").replace("\t", "\\t")

    private fun Lexer.dump(): String =
        tokenize().joinToString("\n") {
            "${it::class.simpleName!!.removeSuffix("Token")}\t${it.data.text.escaped()}"
        }

    private fun check(
        name: String,
        render: (String) -> String,
        cases: Map<String, List<String>>,
    ) {
        val actual =
            cases.entries.joinToString("\n") { (kind, sources) ->
                sources.joinToString("\n") { source ->
                    "=== $kind :: ${source.escaped()}\n${render(source)}"
                }
            }
        val file = File("src/jvmTest/resources/lexing/snapshots/$name.txt")
        if (System.getenv(UPDATE_VARIABLE) == UPDATE_VALUE) {
            file.parentFile.mkdirs()
            file.writeText(actual)
            return
        }
        assertTrue(file.exists(), "Missing lexer snapshot ${file.path}: rerun with $UPDATE_VARIABLE=1 to write it")
        assertEquals(file.readText().normalizeLineSeparators().toString(), actual, "Lexer snapshot $name changed")
    }

    @Test
    fun `block tokenization is unchanged`() {
        check("block", { QuarkdownFlavor.lexerFactory.newBlockLexer(it).dump() }, LexerEdgeCases.block)
    }

    @Test
    fun `inline tokenization is unchanged`() {
        check(
            "inline",
            { source ->
                InlineLexerVariant.entries.joinToString("\n") { variant ->
                    "-- $variant\n${QuarkdownFlavor.lexerFactory.newInlineLexer(source, variant).dump()}"
                }
            },
            LexerEdgeCases.inline,
        )
    }
}
