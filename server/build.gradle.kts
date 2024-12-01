plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-server-netty:3.0.1")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}
