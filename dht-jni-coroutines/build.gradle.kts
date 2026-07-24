plugins {
    kotlin("jvm")
    `java-library`
}

java {
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    jvmToolchain(21)
}

val coroutinesVersion = providers.gradleProperty("kotlinx.coroutines.version").get()

dependencies {
    api(project(":dht-jni-core"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    System.getProperty("dht.jni.library.path")?.let { path ->
        systemProperty("dht.jni.library.path", path)
    }
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "dht-jni-coroutines",
            "Implementation-Version" to project.version,
        )
    }
}
