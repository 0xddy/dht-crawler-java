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

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "dht-jni-core",
            "Implementation-Version" to project.version,
        )
    }
    metaInf {
        from(layout.projectDirectory.file("proguard-dht-jni.pro")) {
            rename { "dht-jni.pro" }
            into("proguard")
        }
    }
}
