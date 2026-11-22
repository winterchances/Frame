package com.frame.cinema

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.net.Uri

/**
 * Persistent native counterpart of the original myCinemaDB / myCinemaCats stores.
 * URI grants are persisted by MainActivity; all user-visible library state lives here.
 */
data class ReviewEntry(
    val id: Long,
    val videoId: Long?,
    val content: String,
    val rating: Float = 0f,
    val createdAt: Long,
    val title: String = "",
    val coverPath: String? = null,
    val standaloneGroupId: Long? = null,
    val playbackPositionMs: Long? = null
)

data class MovieReviewEntry(
    val id: Long,
    val videoId: Long,
    val title: String = "",
    val content: String,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
    val standaloneReviewId: Long? = null
)

data class BackupWatchTime(val videoId: Long, val dayKey: String, val durationMs: Long)
data class BackupFolderSource(val videoId: Long, val source: VideoFolderSource)
data class BackupSubtitlePreference(val videoId: Long, val preference: SubtitlePreference)
data class BackupSubtitleTrack(val videoId: Long, val track: SubtitleTrackEntry)
data class BackupAsset(val kind: String, val ownerId: Long, val dataBase64: String)
data class BackupAppPreferences(
    val allowBackground: Boolean = true,
    val keepAwake: Boolean = true,
    val subtitleTextSizePercent: Int = 100,
    val appearanceMode: String? = null
)

data class LibraryBackup(
    val videos: List<VideoItem>,
    val reviews: List<ReviewEntry>,
    val categories: List<String>? = null,
    val watchTimes: List<BackupWatchTime>? = null,
    val folderSources: List<BackupFolderSource>? = null,
    val subtitlePreferences: List<BackupSubtitlePreference>? = null,
    val subtitleTracks: List<BackupSubtitleTrack>? = null,
    val assets: List<BackupAsset>? = null,
    val appPreferences: BackupAppPreferences? = null,
    val movieReviews: List<MovieReviewEntry>? = null
)

data class VideoItem(
    val id: Long,
    val uri: String,
    val name: String,
    val size: String,
    val date: String,
    val addedAt: Long,
    val progress: Float = 0f,
    val status: String = "all",
    val isFav: Boolean = false,
    val category: String = "全部分类",
    val bgIndex: Int = 1,
    val review: String = "",
    val rating: Float = 0f,
    val durationMs: Long = 0L,
    val playbackPositionMs: Long? = null,
    val playbackCompleted: Boolean = false,
    val lastPlayedAt: Long = 0L,
    val thumbPath: String? = null,
    // Null is treated as visible for compatibility with backups created before this field existed.
    val isInLibrary: Boolean? = true,
    val originalFileName: String? = null,
    val filenameMetadataJson: String? = null,
    val nameManuallyEdited: Boolean = false
) {
    val contentUri: Uri get() = Uri.parse(uri)
}

data class SubtitlePreference(val selectedUri: String? = null, val enabled: Boolean = true)
data class SubtitleTrackEntry(val uri: String, val name: String, val manual: Boolean = true)
data class VideoFolderSource(val treeUri: String, val relativePath: String)

