package com.quarkdown.test

import com.quarkdown.core.function.error.FunctionCallRuntimeException
import com.quarkdown.core.function.error.InvalidArgumentCountException
import com.quarkdown.core.function.error.MismatchingArgumentTypeException
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
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
            .var {a} {0}
            
            .a
            
            .var {a} {1}
            
            .a
            
            .a {2}
            
            .a
            """.trimIndent(),
        ) {
            assertEquals("<p>0</p><p>1</p><p>2</p>", it)
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
            assertEquals("<pre><code>Line 1\nLine 2</code></pre>", it)
        }

        execute(
            """
            .let {X}
                x:
            
                .var {a}
                    A
            
                .a
            """.trimIndent(),
        ) {
            assertEquals("<p>A</p>", it)
        }

        execute(
            """
            .let {X}
                x:
            
                .var {a}
                    .x
            
                .a
            """.trimIndent(),
        ) {
            assertEquals("<p>X</p>", it)
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

        // Not found.
        execute(
            """
            .var {x}
              - a: 1
              - b: 2
              - c: 3
              
            .get {d} from:{.x}
            """.trimIndent(),
        ) {
            assertEquals("<p><span class=\"codespan-content\"><code>None</code></span></p>", it)
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
    fun destructuring() {
        execute(
            """
            .var {x}
              .dictionary 
                - a: 1
                - b: 2
                - c: 3
            
            .foreach {.x}
                key value:
                **.key** has value **.value**
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><strong>a</strong> has value <strong>1</strong></p>" +
                    "<p><strong>b</strong> has value <strong>2</strong></p>" +
                    "<p><strong>c</strong> has value <strong>3</strong></p>",
                it,
            )
        }

        assertFails {
            execute(
                """
                .var {x}
                  .dictionary 
                    - a: 1
                    - b: 2
                    - c: 3
                
                .foreach {.x}
                    key value aaa:
                    **.key** has value **.value**
                """.trimIndent(),
            ) {}
        }

        execute(
            """
            .docauthors
                - Giorgio
                  - email: gio@test.com
                  - country: Italy
                - Mary
                  - country: USA
                  
            .foreach {.docauthors}
                name info:
                .name's country is .get {country} {.info}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Giorgio&rsquo;s country is Italy</p>" +
                    "<p>Mary&rsquo;s country is USA</p>",
                it,
            )
        }
    }

    @Test
    fun math() {
        execute(".sum {1} {2}") { assertEquals("<p>3</p>", it) }
        execute(".sum {1} {2}::multiply by:{3}") { assertEquals("<p>9</p>", it) }
        execute(".sum {1} {2}::subtract {1}::multiply by:{3}::divide by:{3}") { assertEquals("<p>2</p>", it) }

        execute(".pi::truncate {2}") { assertEquals("<p>3.14</p>", it) }

        execute(".cos {0}") { assertEquals("<p>1</p>", it) }
        execute(".sin {0}") { assertEquals("<p>0</p>", it) }
        execute(".tan {0}") { assertEquals("<p>0</p>", it) }
        execute(".cos {.pi}") { assertEquals("<p>-1</p>", it) }
        execute(".pi::multiply {2}::cos") { assertEquals("<p>1</p>", it) }

        execute(
            """
            .var {radius} {8}
             
            If we try to calculate the **surface** of a circle of **radius .radius**,
            we will find out it is **.multiply {.pow {.radius} to:{2}} by:{.pi}**
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>If we try to calculate the <strong>surface</strong> of a circle of <strong>radius 8</strong>,\n" +
                    "we will find out it is <strong>201.06194</strong></p>",
                it,
            )
        }

        execute(
            """
            .var {radius} {8}
             
            If we try to calculate the **surface** of a circle of **radius .radius**,
            we will find out it is **.pow {.radius} to:{2}::multiply by:{.pi}**
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>If we try to calculate the <strong>surface</strong> of a circle of <strong>radius 8</strong>,\n" +
                    "we will find out it is <strong>201.06194</strong></p>",
                it,
            )
        }

        execute(".pow {8} to:{2}::multiply by:{.pi}::round") { assertEquals("<p>201</p>", it) }

        execute(".pow {8} to:{2}::multiply by:{.pi}::truncate decimals:{2}") { assertEquals("<p>201.06</p>", it) }

        execute(".pow {8} to:{2}::multiply by:{.pi}::truncate decimals:{1}") { assertEquals("<p>201</p>", it) }

        execute(".pow {8} to:{2}::multiply by:{.pi}::truncate decimals:{0}") { assertEquals("<p>201</p>", it) }

        assertFailsWith<FunctionCallRuntimeException> {
            execute(".pow {8} to:{2}::multiply by:{.pi}::truncate decimals:{-1}") {}
        }

        assertFailsWith<MismatchingArgumentTypeException> {
            execute(".pow {8} to:{2}::multiply by:{.pi}::truncate decimals:{1.5}") {}
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
                    .t1 {.t2}
                    .t2 {.tmp}
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
                        .t1 {.t2}
                        .var {t2} {.tmp}

            .fib {5}
            """.trimIndent()

        val alternativeIterative =
            """
            .var {t1} {0}
            .var {t2} {1}
            
            .function {newtablecolumn}
                n:
                |  $ F_{.n} $  |
                |:-------------:|
                |      .t1      |
            
            .table
                .foreach {0..4}
                    .newtablecolumn {.1}
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
                "<th align=\"center\"><formula>F_{0}</formula></th>" +
                "<th align=\"center\"><formula>F_{1}</formula></th>" +
                "<th align=\"center\"><formula>F_{2}</formula></th>" +
                "<th align=\"center\"><formula>F_{3}</formula></th>" +
                "<th align=\"center\"><formula>F_{4}</formula></th>" +
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
                "<h1>Hello, world!</h1><p>3 <formula>\\times</formula> 3 is 9</p><h3>End</h3>",
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
                "<h1>Hello, world!</h1><p>1 <formula>\\times</formula> 1 is 1</p><h3>End</h3>" +
                    "<h1>Hello, world!</h1><p>2 <formula>\\times</formula> 2 is 4</p><h3>End</h3>" +
                    "<h1>Hello, world!</h1><p>3 <formula>\\times</formula> 3 is 9</p><h3>End</h3>" +
                    "<h1>Hello, world!</h1><p>4 <formula>\\times</formula> 4 is 16</p><h3>End</h3>",
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

        execute(
            """
            .docauthors
                - Llion Jones
                  - branch: Google Research
                  - email: llion@google.com
                - Aidan N. Gomez
                  - branch: University of Toronto
                  - email: aidan@cs.toronto.edu
                - Łukasz Kaiser
                  - branch: Google Brain
                  - email: lukaszkaiser@google.com
                - Illia Polosukhin
                  - email: illia.polosukhin@gmail.com
                  
            .function {author}
                name branch email:
                .container
                    **.name**  
                    .branch  
                    .text {.email} size:{small}  
                    .whitespace
            
            .grid columns:{2} alignment:{spacearound}
                .foreach {.docauthors}
                    name info:
                    .author {.name} {.get {branch} from:{.info} orelse:{-}} {.get {email} from:{.info}}
            """.trimIndent(),
        ) {
            assertEquals(
                "<div style=\"grid-template-columns: auto auto; justify-content: space-around; align-items: center;\" " +
                    "class=\"stack stack-grid\">" +
                    "<div class=\"container\">" +
                    "<p><strong>Llion Jones</strong><br />Google Research<br />" +
                    "<span class=\"size-small\"><a href=\"llion@google.com\">llion@google.com</a></span><br /><span>&nbsp;</span></p>" +
                    "</div><div class=\"container\">" +
                    "<p><strong>Aidan N. Gomez</strong>" +
                    "<br />University of Toronto<br />" +
                    "<span class=\"size-small\"><a href=\"aidan@cs.toronto.edu\">aidan@cs.toronto.edu</a></span><br />" +
                    "<span>&nbsp;</span></p>" +
                    "</div><div class=\"container\"><p><strong>Łukasz Kaiser</strong>" +
                    "<br />Google Brain<br />" +
                    "<span class=\"size-small\"><a href=\"lukaszkaiser@google.com\">lukaszkaiser@google.com</a></span><br />" +
                    "<span>&nbsp;</span></p></div>" +
                    "<div class=\"container\"><p><strong>Illia Polosukhin</strong>" +
                    "<br />-<br />" +
                    "<span class=\"size-small\"><a href=\"illia.polosukhin@gmail.com\">illia.polosukhin@gmail.com</a></span><br />" +
                    "<span>&nbsp;</span></p></div></div>",
                it,
            )
        }
    }

    @Test
    fun functional() {
        assertFails {
            // Lambda cannot be inferred: .1 is not defined
            execute(
                """
                .takeif {3} { .islower {.1} than:{5} }
                """.trimIndent(),
            ) {}
        }

        execute(
            """
            .takeif {3} { @lambda .islower {.1} than:{5} }
            """.trimIndent(),
        ) {
            assertEquals("<p>3</p>", it)
        }

        execute(
            """
            .takeif {3} { @lambda .islower {.1} than:{2} }
            """.trimIndent(),
        ) {
            assertEquals("<p><span class=\"codespan-content\"><code>None</code></span></p>", it)
        }

        execute(
            """
            .takeif {3} { @lambda x: .islower {.x} than:{5} }
            """.trimIndent(),
        ) {
            assertEquals("<p>3</p>", it)
        }

        execute(
            """
            .otherwise {.takeif {3} {@lambda x: .iseven {.x}}} {0}
            """.trimIndent(),
        ) {
            assertEquals("<p>0</p>", it)
        }

        // With chaining.
        execute(
            """
            .takeif {3} {@lambda x: .iseven {.x}}::otherwise {0}
            """.trimIndent(),
        ) {
            assertEquals("<p>0</p>", it)
        }
    }

    @Test
    fun `chart of element repetition`() {
        execute(
            """
            .var {x}
                - b
                - a
                - b
                - c
                - b
                - a
                - d
                - e
                - f
                - e
                - d
                - b
            
            .x {.x::sorted}
            
            .xychart bars:{yes} lines:{no} xtags:{.x::distinct}
                .foreach {.x::groupvalues}
                    .1::size
            """.trimIndent(),
        ) {
            assertEquals(
                """
                <figure><pre class="mermaid fill-height">xychart-beta
                	x-axis [a, b, c, d, e, f]
                	bar [2.0, 4.0, 1.0, 2.0, 2.0, 1.0]
                </pre></figure>
                """.trimIndent(),
                it,
            )
        }
    }

    @Test
    fun `chart from csv`() {
        execute(
            """
            .let {.csv {csv/sales.csv}}
                data:
                .var {columns}
                    .tablecolumns
                        .data
            
                .xychart xtags:{.columns::first} y:{Sales} bars:{yes}
                    .columns::second
                    .columns::third
            """.trimIndent(),
        ) {
            assertEquals(
                """
                <figure><pre class="mermaid fill-height">xychart-beta
                    x-axis [2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024, 2025]
                    y-axis &quot;Sales&quot;
                    bar [230.0, 190.0, 180.0, 175.0, 200.0, 250.0, 290.0, 350.0, 470.0]
                    line [230.0, 190.0, 180.0, 175.0, 200.0, 250.0, 290.0, 350.0, 470.0]
                    bar [85.0, 100.0, 135.0, 180.0, 240.0, 320.0, 430.0, 580.0, 800.0, 0.0]
                    line [85.0, 100.0, 135.0, 180.0, 240.0, 320.0, 430.0, 580.0, 800.0, 0.0]
                </pre></figure>
                """.trimIndent(),
                it.toString().replace("\t", "    "),
            )
        }
    }

    @Test
    fun `all emojis to table`() {
        execute(
            """
            .var {headers}
                - Emoji
                - Code
            
            .tablebyrows {.headers}
                .foreach {.allemojis}
                    emoji code:
                    .pair {.emoji} {.code::codespan}
            """.trimIndent(),
        ) {
            val out =
                "<table><thead><tr><th>Emoji</th><th>Code</th></tr></thead><tbody>" +
                    "<tr><td>\uD83D\uDE00</td><td><span class=\"codespan-content\"><code>smile</code></span></td></tr>" +
                    "<tr><td>\uD83D\uDE03</td><td><span class=\"codespan-content\"><code>smile-with-big-eyes</code></span></td></tr>" +
                    "<tr><td>\uD83D\uDE04</td><td><span class=\"codespan-content\"><code>grin</code></span></td></tr><tr>"
            assertEquals(
                out,
                it.toString().substring(0, out.length),
            )
        }
    }
}
