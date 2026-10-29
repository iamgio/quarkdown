package com.quarkdown.cli

import com.github.ajalt.clikt.testing.test
import com.quarkdown.cli.creator.FakePackageRepository
import com.quarkdown.cli.creator.command.CreateProjectCommand
import com.quarkdown.cli.creator.templatePackage
import com.quarkdown.core.util.normalizeLineSeparators
import com.quarkdown.pm.model.AtomManifest
import com.quarkdown.pm.model.PackageFile
import com.quarkdown.pm.model.PackageManifest
import com.quarkdown.pm.model.RepositoryPackage
import com.quarkdown.pm.model.TemplateManifest
import com.quarkdown.pm.net.RemoteFetchException
import io.ktor.http.Url
import org.junit.Test
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests for [CreateProjectCommand]
 */
class ProjectCreatorCommandTest : TempDirectory() {
    private val cvTemplate =
        TemplateManifest(
            doctype = "paged",
            content = "template.qd",
            additionalFiles = listOf("sections/experience.qd", "references.bib"),
        )

    private val cvFiles =
        mapOf(
            "main.qd" to "# CV library",
            "template.qd" to "# My CV\n\n.experience",
            "sections/experience.qd" to "# Experience",
            "references.bib" to "@book{}",
        )

    private val repositories =
        FakePackageRepository(
            mapOf(
                "@atoms/cv:1.0.0" to templatePackage("cv", template = cvTemplate, files = cvFiles),
                "@atoms/themed-cv:1.0.0" to templatePackage("themed-cv", template = cvTemplate.copy(enforceTheme = true), files = cvFiles),
                "@atoms/plain:1.0.0" to templatePackage("plain", files = cvFiles),
            ),
        ).asAtoms()

    private val command = CreateProjectCommand(repositories)

    private fun mainFile(directory: File = super.directory) =
        directory
            .resolve("main.qd")
            .readText()
            .normalizeLineSeparators()
            .toString()

    /**
     * Runs the command with the template option and the given extra arguments, with no `--type`.
     */
    private fun createFromTemplate(
        vararg additionalArgs: String,
        reference: String = "@atoms/cv:1.0.0",
    ) = command.test(
        directory.resolve(".").absolutePath,
        "--template",
        reference,
        "--name",
        "cv",
        "--authors",
        "Jane",
        "--description",
        "",
        "--lang",
        "",
        "--main-file",
        "main",
        *additionalArgs,
    )

    @BeforeTest
    fun setup() {
        super.reset()
    }

    /**
     * Runs the command with the given arguments and verifies the created files.
     * @param additionalArgs additional command line arguments to pass
     * @param fixedMainFileName whether to use a fixed main file name
     * @param directory directory to create the project in
     * @param includeDescription whether to include the description argument
     * @param includeKeywords whether to include the keywords argument
     * @return content of the main file
     */
    private fun test(
        additionalArgs: Array<String> = emptyArray(),
        fixedMainFileName: Boolean = true,
        directory: File = super.directory,
        includeDescription: Boolean = true,
        includeKeywords: Boolean = true,
    ): String {
        // resolve(".") tests the canonical path instead of the actual one.
        command.test(
            directory.resolve(".").absolutePath,
            "--name",
            "test",
            "--authors",
            "Aaa, Bbb,Ccc",
            "--type",
            "plain",
            "--description",
            if (includeDescription) "A test document" else "",
            "--lang",
            "en",
            "--color-theme",
            "darko",
            "--layout-theme",
            "latex",
            *additionalArgs,
            *if (includeKeywords) arrayOf("--keywords", "testing,slides, quarkdown") else emptyArray(),
            *if (fixedMainFileName) arrayOf("--main-file", "main") else emptyArray(),
        )
        assertTrue(directory.exists())

        val mainFileName = "main.qd"

        assertTrue(mainFileName in directory.listFiles()!!.map { it.name })

        val main =
            directory
                .listFiles()!!
                .first { it.name == mainFileName }
                .readText()
                .normalizeLineSeparators()
                .toString()
        assertTrue(main.startsWith(".docname {test}"))
        if (includeDescription) {
            assertTrue(".docdescription {A test document}" in main)
        }
        if (includeKeywords) {
            assertTrue(".dockeywords\n  - testing\n  - slides\n  - quarkdown" in main)
        }
        assertTrue("- Aaa" in main)
        assertTrue("- Bbb" in main)
        assertTrue("- Ccc" in main)
        assertTrue(".doctype {plain}" in main)
        assertTrue(".doclang {English}" in main)
        assertTrue(".theme {darko} layout:{latex}" in main)

        return main
    }

    @Test
    fun default() {
        test()
        assertEquals(2, directory.listFiles()!!.size)
    }

    @Test
    fun `default with relative name`() {
        test(fixedMainFileName = false)
        assertEquals(2, directory.listFiles()!!.size)
    }

    @Test
    fun `default in new directory`() {
        val dir = File(super.directory, "subdir")
        test(directory = dir)
        assertEquals(2, dir.listFiles()!!.size)
    }

    @Test
    fun `default empty`() {
        test(arrayOf("--empty"))
        assertEquals(1, directory.listFiles()!!.size)
    }

