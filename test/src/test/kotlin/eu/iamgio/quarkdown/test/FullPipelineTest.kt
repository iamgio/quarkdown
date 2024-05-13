package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.PipelineHooks
import eu.iamgio.quarkdown.stdlib.Stdlib
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that cover the whole pipeline from lexing to rendering, including function call expansion.
 * [Stdlib] is used as a library.
 */
class FullPipelineTest {
    /**
     * Executes a Quarkdown source.
     * @param hook action run after rendering
     */
    private fun execute(
        source: String,
        hook: (CharSequence) -> Unit,
    ) {
        val hooks =
            PipelineHooks(
                afterRendering = { hook(it) },
            )

        val pipeline =
            Pipeline(
                MutableContext(QuarkdownFlavor),
                libraries = setOf(Stdlib.library),
                renderer = { rendererFactory, context -> rendererFactory.html(context) },
                hooks,
            )

        pipeline.execute(source)
    }

    @Test
    fun text() {
        execute("Hello, world!") {
            assertEquals("<p>Hello, world!</p>", it)
        }

        execute("# Title\n Hello, world!\n## Subtitle\nHello, world!") {
            assertEquals(
                "<h1>Title</h1><p>Hello, world!</p><h2>Subtitle</h2><p>Hello, world!</p>",
                it,
            )
        }
    }
}
