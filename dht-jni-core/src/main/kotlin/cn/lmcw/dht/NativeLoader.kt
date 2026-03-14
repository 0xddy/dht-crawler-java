package cn.lmcw.dht

import java.io.InputStream
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

/** Extract platform `dht_crawler` native from classpath and [System.load] it. */
object NativeLoader {

    const val LIB_VERSION = "1.0.0"

    @Volatile
    private var loaded = false

    private val loadLock = Any()

    private val loadedFromPath = AtomicBoolean(false)

    private const val RESOURCE_PREFIX = "/native/"

    internal fun markLoadedFromPath() {
        loadedFromPath.set(true)
        loaded = true
    }

    fun isLoaded(): Boolean = loaded || loadedFromPath.get()

    fun loadFromPath(path: Path) {
        synchronized(loadLock) {
            if (isLoaded()) return
            System.load(path.toAbsolutePath().toString())
            markLoadedFromPath()
        }
    }

    fun load() {
        if (loaded || loadedFromPath.get()) return
        synchronized(loadLock) {
            if (loaded || loadedFromPath.get()) return
            val resourcePath = resourcePathForCurrentPlatform()
                ?: throw IllegalStateException(
                    "Unsupported platform: ${System.getProperty("os.name")} / ${System.getProperty("os.arch")}. " +
                        "Put native under src/main/resources/native/ or call DhtCrawlerNative.loadFromPath(Path) before using DhtCrawler.",
                )
            val fileName = resourcePath.substringAfterLast('/')
            val dir = extractDir()
            dir.createDirectories()
            val target = dir.resolve(fileName)
            val lockFile = dir.resolve(".extract.lock")
            FileChannel.open(
                lockFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
            ).use { ch ->
                var lock: FileLock? = null
                try {
                    lock = ch.lock()
                    if (!needsExtract(target, resourcePath)) {
                        System.load(target.toAbsolutePath().toString())
                        loaded = true
                        return
                    }
                    javaClass.getResourceAsStream(resourcePath)?.use { input ->
                        Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING)
                    } ?: throw UnsatisfiedLinkError("Missing native resource: $resourcePath")
                    System.load(target.toAbsolutePath().toString())
                    loaded = true
                } finally {
                    lock?.release()
                }
            }
        }
    }

    private fun extractDir(): Path {
        val home = System.getProperty("user.home") ?: ""
        val base = Path.of(home, ".cache", "dht-jni", LIB_VERSION)
        if (home.isNotEmpty() && Path.of(home).exists()) {
            return base
        }
        return Files.createTempDirectory("dht-jni-$LIB_VERSION-")
    }

    private fun needsExtract(target: Path, resourcePath: String): Boolean {
        if (!target.isRegularFile()) return true
        val expected = resourceSize(resourcePath) ?: return false
        return Files.size(target) != expected
    }

    private fun resourceSize(resourcePath: String): Long? {
        javaClass.getResourceAsStream(resourcePath)?.use { inp: InputStream ->
            var n = 0L
            val buf = ByteArray(65536)
            while (true) {
                val r = inp.read(buf)
                if (r <= 0) break
                n += r
            }
            return n
        }
        return null
    }

    fun resourcePathForCurrentPlatform(): String? {
        val os = System.getProperty("os.name", "").lowercase()
        val arch = System.getProperty("os.arch", "").lowercase()
        val archNorm = when (arch) {
            "amd64", "x86_64" -> "x64"
            "aarch64", "arm64" -> "aarch64"
            else -> null
        } ?: return null
        return when {
            os.contains("win") && archNorm == "x64" -> "${RESOURCE_PREFIX}win-x64/dht_crawler.dll"
            os.contains("linux") && archNorm == "x64" -> "${RESOURCE_PREFIX}linux-x64/libdht_crawler.so"
            os.contains("linux") && archNorm == "aarch64" -> "${RESOURCE_PREFIX}linux-aarch64/libdht_crawler.so"
            (os.contains("mac") || os.contains("darwin")) && archNorm == "x64" -> "${RESOURCE_PREFIX}osx-x64/libdht_crawler.dylib"
            (os.contains("mac") || os.contains("darwin")) && archNorm == "aarch64" -> "${RESOURCE_PREFIX}osx-aarch64/libdht_crawler.dylib"
            else -> null
        }
    }
}

object DhtCrawlerNative {

    fun loadFromPath(path: Path) {
        NativeLoader.loadFromPath(path)
    }
}
