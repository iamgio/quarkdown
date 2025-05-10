plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(project(":quarkdown-core"))
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.10.0")
    dokkaPlugin(project(":quarkdown-quarkdoc"))
}
