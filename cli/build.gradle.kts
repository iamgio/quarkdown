plugins {
    kotlin("jvm")
    application
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(project(":core"))
    implementation(project(":server"))
    implementation(project(":stdlib"))
    implementation("com.github.ajalt.clikt:clikt:5.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("io.methvin:directory-watcher:0.18.0")
}

application {
    mainClass.set("eu.iamgio.quarkdown.cli.QuarkdownCliKt")
}
