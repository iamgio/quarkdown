package com.quarkdown.interaction.cdp

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Tests for the Chrome DevTools Protocol primitives used by PDF generation.
 */
class CdpPrimitivesTest {
    @Test
    fun `request carries id, method and params`() {
        val json = buildCdpRequest(3, "Page.navigate", buildJsonObject { put("url", "http://localhost/") })
        assertEquals(
            """{"id":3,"method":"Page.navigate","params":{"url":"http://localhost/"}}""",
            json,
        )
    }

    @Test
    fun `response with matching id returns result`() {
        val result = parseCdpResponse("""{"id":3,"result":{"frameId":"abc"}}""", expectedId = 3)
        assertEquals("abc", result!!["frameId"]!!.jsonPrimitive.content)
    }

    @Test
    fun `event and mismatched id are ignored`() {
        assertNull(parseCdpResponse("""{"method":"Page.frameNavigated","params":{}}""", expectedId = 3))
        assertNull(parseCdpResponse("""{"id":2,"result":{}}""", expectedId = 3))
    }

    @Test
    fun `error response throws`() {
        assertFailsWith<CdpException> {
            parseCdpResponse("""{"id":3,"error":{"code":-32601,"message":"not found"}}""", expectedId = 3)
        }
    }

    @Test
    fun `devtools ws url is extracted from log line`() {
        assertEquals(
            "ws://127.0.0.1:9222/devtools/browser/uuid",
            DevToolsEndpoint.wsUrlFromLogLine("DevTools listening on ws://127.0.0.1:9222/devtools/browser/uuid"),
        )
        assertNull(DevToolsEndpoint.wsUrlFromLogLine("some unrelated line"))
    }

    @Test
    fun `json list url is derived from ws url`() {
        assertEquals(
            "http://127.0.0.1:9222/json/list",
            DevToolsEndpoint.jsonListUrl("ws://127.0.0.1:9222/devtools/browser/uuid"),
        )
    }
}