    @Test
    fun `empty description`() {
        val main = test(includeDescription = false)
        assertTrue(".docdescription" !in main)
    }

    @Test
    fun `no keywords`() {
        val main = test(includeKeywords = false)
        assertTrue(".dockeywords" !in main)
    }

    @Test
    fun slides() {
        command.test(
            directory.resolve(".").absolutePath,
            "--name",
            "test",
            "--authors",
            "Aaa",
            "--type",
            "slides",
            "--description",
            "A presentation",
            "--lang",
            "",
            "--main-file",
            "main",
        )
        assertTrue(directory.exists())

        // Slides projects supply the main file and the image assets.
        val fileNames = directory.listFiles()!!.map { it.name }
        assertEquals(setOf("main.qd", "image"), fileNames.toSet())

        val main =
            directory
                .resolve("main.qd")
                .readText()
                .normalizeLineSeparators()
                .toString()
        assertContains(main, ".doctype {slides}")
        // Default theme for slides.
        assertContains(main, ".theme {paperwhite} layout:{focus}")
        // Slides starter content.
        assertContains(main, ".footer")
        assertContains(main, "# .docname")
        assertContains(main, ".text {.docdescription} variant:{smallcaps}")
        assertContains(main, "\n## First slide\n")
        assertContains(main, "\n## Second slide\n")
        assertContains(main, "image/logo.png")
    }

    @Test
    fun docs() {
        command.test(
            directory.resolve(".").absolutePath,
            "--name",
            "test",
            "--authors",
            "",
            "--type",
            "docs",
            "--description",
            "",
            "--lang",
            "",
            "--main-file",
            "main",
        )
        assertTrue(directory.exists())

        val fileNames = directory.listFiles()!!.map { it.name }

        // Docs projects create: main.qd, _setup.qd, _nav.qd, page-1.qd, page-2.qd, page-3.qd
        assertTrue("main.qd" in fileNames)
        assertTrue("_setup.qd" in fileNames)
        assertTrue("_nav.qd" in fileNames)
        assertTrue("page-1.qd" in fileNames)
        assertTrue("page-2.qd" in fileNames)
        assertTrue("page-3.qd" in fileNames)

        val setup = directory.resolve("_setup.qd").readText()
        assertContains(setup, ".pagemargin {topleft}")
        assertTrue(".doctype" !in setup) // .include {docs} is used on each page instead.

        val main = directory.resolve("main.qd").readText()
        assertContains(main, ".docname {test}")
        assertContains(main, ".include {docs}")

        // Each page includes the `docs` library.
        for (i in 1..3) {
            assertContains(directory.resolve("page-$i.qd").readText(), ".include {docs}")
        }
    }

    @Test
    fun `template doctype is used when --type is absent`() {
        assertEquals(0, createFromTemplate().statusCode)
        val main = mainFile()
        assertContains(main, ".doctype {paged}")
        assertContains(main, ".include {@atoms/cv:1.0.0}")
        assertContains(main, "# My CV\n\n.experience")
    }

    @Test
    fun `explicit --type wins over the template doctype`() {
        assertEquals(0, createFromTemplate("--type", "slides").statusCode)
        assertContains(mainFile(), ".doctype {slides}")
    }

    @Test
    fun `template gets the default theme for its doctype`() {
        assertEquals(0, createFromTemplate().statusCode)
        assertContains(mainFile(), ".theme {paperwhite} layout:{latex}")
    }

    @Test
    fun `theme-enforcing template omits the theme`() {
        assertEquals(0, createFromTemplate(reference = "@atoms/themed-cv:1.0.0").statusCode)
        assertFalse(".theme" in mainFile())
    }

    @Test
    fun `paged template omits the page counter`() {
        assertEquals(0, createFromTemplate().statusCode)
        assertFalse(".pagemargin" in mainFile())
    }

    @Test
    fun `template with a color theme option gets the default layout for its doctype`() {
        assertEquals(0, createFromTemplate("--color-theme", "darko").statusCode)
        assertContains(mainFile(), ".theme {darko} layout:{latex}")
    }

    @Test
    fun `theme-enforcing template omits the theme even with theme options`() {
        assertEquals(
            0,
            createFromTemplate("--color-theme", "darko", "--layout-theme", "latex", reference = "@atoms/themed-cv:1.0.0").statusCode,
        )
        assertFalse(".theme" in mainFile())
    }

    @Test
    fun `template additional files are saved at their paths`() {
        assertEquals(0, createFromTemplate().statusCode)
        assertEquals("# Experience", directory.resolve("sections/experience.qd").readText())
        assertEquals("@book{}", directory.resolve("references.bib").readText())
    }

    @Test
    fun `empty with template keeps the include and drops content and files`() {
        assertEquals(0, createFromTemplate("--empty").statusCode)
        val main = mainFile()
        assertContains(main, ".include {@atoms/cv:1.0.0}")
        assertFalse("# My CV" in main)
        assertEquals(setOf("main.qd"), directory.listFiles()!!.map { it.name }.toSet())
    }

