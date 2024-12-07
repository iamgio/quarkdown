package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.function.error.InvalidArgumentCountException
import eu.iamgio.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for scripting capabilities.
 */
class ScriptingTest {
    @Test
    fun `flow functions`() {
        execute(".if { .islower {2} than:{3} }\n  **Text**") {
            assertEquals("<p><strong>Text</strong></p>", it)
        }

        execute(".if { .islower {3} than:{2} }\n  **Text**") {
            assertEquals("", it)
        }

        execute(".ifnot { .islower {3} than:{2} }\n  **Text**") {
            assertEquals("<p><strong>Text</strong></p>", it)
        }

        execute(".foreach {..3}\n  **N:** .1") {
            assertEquals("<p><strong>N:</strong> 1</p><p><strong>N:</strong> 2</p><p><strong>N:</strong> 3</p>", it)
        }

        execute(".foreach {2..4}\n  **N:** .1") {
            assertEquals("<p><strong>N:</strong> 2</p><p><strong>N:</strong> 3</p><p><strong>N:</strong> 4</p>", it)
        }

        execute(".foreach {.range from:{2} to:{4}}\n  **N:** .1") {
            assertEquals("<p><strong>N:</strong> 2</p><p><strong>N:</strong> 3</p><p><strong>N:</strong> 4</p>", it)
        }

        execute(
            """
            ## Title
            .foreach {..2}
                n:
                Hi .n
            """.trimIndent(),
        ) {
            assertEquals("<h2>Title</h2><p>Hi 1</p><p>Hi 2</p>", it)
        }

        execute(
            """
            .foreach {.range to:{.sum {1} {1}}}
                ## Hello .1
                .foreach {..1}
                    **Hi**!
            """.trimIndent(),
        ) {
            assertEquals("<h2>Hello 1</h2><p><strong>Hi</strong>!</p><h2>Hello 2</h2><p><strong>Hi</strong>!</p>", it)
        }

        execute(
            """
            .repeat {2}
                ## Hello .1
                .repeat {1}
                    **Hi**!
            """.trimIndent(),
        ) {
            assertEquals("<h2>Hello 1</h2><p><strong>Hi</strong>!</p><h2>Hello 2</h2><p><strong>Hi</strong>!</p>", it)
        }

        execute(
            """
            .foreach {..2}
                .foreach {.range to:{2}}
                    .foreach {..2}
                        ## Title 2
                    # Title 1
            
                Some text
            ### Title 3
            """.trimIndent(),
        ) {
            assertEquals(
                (
                    "<h2>Title 2</h2><h2>Title 2</h2><div class=\"page-break\" data-hidden=\"\"></div><h1>Title 1</h1>".repeat(
                        2,
                    ) +
                        "<p>Some text</p>"
                ).repeat(2) + "<h3>Title 3</h3>",
                it,
            )
        }

        execute(".function {hello}\n  *Hello*!\n\n.hello") {
            assertEquals("<p><em>Hello</em>!</p>", it)
        }

        execute(".function {hello}\n   target:\n  **Hello** .target!\n\n.hello {world}") {
            assertEquals("<p><strong>Hello</strong> world!</p>", it)
        }

        assertFailsWith<InvalidArgumentCountException> {
            execute(".function {hello}\n   target:\n  `Hello` .target!\n\n.hello") {}
        }

        execute(
            """
            .if {yes}
                .function {hello}
                    name:
                    Hello, *.name*!
                
                #### .hello {world}
                .hello {iamgio}
            """.trimIndent(),
        ) {
            assertEquals("<h4>Hello, <em>world</em>!</h4><p>Hello, <em>iamgio</em>!</p>", it)
        }

        execute(
            """
            .let {world}
                Hello, **.1**!
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello, <strong>world</strong>!</p>", it)
        }

        execute(
            """
            .foreach {..3}
                .let {.1}
                    x:
                    .sum {3} {.x}
            """.trimIndent(),
        ) {
            assertEquals("<p>4</p><p>5</p><p>6</p>", it)
        }

        execute(
            """
            .let {code.txt}
                file:
                .let {.read {.file} {..2}}
                    .code
                        .1
            """.trimIndent(),
        ) {
            assertEquals("<pre><code>Line 1${System.lineSeparator()}Line 2</code></pre>", it)
        }
    }

    @Test
    fun `type inference`() {
        execute(
            """
            .function {x}
                arg:
                .if {.arg}
                    Hi
                .ifnot {.arg}
                    Hello

            .x {no}
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello</p>", it)
        }

        execute(
            """
            .var {x} {no}
            .if {.x}
                Hi
            .ifnot {.x}
                Hello
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello</p>", it)
        }
    }

    @Test
    fun iterables() {
        val abc =
            """
            .var {abc}
              - A
              - B
              - C
            
            
            """.trimIndent()

        execute(
            abc +
                """
                .foreach {.abc}
                  .1
                """.trimIndent(),
        ) {
            assertEquals("<p>A</p><p>B</p><p>C</p>", it)
        }

        execute(
            abc +
                """
                .foreach {.abc}
                  .lowercase {.1}
                """.trimIndent(),
        ) {
            assertEquals("<p>a</p><p>b</p><p>c</p>", it)
        }

        execute("$abc.getat {2} from:{.abc}") {
            assertEquals("<p>B</p>", it)
        }

        // Out of bounds.
        execute("$abc.getat {5} from:{.abc}") {
            assertEquals("<p><input disabled=\"\" type=\"checkbox\" /></p>", it)
        }

        execute("$abc.first from:{.abc}") {
            assertEquals("<p>A</p>", it)
        }

        execute("$abc.last from:{.abc}") {
            assertEquals("<p>C</p>", it)
        }

        execute(
            """
            .var {nums}
              - 1
              - 2
              - 3
              - 4

            .foreach {.nums}
              n:
              .pow {.n} to:{2}
            """.trimIndent(),
        ) {
            assertEquals("<p>1</p><p>4</p><p>9</p><p>16</p>", it)
        }
    }

    @Test
    fun pairs() {
        execute(
            """
            .var {p} {.pair {1} {2}}
            .sum {.first {.p}} {.second {.p}}
            """.trimIndent(),
        ) {
            assertEquals("<p>3</p>", it)
        }

        execute(
            """
            .foreach {.pair {1} {2}}
              .1
            """.trimIndent(),
        ) {
            assertEquals("<p>1</p><p>2</p>", it)
        }
    }

    @Test
    fun dictionaries() {
        val authors =
            """
            .docauthors
              - John
                - from: USA
              - Maria
                - from: Italy
            
            """.trimIndent()

        execute(
            authors +
                """
                .var {john} {.get {John} from:{.docauthors}}
                
                .get {from} from:{.john}
                """.trimIndent(),
        ) {
            assertEquals(
                "<p>USA</p>",
                it,
            )
        }

        execute(
            authors +
                """
                .foreach {.docauthors}
                  An author is .first {.1}, from .get {from} from:{.second {.1}}
                """.trimIndent(),
        ) {
            assertEquals(
                "<p>An author is John, from USA</p>" +
                    "<p>An author is Maria, from Italy</p>",
                it,
            )
        }

        execute(
            """
            .var {x}
              - a: 1
              - b: 2
              - c: 3
              
            .get {b} from:{.x}
            """.trimIndent(),
        ) {
            assertEquals("<p>2</p>", it)
        }

        execute(
            """
            .var {x}
              - a:
                - aa: 1
                - ab: 2
              - b:
                - ba: 3
                - bb: 4
              
            .get {ba} from:{.get {b} from:{.x}}
            """.trimIndent(),
        ) {
            assertEquals("<p>3</p>", it)
        }

        execute(
            """            
            .var {x}
                .dictionary
                    - a
                      - aa: 1
                      - ab: 2
                    - b
                      - ba: 3
                      - bb: 4
            
            .foreach {.x}
                .var {name} {.first {.1}}
                .var {dict} {.second {.1}}
                .var {key} {.concatenate {.name} {b}} 
                .var {value} {.get {.key} {.dict}}
            
                .name, .value
            """.trimIndent(),
        ) {
            assertEquals("<p>a, 2</p><p>b, 4</p>", it)
        }
    }

    @Test
    fun fibonacci() {
        // Iterative Fibonacci sequence calculation.
        val iterative =
            """
            .var {t1} {0}
            .var {t2} {1}
            
            .table
                .foreach {0..4}
                    | $ F_{.1} $ |
                    |:-------------:|
                    |      .t1      |
                    .var {tmp} {.sum {.t1} {.t2}}
                    .var {t1} {.t2}
                    .var {t2} {.tmp}
            """.trimIndent()

        val iterativeInFunction =
            """
            .function {fib}
                n:
                .var {t1} {0}
                .var {t2} {1}
                
                .table
                    .repeat {.n}
                        | $ F_{.subtract {.1} {1}} $ |
                        |:--------------------------:|
                        |             .t1            |
                        .var {tmp} {.sum {.t1} {.t2}}
                        .var {t1} {.t2}
                        .var {t2} {.tmp}

            .fib {5}
            """.trimIndent()

        val alternativeIterative =
            """
            .var {t1} {0}
            .var {t2} {1}
            
            .function {tablecolumn}
                n:
                |  $ F_{.n} $  |
                |:-------------:|
                |      .t1      |
            
            .table
                .foreach {0..4}
                    .tablecolumn {.1}
                    .var {tmp} {.sum {.t1} {.t2}}
                    .var {t1} {.t2}
                    .var {t2} {.tmp}
            """.trimIndent()

        // Recursive Fibonacci sequence calculation.
        val recursive =
            """
            .function {fib}
                n:
                .if { .islower {.n} than:{2} }
                    .n
                .ifnot { .islower {.n} than:{2} }
                    .sum {
                        .fib { .subtract {.n} {1} }
                    } {
                        .fib { .subtract {.n} {2} }
                    }
              
            .table
                .foreach {0..4}
                    | $ F_{.1} $  |
                    |:------------:|
                    | .fib {.1} |
            """.trimIndent()

        val out =
            "<table><thead><tr>" +
                "<th align=\"center\">__QD_INLINE_MATH__\$F_{0}\$__QD_INLINE_MATH__</th>" +
                "<th align=\"center\">__QD_INLINE_MATH__\$F_{1}\$__QD_INLINE_MATH__</th>" +
                "<th align=\"center\">__QD_INLINE_MATH__\$F_{2}\$__QD_INLINE_MATH__</th>" +
                "<th align=\"center\">__QD_INLINE_MATH__\$F_{3}\$__QD_INLINE_MATH__</th>" +
                "<th align=\"center\">__QD_INLINE_MATH__\$F_{4}\$__QD_INLINE_MATH__</th>" +
                "</tr></thead><tbody><tr>" +
                "<td align=\"center\">0</td>" +
                "<td align=\"center\">1</td>" +
                "<td align=\"center\">1</td>" +
                "<td align=\"center\">2</td>" +
                "<td align=\"center\">3</td>" +
                "</tr></tbody></table>"

        execute(iterative) {
            assertEquals(out, it)
        }

        execute(iterativeInFunction) {
            assertEquals(out, it)
        }

        execute(alternativeIterative) {
            assertEquals(out, it)
        }

        execute(recursive) {
            assertEquals(out, it)
        }
    }

    @Test
    fun `layout builder`() {
        val layoutFunction =
            """
            .noautopagebreak
            
            .function {mylayout}
                name number:
                # Hello, .name!
                
                .number $ \times $ .number is .multiply {.number} by:{.number}
                
                ### End
                
            """.trimIndent()

        execute(
            "$layoutFunction\n.mylayout {world} {3}",
        ) {
            assertEquals(
                "<h1>Hello, world!</h1><p>3 __QD_INLINE_MATH__$\\times\$__QD_INLINE_MATH__ 3 is 9</p><h3>End</h3>",
                it,
            )
        }

        execute(
            layoutFunction +
                """
                    
                .repeat {4}
                    n:
                    .mylayout {world} {.n}
                """.trimIndent(),
        ) {
            assertEquals(
                "<h1>Hello, world!</h1><p>1 __QD_INLINE_MATH__$\\times\$__QD_INLINE_MATH__ 1 is 1</p><h3>End</h3>" +
                    "<h1>Hello, world!</h1><p>2 __QD_INLINE_MATH__$\\times\$__QD_INLINE_MATH__ 2 is 4</p><h3>End</h3>" +
                    "<h1>Hello, world!</h1><p>3 __QD_INLINE_MATH__$\\times\$__QD_INLINE_MATH__ 3 is 9</p><h3>End</h3>" +
                    "<h1>Hello, world!</h1><p>4 __QD_INLINE_MATH__$\\times\$__QD_INLINE_MATH__ 4 is 16</p><h3>End</h3>",
                it,
            )
        }

        execute(
            """
            .function {poweredby}
                credits:
                .text {powered by .credits} size:{small} variant:{smallcaps}
            
            This **exciting feature**, .poweredby {[Quarkdown](https://github.com/iamgio/quarkdown)}, looks great!
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>This <strong>exciting feature</strong>, <span class=\"size-small\" style=\"font-variant: small-caps;\">" +
                    "powered by <a href=\"https://github.com/iamgio/quarkdown\">Quarkdown</a></span>, looks great!</p>",
                it,
            )
        }

        execute(
            """
            .repeat {3}
                .container width:{1cm}
                    Item .1
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\" style=\"width: 1.0cm;\">" +
                    "<p>Item 1</p></div><div class=\"container\" style=\"width: 1.0cm;\">" +
                    "<p>Item 2</p></div><div class=\"container\" style=\"width: 1.0cm;\">" +
                    "<p>Item 3</p></div>",
                it,
            )
        }
    }
}
