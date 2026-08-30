plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.fleeksoft.ksoup:ksoup:0.2.6")
}

tasks.test {
    useJUnitPlatform()
}
