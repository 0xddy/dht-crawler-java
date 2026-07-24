// Root aggregator; see subprojects dht-jni, dht-jni-sample
// 版本：gradle.properties 的 dht.kt.version，或命令行 -Pdht.kt.version=x.y.z
val dhtKtVersion: String =
    (findProperty("dht.kt.version") as String?)?.trim()?.takeIf { it.isNotEmpty() } ?: "2.0.0"

subprojects {
    group = "cn.lmcw"
    version = dhtKtVersion
    repositories {
        mavenCentral()
    }
}
