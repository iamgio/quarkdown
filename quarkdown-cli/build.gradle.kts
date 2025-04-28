plugins {
    kotlin("jvm")
    application
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.apache.pdfbox:pdfbox:3.0.4")
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-server"))
    implementation(project(":quarkdown-interaction"))
    implementation(project(":quarkdown-pdf"))
    implementation(project(":quarkdown-stdlib"))
    implementation("com.github.ajalt.clikt:clikt:5.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("io.methvin:directory-watcher:0.19.0")
}

application {
    mainClass.set("eu.iamgio.quarkdown.cli.QuarkdownCliKt")
}
