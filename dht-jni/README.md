# dht-jni（聚合）

无源码，仅把 **dht-jni-core** 与 **全平台 native** 绑在一起。

- 继续用 `implementation(project(":dht-jni"))` 时，行为与拆分前一致（classpath 上仍有全部平台的 so/dll）。
- 若发布到 Maven，可只发 **dht-jni-core** + 各 **dht-jni-native-***，由使用方按平台选一条 `runtimeOnly`。
