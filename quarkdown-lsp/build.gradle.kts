plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.24.0")
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-quarkdoc-reader"))
}
