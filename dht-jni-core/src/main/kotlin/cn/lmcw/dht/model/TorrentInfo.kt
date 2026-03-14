package cn.lmcw.dht.model

/** Rust JNI full ctor with two List args — see dht-crawler jni/types.rs */
class TorrentInfo constructor(
    @JvmField val infoHash: String,
    @JvmField val magnetLink: String,
    @JvmField val name: String,
    @JvmField val totalSize: Long,
    @JvmField val files: List<FileInfo>?,
    @JvmField val pieceLength: Long,
    @JvmField val peers: List<String>?,
    @JvmField val timestamp: Long,
) {
    override fun toString(): String =
        "TorrentInfo(infoHash='$infoHash', name='$name', totalSize=$totalSize, files=${files?.size})"
}
