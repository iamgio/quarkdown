plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.24.0")
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
    implementation("com.vladsch.flexmark:flexmark-html2md-converter:0.64.8")
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-quarkdoc-reader"))
}
