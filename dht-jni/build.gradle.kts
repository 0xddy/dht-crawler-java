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

// 不发布空 jar；依赖方仍通过本模块解析到 core + natives
tasks.jar {
    enabled = false
}
