package com.quarkdown.test

import com.quarkdown.core.ast.attributes.presence.hasMermaidDiagram
import com.quarkdown.core.context.subdocument.subdocumentGraph
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.test.util.execute
import com.quarkdown.test.util.getSubResources
import com.quarkdown.test.util.getSubdocumentResource
import com.quarkdown.test.util.getSubdocumentResourceCount
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val NON_EXISTENT_FUNCTION = "somenonexistentfunction"

/**
 * Tests for subdocument generation.
 */
class SubdocumentTest {
    private fun subdoc(
        name: String,
        content: String,
    ) = Subdocument.Resource(
        name = name,
        path = name,
        content = content,
    )

    private val simpleSubdoc = subdoc("subdoc1", content = "Content")
    private val referenceToParentSubdoc = subdoc("subdoc2", content = ".$NON_EXISTENT_FUNCTION")
    private val definitionSubdoc = subdoc("subdoc3", content = ".function {$NON_EXISTENT_FUNCTION}\n\thello")
    private val thirdPartySubdoc = subdoc("subdoc4", content = ".mermaid\n\tgraph TD\n\t\tA-->B")
    private val echoDocumentNameSubdoc = subdoc("subdoc5", content = ".docname")
    private val modifyAndEchoDocumentNameSubdoc = subdoc("subdoc6", content = ".docname {Changed name}\n\n.docname")

    @Test
    fun `root to subdocument`() {
        execute(
            "",
            subdocumentGraph = { it.addVertex(simpleSubdoc).addEdge(Subdocument.Root, simpleSubdoc) },
            outputResourceHook = { group ->
                val resource = getSubdocumentResource(group, simpleSubdoc, this)
                assertContains(resource.content, "<html>")
                assertEquals(2, subdocumentGraph.vertices.size)
                assertEquals(2, getSubdocumentResourceCount(group))
                assertContains(getSubResources(group).map { it.name }, simpleSubdoc.name)
            },
        ) {}
    }

    @Test
    fun `collision-proof subdocument name`() {
        execute(
            "",
            subdocumentGraph = { it.addVertex(simpleSubdoc).addEdge(Subdocument.Root, simpleSubdoc) },
            minimizeSubdocumentCollisions = true,
            outputResourceHook = { group ->
                val resources = getSubResources(group).map { it.name }
                assertContains(resources, simpleSubdoc.uniqueName)
                assertFalse(simpleSubdoc.name in resources)
            },
        ) {}
    }

    @Test
    fun `context should be shared to subdocument`() {
        execute(
            """
            .doctype {paged}
            
            .function {$NON_EXISTENT_FUNCTION}
              hello
            """.trimIndent(),
            subdocumentGraph = {
                it.addVertex(referenceToParentSubdoc).addEdge(Subdocument.Root, referenceToParentSubdoc)
            },
            outputResourceHook = { group ->
                val resource = getSubdocumentResource(group, referenceToParentSubdoc, this)
                assertEquals(DocumentType.PAGED, documentInfo.type)
                assertContains(resource.content, "paged")
            },
        ) {}
    }

    @Test
    fun `context should not be shared from subdocument to parent`() {
        execute(
            ".doctype {paged}",
            subdocumentGraph = { it.addVertex(definitionSubdoc).addEdge(Subdocument.Root, definitionSubdoc) },
            outputResourceHook = {
                assertEquals(DocumentType.PAGED, documentInfo.type)
                assertNull(getFunctionByName(NON_EXISTENT_FUNCTION))
            },
        ) {}
    }

    @Test
    fun `third-party presence should not be shared from subdocument to parent`() {
        execute(
            "",
            subdocumentGraph = { it.addVertex(thirdPartySubdoc).addEdge(Subdocument.Root, thirdPartySubdoc) },
            outputResourceHook = {
                assertFalse(attributes.hasMermaidDiagram)

                // Root should not have the mermaid script, the subdocument should.
                val rootResource = getSubdocumentResource(it, Subdocument.Root, this)
                val subdocResource = getSubdocumentResource(it, thirdPartySubdoc, this)
                assertFalse(rootResource.content.contains("mermaid.min.js"))
                assertContains(subdocResource.content, "mermaid.min.js")
            },
        ) {}
    }

    @Test
    fun `subdocument inherits parent's document info`() {
        execute(
            ".docname {My doc}",
            subdocumentGraph = {
                it.addVertex(echoDocumentNameSubdoc).addEdge(Subdocument.Root, echoDocumentNameSubdoc)
            },
            outputResourceHook = { group ->
                val subdocResource = getSubdocumentResource(group, echoDocumentNameSubdoc, this)
                assertEquals("My doc", group?.name)
                assertEquals("My doc", documentInfo.name)
                assertContains(subdocResource.content, "<title>My doc</title>")
            },
        ) {}
    }

    @Test
    fun `subdocument should not share document info modifications with parent`() {
        execute(
            ".docname {Parent doc}",
            subdocumentGraph = {
                it.addVertex(modifyAndEchoDocumentNameSubdoc).addEdge(Subdocument.Root, modifyAndEchoDocumentNameSubdoc)
            },
            outputResourceHook = { group ->
                val mainResource = getSubdocumentResource(group, Subdocument.Root, this)
                val subdocResource = getSubdocumentResource(group, modifyAndEchoDocumentNameSubdoc, this)
                assertEquals("Parent doc", group?.name)
                assertEquals("Parent doc", documentInfo.name)
                assertContains(mainResource.content, "<title>Parent doc</title>")
                assertContains(subdocResource.content, "<title>Changed name</title>")
            },
        ) {}
    }

