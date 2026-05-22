
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import java.time.Year

plugins {
    kotlin("jvm") version "2.3.10"
    id("org.jetbrains.dokka") version "2.0.0"
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
    id("com.github.ben-manes.versions") version "0.53.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.19"
    application
}

group = "com.quarkdown"
version = file("version.txt").readText().trim()

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "se.patrikerdes.use-latest-versions")
}

// Fat JAR / Distribution dependencies
gradle.projectsEvaluated {
    dependencies {
        subprojects.forEach {
            when {
                it.extra.has("noRuntime") && it.extra["noRuntime"] == true -> {
                    compileOnly(it)
                }

                else -> {
                    implementation(it)
                }
            }
        }
    }
}

application {
    mainClass.set("com.quarkdown.cli.QuarkdownCliKt")
}

ktlint {
    version.set("1.7.1")
}

// Dokka

dokka {
    dokkaPublications.html {
        outputDirectory.set(
            layout.buildDirectory
                .file("docs")
                .get()
                .asFile,
        )
    }
}

/**
 * Whether [project] uses the Quarkdoc plugin, which means its documentation must be included in the distribution zip.
 */
fun usesQuarkdoc(project: Project): Boolean {
    val quarkdoc = project(":quarkdown-quarkdoc")
    return project.configurations
        .asSequence()
        .flatMap { it.dependencies }
        .filterIsInstance<ProjectDependency>()
        .any { it.dependencyProject == quarkdoc }
}

val quarkdocGenerate =
    tasks.register("quarkdocGenerate") {
        group = "documentation"
        description = "Generates the Quarkdoc documentation for modules that include the Quarkdoc plugin."

        dependencies {
            subprojects.filter(::usesQuarkdoc).forEach {
                dokka(it)
            }
        }

        dependsOn(tasks.dokkaGenerate)
    }

tasks.register("quarkdocGenerateAll") {
    group = "documentation"
    description = "Generates the Quarkdoc documentation for all modules."

    dependencies {
        subprojects.forEach {
            dokka(it)
        }
    }

    dependsOn(tasks.dokkaGenerate)
}

allprojects {
    fun asset(path: String): File = project(":quarkdown-quarkdoc").projectDir.resolve("src/main/resources/$path")

    dokka {
        pluginsConfiguration.html {
            val year = Year.now().value
            footerMessage.set("&copy; $year Quarkdown")
            customAssets.from(*asset("assets/images").listFiles()!!)
            customStyleSheets.from(asset("styles/stylesheet.css"))
        }
    }
}

// Tasks

/**
 * Populates a [CopySpec] with the Quarkdown install library layout, with subdirectories such as `qd/` and `html/`.
 * Used by both [distributions]`.main` and [assembleDevLib].
 *
 * This layout is navigable at runtime via the `install-layout-navigator` module.
 */
val installLibLayout: CopySpec.() -> Unit = {
    // .qd library files.
    into("qd") {
        from(project(":quarkdown-libs").file("src/main/resources")) {
            include("*.qd")
        }
    }
    // HTML rendering resources (third-party libraries, themes, scripts) for offline rendering.
    into("html") {
        from(project(":quarkdown-html").layout.buildDirectory.dir("install"))
    }
    // Agent skills.
    into("skills") {
        from(rootProject.file("skills"))
    }
}

// Bundled JVM runtime

/**
 * A target platform for which a bundled minimal JRE is built via `jlink` and a distribution zip is produced.
 * Cross-compilation works because `jlink` only reads platform-specific bytes from the target JDK's `jmods/`,
 * so any host `jlink` (e.g., the CI Linux runner's) can emit a Windows or macOS runtime by pointing
 * `--module-path` at the target JDK's `jmods/` directory.
 */
data class JlinkTarget(
    /** Used in zip file names, e.g. `quarkdown-linux-x64.zip`. */
    val id: String,
    /** Used as suffix for per-target task names, e.g. `bundleRuntimeLinuxX64`. */
    val taskSuffix: String,
    /** Filename of the Temurin JDK archive on Adoptium's GitHub releases. */
    val jdkArchive: String,
    /** Path within the extracted archive to JAVA_HOME (its `jmods/` parent). */
    val jdkHomeRelative: String,
)

val jdkVersion = providers.gradleProperty("bundledJdkVersion").get()
val jdkVersionEncoded = jdkVersion.replace("+", "%2B") // URL-encoded for the GitHub release path
val jdkBaseUrl = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-$jdkVersionEncoded/"
val jdkFileVersion = jdkVersion.replace("+", "_") // Adoptium uses underscores in archive filenames

