pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    kotlin("jvm") version "2.3.10" apply false
}

rootProject.name = "dht-kt"
include(
    "dht-jni-core",
    "dht-jni-native-win-x64",
    "dht-jni-native-linux-x64",
    "dht-jni-native-linux-aarch64",
    "dht-jni-native-osx-x64",
    "dht-jni-native-osx-aarch64",
    "dht-jni",
    "dht-jni-sample",
)
