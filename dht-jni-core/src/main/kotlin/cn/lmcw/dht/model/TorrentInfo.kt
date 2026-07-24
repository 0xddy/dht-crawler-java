package cn.lmcw.dht.model

/** Validated torrent metadata delivered by the native crawler. */
data class TorrentInfo(
    val infoHash: String,
    val magnetLink: String,
    val name: String,
    val totalSize: Long,
    val files: List<FileInfo>,
    val pieceLength: Long,
    val peers: List<String>,
    val timestamp: Long,
)
