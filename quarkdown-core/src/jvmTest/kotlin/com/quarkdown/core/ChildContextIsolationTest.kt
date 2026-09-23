package com.quarkdown.core

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.SharedContext
import com.quarkdown.core.context.SubdocumentContext
import com.quarkdown.core.document.DocumentInfo
import com.quarkdown.core.document.sub.Subdocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

/**
 * Tests for property isolation and sharing behavior in [SharedContext] and [SubdocumentContext].
 */
class ChildContextIsolationTest {
    // SharedContext isolation tests

    @Test
    fun `SharedContext shares documentInfo with parent`() {
        val parent = MutableContext()
        val child = SharedContext(parent)

        val newInfo = DocumentInfo(name = "Test")
        child.documentInfo = newInfo

        assertSame(newInfo, parent.documentInfo)
        assertSame(parent.documentInfo, child.documentInfo)
    }

    @Test
    fun `SharedContext shares libraries with parent`() {
        val parent = MutableContext()
        val child = SharedContext(parent)

        assertSame(parent.libraries, child.libraries)
    }

    @Test
    fun `SharedContext shares options with parent`() {
        val parent = MutableContext()
        val child = SharedContext(parent)

        assertSame(parent.options, child.options)
    }

    @Test
    fun `SharedContext shares attributes with parent`() {
        val parent = MutableContext()
        val child = SharedContext(parent)

        assertSame(parent.attributes, child.attributes)
    }

    @Test
    fun `SharedContext shares localizationTables with parent`() {
        val parent = MutableContext()
        val child = SharedContext(parent)

        assertSame(parent.localizationTables, child.localizationTables)
    }

    @Test
    fun `SharedContext shares mediaStorage with parent`() {
        val parent = MutableContext()
        val child = SharedContext(parent)

        assertSame(parent.mediaStorage, child.mediaStorage)
    }

    // SubdocumentContext isolation tests

    private fun createSubdocumentContext(): Triple<MutableContext, Subdocument.Resource, SubdocumentContext> {
        val parent = MutableContext()
        val subdoc = Subdocument.Resource(name = "sub", path = "/test/sub.qd", content = "")
        val child = SubdocumentContext(parent, subdoc)
        return Triple(parent, subdoc, child)
    }

    @Test
    fun `SubdocumentContext has isolated documentInfo`() {
        val (parent, _, child) = createSubdocumentContext()

        // Initially they have equal values (copied from parent)
        assertEquals(parent.documentInfo, child.documentInfo)

        // Modify child's documentInfo
        val childInfo = DocumentInfo(name = "Child Doc")
        child.documentInfo = childInfo

        // Parent should be unchanged
        assertNotEquals(parent.documentInfo, child.documentInfo)
        assertEquals(childInfo, child.documentInfo)
    }

    @Test
    fun `SubdocumentContext shares options with parent`() {
        val (parent, _, child) = createSubdocumentContext()
        assertSame(parent.options, child.options)
    }

    @Test
    fun `SubdocumentContext shares loadableLibraries with parent`() {
        val (parent, _, child) = createSubdocumentContext()
        assertSame(parent.loadableLibraries, child.loadableLibraries)
    }

    @Test
    fun `SubdocumentContext shares localizationTables with parent`() {
        val (parent, _, child) = createSubdocumentContext()
        assertSame(parent.localizationTables, child.localizationTables)
    }

    @Test
    fun `SubdocumentContext shares sharedSubdocumentsData with parent`() {
        val (parent, _, child) = createSubdocumentContext()
        assertSame(parent.sharedSubdocumentsData, child.sharedSubdocumentsData)
    }

    @Test
    fun `SubdocumentContext has own subdocument`() {
        val (parent, subdoc, child) = createSubdocumentContext()
        assertSame(Subdocument.Root, parent.subdocument)
        assertSame(subdoc, child.subdocument)
    }
}
