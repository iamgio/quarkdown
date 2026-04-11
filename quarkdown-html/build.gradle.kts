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

val scssSrcDir: File = projectDir.resolve("src/main/scss")
val themesOutDir: File = thirdPartyOutDir.resolve("theme")

/** Theme kinds whose stylesheets are reshaped into per-theme subdirectories. */
val themeKinds = listOf("layout", "color", "locale")

/**
 * Parses a theme manifest file of shape `{"exports": ["node_modules/@fontsource/foo", ...]}`.
 * Returns an empty list if the file does not exist or has no `exports` key.
 */
fun readThemeExports(manifest: File): List<String> {
    if (!manifest.isFile) return emptyList()
    val json = JsonSlurper().parse(manifest) as? Map<*, *> ?: return emptyList()
    return (json["exports"] as? List<*>).orEmpty().filterIsInstance<String>()
}

/**
 * Top-level theme names under `src/main/scss/<kind>/`, excluding SCSS partials (`_*.scss`).
 * Resolved at configuration time, so the task can declare per-theme copy specs upfront.
 */
fun discoverThemeNames(kind: String): List<String> =
    scssSrcDir
        .resolve(kind)
        .listFiles { f -> f.isFile && f.extension == "scss" && !f.name.startsWith("_") }
        ?.map { it.nameWithoutExtension }
        .orEmpty()

// Reshapes the flat SCSS-compiled output into a per-theme directory layout under
// build/thirdparty/theme/, and copies each theme's declared export assets alongside
// its CSS file. The flat global.css is preserved as-is; layouts, colors, and locales
// each become <kind>/<name>/<name>.css plus any sibling assets declared in <name>.json.
val assembleThemes =
    tasks.register<Sync>("assembleThemes") {
        group = "build"
        description = "Reshapes SCSS-compiled themes and copies their exported assets into build/thirdparty/theme/"
        dependsOn(tasks.npmInstall) // exports may reference node_modules
        dependsOn(tasks.compileSass)

        into(themesOutDir)

        // Flat: global theme.
        from(scssCompiledDir) {
            include("global.css", "global.css.map")
        }

        // Nested: <kind>/<name>/<name>.css (+ exports declared in <name>.json).
        themeKinds.forEach { kind ->
            discoverThemeNames(kind).forEach { name ->
                from(scssCompiledDir.resolve(kind)) {
                    include("$name.css", "$name.css.map")
                    into("$kind/$name")
                }
                readThemeExports(scssSrcDir.resolve("$kind/$name.json")).forEach { exportPath ->
                    val src = projectDir.resolve(exportPath)
                    if (src.exists()) {
                        from(src) {
                            into("$kind/$name/${src.name}")
                        }
                    } else {
                        logger.warn("assembleThemes: export path not found: $exportPath (theme $name)")
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
