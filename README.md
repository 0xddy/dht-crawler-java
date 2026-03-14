# dht-kt

Kotlin JVM bindings for [dht-crawler](https://github.com/0xddy/dht-crawler) over JNI.

**Tooling:** Gradle **9.4.0**, Kotlin **2.3.10**, Shadow **9.3.2**, Foojay resolver **1.0.0**. Natives are packaged in **platform jars** and **extracted on first use**, then loaded via `System.load`.

## 模块结构

| Module | Description |
|--------|-------------|
| **dht-jni-core** | API：`DhtCrawler`、`DhtListener`、`DHTOptions`、`NativeLoader`（JNI 与 [types.rs](https://github.com/0xddy/dht-crawler/blob/master/jni/types.rs) 对齐）；**不含** so/dll |
| **dht-jni-native-*** | 仅资源：单平台一条依赖，减小发布体积 |
| **dht-jni** | 仅多模块 Gradle 用：`api(core)` + 全平台 native；**空 jar、不发 Release** |
| **dht-jni-sample** | 示例入口 |

### Native 子模块与路径

| Gradle 模块 | 资源路径（放入构建产物） |
|-------------|-------------------------|
| `dht-jni-native-win-x64` | `native/win-x64/dht_crawler.dll` |
| `dht-jni-native-linux-x64` | `native/linux-x64/libdht_crawler.so` |
| `dht-jni-native-linux-aarch64` | `native/linux-aarch64/libdht_crawler.so` |
| `dht-jni-native-osx-x64` | `native/osx-x64/libdht_crawler.dylib` |
| `dht-jni-native-osx-aarch64` | `native/osx-aarch64/libdht_crawler.dylib` |

在 [dht-crawler](https://github.com/0xddy/dht-crawler) 根目录：

```bash
cargo build --release --features jni
```

把对应平台的库复制到上表 **模块目录** 下的 `src/main/resources/native/...`（与 README.txt 同级）。

## 依赖方式

**全平台（与以前一样）**

```kotlin
implementation(project(":dht-jni"))
```

**只带 Linux x64（服务器瘦包）**

```kotlin
implementation(project(":dht-jni-core"))
runtimeOnly(project(":dht-jni-native-linux-x64"))
```

仅 core 时 classpath 上没有对应平台 native 会报错，除非先 `DhtCrawlerNative.loadFromPath(Path)`。

### 不用源码、只从 GitHub Release 依赖

每个 [Release](https://github.com/0xddy/dht-crawler-java/releases) 里除 **sample 可运行 fat JAR** 外，还会上传库 JAR；**版本号**由仓库根目录 **`gradle.properties`** 里的 **`dht.kt.version`** 决定（例如 `1.0.0` → `dht-jni-native-linux-x64-1.0.0.jar`）。

| 文件 | 用途 |
|------|------|
| **dht-jni-core-&lt;版本&gt;.jar** | 必装：Kotlin/Java API |
| **dht-jni-core-&lt;版本&gt;-sources.jar** | 可选：源码 |
| **dht-jni-native-linux-x64-&lt;版本&gt;.jar** | Linux x64 + `libdht_crawler.so` |
| **dht-jni-native-win-x64-&lt;版本&gt;.jar** | Windows x64 + dll |

**改版本：** 编辑 `gradle.properties` 中 `dht.kt.version=…` 后再打 tag / 本地 `./gradlew jar`。本地或 CI 也可覆盖：  
`./gradlew -Pdht.kt.version=1.2.3 jar`。手动跑 GitHub Actions 时可在 **「库 JAR 版本号」** 里填写，会覆盖当次构建（不必先改仓库里的 properties）。

Gradle 示例（把 jar 放到 `libs/`，版本与 `dht.kt.version` 一致）：

```kotlin
val v = "1.0.0" // 与 gradle.properties dht.kt.version 对齐
dependencies {
    implementation(files("libs/dht-jni-core-$v.jar"))
    runtimeOnly(files("libs/dht-jni-native-linux-x64-$v.jar"))
}
```

## Run sample

```bash
./gradlew :dht-jni-sample:run
```

JVM 属性同前：`dht.port`、`dht.netMode`、`dht.durationSec`、`dht.statsSec`、`dht.jni.library.path`。

## Pack for server

```bash
./gradlew :dht-jni-sample:build
```

可把 sample 改为只依赖 `dht-jni-core` + `dht-jni-native-linux-x64`，fat jar 里只带 linux-x64 so。见 [dht-jni-sample/DEPLOY.md](dht-jni-sample/DEPLOY.md)。

## ProGuard / R8

规则在 **dht-jni-core** 的 `proguard-dht-jni.pro`；jar 内 `META-INF/proguard/dht-jni.pro`。

## API notes

- 回调在 Rust 工作线程；监听器需线程安全。
- 解压目录：`~/.cache/dht-jni/<version>/`

## GitHub Actions / Release（省配额）

工作流：[`.github/workflows/release.yml`](.github/workflows/release.yml)

| 策略 | 说明 |
|------|------|
| **触发** | 仅 `push tag v*` 或手动 `workflow_dispatch`，**不在每次 push main 时构建** |
| **Native 来源（默认）** | 从 [dht-crawler Releases](https://github.com/0xddy/dht-crawler/releases) **下载预编译 zip**（如 `dht_crawler_jni-v1.0.2-x86_64-unknown-linux-gnu.zip`），**不再在 CI 里编 Rust**，省 Actions 分钟与缓存体积 |
| **备选** | 手动运行里 `native_source=cargo` 时仍会 `git clone` + `cargo build --features jni` |
| **锁定上游版本** | `dht_crawler_tag` 填 `v1.0.2` 等则下载该 tag 的 zip；留空则用 **latest** |
| **发版产物** | 每次 **push tag**：Linux + Windows 胖 JAR，以及 **core / sources / linux-native / win-native**（不再上传空的 `dht-jni-*.jar`） |
| **缓存** | 仅 Gradle cache；无 Cargo 时 Rust 工具链也可不装（push tag 路径零 Rust） |
| **Release 体积** | 旧 Release 可手动删资产；勿重复上传多份相同 JAR |

**发版命令示例**

```bash
git tag v1.0.1
git push origin v1.0.1
```

CI 会拉取上游 **latest** JNI zip、解压出 `libdht_crawler.so`、打 Shadow JAR 并挂到该 tag 的 GitHub Release。

## Legal

DHT 使用须合法合规。

## Windows

若 DLL 需 MSVC 运行库，请安装 [VC++ Redistributable](https://learn.microsoft.com/en-us/cpp/windows/latest-supported-vc-redist)。
