package com.quarkdown.core

import com.quarkdown.core.document.sub.Subdocument
import kotlin.test.Test
import kotlin.test.assertEquals

class SubdocumentUniqueNameTest {
    @Test
    fun `unique name is name at path hash`() {
        val subdocument = Subdocument.Resource(name = "chapter", path = "docs/chapter.qd", content = "")
        assertEquals("chapter@${"docs/chapter.qd".hashCode()}", subdocument.uniqueName)
    }
}
