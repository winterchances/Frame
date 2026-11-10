package com.frame.cinema

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Android storage adapter for the original browser data model.
 *
 * The visible fields map directly to the original JS record:
 * name/size/date/progress/status/isFav/category/bgIndex/review/rating.
 * Android-only fields (persisted content URI, duration and thumbnail path)
 * keep the same user-visible behavior across process restarts.
 */
class NativeLibraryRepository(context: Context) {
    private val app = context.applicationContext
    private val store = VideoStore(app)
    private val thumbnailExecutor = Executors.newSingleThreadExecutor()

    fun videos(): List<VideoItem> = store.allVideos()
    fun categories(): List<String> = store.categories()

    fun importUris(uris: List<Uri>, onMetadataReady: () -> Unit = {}): Int {
        var added = 0
        uris.forEachIndexed { index, uri ->
            if (store.allVideos().any { it.uri == uri.toString() }) return@forEachIndexed
            runCatching {
                app.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val name = queryName(uri)
            val id = System.currentTimeMillis() * 100 + index
            val now = System.currentTimeMillis()
            val item = VideoItem(
                id = id,
                uri = uri.toString(),
                name = name,
                size = querySize(uri),
                date = SimpleDateFormat("M/d/yyyy", Locale.getDefault()).format(Date(now)),
                addedAt = now,
                bgIndex = ((id % 4) + 1).toInt()
            )
            store.upsert(item)
            added++
            thumbnailExecutor.execute {
                val metadata = ThumbnailStore.createVideoMetadata(app, uri, id)
                store.upsert(item.copy(thumbPath = metadata.thumbnailPath, durationMs = metadata.durationMs))
                Handler(Looper.getMainLooper()).post(onMetadataReady)
            }
        }
        return added
    }

    fun importFolder(treeUri: Uri, onMetadataReady: () -> Unit = {}): Int {
        runCatching {
            app.contentResolver.takePersistableUriPermission(
                treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        val root = DocumentFile.fromTreeUri(app, treeUri) ?: return 0
        val files = mutableListOf<Uri>()
        fun collect(directory: DocumentFile) {
            directory.listFiles().forEach { child ->
                if (child.isDirectory) collect(child)
                else if (child.type?.startsWith("video/") == true) files += child.uri
            }
        }
        collect(root)
        return importUris(files, onMetadataReady)
    }

    fun update(item: VideoItem) = store.upsert(item)

    fun rename(id: Long, name: String) {
        store.allVideos().firstOrNull { it.id == id }?.let {
            store.upsert(it.copy(name = name))
        }
    }

    fun favorite(id: Long) {
        store.allVideos().firstOrNull { it.id == id }?.let {
            store.upsert(it.copy(isFav = !it.isFav))
        }
    }

    fun moveCategory(id: Long, category: String) {
        store.allVideos().firstOrNull { it.id == id }?.let {
            store.upsert(it.copy(category = category))
        }
    }

    fun updateCover(id: Long, source: Uri): Boolean {
        val item = store.allVideos().firstOrNull { it.id == id } ?: return false
        val path = ThumbnailStore.saveCustomCover(app, source, id) ?: return false
        store.upsert(item.copy(thumbPath = path))
        return true
    }

    fun delete(id: Long) {
        val item = store.allVideos().firstOrNull { it.id == id }
        ThumbnailStore.delete(app, item?.thumbPath)
        store.delete(id)
    }

    fun clearRecent() {
        store.updateAll(store.allVideos().map {
            it.copy(progress = 0f, status = "all", lastPlayedAt = 0L)
        })
    }

    fun deleteRecent(id: Long) {
        store.allVideos().firstOrNull { it.id == id }?.let {
            store.upsert(it.copy(progress = 0f, status = "all", lastPlayedAt = 0L))
        }
    }

    fun clearAll() {
        store.allVideos().forEach { ThumbnailStore.delete(app, it.thumbPath) }
        store.clear()
        ThumbnailStore.clear(app)
        store.saveCategories(listOf("全部分类", "我的最爱"))
    }

    fun saveCategories(names: List<String>) = store.saveCategories(names)

    fun updatePlayback(id: Long, positionMs: Long, durationMs: Long, ended: Boolean = false) {
        val item = store.allVideos().firstOrNull { it.id == id } ?: return
        val percent = if (durationMs > 0) positionMs.toFloat() / durationMs * 100f else 0f
        store.upsert(
            item.copy(
                progress = if (ended) 100f else percent.coerceIn(0f, 99.9f),
                status = if (ended) "watched" else if (percent > 0) "playing" else item.status,
                durationMs = durationMs,
                lastPlayedAt = System.currentTimeMillis()
            )
        )
    }

    private fun queryName(uri: Uri): String {
        var result: String? = null
        app.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) result = cursor.getString(0) }
        return result ?: uri.lastPathSegment?.substringAfterLast('/') ?: "未命名视频"
    }

    private fun querySize(uri: Uri): String {
        var bytes = 0L
        app.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) bytes = cursor.getLong(0) }
        return if (bytes > 0) String.format(Locale.getDefault(), "%.1f MB", bytes / 1048576f) else ""
    }

    fun close() {
        thumbnailExecutor.shutdown()
        store.close()
    }
}
