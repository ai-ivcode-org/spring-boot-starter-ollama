package org.ivcode.gradle.s3mvn.utils

fun isSnapshot(version: String): Boolean {
    return version.endsWith("-SNAPSHOT")
}
