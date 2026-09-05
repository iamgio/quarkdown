package com.quarkdown.interaction.cdp

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * An error returned by the browser in response to a Chrome DevTools Protocol command.
 */
class CdpException(
    message: String,
) : RuntimeException(message)

/**
 * Builds the JSON text of a Chrome DevTools Protocol command.
 * @param id sequential identifier used to match the response
 * @param method protocol method, e.g. `Page.printToPDF`
 * @param params optional method parameters
 * @return the serialized command, ready to be sent over the debugger WebSocket
 */
internal fun buildCdpRequest(
    id: Int,
    method: String,
    params: JsonObject? = null,
): String =
    buildJsonObject {
        put("id", id)
        put("method", method)
        params?.let { put("params", it) }
    }.toString()

/**
 * Parses an incoming Chrome DevTools Protocol message.
 * @param text raw JSON text of the message
 * @param expectedId identifier of the command awaiting a response
 * @return the `result` object if the message responds to [expectedId],
 *         or `null` if the message is an event or responds to a different command
 * @throws CdpException if the message responds to [expectedId] with an error
 */
internal fun parseCdpResponse(
    text: String,
    expectedId: Int,
): JsonObject? {
    val message = Json.parseToJsonElement(text).jsonObject
    if (message["id"]?.jsonPrimitive?.content != expectedId.toString()) return null

    message["error"]?.jsonObject?.let { error ->
        throw CdpException("CDP error: ${error["message"]?.jsonPrimitive?.content}")
    }
    return message["result"]?.jsonObject ?: JsonObject(emptyMap())
}
