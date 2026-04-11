import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask
import groovy.json.JsonSlurper

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

tasks.compileSass {
    sourceDir = projectDir.resolve("src/main/scss")
    outputDir =
        layout.buildDirectory
            .dir("scss-compiled")
            .get()
            .asFile
}

val bundleTypeScript =
    tasks.register<NpxTask>("bundleTypeScript") {
        group = "build"
        description = "Bundles TypeScript files using esbuild"

        // Make sure npm install runs first
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
    dependsOn(bundleTypeScript)
    dependsOn(bundleThirdParty)
}

val npmUnitTest =
    tasks.register<NpmTask>("npmTest") {
        group = "verification"
        description = "Runs npm tests"
        dependsOn(tasks.npmInstall)
        args.set(listOf("run", "test:run"))
    }

tasks.test {
    dependsOn(npmUnitTest)
    systemProperty("quarkdown.html.thirdparty.dir", thirdPartyOutDir.absolutePath)
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

// Resources

val nodeModules = projectDir.resolve("node_modules")

val thirdPartyOutDir: File =
    layout.buildDirectory
        .dir("thirdparty")
        .get()
        .asFile

/**
 * Declarative specification for copying a library from `node_modules` into `build/thirdparty/`.
 * Multiple specs may share the same [target] (their files are merged into one directory).
 *
 * @param target output directory name under `build/thirdparty/`
 * @param source path relative to `node_modules/`
 * @param includes glob patterns to select files (empty = everything)
 */
data class LibrarySpec(
    val target: String,
    val source: String,
    val includes: List<String> = emptyList(),
)

/**
 * All libraries to copy from `node_modules` into the distribution.
 */
val librariesToBundle =
    listOf(
        LibrarySpec(
            "bootstrap-icons",
            "bootstrap-icons/font",
            listOf("bootstrap-icons.min.css", "fonts/bootstrap-icons.woff2"),
        ),
        LibrarySpec(
            "highlight.js",
            "highlight.js/dist",
            listOf("highlightjs.min.js"),
        ),
        LibrarySpec(
            "highlightjs-line-numbers",
            "highlightjs-line-numbers.js/dist",
            listOf("highlightjs-line-numbers.min.js"),
        ),
        LibrarySpec(
            "highlightjs-copy",
            "highlightjs-copy/dist",
            listOf("highlightjs-copy.min.js", "highlightjs-copy.min.css"),
        ),
        LibrarySpec(
            "katex",
            "katex/dist",
            listOf("katex.min.css", "katex.min.js", "fonts/*.woff2"),
        ),
        LibrarySpec(
            "mermaid",
            "mermaid/dist",
            listOf("mermaid.min.js"),
        ),
        LibrarySpec(
            "reveal.js",
            "reveal.js/dist",
            listOf("reveal.js", "reset.css", "reveal.css", "theme/white.css", "plugin/notes.js"),
        ),
        LibrarySpec("pagedjs", "pagedjs/dist", listOf("paged.polyfill.js")),
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
                "--outfile=${nodeModules.resolve("highlight.js/dist/highlightjs.min.js")}",
            ),
        )
    }

val scssCompiledDir: File =
    layout.buildDirectory
        .dir("scss-compiled")
        .get()
        .asFile

val themesOutDir: File = thirdPartyOutDir.resolve("theme")

/**
 * Parses a theme manifest file of shape `{"exports": ["node_modules/@fontsource/foo", ...]}`.
 * Returns an empty list if the file does not exist or has no `exports` key.
 */
fun readThemeExports(manifest: File): List<String> {
    if (!manifest.isFile) return emptyList()
    val json = JsonSlurper().parse(manifest) as? Map<*, *> ?: return emptyList()
    return (json["exports"] as? List<*>).orEmpty().filterIsInstance<String>()
}

// Reshapes the flat SCSS-compiled output into a per-theme directory layout under
// build/thirdparty/theme/, and copies each theme's declared export assets alongside
// its CSS file. Flat entries (global.css, locale/*.css) are preserved as-is;
// layout and color themes become <kind>/<name>/<name>.css plus exports.
val assembleThemes =
    tasks.register<DefaultTask>("assembleThemes") {
        group = "build"
        description = "Reshapes SCSS-compiled themes and copies their exported assets into build/thirdparty/theme/"
        dependsOn(tasks.compileSass)
        dependsOn(tasks.npmInstall) // exports may reference node_modules

        val scssSrcDir = projectDir.resolve("src/main/scss")
        inputs.dir(scssCompiledDir)
        inputs.dir(scssSrcDir) // picks up .json manifest changes
        outputs.dir(themesOutDir)

        doLast {
            themesOutDir.deleteRecursively()
            themesOutDir.mkdirs()

            // Flat: global.css (+ source map, if present).
            scssCompiledDir.listFiles { f -> f.isFile && f.name.startsWith("global.") }?.forEach { file ->
                copy {
                    from(file)
                    into(themesOutDir)
                }
            }

            // Flat: locale/*.css — not per-theme.
            scssCompiledDir.resolve("locale").takeIf(File::isDirectory)?.let { dir ->
                copy {
                    from(dir) { include("*.css", "*.css.map") }
                    into(themesOutDir.resolve("locale"))
                }
            }

            // Nested: <kind>/<name>/<name>.css (+ exports declared in <name>.json).
            listOf("layout", "color").forEach { kind ->
                val compiledKindDir = scssCompiledDir.resolve(kind)
                if (!compiledKindDir.isDirectory) return@forEach

                compiledKindDir
                    .listFiles { f -> f.isFile && f.name.endsWith(".css") }
                    ?.forEach { cssFile ->
                        val themeName = cssFile.nameWithoutExtension
                        val dest = themesOutDir.resolve("$kind/$themeName").also { it.mkdirs() }

                        copy {
                            from(compiledKindDir) {
                                include("$themeName.css", "$themeName.css.map")
                            }
                            into(dest)
                        }

                        readThemeExports(scssSrcDir.resolve("$kind/$themeName.json")).forEach { exportPath ->
                            val src = projectDir.resolve(exportPath)
                            if (!src.exists()) {
                                logger.warn(
                                    "assembleThemes: export path not found: $exportPath (theme $themeName)",
                                )
                                return@forEach
                            }
                            copy {
                                from(src)
                                into(dest.resolve(src.name))
                            }
                        }
                    }
            }
        }
    }

val bundleThirdParty =
    tasks.register<DefaultTask>("bundleThirdParty") {
        group = "build"
        description = "Bundles runtime third-party libraries and themes from node_modules into the distribution"
        dependsOn(tasks.npmInstall)
        dependsOn(bundleHighlightJs)
        dependsOn(assembleThemes)

        doLast {
            librariesToBundle.forEach { (target, source, includes) ->
                copy {
                    from(nodeModules.resolve(source)) {
                        if (includes.isNotEmpty()) include(includes)
                    }
                    into(thirdPartyOutDir.resolve(target))
                }
            }
        }
    }