class VideoStore(context: Context) : SQLiteOpenHelper(context, "frame_library.db", null, 15) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE videos (
                id INTEGER PRIMARY KEY,
                uri TEXT NOT NULL UNIQUE,
                name TEXT NOT NULL,
                size TEXT NOT NULL DEFAULT '',
                date TEXT NOT NULL,
                added_at INTEGER NOT NULL,
                progress REAL NOT NULL DEFAULT 0,
                status TEXT NOT NULL DEFAULT 'all',
                is_fav INTEGER NOT NULL DEFAULT 0,
                category TEXT NOT NULL DEFAULT '全部分类',
                bg_index INTEGER NOT NULL DEFAULT 1,
                review TEXT NOT NULL DEFAULT '',
                rating INTEGER NOT NULL DEFAULT 0,
                duration_ms INTEGER NOT NULL DEFAULT 0,
                playback_position_ms INTEGER,
                playback_completed INTEGER NOT NULL DEFAULT 0,
                last_played_at INTEGER NOT NULL DEFAULT 0,
                thumb_path TEXT,
                is_in_library INTEGER NOT NULL DEFAULT 1,
                original_file_name TEXT,
                filename_metadata_json TEXT,
                name_manually_edited INTEGER NOT NULL DEFAULT 0
            )""".trimIndent()
        )
        db.execSQL("CREATE TABLE categories (name TEXT PRIMARY KEY NOT NULL, position INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE video_folder_sources (video_id INTEGER PRIMARY KEY NOT NULL, tree_uri TEXT NOT NULL, relative_path TEXT NOT NULL DEFAULT '')")
        db.execSQL("CREATE TABLE subtitle_preferences (video_id INTEGER PRIMARY KEY NOT NULL, selected_uri TEXT, enabled INTEGER NOT NULL DEFAULT 1)")
        db.execSQL("CREATE TABLE subtitle_tracks (video_id INTEGER NOT NULL, uri TEXT NOT NULL, display_name TEXT NOT NULL, manual INTEGER NOT NULL DEFAULT 1, PRIMARY KEY(video_id, uri))")
        db.execSQL("CREATE TABLE watch_time_daily (video_id INTEGER NOT NULL, day_key TEXT NOT NULL, duration_ms INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(video_id, day_key))")
        db.execSQL("""CREATE TABLE reviews (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            video_id INTEGER,
            content TEXT NOT NULL,
            rating INTEGER NOT NULL DEFAULT 0,
            created_at INTEGER NOT NULL,
            title TEXT NOT NULL DEFAULT '',
            cover_path TEXT,
            standalone_group_id INTEGER,
            playback_position_ms INTEGER
        )""".trimIndent())
        db.execSQL("""CREATE TABLE movie_reviews (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            video_id INTEGER NOT NULL,
            title TEXT NOT NULL DEFAULT '',
            content TEXT NOT NULL,
            created_at INTEGER NOT NULL,
            updated_at INTEGER NOT NULL,
            standalone_review_id INTEGER
        )""".trimIndent())
        listOf("全部分类", "我的最爱").forEachIndexed { index, name ->
            val values = ContentValues().apply { put("name", name); put("position", index) }
            db.insert("categories", null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS watch_time_daily (day_key TEXT PRIMARY KEY NOT NULL, duration_ms INTEGER NOT NULL DEFAULT 0)")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE watch_time_daily RENAME TO watch_time_daily_v2")
            db.execSQL("CREATE TABLE watch_time_daily (video_id INTEGER NOT NULL, day_key TEXT NOT NULL, duration_ms INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(video_id, day_key))")
            db.execSQL("INSERT INTO watch_time_daily(video_id, day_key, duration_ms) SELECT -1, day_key, duration_ms FROM watch_time_daily_v2")
            db.execSQL("DROP TABLE watch_time_daily_v2")
        }
        if (oldVersion < 4) {
            db.execSQL("""CREATE TABLE reviews (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                video_id INTEGER,
                content TEXT NOT NULL,
                rating INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL
            )""".trimIndent())
            db.execSQL("""
                INSERT INTO reviews(video_id, content, rating, created_at)
                SELECT id, review, rating,
                    CASE WHEN last_played_at > 0 THEN last_played_at ELSE added_at END
                FROM videos WHERE TRIM(review) <> ''
            """.trimIndent())
            db.execSQL("UPDATE videos SET review = '' WHERE TRIM(review) <> ''")
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE reviews ADD COLUMN title TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE reviews ADD COLUMN cover_path TEXT")
        }
        if (oldVersion < 7) {
            db.execSQL("ALTER TABLE reviews ADD COLUMN standalone_group_id INTEGER")
        }
        if (oldVersion < 8) {
            db.execSQL("ALTER TABLE videos ADD COLUMN is_in_library INTEGER NOT NULL DEFAULT 1")
        }
        if (oldVersion < 9) {
            db.execSQL("ALTER TABLE reviews ADD COLUMN playback_position_ms INTEGER")
        }
        if (oldVersion < 10) {
            db.execSQL("CREATE TABLE IF NOT EXISTS video_folder_sources (video_id INTEGER PRIMARY KEY NOT NULL, tree_uri TEXT NOT NULL, relative_path TEXT NOT NULL DEFAULT '')")
            db.execSQL("CREATE TABLE IF NOT EXISTS subtitle_preferences (video_id INTEGER PRIMARY KEY NOT NULL, selected_uri TEXT, enabled INTEGER NOT NULL DEFAULT 1)")
        }
        if (oldVersion < 11) {
            db.execSQL("CREATE TABLE IF NOT EXISTS subtitle_tracks (video_id INTEGER NOT NULL, uri TEXT NOT NULL, display_name TEXT NOT NULL, manual INTEGER NOT NULL DEFAULT 1, PRIMARY KEY(video_id, uri))")
        }
        if (oldVersion < 12) {
            db.execSQL("ALTER TABLE videos ADD COLUMN original_file_name TEXT")
            db.execSQL("ALTER TABLE videos ADD COLUMN filename_metadata_json TEXT")
            // Existing names may have been manually edited before this feature existed.
            // Protect them rather than guessing whether they are still source filenames.
            db.execSQL("ALTER TABLE videos ADD COLUMN name_manually_edited INTEGER NOT NULL DEFAULT 1")
        }
        if (oldVersion < 13) {
            db.execSQL("ALTER TABLE videos ADD COLUMN playback_position_ms INTEGER")
            db.execSQL("ALTER TABLE videos ADD COLUMN playback_completed INTEGER NOT NULL DEFAULT 0")
        }
        if (oldVersion < 14) {
            db.execSQL("""CREATE TABLE movie_reviews (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                video_id INTEGER NOT NULL,
                title TEXT NOT NULL DEFAULT '',
                content TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )""".trimIndent())
        }
        if (oldVersion < 15) {
            db.execSQL("ALTER TABLE movie_reviews ADD COLUMN standalone_review_id INTEGER")
        }
        if (oldVersion < 5) {
            db.execSQL("""
                UPDATE videos
                SET rating = COALESCE(
                    (
                        SELECT rating FROM reviews
                        WHERE reviews.video_id = videos.id AND reviews.rating > 0
                        ORDER BY created_at ASC, id ASC
                        LIMIT 1
                    ),
                    rating
                )
                WHERE rating = 0
            """.trimIndent())
            db.execSQL("UPDATE reviews SET rating = 0 WHERE rating <> 0")
        }
    }

    @Synchronized fun allVideos(): List<VideoItem> = readableDatabase.rawQuery(
        "SELECT * FROM videos ORDER BY added_at DESC", null
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) add(
                VideoItem(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    uri = cursor.getString(cursor.getColumnIndexOrThrow("uri")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    size = cursor.getString(cursor.getColumnIndexOrThrow("size")),
                    date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    addedAt = cursor.getLong(cursor.getColumnIndexOrThrow("added_at")),
                    progress = cursor.getFloat(cursor.getColumnIndexOrThrow("progress")),
                    status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    isFav = cursor.getInt(cursor.getColumnIndexOrThrow("is_fav")) != 0,
                    category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                    bgIndex = cursor.getInt(cursor.getColumnIndexOrThrow("bg_index")),
                    review = cursor.getString(cursor.getColumnIndexOrThrow("review")),
                    rating = cursor.getFloat(cursor.getColumnIndexOrThrow("rating")),
                    durationMs = cursor.getLong(cursor.getColumnIndexOrThrow("duration_ms")),
                    playbackPositionMs = cursor.getColumnIndexOrThrow("playback_position_ms").let { if (cursor.isNull(it)) null else cursor.getLong(it) },
                    playbackCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("playback_completed")) != 0,
                    lastPlayedAt = cursor.getLong(cursor.getColumnIndexOrThrow("last_played_at")),
                    thumbPath = cursor.getString(cursor.getColumnIndexOrThrow("thumb_path")),
                    isInLibrary = cursor.getInt(cursor.getColumnIndexOrThrow("is_in_library")) != 0,
                    originalFileName = cursor.getString(cursor.getColumnIndexOrThrow("original_file_name")),
                    filenameMetadataJson = cursor.getString(cursor.getColumnIndexOrThrow("filename_metadata_json")),
                    nameManuallyEdited = cursor.getInt(cursor.getColumnIndexOrThrow("name_manually_edited")) != 0
                )
            )
        }
    }

    @Synchronized fun libraryVideos(): List<VideoItem> = allVideos().filter { it.isInLibrary != false }

    @Synchronized fun setInLibrary(id: Long, inLibrary: Boolean) {
        writableDatabase.update(
            "videos",
            ContentValues().apply { put("is_in_library", if (inLibrary) 1 else 0) },
            "id=?",
            arrayOf(id.toString())
        )
    }

    @Synchronized fun updatePlayback(id: Long, positionMs: Long, durationMs: Long, ended: Boolean, lastPlayedAt: Long) {
        val position = positionMs.coerceAtLeast(0L)
        val completed = if (ended) 1 else 0
        writableDatabase.execSQL(
            """
            UPDATE videos SET
                duration_ms = CASE WHEN ? > 0 THEN ? ELSE duration_ms END,
                progress = CASE
                    WHEN ? = 1 THEN 100.0
                    WHEN status = 'watched' OR progress >= 100.0 THEN 100.0
                    WHEN (CASE WHEN ? > 0 THEN ? ELSE duration_ms END) > 0
                        THEN MIN(MAX(?, 0) * 100.0 / (CASE WHEN ? > 0 THEN ? ELSE duration_ms END), 99.9)
                    ELSE progress
                END,
                status = CASE
                    WHEN ? = 1 THEN 'watched'
                    WHEN status = 'watched' OR progress >= 100.0 THEN 'watched'
                    WHEN MAX(?, 0) > 0 OR ((CASE WHEN ? > 0 THEN ? ELSE duration_ms END) <= 0 AND progress > 0)
                        THEN 'playing'
                    ELSE status
                END,
                playback_position_ms = CASE WHEN ? = 1 THEN 0 ELSE MAX(?, 0) END,
                playback_completed = ?,
                last_played_at = ?
            WHERE id = ?
            """.trimIndent(),
            arrayOf(
                durationMs, durationMs,
                completed,
                durationMs, durationMs, position, durationMs, durationMs,
                completed, position, durationMs, durationMs,
                completed, position, completed, lastPlayedAt, id
            )
        )
    }

    @Synchronized fun upsert(video: VideoItem) {
        val v = ContentValues().apply {
            put("id", video.id); put("uri", video.uri); put("name", video.name)
            put("size", video.size); put("date", video.date); put("added_at", video.addedAt)
            put("progress", video.progress); put("status", video.status); put("is_fav", if (video.isFav) 1 else 0)
            put("category", video.category); put("bg_index", video.bgIndex); put("review", video.review)
            put("rating", video.rating); put("duration_ms", video.durationMs)
            if (video.playbackPositionMs == null) putNull("playback_position_ms") else put("playback_position_ms", video.playbackPositionMs)
            put("playback_completed", if (video.playbackCompleted) 1 else 0)
            put("last_played_at", video.lastPlayedAt); put("thumb_path", video.thumbPath)
            put("is_in_library", if (video.isInLibrary == false) 0 else 1)
            put("original_file_name", video.originalFileName)
            put("filename_metadata_json", video.filenameMetadataJson)
            put("name_manually_edited", if (video.nameManuallyEdited) 1 else 0)
        }
        writableDatabase.insertWithOnConflict("videos", null, v, SQLiteDatabase.CONFLICT_REPLACE)
    }

    @Synchronized fun updateAll(videos: List<VideoItem>) {
        writableDatabase.beginTransaction()
        try {
            videos.forEach(::upsert)
            writableDatabase.setTransactionSuccessful()
        } finally { writableDatabase.endTransaction() }
    }

    @Synchronized fun replaceAll(
        videos: List<VideoItem>,
        categories: List<String>,
        reviews: List<ReviewEntry>? = null,
        watchTimes: List<BackupWatchTime> = emptyList(),
        folderSources: List<BackupFolderSource> = emptyList(),
        subtitlePreferences: List<BackupSubtitlePreference> = emptyList(),
        subtitleTracks: List<BackupSubtitleTrack> = emptyList(),
        movieReviews: List<MovieReviewEntry>? = null
    ) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("videos", null, null)
            db.delete("video_folder_sources", null, null)
            db.delete("subtitle_preferences", null, null)
            db.delete("subtitle_tracks", null, null)
            db.delete("watch_time_daily", null, null)
            db.delete("reviews", null, null)
            db.delete("movie_reviews", null, null)
            db.delete("categories", null, null)

            val restoredReviews = reviews ?: videos.filter { it.review.isNotBlank() }.map {
                ReviewEntry(0L, it.id, it.review, it.rating, it.lastPlayedAt.takeIf { time -> time > 0L } ?: it.addedAt)
            }
            val legacyRatings = restoredReviews
                .filter { it.videoId != null && it.rating > 0 }
                .groupBy { it.videoId!! }
                .mapValues { (_, entries) ->
                    entries.minWithOrNull(compareBy<ReviewEntry> { it.createdAt }.thenBy { it.id })?.rating ?: 0f
                }
            val normalizedVideos = videos.map { video ->
                if (video.rating > 0f) video else video.copy(rating = (legacyRatings[video.id] ?: 0).toFloat())
            }

            normalizedVideos.forEach { video ->
                val values = ContentValues().apply {
                    put("id", video.id); put("uri", video.uri); put("name", video.name)
                    put("size", video.size); put("date", video.date); put("added_at", video.addedAt)
                    put("progress", video.progress); put("status", video.status)
                    put("is_fav", if (video.isFav) 1 else 0); put("category", video.category)
                    put("bg_index", video.bgIndex); put("review", "")
                    put("rating", video.rating.coerceIn(0f, 5f))
                    put("duration_ms", video.durationMs)
                    if (video.playbackPositionMs == null) putNull("playback_position_ms") else put("playback_position_ms", video.playbackPositionMs)
                    put("playback_completed", if (video.playbackCompleted) 1 else 0)
                    put("last_played_at", video.lastPlayedAt)
                    put("thumb_path", video.thumbPath)
                     put("is_in_library", if (video.isInLibrary == false) 0 else 1)
                    put("original_file_name", video.originalFileName)
                    put("filename_metadata_json", video.filenameMetadataJson)
                    put("name_manually_edited", if (video.nameManuallyEdited) 1 else 0)
                }
                db.insertWithOnConflict("videos", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            categories.forEachIndexed { index, name ->
                db.insert("categories", null, ContentValues().apply {
                    put("name", name); put("position", index)
                })
            }
            folderSources.forEach { entry ->
                db.insertWithOnConflict("video_folder_sources", null, ContentValues().apply {
                    put("video_id", entry.videoId)
                    put("tree_uri", entry.source.treeUri)
                    put("relative_path", entry.source.relativePath)
                }, SQLiteDatabase.CONFLICT_REPLACE)
            }
            subtitlePreferences.forEach { entry ->
                db.insertWithOnConflict("subtitle_preferences", null, ContentValues().apply {
                    put("video_id", entry.videoId)
                    if (entry.preference.selectedUri == null) putNull("selected_uri") else put("selected_uri", entry.preference.selectedUri)
                    put("enabled", if (entry.preference.enabled) 1 else 0)
                }, SQLiteDatabase.CONFLICT_REPLACE)
            }
            subtitleTracks.forEach { entry ->
                db.insertWithOnConflict("subtitle_tracks", null, ContentValues().apply {
                    put("video_id", entry.videoId)
                    put("uri", entry.track.uri)
                    put("display_name", entry.track.name)
                    put("manual", if (entry.track.manual) 1 else 0)
                }, SQLiteDatabase.CONFLICT_REPLACE)
            }
            watchTimes.forEach { entry ->
                if (entry.durationMs > 0L) db.insertWithOnConflict("watch_time_daily", null, ContentValues().apply {
                    put("video_id", entry.videoId)
                    put("day_key", entry.dayKey)
                    put("duration_ms", entry.durationMs)
                }, SQLiteDatabase.CONFLICT_REPLACE)
            }
            restoredReviews.forEach { review ->
                val values = ContentValues().apply {
                    if (review.id > 0L) put("id", review.id)
                    if (review.videoId == null) putNull("video_id") else put("video_id", review.videoId)
                    put("content", review.content)
                    put("rating", if (review.videoId == null) review.rating.coerceIn(0f, 5f) else 0f)
                    put("created_at", review.createdAt)
                    put("title", review.title)
                    put("cover_path", review.coverPath)
                    if (review.standaloneGroupId == null) putNull("standalone_group_id") else put("standalone_group_id", review.standaloneGroupId)
                    if (review.playbackPositionMs == null) putNull("playback_position_ms") else put("playback_position_ms", review.playbackPositionMs.coerceAtLeast(0L))
                }
                db.insertWithOnConflict("reviews", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            val validVideoIds = videos.map { it.id }.toSet()
            val validStandaloneIds = restoredReviews.filter {
                it.videoId == null && it.standaloneGroupId == null
            }.map { it.id }.toSet()
            movieReviews.orEmpty().forEach { review ->
                val standaloneId = review.standaloneReviewId
                val validOwner = if (standaloneId != null) standaloneId in validStandaloneIds else review.videoId in validVideoIds
                if (review.content.isNotBlank() && validOwner) {
                    val values = ContentValues().apply {
                        if (review.id > 0L) put("id", review.id)
                        put("video_id", if (standaloneId != null) 0L else review.videoId)
                        put("standalone_review_id", standaloneId)
                        put("title", review.title)
                        put("content", review.content)
                        put("created_at", review.createdAt)
                        put("updated_at", review.updatedAt.coerceAtLeast(review.createdAt))
                    }
                    db.insertWithOnConflict("movie_reviews", null, values, SQLiteDatabase.CONFLICT_REPLACE)
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    @Synchronized fun allReviews(): List<ReviewEntry> = readableDatabase.rawQuery(
        "SELECT id, video_id, content, rating, created_at, title, cover_path, standalone_group_id, playback_position_ms FROM reviews ORDER BY created_at DESC, id DESC", null
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                val videoIdIndex = cursor.getColumnIndexOrThrow("video_id")
                add(
                    ReviewEntry(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        videoId = if (cursor.isNull(videoIdIndex)) null else cursor.getLong(videoIdIndex),
                        content = cursor.getString(cursor.getColumnIndexOrThrow("content")),
                        rating = cursor.getFloat(cursor.getColumnIndexOrThrow("rating")),
                        createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
                        title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        coverPath = cursor.getString(cursor.getColumnIndexOrThrow("cover_path")),
                        standaloneGroupId = cursor.getColumnIndexOrThrow("standalone_group_id").let { if (cursor.isNull(it)) null else cursor.getLong(it) },
                        playbackPositionMs = cursor.getColumnIndexOrThrow("playback_position_ms").let { if (cursor.isNull(it)) null else cursor.getLong(it) }
                    )
                )
            }
        }
    }

    @Synchronized fun allMovieReviews(): List<MovieReviewEntry> = readableDatabase.rawQuery(
        "SELECT id, video_id, title, content, created_at, updated_at, standalone_review_id FROM movie_reviews ORDER BY created_at DESC, id DESC", null
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                add(MovieReviewEntry(
                    id = cursor.getLong(0),
                    videoId = cursor.getLong(1),
                    title = cursor.getString(2),
                    content = cursor.getString(3),
                    createdAt = cursor.getLong(4),
                    updatedAt = cursor.getLong(5),
                    standaloneReviewId = if (cursor.isNull(6)) null else cursor.getLong(6)
                ))
            }
        }
    }

    @Synchronized fun addMovieReview(videoId: Long, title: String, content: String, now: Long): Long {
        val values = ContentValues().apply {
            put("video_id", videoId)
            put("title", title)
            put("content", content)
            put("created_at", now)
            put("updated_at", now)
        }
        return writableDatabase.insert("movie_reviews", null, values)
    }

    @Synchronized fun addStandaloneMovieReview(standaloneReviewId: Long, title: String, content: String, now: Long): Long {
        val values = ContentValues().apply {
            put("video_id", 0L)
            put("standalone_review_id", standaloneReviewId)
            put("title", title)
            put("content", content)
            put("created_at", now)
            put("updated_at", now)
        }
        return writableDatabase.insert("movie_reviews", null, values)
    }

    @Synchronized fun updateStandaloneReviewRating(id: Long, rating: Float) {
        writableDatabase.update(
            "reviews",
            ContentValues().apply { put("rating", rating.coerceIn(0f, 5f)) },
            "id=? AND video_id IS NULL AND standalone_group_id IS NULL",
            arrayOf(id.toString())
        )
    }

    @Synchronized fun updateMovieReview(id: Long, title: String, content: String, updatedAt: Long) {
        val values = ContentValues().apply {
            put("title", title)
            put("content", content)
            put("updated_at", updatedAt)
        }
        writableDatabase.update("movie_reviews", values, "id=?", arrayOf(id.toString()))
    }

    @Synchronized fun deleteMovieReview(id: Long) {
        writableDatabase.delete("movie_reviews", "id=?", arrayOf(id.toString()))
    }

    @Synchronized fun addReview(videoId: Long?, content: String, rating: Float, createdAt: Long, title: String = "", coverPath: String? = null, standaloneGroupId: Long? = null, playbackPositionMs: Long? = null): Long {
        val values = ContentValues().apply {
            if (videoId == null) putNull("video_id") else put("video_id", videoId)
            put("content", content)
            put("rating", rating.coerceIn(0f, 5f))
            put("created_at", createdAt)
            put("title", title)
            put("cover_path", coverPath)
            if (standaloneGroupId == null) putNull("standalone_group_id") else put("standalone_group_id", standaloneGroupId)
            if (playbackPositionMs == null) putNull("playback_position_ms") else put("playback_position_ms", playbackPositionMs.coerceAtLeast(0L))
        }
        return writableDatabase.insert("reviews", null, values)
    }

    @Synchronized fun updateReview(id: Long, videoId: Long?, content: String, rating: Float, title: String = "", coverPath: String? = null, standaloneGroupId: Long? = null) {
        val values = ContentValues().apply {
            if (videoId == null) putNull("video_id") else put("video_id", videoId)
            put("content", content)
            put("rating", rating.coerceIn(0f, 5f))
            put("title", title)
            put("cover_path", coverPath)
            if (standaloneGroupId == null) putNull("standalone_group_id") else put("standalone_group_id", standaloneGroupId)
        }
        writableDatabase.update("reviews", values, "id=?", arrayOf(id.toString()))
    }

    @Synchronized fun deleteReview(id: Long) {
        writableDatabase.delete("reviews", "id=?", arrayOf(id.toString()))
    }

    @Synchronized fun saveVideoFolderSource(videoId: Long, source: VideoFolderSource) {
        writableDatabase.insertWithOnConflict(
            "video_folder_sources", null,
            ContentValues().apply {
                put("video_id", videoId)
                put("tree_uri", source.treeUri)
                put("relative_path", source.relativePath)
            },
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    @Synchronized fun videoFolderSource(videoId: Long): VideoFolderSource? =
        readableDatabase.rawQuery(
            "SELECT tree_uri, relative_path FROM video_folder_sources WHERE video_id=?",
            arrayOf(videoId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) VideoFolderSource(cursor.getString(0), cursor.getString(1)) else null
        }

    @Synchronized fun saveSubtitlePreference(videoId: Long, preference: SubtitlePreference) {
        writableDatabase.insertWithOnConflict(
            "subtitle_preferences", null,
            ContentValues().apply {
                put("video_id", videoId)
                if (preference.selectedUri == null) putNull("selected_uri") else put("selected_uri", preference.selectedUri)
                put("enabled", if (preference.enabled) 1 else 0)
            },
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    @Synchronized fun subtitlePreference(videoId: Long): SubtitlePreference =
        readableDatabase.rawQuery(
            "SELECT selected_uri, enabled FROM subtitle_preferences WHERE video_id=?",
            arrayOf(videoId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) SubtitlePreference(
                selectedUri = if (cursor.isNull(0)) null else cursor.getString(0),
                enabled = cursor.getInt(1) != 0
            ) else SubtitlePreference()
        }

    @Synchronized fun saveSubtitleTrack(videoId: Long, track: SubtitleTrackEntry) {
        writableDatabase.insertWithOnConflict(
            "subtitle_tracks", null,
            ContentValues().apply {
                put("video_id", videoId)
                put("uri", track.uri)
                put("display_name", track.name)
                put("manual", if (track.manual) 1 else 0)
            },
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    @Synchronized fun subtitleTracks(videoId: Long): List<SubtitleTrackEntry> =
        readableDatabase.rawQuery(
            "SELECT uri, display_name, manual FROM subtitle_tracks WHERE video_id=? ORDER BY display_name COLLATE NOCASE",
            arrayOf(videoId.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(
                    SubtitleTrackEntry(cursor.getString(0), cursor.getString(1), cursor.getInt(2) != 0)
                )
            }
        }

    @Synchronized fun allWatchTimes(): List<BackupWatchTime> =
        readableDatabase.rawQuery("SELECT video_id, day_key, duration_ms FROM watch_time_daily ORDER BY day_key, video_id", null).use { cursor ->
            buildList { while (cursor.moveToNext()) add(BackupWatchTime(cursor.getLong(0), cursor.getString(1), cursor.getLong(2))) }
        }

    @Synchronized fun allFolderSources(): List<BackupFolderSource> =
        readableDatabase.rawQuery("SELECT video_id, tree_uri, relative_path FROM video_folder_sources", null).use { cursor ->
            buildList { while (cursor.moveToNext()) add(BackupFolderSource(cursor.getLong(0), VideoFolderSource(cursor.getString(1), cursor.getString(2)))) }
        }

    @Synchronized fun allSubtitlePreferences(): List<BackupSubtitlePreference> =
        readableDatabase.rawQuery("SELECT video_id, selected_uri, enabled FROM subtitle_preferences", null).use { cursor ->
            buildList { while (cursor.moveToNext()) add(BackupSubtitlePreference(cursor.getLong(0), SubtitlePreference(if (cursor.isNull(1)) null else cursor.getString(1), cursor.getInt(2) != 0))) }
        }

    @Synchronized fun allSubtitleTracks(): List<BackupSubtitleTrack> =
        readableDatabase.rawQuery("SELECT video_id, uri, display_name, manual FROM subtitle_tracks", null).use { cursor ->
            buildList { while (cursor.moveToNext()) add(BackupSubtitleTrack(cursor.getLong(0), SubtitleTrackEntry(cursor.getString(1), cursor.getString(2), cursor.getInt(3) != 0))) }
        }

    @Synchronized fun addWatchTime(videoId: Long, dayKey: String, durationMs: Long) {
        if (durationMs <= 0L) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            val existing = db.rawQuery(
                "SELECT duration_ms FROM watch_time_daily WHERE video_id = ? AND day_key = ?",
                arrayOf(videoId.toString(), dayKey)
            ).use { cursor -> if (cursor.moveToFirst()) cursor.getLong(0) else 0L }
            db.insertWithOnConflict(
                "watch_time_daily",
                null,
                ContentValues().apply {
                    put("video_id", videoId)
                    put("day_key", dayKey)
                    put("duration_ms", existing + durationMs)
                },
                SQLiteDatabase.CONFLICT_REPLACE
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    @Synchronized fun watchTimeSince(firstDayKey: String): Long = readableDatabase.rawQuery(
        "SELECT COALESCE(SUM(duration_ms), 0) FROM watch_time_daily WHERE day_key >= ?",
        arrayOf(firstDayKey)
    ).use { cursor -> if (cursor.moveToFirst()) cursor.getLong(0) else 0L }

    @Synchronized fun clearWatchTimeForVideo(videoId: Long) {
        writableDatabase.delete("watch_time_daily", "video_id=?", arrayOf(videoId.toString()))
    }

    @Synchronized fun clearWatchTime() {
        writableDatabase.delete("watch_time_daily", null, null)
    }

    @Synchronized fun delete(id: Long) {
        writableDatabase.beginTransaction()
        try {
            writableDatabase.delete("reviews", "video_id=?", arrayOf(id.toString()))
            writableDatabase.delete("movie_reviews", "video_id=?", arrayOf(id.toString()))
            writableDatabase.delete("videos", "id=?", arrayOf(id.toString()))
            writableDatabase.delete("video_folder_sources", "video_id=?", arrayOf(id.toString()))
            writableDatabase.delete("subtitle_preferences", "video_id=?", arrayOf(id.toString()))
            writableDatabase.delete("subtitle_tracks", "video_id=?", arrayOf(id.toString()))
            writableDatabase.setTransactionSuccessful()
        } finally { writableDatabase.endTransaction() }
    }
    @Synchronized fun clear() {
        writableDatabase.beginTransaction()
        try {
            writableDatabase.delete("reviews", null, null)
            writableDatabase.delete("movie_reviews", null, null)
            writableDatabase.delete("videos", null, null)
            writableDatabase.delete("video_folder_sources", null, null)
            writableDatabase.delete("subtitle_preferences", null, null)
            writableDatabase.delete("subtitle_tracks", null, null)
            writableDatabase.setTransactionSuccessful()
        } finally { writableDatabase.endTransaction() }
    }

    @Synchronized fun categories(): List<String> = readableDatabase.rawQuery(
        "SELECT name FROM categories ORDER BY position", null
    ).use { c -> buildList { while (c.moveToNext()) add(c.getString(0)) } }

    @Synchronized fun saveCategories(names: List<String>) {
        writableDatabase.beginTransaction()
        try {
            writableDatabase.delete("categories", null, null)
            names.filterNot { it == "全部" }.distinct().forEachIndexed { index, name ->
                writableDatabase.insert("categories", null, ContentValues().apply {
                    put("name", name); put("position", index)
                })
            }
            writableDatabase.setTransactionSuccessful()
        } finally { writableDatabase.endTransaction() }
    }
}
