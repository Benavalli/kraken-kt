plugins {
    kotlin("jvm")
}

group = "com.benavalli.kraken"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.pi4j:pi4j-ktx:2.4.0") // Kotlin DSL
    implementation("com.pi4j:pi4j-core:2.8.0")
    implementation("com.pi4j:pi4j-plugin-raspberrypi:2.8.0")
    implementation("com.pi4j:pi4j-plugin-pigpio:2.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("io.insert-koin:koin-core:4.0.2")
    implementation(project(":model"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}