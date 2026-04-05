
import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask
import groovy.json.JsonOutput

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.3.10"
    id("com.github.node-gradle.node") version "7.1.0"
    id("io.miret.etienne.sass") version "1.6.0"
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(testFixtures(project(":quarkdown-core")))
    testImplementation("org.apache.pdfbox:pdfbox:3.0.6")
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-interaction"))
    implementation(project(":quarkdown-server"))
    implementation(project(":quarkdown-plaintext")) // For search index generation
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
}

// Third-party library bundling
//
// Libraries from node_modules are copied into src/main/resources/render/lib/
// at Gradle build time, so they are bundled in the JAR and copied to the
// Quarkdown output directory at compilation time.
//
// A nested third-party-manifest.json is generated at the end so that the
// Kotlin runtime can enumerate JAR resources (JAR directories are not listable).
//
// To add a new library:
// 1. Add the npm package to package.json
// 2. Add a LibrarySpec entry to `librariesToBundle`
// 3. Add a ThirdPartyLibrary subclass in ThirdPartyLibrary.kt

val libDir = projectDir.resolve("src/main/resources/render/lib")
val nodeModules = projectDir.resolve("node_modules")
val scssThirdParty = projectDir.resolve("src/main/scss/thirdparty")

/**
 * Declarative specification for copying a library from `node_modules` into `lib/`.
 * Multiple specs may share the same [target] (their files are merged into one directory).
 *
 * @param target output directory name under `lib/`
 * @param source path relative to `node_modules/`
 * @param includes glob patterns to select files (empty = everything)
 */
data class LibrarySpec(
    val target: String,
    val source: String,
    val includes: List<String> = emptyList(),
)

/**
 * All libraries to copy from `node_modules` into JAR resources.
 * To add a new library, append a `LibrarySpec` entry here.
 */
val librariesToBundle =
    listOf(
        LibrarySpec("bootstrap-icons", "bootstrap-icons/font", listOf("bootstrap-icons.min.css", "fonts/bootstrap-icons.woff2")),
        LibrarySpec("highlight.js", "highlightjs-line-numbers.js/dist", listOf("highlightjs-line-numbers.min.js")),
        LibrarySpec("highlight.js", "highlightjs-copy/dist", listOf("highlightjs-copy.min.js", "highlightjs-copy.min.css")),
        LibrarySpec("katex", "katex/dist", listOf("katex.min.css", "katex.min.js", "fonts/*.woff2")),
        LibrarySpec("mermaid", "mermaid/dist", listOf("mermaid.min.js")),
        LibrarySpec("reveal.js", "reveal.js/dist", listOf("reveal.js", "reset.css", "reveal.css", "theme/white.css", "plugin/notes.js")),
        LibrarySpec("pagedjs", "pagedjs/dist", listOf("paged.polyfill.js")),
    )

/**
 * @fontsource font sets, keyed by layout theme name.
 * Each entry lists the @fontsource package names to include.
 * All latin-subset woff2 variants are auto-discovered from each package's `files/` directory,
 * and `@font-face` declarations are derived from the `{font}-latin-{weight}-{style}.woff2` naming convention.
 */
val fontsourceSets =
    mapOf(
        "fonts/minimal" to listOf("lato", "inter", "noto-sans-mono"),
        "fonts/beamer" to listOf("source-sans-pro", "fira-sans", "noto-sans-mono"),
    )

/**
 * Highlight.js theme CSS files to copy as SCSS partials for compile-time inlining into color themes.
 * Key: source path relative to `node_modules/highlight.js/styles/`.
 * Value: output SCSS partial filename (without leading `_` or `.scss` extension).
 */
val hljsScssPartials =
    mapOf(
        "default.css" to "hljs-default",
        "atom-one-dark.min.css" to "hljs-atom-one-dark",
    )

/**
 * Bundles highlight.js with all common languages into a single browser-ready file,
 * since the npm package is modular and does not include a pre-built bundle.
 */
