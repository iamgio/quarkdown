package com.quarkdown.server.preview

import kotlin.test.Test
import kotlin.test.assertContains

/**
 * Tests for [HtmlLivePreviewWrapper] focused on URL plumbing through the JTE template.
 */
class HtmlLivePreviewWrapperTest {
    @Test
    fun `default endpoint root produces a same-origin absolute reload URL`() {
        val rendered = HtmlLivePreviewWrapper(srcFile = "/index.html").render()
        assertContains(rendered, $$"new EventSource(`/${endpoint}`)")
    }

    @Test
    fun `custom endpoint root is forwarded verbatim into the reload URL`() {
        val rendered =
            HtmlLivePreviewWrapper(
                srcFile = "/index.html",
                endpointRoot = "../",
            ).render()
        assertContains(rendered, $$"new EventSource(`../${endpoint}`)")
    }
}