    @Test
    fun `simple subdocument from file`() {
        arrayOf(
            "The link is: [1](subdoc/simple-1.qd)",
            "The link is: .subdocument {subdoc/simple-1.qd} label:{1}",
        ).forEach { source ->
            execute(
                source,
                outputResourceHook = {
                    println(subdocumentGraph.vertices)
                    assertEquals(2, subdocumentGraph.vertices.size)
                    assertEquals(2, getSubdocumentResourceCount(it))
                },
            ) {
                if (subdocument == Subdocument.Root) {
                    assertEquals("<p>The link is: <a href=\"./simple-1\">1</a></p>", it)
                }
            }
        }
    }

    @Test
    fun `empty label subdocument from file`() {
        arrayOf(
            "The link is: [](subdoc/simple-1.qd)",
            "The link is: .subdocument {subdoc/simple-1.qd}",
        ).forEach { source ->
            execute(
                source,
                outputResourceHook = {
                    assertEquals(2, subdocumentGraph.vertices.size)
                    assertEquals(2, getSubdocumentResourceCount(it))
                },
            ) {
                if (subdocument == Subdocument.Root) {
                    assertEquals("<p>The link is: <a href=\"./simple-1\"></a></p>", it)
                }
            }
        }
    }

    @Test
    fun `stdlib call in subdocument from file`() {
        arrayOf(
            "[Lorem](subdoc/stdlib-call.qd)",
            ".subdocument {subdoc/stdlib-call.qd} label:{Lorem}",
        ).forEach { source ->
            execute(
                source,
                outputResourceHook = {
                    assertEquals(2, subdocumentGraph.vertices.size)
                    assertEquals(2, getSubdocumentResourceCount(it))
                },
            ) {
                if (subdocument != Subdocument.Root) {
                    assertContains(it, "Lorem ipsum")
                }
            }
        }
    }

    @Test
    fun `stdlib call in included file from subdocument`() {
        arrayOf(
            "[Include](subdoc/include-stdlib.qd)",
            ".subdocument {subdoc/include-stdlib.qd} label:{Include}",
        ).forEach { source ->
            execute(
                source,
                outputResourceHook = {
                    assertEquals(2, subdocumentGraph.vertices.size)
                    assertEquals(2, getSubdocumentResourceCount(it))
                },
            ) {
                if (subdocument != Subdocument.Root) {
                    assertContains(it, "Lorem ipsum")
                }
            }
        }
    }

    @Test
    fun `root to gateway to 1 and 2`() {
        arrayOf(
            "[Gateway](subdoc/gateway.qd)",
            ".subdocument {subdoc/gateway.qd} label:{Gateway}",
        ).forEach { source ->
            execute(
                source,
                outputResourceHook = {
                    assertEquals(4, subdocumentGraph.vertices.size)
                    assertEquals(4, getSubdocumentResourceCount(it))
                },
            ) {}
        }
    }

    @Test
    fun `circular, root to 1 to 2 to 1`() {
        arrayOf(
            "[1](subdoc/circular-1.qd)",
            ".subdocument {subdoc/circular-1.qd} label:{1}",
        ).forEach { source ->
            execute(
                source,
                outputResourceHook = {
                    assertEquals(3, subdocumentGraph.vertices.size)
                    assertEquals(3, getSubdocumentResourceCount(it))
                },
            ) {}
        }
    }

    @Test
    fun `recursive, root to 1 recursively`() {
        arrayOf(
            "[1](subdoc/recursive.qd)",
            ".subdocument {subdoc/recursive.qd} label:{1}",
        ).forEach { source ->
            execute(
                source,
                outputResourceHook = {
                    assertEquals(2, subdocumentGraph.vertices.size)
                    assertEquals(2, getSubdocumentResourceCount(it))
                },
            ) {}
        }
    }

    @Test
    fun `subdocument link should mark current subdocument and  account for non-root path`() {
        arrayOf(
            "[Document](subdoc/nav-includer.qd)",
            ".subdocument {subdoc/nav-includer.qd} label:{Document}",
        ).forEach { source ->
            execute(
                source,
                outputResourceHook = {
                    assertEquals(4, subdocumentGraph.vertices.size)
                    assertEquals(4, getSubdocumentResourceCount(it))
                },
            ) {
                if (subdocument.name == "nav-includer") {
                    assertEquals(
                        "<ul><li><a href=\"../simple-1\">1</a></li>" +
                            "<li><a href=\"../simple-2\">2</a></li>" +
                            "<li><a href=\"../nav-includer\" aria-current=\"page\">3</a></li></ul>",
                        it,
                    )
                }
            }
        }
    }

    @Test
    fun `all from directory`() {
        execute(
            """
            .foreach {.listfiles {subdoc} sortby:{name}}
                path:
                .path::subdocument label:{.path::filename extension:{no}}
            """.trimIndent(),
            outputResourceHook = {
                val files = File(fileSystem.workingDirectory, "subdoc").listFiles()!!
                assertEquals(files.size + 1, subdocumentGraph.vertices.size) // +1 for root

                files.forEach { file ->
                    val subdocName = file.nameWithoutExtension
                    assertTrue(subdocumentGraph.vertices.any { vertex -> vertex.name.startsWith(subdocName) })
                }
            },
        ) {}
    }
}
