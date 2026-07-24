package cn.lmcw.dht.internal

import cn.lmcw.dht.DhtCallbacks
import cn.lmcw.dht.model.TorrentInfo

/** JNI callback surface called by dht-crawler/jni/callbacks.rs. */
internal class NativeListener(private val callbacks: DhtCallbacks) {
    fun onTorrent(info: TorrentInfo) {
        callbacks.onTorrent?.invoke(info)
    }

    fun onError(message: String) {
        callbacks.onError?.invoke(message)
    }

    fun onMetadataFetch(infoHash: String): Boolean =
        callbacks.metadataFilter?.invoke(infoHash) ?: true
}