val jlinkTargets =
    listOf(
        JlinkTarget(
            id = "linux-x64",
            taskSuffix = "LinuxX64",
            jdkArchive = "OpenJDK17U-jdk_x64_linux_hotspot_$jdkFileVersion.tar.gz",
            jdkHomeRelative = "jdk-$jdkVersion",
        ),
        JlinkTarget(
            id = "macos-x64",
            taskSuffix = "MacosX64",
            jdkArchive = "OpenJDK17U-jdk_x64_mac_hotspot_$jdkFileVersion.tar.gz",
            jdkHomeRelative = "jdk-$jdkVersion/Contents/Home",
        ),
        JlinkTarget(
            id = "macos-aarch64",
            taskSuffix = "MacosAarch64",
            jdkArchive = "OpenJDK17U-jdk_aarch64_mac_hotspot_$jdkFileVersion.tar.gz",
            jdkHomeRelative = "jdk-$jdkVersion/Contents/Home",
        ),
        JlinkTarget(
            id = "windows-x64",
            taskSuffix = "WindowsX64",
            jdkArchive = "OpenJDK17U-jdk_x64_windows_hotspot_$jdkFileVersion.zip",
            jdkHomeRelative = "jdk-$jdkVersion",
        ),
    )

// SPI-loaded modules that jdeps cannot detect via static analysis:
// - `jdk.crypto.ec`: TLS handshakes with EC certificates (most modern HTTPS endpoints).
// - `jdk.localedata`: display names and resource bundles used by Quarkdown's localization.
val jlinkSpiModules = listOf("jdk.crypto.ec", "jdk.localedata")

/**
 * Resolves the full set of JDK modules required by Quarkdown's runtime classpath,
 * combining `jdeps` static analysis with explicitly listed SPI-loaded modules.
 * Module names are platform-agnostic, so the host's `jdeps` can be used regardless of target.
 */
fun resolveRequiredModules(): String {
    val jdepsOutput =
        ByteArrayOutputStream()
            .also { out ->
                exec {
                    val jars = configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }
                    commandLine(
                        "jdeps",
                        "--ignore-missing-deps",
                        "--multi-release",
                        "17",
                        "--print-module-deps",
                        *jars.map { it.absolutePath }.toTypedArray(),
                    )
                    standardOutput = out
                }
            }.toString()
            .trim()

    // jdeps may output warnings before the final line; the module list is always the last line.
    val detectedModules = jdepsOutput.lines().last().trim()
    return (detectedModules.split(",") + jlinkSpiModules).joinToString(",")
}

/**
 * Host-platform bundled JRE, used by `installDist` for development and host-only runs.
 * Cross-platform release zips use the per-target `bundleRuntime<Target>` tasks instead.
 */
val bundleRuntime by tasks.registering {
    group = "distribution"
    description = "Creates a minimal JRE via jlink for the host platform (used by installDist)."

    dependsOn(tasks.jar, subprojects.map { it.tasks.named("jar") })

    val runtimeDir = layout.buildDirectory.dir("runtime")
    outputs.dir(runtimeDir)

    doLast {
        val outputDir = runtimeDir.get().asFile
        delete(outputDir)
        exec {
            commandLine(
                "jlink",
                "--add-modules",
                resolveRequiredModules(),
                "--strip-debug",
                "--no-man-pages",
                "--no-header-files",
                "--compress",
                "2",
                "--output",
                outputDir.absolutePath,
            )
        }
    }
}

// Per-target tasks: download the JDK, build a target-specific JRE via jlink,
// and assemble a distribution zip with that runtime bundled in.
jlinkTargets.forEach { target ->
    val jdkRoot = layout.buildDirectory.dir("jdks/${target.id}")
    val runtimeDir = layout.buildDirectory.dir("runtimes/${target.id}")

    val downloadJdkTask =
        tasks.register("downloadJdk${target.taskSuffix}") {
            group = "distribution"
            description = "Downloads and extracts the Temurin JDK for ${target.id} (used for jlink cross-compilation)."
            outputs.dir(jdkRoot)

            doLast {
                val rootDir = jdkRoot.get().asFile
                val jdkHomeDir = File(rootDir, target.jdkHomeRelative)
                if (jdkHomeDir.resolve("jmods").isDirectory) return@doLast

                delete(rootDir)
                rootDir.mkdirs()
                val archive = File(rootDir, target.jdkArchive)
                ant.invokeMethod("get", mapOf("src" to "$jdkBaseUrl${target.jdkArchive}", "dest" to archive))

                when {
                    archive.name.endsWith(".tar.gz") -> {
                        exec {
                            commandLine("tar", "-xzf", archive.absolutePath, "-C", rootDir.absolutePath)
                        }
                    }

                    archive.name.endsWith(".zip") -> {
                        copy {
                            from(zipTree(archive))
                            into(rootDir)
                        }
                    }
                }
                archive.delete()
            }
        }

    val bundleRuntimeTargetTask =
        tasks.register("bundleRuntime${target.taskSuffix}") {
            group = "distribution"
            description = "Creates a minimal JRE via jlink for ${target.id}, using the target platform's jmods."
            dependsOn(downloadJdkTask, tasks.jar, subprojects.map { it.tasks.named("jar") })
            inputs.dir(jdkRoot)
            outputs.dir(runtimeDir)

            doLast {
                val jmodsDir =
                    jdkRoot
                        .get()
                        .asFile
                        .resolve(target.jdkHomeRelative)
                        .resolve("jmods")
                val outputDir = runtimeDir.get().asFile
                delete(outputDir)

                exec {
                    commandLine(
                        "jlink",
                        "--module-path",
                        jmodsDir.absolutePath,
                        "--add-modules",
                        resolveRequiredModules(),
                        "--strip-debug",
                        "--no-man-pages",
                        "--no-header-files",
                        "--compress",
                        "2",
                        "--output",
                        outputDir.absolutePath,
                    )
                }
            }
        }

    tasks.register("dist${target.taskSuffix}Zip", Zip::class) {
        group = "distribution"
        description = "Builds the Quarkdown distribution zip for ${target.id}, with a bundled jlink runtime."
        archiveBaseName.set("quarkdown-${target.id}")
        archiveVersion.set("")
        destinationDirectory.set(layout.buildDirectory.dir("distributions"))

        // Wrap everything in a top-level `quarkdown/` directory, mirroring the legacy zip layout.
        into("quarkdown") {
            // bin/, lib/, docs/ from installDist (excluding its host-targeted runtime/).
            from(tasks.installDist.map { it.destinationDir }) {
                exclude("runtime/**")
            }
            // Per-target runtime.
            from(runtimeDir) {
                into("runtime")
            }
        }

        dependsOn(tasks.installDist, bundleRuntimeTargetTask)
    }
}

