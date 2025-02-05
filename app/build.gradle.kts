plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")

    // Apply the Application plugin to add support for building an executable JVM application.
    application
}

dependencies {
    implementation(project(":model"))
    implementation(project(":domain"))
    implementation(project(":database"))
    implementation(project(":io"))
    implementation(project(":utils"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("io.insert-koin:koin-core:4.0.2")
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "com.benavalli.kraken.app.AppKt"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.benavalli.kraken.app.AppKt" // Ensure this matches your main class
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // ✅ Ignore duplicate files
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    from(project(":io").tasks.getByName("jar").outputs.files.map { zipTree(it) })
}

