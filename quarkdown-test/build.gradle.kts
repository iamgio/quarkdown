plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-stdlib"))
}

tasks.test {
    useJUnitPlatform()
}
