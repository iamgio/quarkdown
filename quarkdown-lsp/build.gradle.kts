plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.14.11")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:1.0.0")
    implementation("com.quarkdown.better-parse:better-parse:0.4.5")
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-quarkdoc-reader"))
}
