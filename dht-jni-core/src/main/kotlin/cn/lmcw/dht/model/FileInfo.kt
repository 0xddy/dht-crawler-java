package cn.lmcw.dht.model

/** One file entry from validated torrent metadata. */
data class FileInfo(
    val path: String,
    val size: Long,
)