// Aggregate task that builds distribution zips for every target platform.
tasks.register("distZipAll") {
    group = "distribution"
    description = "Builds Quarkdown distribution zips for all target platforms."
    dependsOn(jlinkTargets.map { "dist${it.taskSuffix}Zip" })
}

distributions.main {
    contents {
        into("lib", installLibLayout)
        // Bundled minimal JRE for the host platform, created by bundleRuntime.
        // Per-platform zips use the per-target bundleRuntime<Target> tasks instead.
        from(layout.buildDirectory.dir("runtime")) {
            into("runtime")
        }
        // Include the generated Dokka documentation, generated by quarkdocGenerate,
        // in the 'docs' directory.
        val dokkaOutputDir = layout.buildDirectory.file("docs")
        from(dokkaOutputDir) {
            into("docs")
        }
        // .qd wiki sources for the agent skill.
        into("docs/wiki") {
            from(rootProject.file("docs")) {
                include("**/*.qd")
            }
            includeEmptyDirs = false
        }
    }
}

// Assembles a dev-time install `lib/` layout at `<rootProject>/build/dev-lib`, mirroring
// the distribution layout so that `gradle run` and IntelliJ runs can resolve the install
// directory at runtime.
val assembleDevLib by tasks.registering(Sync::class) {
    dependsOn(":quarkdown-html:bundleThirdParty")
    into(layout.buildDirectory.dir("dev-lib"))
    installLibLayout()
}

tasks.installDist {
    dependsOn(quarkdocGenerate, bundleRuntime)
}

tasks.distZip {
    dependsOn(quarkdocGenerate, bundleRuntime)
    archiveVersion.set("")
}

tasks.distTar {
    dependsOn(quarkdocGenerate, bundleRuntime)
    archiveVersion.set("")
}

tasks.test {
    useJUnitPlatform()
    dependsOn(tasks.ktlintCheck)
}

tasks.named<CreateStartScripts>("startScripts") {
    classpath = files("lib/*") // Fixes the 'Input line is too long' error on Windows.
    // Prepends subscripts to the generated start scripts.
    doLast {
        val dir = file("scripts")
        val scripts = sequenceOf("bootstrap")

        scripts.forEach { scriptName ->
            val unixPrefix = dir.resolve("$scriptName.sh").readText() + "\n"
            val windowsPrefix = dir.resolve("$scriptName.bat").readText() + "\n"
            unixScript.writeText("#!/bin/sh\n\n" + unixPrefix + unixScript.readText())
            windowsScript.writeText("@echo off\n\n" + windowsPrefix + windowsScript.readText())
        }
    }
}

tasks.wrapper {
    gradleVersion = "8.3"
    distributionType = Wrapper.DistributionType.ALL
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.register("printVersion") {
    doLast {
        println(project.version)
    }
}

// Dependency updates

allprojects {
    tasks.dependencyUpdates {
        rejectVersionIf {
            Regex("[.-](alpha|beta|rc|cr|m|preview|b|ea)", RegexOption.IGNORE_CASE) in candidate.version
        }
    }

    tasks.useLatestVersions {
        updateBlacklist =
            listOf(
                "org.jetbrains.kotlin",
                "org.jetbrains.dokka",
            )
    }
}
