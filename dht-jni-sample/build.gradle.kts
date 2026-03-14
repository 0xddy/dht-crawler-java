import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    application
    // Latest Shadow; targets Gradle 8.11+ / 9.x — see https://plugins.gradle.org/plugin/com.gradleup.shadow
    id("com.gradleup.shadow") version "9.3.2"
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":dht-jni"))
    testImplementation(kotlin("test"))
}

application {
    applicationName = "dht-sample"
    mainClass.set("cn.lmcw.dht.sample.SampleMainKt")
    applicationDefaultJvmArgs = listOf(
        "-Xms256m",
        "-Xmx512m",
    )
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("dht-sample")
    archiveClassifier.set("all")
    mergeServiceFiles()
    manifest {
        attributes("Main-Class" to application.mainClass.get())
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.distZip {
    archiveFileName.set("${application.applicationName}-${project.version}.zip")
}

tasks.distTar {
    archiveFileName.set("${application.applicationName}-${project.version}.tar")
    compression = Compression.GZIP
}
