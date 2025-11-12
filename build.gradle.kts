plugins {
    kotlin("jvm") version "2.1.10"
}

group = "org.ivcode"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // SLF4J Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.9")

    // Kotlin reflection (needed when you want to read KClass annotations, properties, etc.)
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.10")

    implementation("io.github.ollama4j:ollama4j:1.1.4")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}