    @Test
    fun `template refusals are usage errors naming the reference`() {
        for (reference in listOf("@atoms/cv", "@nope/cv:1.0.0", "@atoms/missing:1.0.0", "@atoms/plain:1.0.0")) {
            val result = createFromTemplate(reference = reference)
            assertNotEquals(0, result.statusCode, reference)
            assertContains(result.stderr, reference)
            assertFalse(directory.resolve("main.qd").exists(), reference)
        }
    }

    @Test
    fun `prompted doctype defaults to plain on no input`() {
        val result =
            command.test(
                listOf(
                    directory.resolve(".").absolutePath,
                    "--name",
                    "t",
                    "--authors",
                    "",
                    "--description",
                    "",
                    "--lang",
                    "",
                    "--main-file",
                    "main",
                ),
                stdin = "",
            )
        assertEquals(0, result.statusCode, result.stderr)
        assertContains(mainFile(), ".doctype {plain}")
    }

    @Test
    fun `prompted doctype reads the answer`() {
        val result =
            command.test(
                listOf(
                    directory.resolve(".").absolutePath,
                    "--name",
                    "t",
                    "--authors",
                    "",
                    "--description",
                    "",
                    "--lang",
                    "",
                    "--main-file",
                    "main",
                ),
                stdin = "slides\n",
            )
        assertEquals(0, result.statusCode, result.stderr)
        assertContains(mainFile(), ".doctype {slides}")
    }

    @Test
    fun `docs template includes the package in _setup`() {
        val docsRepositories =
            FakePackageRepository(
                mapOf(
                    "@atoms/wiki:1.0.0" to
                        templatePackage(
                            "wiki",
                            template = TemplateManifest(doctype = "docs", content = "template.qd"),
                            files = mapOf("template.qd" to "Welcome to the wiki."),
                        ),
                ),
            ).asAtoms()
        val result =
            CreateProjectCommand(docsRepositories).test(
                directory.resolve(".").absolutePath,
                "--template",
                "@atoms/wiki:1.0.0",
                "--name",
                "wiki",
                "--authors",
                "",
                "--description",
                "",
                "--lang",
                "",
                "--main-file",
                "main",
            )
        assertEquals(0, result.statusCode, result.stderr)
        assertContains(directory.resolve("_setup.qd").readText(), ".include {@atoms/wiki:1.0.0}")
        val main = mainFile()
        assertContains(main, ".include {docs}")
        assertContains(main, "Welcome to the wiki.")
        assertFalse("@atoms" in main)
    }

    @Test
    fun `template additional files keep their exact names`() {
        val dotted =
            FakePackageRepository(
                mapOf(
                    "@atoms/dotted:1.0.0" to
                        templatePackage(
                            "dotted",
                            template =
                                TemplateManifest(
                                    content = "template.qd",
                                    additionalFiles = listOf(".gitignore", "assets/My Photo.png"),
                                ),
                            files = mapOf("template.qd" to "# T", ".gitignore" to "output/", "assets/My Photo.png" to "png"),
                        ),
                ),
            ).asAtoms()
        val result =
            CreateProjectCommand(dotted).test(
                directory.resolve(".").absolutePath,
                "--template",
                "@atoms/dotted:1.0.0",
                "--type",
                "plain",
                "--name",
                "t",
                "--authors",
                "",
                "--description",
                "",
                "--lang",
                "",
                "--main-file",
                "main",
            )
        assertEquals(0, result.statusCode, result.stderr)
        assertEquals("output/", directory.resolve(".gitignore").readText())
        assertEquals("png", directory.resolve("assets/My Photo.png").readText())
    }

    @Test
    fun `template refusal happens before any prompt`() {
        val result =
            command.test(
                listOf(directory.resolve(".").absolutePath, "--template", "@atoms/cv", "--main-file", "main"),
                stdin = "",
            )
        assertNotEquals(0, result.statusCode)
        assertContains(result.stderr, "@atoms/cv")
        assertFalse("Project name" in result.stdout)
    }

    @Test
    fun `a failed template download leaves no project directory`() {
        val unreachable =
            RepositoryPackage(
                PackageManifest(
                    AtomManifest(name = "offline", version = "1.0.0", entrypoint = "main.qd"),
                    TemplateManifest(content = "template.qd", additionalFiles = listOf("assets/photo.png")),
                ),
                listOf(
                    PackageFile("template.qd") { "# T".encodeToByteArray() },
                    PackageFile("assets/photo.png") { throw RemoteFetchException.NotFound(Url("https://example.com/photo.png")) },
                ),
            )
        val target = File(directory, "offline-project")
        val result =
            CreateProjectCommand(FakePackageRepository(mapOf("@atoms/offline:1.0.0" to unreachable)).asAtoms()).test(
                target.absolutePath,
                "--template",
                "@atoms/offline:1.0.0",
                "--type",
                "plain",
                "--name",
                "t",
                "--authors",
                "",
                "--description",
                "",
                "--lang",
                "",
                "--main-file",
                "main",
            )
        assertNotEquals(0, result.statusCode)
        assertContains(result.stderr, "https://example.com/photo.png")
        assertFalse(target.exists())
    }
}
