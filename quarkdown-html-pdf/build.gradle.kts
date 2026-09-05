plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.apache.pdfbox:pdfbox:3.0.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-html"))
    implementation(project(":quarkdown-install-layout-navigator"))
    implementation(project(":quarkdown-interaction"))
    implementation(project(":quarkdown-server"))
}