val bundleHighlightJs =
    tasks.register<NpxTask>("bundleHighlightJs") {
        group = "build"
        description = "Bundles highlight.js into a single browser-ready file"
        dependsOn(tasks.npmInstall)

        command.set("esbuild")
        args.set(
            listOf(
                "node_modules/highlight.js/lib/common.js",
                "--bundle",
                "--platform=browser",
                "--format=iife",
                "--global-name=hljs",
                "--minify",
                "--outfile=${libDir.resolve("highlight.js/highlight.min.js")}",
            ),
        )
    }

/**
 * Main bundling task: copies libraries, bundles fontsource fonts,
 * copies hljs SCSS partials, and generates the nested manifest JSON.
 */
val bundleThirdParty =
    tasks.register<DefaultTask>("bundleThirdParty") {
        group = "build"
        description = "Bundles third-party libraries from node_modules into JAR resources"
        dependsOn(tasks.npmInstall)
        dependsOn(bundleHighlightJs)

        doLast {
            librariesToBundle.forEach { (target, source, includes) ->
                copy {
                    from(nodeModules.resolve(source)) {
                        if (includes.isNotEmpty()) include(includes)
                    }
                    into(libDir.resolve(target))
                }
            }

            fontsourceSets.forEach { (targetSubdir, fontNames) ->
                bundleFontsource(targetSubdir, fontNames)
            }

            scssThirdParty.mkdirs()
            hljsScssPartials.forEach { (source, partialName) ->
                nodeModules
                    .resolve("highlight.js/styles/$source")
                    .copyTo(scssThirdParty.resolve("_$partialName.scss"), overwrite = true)
            }

            generateNestedManifest()
        }
    }

/**
 * Auto-discovers latin-subset woff2 files from the given @fontsource packages,
 * copies them into the given [targetSubdir] under `lib/`, and generates a `fonts.css`
 * with `@font-face` declarations derived from the file naming convention.
 *
 * @fontsource files follow the pattern `{font}-latin-{weight}-{style}.woff2`,
 * from which font-weight and font-style are extracted automatically.
 */
fun bundleFontsource(
    targetSubdir: String,
    fontNames: List<String>,
) {
    val targetDir = libDir.resolve(targetSubdir)
    val cssBuilder = StringBuilder()

    // Matches e.g. "lato-latin-400-normal.woff2" -> (lato, 400, normal)
    val variantPattern = Regex("""^(.+)-latin-(\d+)-(\w+)\.woff2$""")

    for (fontName in fontNames) {
        val filesDir = nodeModules.resolve("@fontsource/$fontName/files")
        val family = fontName.split("-").joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }

        filesDir.listFiles()
            ?.filter { it.name.startsWith("$fontName-latin-") && it.extension == "woff2" }
            ?.sortedBy { it.name }
            ?.forEach { sourceFile ->
                val match = variantPattern.matchEntire(sourceFile.name) ?: return@forEach
                val (_, weight, style) = match.destructured

                sourceFile.copyTo(targetDir.resolve(sourceFile.name), overwrite = true)

                cssBuilder.appendLine(
                    """
                    |/* $fontName-latin-$weight-$style */
                    |@font-face {
                    |  font-family: '$family';
                    |  font-style: $style;
                    |  font-display: swap;
                    |  font-weight: $weight;
                    |  src: url('./${sourceFile.name}') format('woff2');
                    |}
                    """.trimMargin(),
                )
                cssBuilder.appendLine()
            }
    }

    targetDir.resolve("fonts.css").writeText(cssBuilder.toString())
}

