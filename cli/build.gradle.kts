plugins {
    kotlin("jvm")
    application
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.apache.pdfbox:pdfbox:3.0.4")
    implementation(project(":core"))
    implementation(project(":server"))
    implementation(project(":pdf"))
    implementation(project(":stdlib"))
    implementation("com.github.ajalt.clikt:clikt:5.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("io.methvin:directory-watcher:0.19.0")
}

application {
    mainClass.set("eu.iamgio.quarkdown.cli.QuarkdownCliKt")
}
