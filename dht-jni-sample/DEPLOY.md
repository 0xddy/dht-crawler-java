# Deploy dht-sample on a server

## Requirements

- **JRE or JDK 21** on the server
- **Linux x64 / ARM64** 等：fat JAR 需包含对应 `native/.../libdht_crawler.so`。本地构建前把上游 JNI 解压进对应模块（如 `dht-jni-native-linux-aarch64/.../linux-aarch64/`）；**Release** 上已有 `dht-sample-*-linux-aarch64-all.jar` 与 `dht-jni-native-linux-aarch64-*.jar`（CI：`ubuntu-24.04-arm` + 上游 `aarch64-unknown-linux-gnu.zip`）。macOS Apple Silicon 同理：`macos-aarch64-all` + `osx-aarch64` native jar。
- Firewall: allow the **UDP port** you set (sample default `12313`)

## Build artifacts (on your dev machine)

```bash
./gradlew :dht-jni-sample:build
```

Outputs:

| Artifact | Path |
|----------|------|
| **Fat JAR (recommended)** | `dht-jni-sample/build/libs/dht-sample-2.0.0-all.jar` |
| **ZIP bundle** | `dht-jni-sample/build/distributions/dht-sample-2.0.0.zip` |
| **TGZ bundle** | `./gradlew :dht-jni-sample:distTar` → `build/distributions/dht-sample-2.0.0.tar` (gzip) |

## 瘦包（仅 Linux x64）

在 `dht-jni-sample/build.gradle.kts` 中把

`implementation(project(":dht-jni"))`

改为：

```kotlin
implementation(project(":dht-jni-core"))
implementation(project(":dht-jni-coroutines"))
runtimeOnly(project(":dht-jni-native-linux-x64"))
```

再 `./gradlew :dht-jni-sample:shadowJar`，fat jar 内不再含 win/osx 等 native。

## Option A — single JAR

Copy `dht-sample-*-all.jar` to the server, then:

```bash
nohup java -Ddht.port=12313 -jar dht-sample-2.0.0-all.jar \
  > dht-sample.out 2>&1 &
```

Note: JVM **must** see system properties **before** `-jar`. Prefer:

```bash
java -Ddht.port=12313 -jar dht-sample-2.0.0-all.jar
```

可选参数：

- `-Ddht.networkMode=IPV4_ONLY|IPV6_ONLY|DUAL_STACK`
- `-Ddht.durationSec=60`
- `-Ddht.statsSec=30`

## Option B — ZIP distribution

```bash
unzip dht-sample-2.0.0.zip
cd dht-sample-2.0.0
./bin/dht-sample
```

Override JVM options:

```bash
export JAVA_OPTS="-Ddht.port=6881"
./bin/dht-sample
```

(On Windows use `bin\dht-sample.bat`.)

## systemd (example)

```ini
[Unit]
Description=dht-sample DHT crawler
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/dht-sample
ExecStart=/usr/bin/java -Xms256m -Xmx512m -Ddht.port=12313 -jar /opt/dht-sample/dht-sample-2.0.0-all.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

## If native is not inside the JAR

Use an `.so` built on the same distro/glibc as the server:

```bash
java -Ddht.jni.library.path=/opt/dht-sample/libdht_crawler.so -jar dht-sample-2.0.0-all.jar
```

`DhtCrawlerNative.load(path)` 必须在创建 crawler 前调用；sample 会在启动时读取
`dht.jni.library.path` 并完成加载。
