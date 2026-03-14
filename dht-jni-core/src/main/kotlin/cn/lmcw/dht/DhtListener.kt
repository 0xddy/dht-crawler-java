package cn.lmcw.dht

import cn.lmcw.dht.model.TorrentInfo

/**
 * Rust JNI calls [onTorrent], [onError], [onMetadataFetch] (dht-crawler jni/callbacks.rs).
 * Implementations must be thread-safe.
 */
interface DhtListener {
    fun onTorrent(info: TorrentInfo)
    fun onError(message: String)

    /** Return true to fetch metadata; keep fast. */
    fun onMetadataFetch(infoHash: String): Boolean = true
}
