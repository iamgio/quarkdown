import java.net.URLClassLoader
import java.util.zip.ZipFile

plugins {
    kotlin("jvm")
    id("org.graalvm.buildtools.native") version "1.1.10"
    application
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.apache.pdfbox:pdfbox:3.0.6")
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-template"))
    implementation(project(":quarkdown-html"))
    implementation(project(":quarkdown-markdown"))
    implementation(project(":quarkdown-plaintext"))
    implementation(project(":quarkdown-server"))
    implementation(project(":quarkdown-interaction"))
    implementation(project(":quarkdown-stdlib"))
    implementation(project(":quarkdown-lsp"))
    implementation(project(":quarkdown-install-layout-navigator"))
    implementation("com.github.ajalt.clikt:clikt:5.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("io.methvin:directory-watcher:0.19.1")
}

application {
    mainClass.set("com.quarkdown.cli.QuarkdownCliKt")
}

// Writes the project version to a file in the resources directory, so it can be accessed at runtime.
val writeVersionFile by tasks.registering {
    val version = project.parent?.version ?: "unknown"
    val versionFile = "version.txt"
    val outputFile = layout.projectDirectory.file("src/main/resources/$versionFile").asFile

    doLast {
        outputFile.writeText(version.toString())
    }
}

tasks.processResources {
    dependsOn(writeVersionFile)
    dependsOn(":assembleDevLib")
}

// GraalVM native image
//
// Applied here rather than on the root project because this module declares its runtime classpath
// statically, which the plugin needs while configuring. The root project assembles its own
// classpath in a `projectsEvaluated` hook, too late for the plugin to resolve.
//
// Native images cannot be cross-compiled: the binary produced is always for the host platform.
// A GraalVM JDK must be running Gradle, or be pointed at by GRAALVM_HOME.
graalvmNative {
    // Uses the GraalVM running Gradle (or GRAALVM_HOME) rather than requiring a registered toolchain.
    toolchainDetection.set(false)

    // Pulls community-maintained reachability metadata for third-party libraries, such as Netty.
    metadataRepository {
        enabled.set(true)
    }

    binaries.named("main") {
        imageName.set("quarkdown")
        mainClass.set("com.quarkdown.cli.QuarkdownCliKt")

        // Netty and slf4j-simple capture logger instances during class initialization. Those objects
        // would end up in the image heap, which the builder rejects, so both are initialized at run time.
        buildArgs.add("--initialize-at-run-time=io.netty")
        buildArgs.add("--initialize-at-run-time=org.slf4j.simple")

        // The JDK ships locale data that native-image strips by default, keeping only the root locale.
        // Without this, `.doclang` would not resolve locale names and bibliography localization would
        // silently fall back while still reporting a successful compilation.
        buildArgs.add("-H:+UnlockExperimentalVMOptions")
        buildArgs.add("-H:+IncludeAllLocales")

        // Caps the image builder's own memory, for hosts that have less than it would otherwise take
        // (the macOS arm64 CI runner has 7 GB, against a default peak of about 6.7 GB). Parallelism is
        // lowered alongside it, since the builder's footprint scales with the number of threads.
        providers.gradleProperty("native.buildMemory").orNull?.let { memory ->
            buildArgs.add("-J-Xmx$memory")
            buildArgs.add("--parallelism=2")
        }
    }
}

// Native image reflection metadata
//
// A few third-party libraries reach classes reflectively, which the image builder cannot see.
// Rather than recording what a traced run happened to touch, the list is derived from the
// dependency jars: each scope below states which classes a library reflects over and why.
//
// Deriving it has three properties that a recorded trace does not. It cannot drift, because it is
// recomputed from the jars on every build. It is platform-independent, because a jar's contents do
// not depend on the machine reading it. And a dependency upgrade is picked up automatically.

/**
 * A set of classes a library reaches reflectively, identified by where they live rather than by
 * enumerating them, so that the list survives the library changing.
 *
 * @param reason why the library needs these classes registered
 * @param jar matches the file names of the jars to scan
 * @param packagePrefix only classes under this package are registered
 * @param enumsOnly registers only enum classes, for libraries that reflect solely over enum constants
 */
data class ReflectionScope(
    val reason: String,
    val jar: Regex,
    val packagePrefix: String = "",
    val enumsOnly: Boolean = false,
)

val reflectionScopes =
    listOf(
        ReflectionScope(
            reason = "LSP4J serializes every protocol message through Gson, which reads fields reflectively",
            jar = Regex("^org\\.eclipse\\.lsp4j.*\\.jar$"),
            packagePrefix = "org.eclipse.lsp4j",
        ),
        ReflectionScope(
            reason = "JNA maps native structures, callbacks and library interfaces by reflection",
            jar = Regex("^jna-.*\\.jar$"),
            packagePrefix = "com.sun.jna",
        ),
        ReflectionScope(
            reason = "The macOS file watcher behind --watch binds to FSEvents through JNA",
            jar = Regex("^directory-watcher-.*\\.jar$"),
            packagePrefix = "io.methvin.watchservice",
        ),
        ReflectionScope(
            reason = "flexmark reads enum constants reflectively to build its option bit sets",
            jar = Regex("^flexmark-.*\\.jar$"),
            enumsOnly = true,
        ),
    )

/**
 * Emits `reflect-config.json` covering every class matched by [reflectionScopes].
 *
 * Registering a whole package is deliberately coarser than the minimum a given run needs. The cost
 * is a slightly larger binary; the benefit is that no future code path can reach a class the build
 * forgot to declare.
 */
val generateNativeImageReflectionMetadata by tasks.registering {
    group = "build"
    description = "Derives native image reflection metadata from the dependency jars."

    val runtimeClasspath = configurations.named("runtimeClasspath")
    val outputFile =
        layout.buildDirectory.file(
            "generated/native-image/META-INF/native-image/com.quarkdown/quarkdown-cli/reflect-config.json",
        )

    inputs.files(runtimeClasspath)
    outputs.file(outputFile)

    doLast {
        val jars = runtimeClasspath.get().files.filter { it.name.endsWith(".jar") }

        // Only needed to tell enums apart from ordinary classes. Loading is done without running
        // static initializers, so it cannot execute library code.
        val classLoader = URLClassLoader(jars.map { it.toURI().toURL() }.toTypedArray(), null)

        fun isEnum(className: String): Boolean =
            try {
                Class.forName(className, false, classLoader).isEnum
            } catch (_: Throwable) {
                false
            }

        val registered = sortedSetOf<String>()

        reflectionScopes.forEach { scope ->
            val matchingJars = jars.filter { scope.jar.matches(it.name) }
            check(matchingJars.isNotEmpty()) {
                "No jar on the runtime classpath matches ${scope.jar}. " +
                    "A dependency was renamed or removed, and the classes it needs registered would " +
                    "silently stop being declared. Update the scope in quarkdown-cli/build.gradle.kts."
            }

            val before = registered.size
            matchingJars.forEach { jar ->
                ZipFile(jar).use { zip ->
                    zip
                        .entries()
                        .asSequence()
                        .filter { it.name.endsWith(".class") }
                        .map { it.name.removeSuffix(".class").replace('/', '.') }
                        .filter { it.startsWith(scope.packagePrefix) }
                        .filter { !scope.enumsOnly || isEnum(it) }
                        .forEach(registered::add)
                }
            }

            val added = registered.size - before
            check(added > 0) { "Scope '${scope.reason}' matched no classes, which cannot be right." }
            logger.info("  native image reflection: $added classes for ${scope.jar.pattern}")
        }

        val entries =
            registered.joinToString(",\n") {
                """  { "name": "$it", "allDeclaredFields": true, "allDeclaredMethods": true, """ +
                    """"allDeclaredConstructors": true, "unsafeAllocated": true }"""
            }

        outputFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText("[\n$entries\n]\n")
        }
        logger.lifecycle("Native image reflection metadata: ${registered.size} classes from ${reflectionScopes.size} scopes.")
    }
}

tasks.jar {
    dependsOn(generateNativeImageReflectionMetadata)
    from(layout.buildDirectory.dir("generated/native-image"))
}
