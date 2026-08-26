extra["noRuntime"] = true

plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.9")
}

tasks.test {
    useJUnitPlatform()
}
