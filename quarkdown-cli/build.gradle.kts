plugins {
    kotlin("jvm")
    application
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.apache.pdfbox:pdfbox:3.0.6")
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-html"))
    implementation(project(":quarkdown-plaintext"))
    implementation(project(":quarkdown-server"))
    implementation(project(":quarkdown-interaction"))
    implementation(project(":quarkdown-stdlib"))
    implementation(project(":quarkdown-lsp"))
    implementation("com.github.ajalt.clikt:clikt:5.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
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
}
