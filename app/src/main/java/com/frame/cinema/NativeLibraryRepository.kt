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
    fun reviews(): List<ReviewEntry> = store.allReviews()
    fun categories(): List<String> = store.categories()

    fun addReview(videoId: Long?, content: String, rating: Int) {
        val text = content.trim()
        if (text.isNotEmpty()) store.addReview(videoId, text, rating, System.currentTimeMillis())
    }

    fun updateReview(id: Long, videoId: Long?, content: String, rating: Int) {
        val text = content.trim()
        if (text.isNotEmpty()) store.updateReview(id, videoId, text, rating)
        else store.deleteReview(id)
    }

    fun deleteReview(id: Long) = store.deleteReview(id)

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
                // Import may be cancelled by removing the item or clearing the library while
                // metadata extraction is running. Never resurrect a deleted database row.
                val current = store.allVideos().firstOrNull { it.id == id }
                if (current != null) {
                    store.upsert(
                        current.copy(
                            thumbPath = metadata.thumbnailPath ?: current.thumbPath,
                            durationMs = metadata.durationMs.takeIf { it > 0L } ?: current.durationMs
                        )
                    )
                    Handler(Looper.getMainLooper()).post(onMetadataReady)
                } else {
                    ThumbnailStore.delete(app, metadata.thumbnailPath)
                }
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

    fun replaceAll(items: List<VideoItem>, reviews: List<ReviewEntry>? = null) {
        val oldThumbnails = store.allVideos().mapNotNull { it.thumbPath }.toSet()
        val restored = items.map { item ->
            item.copy(thumbPath = item.thumbPath?.takeIf { path -> java.io.File(path).isFile })
        }
        val retainedThumbnails = restored.mapNotNull { it.thumbPath }.toSet()
        val restoredCategories = (listOf("全部分类", "我的最爱") + restored.map { it.category }).distinct()
        store.replaceAll(restored, restoredCategories, reviews)
        (oldThumbnails - retainedThumbnails).forEach { ThumbnailStore.delete(app, it) }
    }

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
        if (item.thumbPath != path) ThumbnailStore.delete(app, item.thumbPath)
        return true
    }

    fun delete(id: Long) {
        val item = store.allVideos().firstOrNull { it.id == id }
        ThumbnailStore.delete(app, item?.thumbPath)
        store.delete(id)
        store.clearWatchTimeForVideo(id)
    }

    fun clearRecent() {
        store.updateAll(store.allVideos().map {
            it.copy(progress = 0f, status = "all", lastPlayedAt = 0L)
        })
        store.clearWatchTime()
    }

    fun deleteRecent(id: Long) {
        store.allVideos().firstOrNull { it.id == id }?.let {
            store.upsert(it.copy(progress = 0f, status = "all", lastPlayedAt = 0L))
        }
        store.clearWatchTimeForVideo(id)
    }

    fun clearAll() {
        store.allVideos().forEach { ThumbnailStore.delete(app, it.thumbPath) }
        store.clear()
        store.clearWatchTime()
        ThumbnailStore.clear(app)
        store.saveCategories(listOf("全部分类", "我的最爱"))
    }

    fun saveCategories(names: List<String>) = store.saveCategories(names)

    fun recordWatchTime(videoId: Long, durationMs: Long, timestamp: Long = System.currentTimeMillis()) {
        if (durationMs <= 0L) return
        val dayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
        store.addWatchTime(videoId, dayKey, durationMs)
    }

    fun weeklyWatchTimeMs(now: Long = System.currentTimeMillis()): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = now
            firstDayOfWeek = java.util.Calendar.MONDAY
            set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val firstDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        return store.watchTimeSince(firstDay)
    }

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
