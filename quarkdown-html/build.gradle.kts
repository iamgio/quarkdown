import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask
import groovy.json.JsonSlurper

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.3.10"
    id("com.github.node-gradle.node") version "7.1.0"
    id("io.miret.etienne.sass") version "1.6.0"
}

node {
    download.set(true)
    version.set("22.22.2")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(testFixtures(project(":quarkdown-core")))
    testImplementation("org.apache.pdfbox:pdfbox:3.0.6")
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-interaction"))
    implementation(project(":quarkdown-server"))
    implementation(project(":quarkdown-plaintext")) // For search index generation
    implementation(project(":quarkdown-install-layout-navigator"))
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
    // Some SCSS partials load stylesheets from node_modules.
    dependsOn(tasks.npmInstall)
}

/**
 * Declarative specification for copying a library from `node_modules` into `build/install/`.
 * Multiple specs may share the same [target] (their files are merged into one directory).
 *
 * Every bundled library is also passed to esbuild as `--external:<target>`, since these
 * libraries are loaded at runtime from the bundled `lib/` directory and must not be
 * inlined into the TypeScript bundle.
 *
 * @param target output directory name under `build/install/lib/`
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

val bundleTypeScript =
    tasks.register<NpxTask>("bundleTypeScript") {
        group = "build"
        description = "Bundles TypeScript files using esbuild into build/install/script/quarkdown.min.js"

        // Make sure npm install runs first
        dependsOn(tasks.npmInstall)

        // Declared inputs/outputs so Gradle can skip the task when unchanged.
        inputs.dir(projectDir.resolve("src/main/typescript"))
        inputs.file(projectDir.resolve("package.json"))
        outputs.file(layout.buildDirectory.file("install/script/quarkdown.min.js"))
        outputs.file(layout.buildDirectory.file("install/script/quarkdown.min.js.map"))

        command.set("esbuild")
        args.set(
            listOf(
                "src/main/typescript/index.ts",
                "--bundle",
                "--platform=browser",
                "--format=iife",
                "--minify",
                "--outfile=build/install/script/quarkdown.min.js",
                "--sourcemap",
            ) + librariesToBundle.map { "--external:${it.target}" },
        )
    }

tasks.processResources {
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
    dependsOn(":assembleDevLib")
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

val installOutDir: File =
    layout.buildDirectory
        .dir("install")
        .get()
        .asFile

/**
 * Bundles highlight.js with all common languages into a single browser-ready file,
 * since the npm package is modular and does not include a pre-built bundle.
 */
val bundleHighlightJs =
    tasks.register<NpxTask>("bundleHighlightJs") {
        group = "build"
        description = "Bundles highlight.js into a single browser-ready file"
        dependsOn(tasks.npmInstall)

        // Declared inputs/outputs so Gradle can skip the task when unchanged.
        inputs.file(nodeModules.resolve("highlight.js/lib/common.js"))
        outputs.file(nodeModules.resolve("highlight.js/dist/highlightjs.min.js"))

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
val themesOutDir: File = installOutDir.resolve("theme")

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

/** CSS file plus its source map, the unit we copy around per stylesheet. */
fun cssWithMap(name: String): List<String> = listOf("$name.css", "$name.css.map")

// Reshapes the flat SCSS-compiled output into a per-theme directory layout under
// build/install/theme/, and copies each theme's declared export assets alongside
// its CSS file. The flat global.css is preserved as-is; layouts, colors, and locales
// each become <kind>/<name>/<name>.css plus any sibling assets declared in <name>.json.
val assembleThemes =
    tasks.register<Sync>("assembleThemes") {
        group = "build"
        description = "Reshapes SCSS-compiled themes and copies their exported assets into build/install/theme/"
        dependsOn(tasks.npmInstall) // exports may reference node_modules
        dependsOn(tasks.compileSass)

        into(themesOutDir)

        // Flat: global theme.
        from(scssCompiledDir) {
            include(cssWithMap("global"))
        }

        // Collected lazily so we can validate export paths at execution time, after
        // any task that might produce them (e.g. `npmInstall`) has actually run.
        val declaredExports = mutableListOf<File>()

        // Nested: <kind>/<name>/<name>.css (+ exports declared in <name>.json).
        themeKinds.forEach { kind ->
            discoverThemeNames(kind).forEach { name ->
                from(scssCompiledDir.resolve(kind)) {
                    include(cssWithMap(name))
                    into("$kind/$name")
                }
                readThemeExports(scssSrcDir.resolve("$kind/$name.json")).forEach { exportPath ->
                    val src = projectDir.resolve(exportPath)
                    declaredExports += src
                    from(src) {
                        into("$kind/$name/${src.name}")
                    }
                }
            }
        }

        // Fail loudly if any declared export path is still missing by the time the task
        // runs. Doing this in `doFirst` rather than at configuration time lets upstream
        // tasks (e.g. `npmInstall`) create the files before we look for them.
        doFirst {
            val missing = declaredExports.filterNot(File::exists)
            if (missing.isNotEmpty()) {
                throw GradleException(
                    "assembleThemes: missing theme export paths:\n" +
                        missing.joinToString("\n") { "  - ${it.relativeTo(projectDir)}" },
                )
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
        dependsOn(bundleTypeScript)

        doLast {
            librariesToBundle.forEach { (target, source, includes) ->
                copy {
                    from(nodeModules.resolve(source)) {
                        if (includes.isNotEmpty()) include(includes)
                    }
                    into(installOutDir.resolve("lib/$target"))
                }
            }
        }
    }
