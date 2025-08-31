package com.quarkdown.server.endpoints

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import java.io.File

/**
 * Default file to serve if none is specified
 */
private const val DEFAULT_FILE = "index.html"

/**
 * Handler of the live preview endpoint (`/live/<file>`) which serves static files relative to a target file or directory.
 * Additionally, for HTML files, it serves a wrapper HTML that includes a WebSocket script + iframe for live previewing.
 * @param origin the root directory from which files are served
 */
class LivePreviewEndpoint(
    private val origin: File,
) {
    /**
     * Resolves the target file based on the request parameters.
     * If no specific file is requested, defaults to [DEFAULT_FILE].
     * @param call the application call
     * @return the resolved file, even if it does not exist
     */
    private fun getTargetFile(call: ApplicationCall): File {
        val segments = call.parameters.getAll("file")?.takeIf { it.isNotEmpty() } ?: listOf(DEFAULT_FILE)
        val path = segments.joinToString("/") // e.g. file.html or subdir/file.html
        return origin.resolve(path)
    }

    /**
     * Handles a request to the live preview endpoint, by serving the requested file or a wrapper HTML for live preview.
     * @param call the application call
     * @param port the port the server is running on
     */
    suspend fun handleRequest(
        call: ApplicationCall,
        port: Int,
    ) {
        val file = getTargetFile(call)

        if (!file.exists() || !file.isFile) {
            call.respond(HttpStatusCode.NotFound, null)
            return
        }

        when {
            file.extension.lowercase() == "html" ->
                call.respondText(createHtmlWrapperText(file, port), ContentType.Text.Html)

            // Non-HTML files are served directly.
            file.exists() ->
                call.respondFile(file)
        }
    }

    private fun createHtmlWrapperText(
        targetFile: File,
        serverPort: Int,
    ): String {
        val websocketsScriptContent = LivePreviewEndpoint::class.java.getResource("/script/websockets.js")!!.readText()

        // Since we are one level deep in /live/, we need to adjust the relative path to the source file.
        val sourceFile = "../${targetFile.name}"

        val style =
            "position: fixed; top: 0; left: 0; width: 100%; height: 100%; border: none;" +
                "margin: 0; padding: 0; overflow: hidden; z-index: 999999;"

        return """
            <!DOCTYPE html>
            <html>
            <head>
            </head>
            <body>
            <iframe id="content-frame" src="$sourceFile" style="$style"></iframe>
            <script>
            $websocketsScriptContent
            
            startWebSockets("localhost:$serverPort")
            </script>
            </body>
            """.trimIndent()
    }
}
