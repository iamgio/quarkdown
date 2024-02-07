package eu.iamgio.quarkdown

/**
 * Reads the text content of a test resource.
 * @param path path to the test resource
 * @return text of the test resource
 * @throws IllegalAccessError if the resource was not found
 */
fun readSource(path: String) =
    LexerTest::class.java.getResourceAsStream(path)?.bufferedReader()?.readText()
        ?: throw IllegalAccessError("No resource $path")
