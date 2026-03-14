/**
 * 聚合模块：core + 全平台 native，等价于「一个依赖拿齐所有 so/dll」。
 * 若只要单一平台：api(project(":dht-jni-core")) + runtimeOnly(project(":dht-jni-native-linux-x64")) 等。
 */
plugins {
    `java-library`
}

dependencies {
    api(project(":dht-jni-core"))
    runtimeOnly(project(":dht-jni-native-win-x64"))
    runtimeOnly(project(":dht-jni-native-linux-x64"))
    runtimeOnly(project(":dht-jni-native-linux-aarch64"))
    runtimeOnly(project(":dht-jni-native-osx-x64"))
    runtimeOnly(project(":dht-jni-native-osx-aarch64"))
}

// 必须生成 jar：Shadow / 部分任务会展开 project 依赖的 libs/*.jar；
// 本模块无源码，jar 几乎为空，仅作聚合坐标；实质依赖仍是 api(core)+runtimeOnly(natives)。
tasks.jar {
    enabled = true
    manifest {
        attributes(
            "Implementation-Title" to "dht-jni (aggregator)",
            "Implementation-Version" to project.version.toString(),
        )
    }
    // 与 dht-jni-core 一致，便于只依赖聚合 jar 时也能 -include META-INF/proguard/dht-jni.pro
    metaInf {
        from(layout.projectDirectory.file("proguard-dht-jni.pro")) {
            rename { "dht-jni.pro" }
            into("proguard")
        }
    }
}
