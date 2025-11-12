plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
    kotlin("jvm") version embeddedKotlinVersion
}

gradlePlugin {
    plugins {
        create("s3mvn") {
            id = "s3mvn"
            implementationClass = "org.ivcode.gradle.s3mvn.S3MvnPlugin"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}