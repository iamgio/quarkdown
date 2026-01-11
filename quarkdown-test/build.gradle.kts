extra["noRuntime"] = true

plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-html"))
    implementation(project(":quarkdown-plaintext"))
    implementation(project(":quarkdown-stdlib"))
}

tasks.test {
    useJUnitPlatform()
}
