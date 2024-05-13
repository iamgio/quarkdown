plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(project(":core"))
    implementation(project(":stdlib"))
}

tasks.test {
    useJUnitPlatform()
}
