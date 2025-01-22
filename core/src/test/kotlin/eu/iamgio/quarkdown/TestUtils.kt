package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.tokens.NewlineToken
import eu.iamgio.quarkdown.lexer.tokens.PlainTextToken
import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.PipelineOptions
import eu.iamgio.quarkdown.visitor.token.TokenVisitor
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.assertIs

/**
 * Asserts that the contents of two nodes are equal.
 * @param expected expected node
 * @param actual actual node
 */
fun assertNodeEquals(
    expected: Node,
    actual: Node,
) = assertThat(actual)
    .usingRecursiveComparison()
    .isEqualTo(expected)!!

/**
 * Reads the text content of a test resource.
 * @param path path to the test resource
 * @return text of the test resource
 * @throws IllegalAccessError if the resource was not found
 */
fun readSource(path: String) =
    LexerTest::class.java
        .getResourceAsStream(path)
        ?.bufferedReader()
        ?.readText()
        ?: throw IllegalAccessError("No resource $path")

/**
 * Tokenizes and parses some input.
 * @param assertType if `true`, asserts each output node is of type [T]
 * @param lexer lexer to use to tokenize
 * @param parser parser to use on each token
 * @param T type of the nodes to output
 * @return iterator of the parsed nodes
 */
inline fun <reified T : Node> nodesIterator(
    lexer: Lexer,
    parser: TokenVisitor<Node>,
    assertType: Boolean = true,
): Iterator<T> =
    lexer
        .tokenize()
        .asSequence()
        .filterNot { it is NewlineToken }
        .filterNot { it is PlainTextToken && it.data.text.isBlank() }
        .map { it to it.accept(parser) }
        .onEach { (token, node) ->
            if (assertType) {
                assertIs<T>(
                    node,
                    message = "From token:\n${token.data.text}\n\n",
                )
            }
        }.map { it.second }
        .filterIsInstance<T>()
        .iterator()

/**
 * Attaches a mock pipeline to a context for tests only, which does not support rendering.
 * @param options options of the pipeline
 */
fun MutableContext.attachMockPipeline(options: PipelineOptions = PipelineOptions()) =
    Pipeline(
        this,
        options,
        libraries = emptySet(),
        renderer = { _, _ -> throw UnsupportedOperationException() },
    )
