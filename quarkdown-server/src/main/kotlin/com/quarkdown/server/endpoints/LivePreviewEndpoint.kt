package com.quarkdown.server.endpoints

import com.quarkdown.server.preview.HtmlLivePreviewWrapper
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
 * Additionally, for HTML files, it serves a wrapper HTML that includes a reload event-stream subscription + iframe for live previewing.
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
     */
    suspend fun handleRequest(call: ApplicationCall) {
        val file = getTargetFile(call)

        if (!file.exists() || !file.isFile) {
            call.respondText("Not Found", status = HttpStatusCode.NotFound)
            return
        }

        when {
            file.extension.lowercase() == "html" -> {
                call.respondText(createHtmlWrapperText(file), ContentType.Text.Html)
            }

            // Non-HTML files are served directly.
            else -> {
                call.respondFile(file)
            }
        }
    }

    private fun createHtmlWrapperText(targetFile: File): String {
        val sourceFile = "/${targetFile.relativeTo(origin).invariantSeparatorsPath}"
        return HtmlLivePreviewWrapper(srcFile = sourceFile).render()
    }
}
