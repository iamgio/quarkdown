plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(project(":quarkdown-core"))
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.10.0")
    implementation("org.kodein.emoji:emoji-kt:2.0.1")
    dokkaPlugin(project(":quarkdown-quarkdoc"))
}
