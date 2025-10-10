plugins {
    kotlin("jvm")
    id("io.miret.etienne.sass") version "1.5.2"
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(testFixtures(project(":quarkdown-core")))
    testImplementation("org.apache.pdfbox:pdfbox:3.0.4")
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-interaction"))
    implementation(project(":quarkdown-server"))
}

tasks.compileSass {
    val dir = projectDir.resolve("src/main/resources/render/theme")
    sourceDir = dir
    outputDir = dir
}

val npmInstall =
    tasks.register<Exec>("npmInstall") {
        group = "build"
        description = "Runs dependencies via npm"
        commandLine("npm", "install")
    }

val bundleTypeScript =
    tasks.register<Exec>("bundleTypeScript") {
        group = "build"
        description = "Bundles TypeScript files using esbuild"

        // Make sure npm install runs first
        dependsOn(npmInstall)

        commandLine(
            "npx",
            "esbuild",
            "src/main/typescript/index.ts",
            "--bundle",
            "--platform=browser",
            "--format=iife",
            "--outfile=src/main/resources/render/script/quarkdown.js",
            "--external:reveal.js",
            "--sourcemap",
        )
    }

tasks.processResources {
    dependsOn(tasks.compileSass)
    dependsOn(bundleTypeScript)
}
