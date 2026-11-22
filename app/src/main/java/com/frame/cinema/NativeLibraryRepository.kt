package com.frame.cinema

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.io.File
import android.util.Base64
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
    private val pendingWatchTimes = linkedMapOf<Pair<Long, String>, Long>()
    private var pendingWatchTimeTotalMs = 0L

    fun videos(): List<VideoItem> = store.libraryVideos()
    fun backupVideos(): List<VideoItem> = store.allVideos()
    fun reviews(): List<ReviewEntry> = store.allReviews()
    fun movieReviews(): List<MovieReviewEntry> = store.allMovieReviews()
    fun backupMovieReviews(): List<MovieReviewEntry> = store.allMovieReviews()
    fun categories(): List<String> = store.categories()
    fun backupWatchTimes(): List<BackupWatchTime> {
        flushWatchTime()
        return store.allWatchTimes()
    }
    fun backupFolderSources(): List<BackupFolderSource> = store.allFolderSources()
    fun backupSubtitlePreferences(): List<BackupSubtitlePreference> = store.allSubtitlePreferences()
    fun backupSubtitleTracks(): List<BackupSubtitleTrack> = store.allSubtitleTracks()

    fun backupAssets(): List<BackupAsset> = buildList {
        store.allVideos().forEach { video ->
            video.thumbPath?.let { path ->
                runCatching { File(path).takeIf { it.isFile }?.readBytes() }.getOrNull()?.let { bytes ->
                    add(BackupAsset("video_thumbnail", video.id, Base64.encodeToString(bytes, Base64.NO_WRAP)))
                }
            }
        }
        store.allReviews().forEach { review ->
            review.coverPath?.let { path ->
                runCatching { File(path).takeIf { it.isFile }?.readBytes() }.getOrNull()?.let { bytes ->
                    add(BackupAsset("review_cover", review.id, Base64.encodeToString(bytes, Base64.NO_WRAP)))
                }
            }
        }
    }

    fun addMovieReview(videoId: Long, title: String, content: String): Long? {
        val cleanContent = content.trim()
        if (cleanContent.isEmpty() || store.allVideos().none { it.id == videoId }) return null
        return store.addMovieReview(videoId, title.trim(), cleanContent, System.currentTimeMillis()).takeIf { it > 0L }
    }

    fun addStandaloneMovieReview(standaloneReviewId: Long): Long? {
        val parent = store.allReviews().firstOrNull {
            it.id == standaloneReviewId && it.videoId == null && it.standaloneGroupId == null
        } ?: return null
        val existing = store.allMovieReviews().firstOrNull { it.standaloneReviewId == standaloneReviewId }
        if (existing != null) return existing.id
        return store.addStandaloneMovieReview(
            standaloneReviewId,
            parent.title.trim(),
            "",
            System.currentTimeMillis()
        ).takeIf { it > 0L }
    }

    fun updateStandaloneReviewRating(standaloneReviewId: Long, rating: Float): Boolean {
        val parent = store.allReviews().firstOrNull {
            it.id == standaloneReviewId && it.videoId == null && it.standaloneGroupId == null
        } ?: return false
        store.updateStandaloneReviewRating(parent.id, rating)
        return true
    }

    fun updateMovieReview(id: Long, title: String, content: String): Boolean {
        val cleanContent = content.trim()
        if (cleanContent.isEmpty()) return false
        val existing = store.allMovieReviews().firstOrNull { it.id == id } ?: return false
        store.updateMovieReview(id, title.trim(), cleanContent, System.currentTimeMillis())
        return true
    }

    fun deleteMovieReview(id: Long) {
        store.deleteMovieReview(id)
    }

    fun addReview(videoId: Long?, content: String, rating: Float) {
        val text = content.trim()
        if (text.isNotEmpty()) {
            store.addReview(videoId, text, 0f, System.currentTimeMillis())
            if (videoId != null && rating > 0f) updateVideoRating(videoId, rating)
        }
    }

    fun addTimestampReview(videoId: Long, content: String, playbackPositionMs: Long): Boolean {
        val text = content.trim()
        if (text.isEmpty() || store.allVideos().none { it.id == videoId }) return false
        store.addReview(
            videoId = videoId,
            content = text,
            rating = 0f,
            createdAt = System.currentTimeMillis(),
            playbackPositionMs = playbackPositionMs.coerceAtLeast(0L)
        )
        return true
    }

    fun updateReview(id: Long, videoId: Long?, content: String, rating: Float) {
        val text = content.trim()
        if (text.isNotEmpty()) store.updateReview(id, videoId, text, 0f)
        else store.deleteReview(id)
    }

    fun addStandaloneReview(title: String, content: String, rating: Float, coverUri: Uri?): Boolean {
        val text = content.trim()
        if (text.isEmpty()) return false
        val now = System.currentTimeMillis()
        val id = store.addReview(null, text, rating, now, title.trim(), null)
        val coverPath = coverUri?.let { saveReviewCover(it, id) }
        if (coverPath != null) store.updateReview(id, null, text, rating, title.trim(), coverPath)
        return true
    }

    fun addStandaloneChildReview(groupId: Long, content: String): Boolean {
        val text = content.trim()
        if (text.isEmpty()) return false
        val parent = store.allReviews().firstOrNull { it.id == groupId && it.videoId == null && it.standaloneGroupId == null }
            ?: return false
        store.addReview(null, text, 0f, System.currentTimeMillis(), standaloneGroupId = parent.id)
        return true
    }

    fun updateStandaloneChildReview(id: Long, groupId: Long, content: String): Boolean {
        val text = content.trim()
        if (text.isEmpty()) return false
        val child = store.allReviews().firstOrNull { it.id == id && it.standaloneGroupId == groupId }
            ?: return false
        store.updateReview(child.id, null, text, 0f, standaloneGroupId = groupId)
        return true
    }

    fun updateStandaloneReview(id: Long, title: String, content: String, rating: Float, coverUri: Uri?, existingCoverPath: String?): Boolean {
        val text = content.trim()
        if (text.isEmpty()) return false
        val newPath = coverUri?.let { saveReviewCover(it, id) }
        val finalPath = newPath ?: existingCoverPath
        store.updateReview(id, null, text, rating, title.trim(), finalPath)
        if (newPath != null && existingCoverPath != null && existingCoverPath != newPath) File(existingCoverPath).delete()
        return true
    }

    fun updateVideoRating(id: Long, rating: Float) {
        store.allVideos().firstOrNull { it.id == id }?.let { video ->
            store.upsert(video.copy(rating = rating.coerceIn(0f, 5f)))
        }
    }

    fun deleteReview(id: Long) {
        val entry = store.allReviews().firstOrNull { it.id == id } ?: return
        if (entry.standaloneGroupId == null && entry.videoId == null) {
            store.allReviews().filter { it.standaloneGroupId == id }.forEach { child ->
                child.coverPath?.let { File(it).delete() }
                store.deleteReview(child.id)
            }
        }
        entry.coverPath?.let { File(it).delete() }
        store.deleteReview(id)
    }

    private fun saveReviewCover(uri: Uri, reviewId: Long): String? = runCatching {
        val directory = File(app.filesDir, "review_covers").apply { mkdirs() }
        val target = File(directory, "review_$reviewId.jpg")
        app.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        target.absolutePath
    }.getOrNull()

    fun importUris(
        uris: List<Uri>,
        onMetadataReady: () -> Unit = {},
        folderSources: Map<String, VideoFolderSource> = emptyMap()
    ): Int {
        var added = 0
        val usedIds = store.allVideos().mapTo(mutableSetOf()) { it.id }
        uris.forEach { uri ->
            runCatching {
                app.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val existing = store.allVideos().firstOrNull { it.uri == uri.toString() }
            if (existing != null) {
                folderSources[uri.toString()]?.let { store.saveVideoFolderSource(existing.id, it) }
                if (existing.isInLibrary == false) {
                    store.setInLibrary(existing.id, true)
                    added++
                }
                return@forEach
            }
            val originalFileName = queryName(uri)
            val parsedFilename = VideoFilenameParser.parse(originalFileName)
            var id: Long
            do {
                id = UUID.randomUUID().leastSignificantBits and Long.MAX_VALUE
            } while (id == 0L || !usedIds.add(id))
            val now = System.currentTimeMillis()
            val item = VideoItem(
                id = id,
                uri = uri.toString(),
                name = parsedFilename?.displayTitle ?: originalFileName,
                size = querySize(uri),
                date = SimpleDateFormat("M/d/yyyy", Locale.getDefault()).format(Date(now)),
                addedAt = now,
                bgIndex = ((id % 4) + 1).toInt(),
                originalFileName = originalFileName,
                filenameMetadataJson = parsedFilename?.let { com.google.gson.Gson().toJson(it.metadata) },
                nameManuallyEdited = false
            )
            store.upsert(item)
            folderSources[uri.toString()]?.let { store.saveVideoFolderSource(id, it) }
            added++
            thumbnailExecutor.execute {
                val metadata = ThumbnailStore.createVideoMetadata(app, uri, id)
                // Import may be cancelled by removing the item or clearing the library while
                // metadata extraction is running. Keep metadata on a hidden row without restoring it.
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
        val folderSources = mutableMapOf<String, VideoFolderSource>()
        fun collect(directory: DocumentFile, relativePath: String) {
            val children = runCatching { directory.listFiles().toList() }.getOrNull() ?: return
            children.forEach { child ->
                runCatching {
                    if (child.isDirectory) {
                        val childPath = if (relativePath.isBlank()) child.name.orEmpty()
                            else "$relativePath/${child.name.orEmpty()}"
                        collect(child, childPath)
                    } else if (child.type?.startsWith("video/") == true) {
                        files += child.uri
                        folderSources[child.uri.toString()] = VideoFolderSource(treeUri.toString(), relativePath)
                    }
                }
            }
        }
        collect(root, "")
        return importUris(files, onMetadataReady, folderSources)
    }

    fun subtitlePreference(videoId: Long): SubtitlePreference = store.subtitlePreference(videoId)

    fun saveSubtitlePreference(videoId: Long, preference: SubtitlePreference) {
        store.saveSubtitlePreference(videoId, preference)
    }

    fun saveSubtitleTrack(videoId: Long, track: SubtitleTrackEntry) {
        store.saveSubtitleTrack(videoId, track)
    }

    fun manualSubtitleTracks(videoId: Long): List<SubtitleTrackEntry> = store.subtitleTracks(videoId)

    fun hasVideoFolderSource(videoId: Long): Boolean {
        val source = store.videoFolderSource(videoId) ?: return false
        val treeUri = Uri.parse(source.treeUri)
        return app.contentResolver.persistedUriPermissions.any {
            it.uri == treeUri && it.isReadPermission
        }
    }

    fun registerVideoFolderSource(videoId: Long, videoUri: Uri, treeUri: Uri): Boolean {
        runCatching {
            app.contentResolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val root = DocumentFile.fromTreeUri(app, treeUri) ?: return false
        fun find(directory: DocumentFile, relativePath: String): String? {
            directory.listFiles().forEach { child ->
                if (child.isDirectory) {
                    val next = if (relativePath.isBlank()) child.name.orEmpty() else "$relativePath/${child.name.orEmpty()}"
                    find(child, next)?.let { return it }
                } else if (sameDocument(child.uri, videoUri)) {
                    return relativePath
                }
            }
            return null
        }
        val parentPath = find(root, "") ?: return false
        store.saveVideoFolderSource(videoId, VideoFolderSource(treeUri.toString(), parentPath))
        return true
    }

    fun findSiblingSubtitles(videoId: Long, videoName: String): List<Pair<Uri, String>> {
        val source = store.videoFolderSource(videoId) ?: return emptyList()
        return runCatching {
            val root = DocumentFile.fromTreeUri(app, Uri.parse(source.treeUri)) ?: return emptyList()
            var directory = root
            source.relativePath.split('/').filter { it.isNotBlank() }.forEach { segment ->
                directory = directory.findFile(segment) ?: return emptyList()
            }
            val siblings = directory.listFiles()
            val storedVideoUri = store.allVideos().firstOrNull { it.id == videoId }?.uri
            val actualVideoName = siblings.firstOrNull { storedVideoUri != null && sameDocument(it.uri, Uri.parse(storedVideoUri)) }?.name ?: videoName
            val baseName = actualVideoName.substringBeforeLast('.', actualVideoName).trim()
            siblings
                .filter { !it.isDirectory && fileExtension(it.name) in setOf("srt", "ass", "ssa", "vtt") }
                .mapNotNull { file ->
                    val name = file.name ?: return@mapNotNull null
                    val subtitleBase = name.substringBeforeLast('.', name)
                    val exact = subtitleBase.equals(baseName, ignoreCase = true)
                    val languageVariant = subtitleBase.startsWith("$baseName.", ignoreCase = true) ||
                        subtitleBase.startsWith("${baseName}_", ignoreCase = true) ||
                        subtitleBase.startsWith("$baseName-", ignoreCase = true)
                    if (exact || languageVariant) Triple(file.uri, name, exact) else null
                }
                .sortedWith(compareByDescending<Triple<Uri, String, Boolean>> { it.third }.thenBy { it.second.lowercase(Locale.ROOT) })
                .map { it.first to it.second }
        }.getOrDefault(emptyList())
    }

    private fun sameDocument(first: Uri, second: Uri): Boolean {
        if (first == second) return true
        val firstId = runCatching { DocumentsContract.getDocumentId(first) }.getOrNull()
        val secondId = runCatching { DocumentsContract.getDocumentId(second) }.getOrNull()
        return firstId != null && firstId == secondId
    }

    private fun fileExtension(name: String?): String = name?.substringAfterLast('.', "")?.lowercase(Locale.ROOT).orEmpty()

    fun update(item: VideoItem) = store.upsert(item)

    fun replaceAll(
        items: List<VideoItem>,
        reviews: List<ReviewEntry>? = null,
        categories: List<String>? = null,
        watchTimes: List<BackupWatchTime> = emptyList(),
        folderSources: List<BackupFolderSource> = emptyList(),
        subtitlePreferences: List<BackupSubtitlePreference> = emptyList(),
        subtitleTracks: List<BackupSubtitleTrack> = emptyList(),
        assets: List<BackupAsset> = emptyList(),
        movieReviews: List<MovieReviewEntry>? = null
    ) {
        val oldThumbnails = store.allVideos().mapNotNull { it.thumbPath }.toSet()
        val oldReviewCovers = store.allReviews().mapNotNull { it.coverPath }.toSet()
        val restoredAssetPaths = mutableMapOf<Pair<String, Long>, String>()
        assets.forEach { asset ->
            runCatching {
                val bytes = Base64.decode(asset.dataBase64, Base64.DEFAULT)
                val directory = when (asset.kind) {
                    "video_thumbnail" -> File(app.filesDir, "video-thumbnails")
                    "review_cover" -> File(app.filesDir, "review_covers")
                    else -> return@runCatching
                }.apply { mkdirs() }
                val target = File(directory, "backup_${asset.ownerId}.jpg")
                target.writeBytes(bytes)
                restoredAssetPaths[asset.kind to asset.ownerId] = target.absolutePath
            }
        }
        val restored = items.map { item ->
            item.copy(
                thumbPath = restoredAssetPaths["video_thumbnail" to item.id]
                    ?: item.thumbPath?.takeIf { path -> File(path).isFile },
                category = if (item.category == "全部") "全部分类" else item.category
            )
        }
        val restoredReviews = reviews?.map { review ->
            review.copy(
                coverPath = restoredAssetPaths["review_cover" to review.id]
                    ?: review.coverPath?.takeIf { path -> File(path).isFile }
            )
        }
        val restoredCategories = (categories?.takeIf { it.isNotEmpty() }
            ?: (listOf("我的最爱") + restored.map { it.category } + "全部分类")).let { names ->
            listOf("我的最爱") +
                names.filterNot { it == "全部" || it == "全部分类" || it == "我的最爱" }.distinct() +
                "全部分类"
        }
        store.replaceAll(
            restored, restoredCategories, restoredReviews,
            watchTimes, folderSources, subtitlePreferences, subtitleTracks, movieReviews
        )
        (oldThumbnails - restored.mapNotNull { it.thumbPath }.toSet()).forEach { ThumbnailStore.delete(app, it) }
        (oldReviewCovers - restoredReviews.orEmpty().mapNotNull { it.coverPath }.toSet()).forEach { File(it).delete() }
    }

    fun rename(id: Long, name: String) {
        store.allVideos().firstOrNull { it.id == id }?.let {
            store.upsert(it.copy(name = name, nameManuallyEdited = true))
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

    fun removeFromLibrary(id: Long) {
        store.setInLibrary(id, false)
    }

    fun clearRecent() {
        flushWatchTime()
        store.updateAll(store.libraryVideos().map {
            it.copy(lastPlayedAt = 0L)
        })
    }

    fun deleteRecent(id: Long) {
        flushWatchTime()
        store.allVideos().firstOrNull { it.id == id }?.let {
            store.upsert(it.copy(lastPlayedAt = 0L))
        }
    }

    fun clearAll() {
        flushWatchTime()
        store.allVideos().forEach { ThumbnailStore.delete(app, it.thumbPath) }
        File(app.filesDir, "review_covers").deleteRecursively()
        store.clear()
        store.clearWatchTime()
        ThumbnailStore.clear(app)
        store.saveCategories(listOf("全部分类", "我的最爱"))
    }

    fun saveCategories(names: List<String>) = store.saveCategories(names)

    @Synchronized fun recordWatchTime(videoId: Long, durationMs: Long, timestamp: Long = System.currentTimeMillis()) {
        if (durationMs <= 0L) return
        val dayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
        val key = videoId to dayKey
        pendingWatchTimes[key] = (pendingWatchTimes[key] ?: 0L) + durationMs
        pendingWatchTimeTotalMs += durationMs
        if (pendingWatchTimeTotalMs >= 10_000L) flushWatchTime()
    }

    @Synchronized fun flushWatchTime() {
        val pending = pendingWatchTimes.toList()
        for ((key, durationMs) in pending) {
            val saved = runCatching { store.addWatchTime(key.first, key.second, durationMs) }.isSuccess
            if (saved) {
                pendingWatchTimes.remove(key)
                pendingWatchTimeTotalMs = (pendingWatchTimeTotalMs - durationMs).coerceAtLeast(0L)
            }
        }
    }

    fun weeklyWatchTimeMs(now: Long = System.currentTimeMillis()): Long {
        flushWatchTime()
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
        store.updatePlayback(id, positionMs, durationMs, ended, System.currentTimeMillis())
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
