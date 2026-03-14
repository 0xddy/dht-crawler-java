package cn.lmcw.dht.model

/** Rust JNI ctor (Ljava/lang/String;J)V */
class FileInfo constructor(
    @JvmField val path: String,
    @JvmField val size: Long,
) {
    override fun toString(): String = "FileInfo(path='$path', size=$size)"
}