/**
 * Generates `third-party-manifest.json` with one top-level key per library.
 *
 * Each key maps to a nested JSON tree of that library's internal directory structure,
 * with files at each level listed under `_files`:
 *
 * ```json
 * {
 *   "katex": {
 *     "_files": ["katex.min.css", "katex.min.js"],
 *     "fonts": { "_files": ["KaTeX_Main-Regular.woff2"] }
 *   },
 *   "fonts/latex": {
 *     "_files": ["fonts.css", "ComputerModern-Serif-Regular.woff"]
 *   }
 * }
 * ```
 *
 * A directory that contains files is a library (e.g. `katex`). A directory that only contains
 * subdirectories is not a library itself; its children are explored recursively until a library
 * is found (e.g. `fonts/` has no files, so `fonts/latex` becomes a library entry).
 * This allows the Kotlin runtime to look up libraries by name without path traversal.
 */
fun generateNestedManifest() {
    fun dirToJson(dir: File): Map<String, Any> =
        buildMap {
            dir.listFiles()
                ?.filter { it.isFile }
                ?.map { it.name }
                ?.sorted()
                ?.takeIf { it.isNotEmpty() }
                ?.let { put("_files", it) }

            dir.listFiles()
                ?.filter { it.isDirectory }
                ?.sortedBy { it.name }
                ?.forEach { put(it.name, dirToJson(it)) }
        }

    val manifest = linkedMapOf<String, Any>()

    fun collectLibraries(dir: File, prefix: String) {
        dir.listFiles()
            ?.filter { it.isDirectory }
            ?.sortedBy { it.name }
            ?.forEach { child ->
                val key = if (prefix.isEmpty()) child.name else "$prefix/${child.name}"
                val hasFiles = child.listFiles()?.any { it.isFile } ?: false

                if (hasFiles) {
                    manifest[key] = dirToJson(child)
                } else {
                    collectLibraries(child, key)
                }
            }
    }

    collectLibraries(libDir, "")

    libDir.resolve("third-party-manifest.json")
        .writeText(JsonOutput.prettyPrint(JsonOutput.toJson(manifest)))
}

// SCSS and TypeScript bundling

tasks.compileSass {
    sourceDir = projectDir.resolve("src/main/scss")
    outputDir = projectDir.resolve("src/main/resources/render/theme")
    dependsOn(bundleThirdParty) // hljs SCSS partials must be copied before SASS compilation
}

val bundleTypeScript =
    tasks.register<NpxTask>("bundleTypeScript") {
        group = "build"
        description = "Bundles TypeScript files using esbuild"

        dependsOn(tasks.npmInstall)

        command.set("esbuild")
        args.set(
            listOf(
                "src/main/typescript/index.ts",
                "--bundle",
                "--platform=browser",
                "--format=iife",
                "--outfile=src/main/resources/render/script/quarkdown.js",
                "--external:reveal.js",
                "--external:pagedjs",
                "--external:katex",
                "--external:highlight.js",
                "--external:mermaid",
                "--sourcemap",
            ),
        )
    }

tasks.processResources {
    dependsOn(tasks.compileSass)
    dependsOn(bundleTypeScript)
    dependsOn(bundleThirdParty)
}

// Tests

val npmUnitTest =
    tasks.register<NpmTask>("npmTest") {
        group = "verification"
        description = "Runs npm tests"
        dependsOn(tasks.npmInstall)
        args.set(listOf("run", "test:run"))
    }

tasks.test {
    dependsOn(npmUnitTest)
}

val installPlaywrightBrowsers =
    tasks.register<NpxTask>("installPlaywrightBrowsers") {
        group = "verification"
        description = "Installs Playwright browsers"
        dependsOn(tasks.npmInstall)
        command.set("playwright")
        args.set(listOf("install", "--with-deps", "chromium"))
    }

val npmE2eTest =
    tasks.register<NpxTask>("e2eTest") {
        group = "verification"
        description = "Runs end-to-end tests"
        dependsOn(installPlaywrightBrowsers)
        command.set("playwright")

        val shard = project.findProperty("shard")?.toString()
        val totalShards = project.findProperty("totalShards")?.toString()

        val argsList = mutableListOf("test")
        if (shard != null && totalShards != null) {
            argsList.add("--shard=$shard/$totalShards")
        }
        args.set(argsList)
    }
