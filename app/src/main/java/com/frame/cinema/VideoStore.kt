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
    val rating: Int = 0,
    val createdAt: Long
)

data class LibraryBackup(
    val videos: List<VideoItem>,
    val reviews: List<ReviewEntry>
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
    val rating: Int = 0,
    val durationMs: Long = 0L,
    val lastPlayedAt: Long = 0L,
    val thumbPath: String? = null
) {
    val contentUri: Uri get() = Uri.parse(uri)
}

class VideoStore(context: Context) : SQLiteOpenHelper(context, "frame_library.db", null, 4) {
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
                last_played_at INTEGER NOT NULL DEFAULT 0,
                thumb_path TEXT
            )""".trimIndent()
        )
        db.execSQL("CREATE TABLE categories (name TEXT PRIMARY KEY NOT NULL, position INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE watch_time_daily (video_id INTEGER NOT NULL, day_key TEXT NOT NULL, duration_ms INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(video_id, day_key))")
        db.execSQL("""CREATE TABLE reviews (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            video_id INTEGER,
            content TEXT NOT NULL,
            rating INTEGER NOT NULL DEFAULT 0,
            created_at INTEGER NOT NULL
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
                    rating = cursor.getInt(cursor.getColumnIndexOrThrow("rating")),
                    durationMs = cursor.getLong(cursor.getColumnIndexOrThrow("duration_ms")),
                    lastPlayedAt = cursor.getLong(cursor.getColumnIndexOrThrow("last_played_at")),
                    thumbPath = cursor.getString(cursor.getColumnIndexOrThrow("thumb_path"))
                )
            )
        }
    }

    @Synchronized fun upsert(video: VideoItem) {
        val v = ContentValues().apply {
            put("id", video.id); put("uri", video.uri); put("name", video.name)
            put("size", video.size); put("date", video.date); put("added_at", video.addedAt)
            put("progress", video.progress); put("status", video.status); put("is_fav", if (video.isFav) 1 else 0)
            put("category", video.category); put("bg_index", video.bgIndex); put("review", video.review)
            put("rating", video.rating); put("duration_ms", video.durationMs)
            put("last_played_at", video.lastPlayedAt); put("thumb_path", video.thumbPath)
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

    @Synchronized fun replaceAll(videos: List<VideoItem>, categories: List<String>, reviews: List<ReviewEntry>? = null) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("videos", null, null)
            db.delete("watch_time_daily", null, null)
            db.delete("reviews", null, null)
            db.delete("categories", null, null)
            videos.forEach { video ->
                val values = ContentValues().apply {
                    put("id", video.id); put("uri", video.uri); put("name", video.name)
                    put("size", video.size); put("date", video.date); put("added_at", video.addedAt)
                    put("progress", video.progress); put("status", video.status)
                    put("is_fav", if (video.isFav) 1 else 0); put("category", video.category)
                    put("bg_index", video.bgIndex); put("review", ""); put("rating", video.rating)
                    put("duration_ms", video.durationMs); put("last_played_at", video.lastPlayedAt)
                    put("thumb_path", video.thumbPath)
                }
                db.insertWithOnConflict("videos", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            categories.forEachIndexed { index, name ->
                db.insert("categories", null, ContentValues().apply {
                    put("name", name); put("position", index)
                })
            }
            val restoredReviews = reviews ?: videos.filter { it.review.isNotBlank() }.map {
                ReviewEntry(0L, it.id, it.review, it.rating, it.lastPlayedAt.takeIf { time -> time > 0L } ?: it.addedAt)
            }
            restoredReviews.forEach { review ->
                val values = ContentValues().apply {
                    if (review.id > 0L) put("id", review.id)
                    if (review.videoId == null) putNull("video_id") else put("video_id", review.videoId)
                    put("content", review.content)
                    put("rating", review.rating.coerceIn(0, 5))
                    put("created_at", review.createdAt)
                }
                db.insertWithOnConflict("reviews", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    @Synchronized fun allReviews(): List<ReviewEntry> = readableDatabase.rawQuery(
        "SELECT id, video_id, content, rating, created_at FROM reviews ORDER BY created_at DESC, id DESC", null
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                val videoIdIndex = cursor.getColumnIndexOrThrow("video_id")
                add(
                    ReviewEntry(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        videoId = if (cursor.isNull(videoIdIndex)) null else cursor.getLong(videoIdIndex),
                        content = cursor.getString(cursor.getColumnIndexOrThrow("content")),
                        rating = cursor.getInt(cursor.getColumnIndexOrThrow("rating")),
                        createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }
    }

    @Synchronized fun addReview(videoId: Long?, content: String, rating: Int, createdAt: Long): Long {
        val values = ContentValues().apply {
            if (videoId == null) putNull("video_id") else put("video_id", videoId)
            put("content", content)
            put("rating", rating.coerceIn(0, 5))
            put("created_at", createdAt)
        }
        return writableDatabase.insert("reviews", null, values)
    }

    @Synchronized fun updateReview(id: Long, videoId: Long?, content: String, rating: Int) {
        val values = ContentValues().apply {
            if (videoId == null) putNull("video_id") else put("video_id", videoId)
            put("content", content)
            put("rating", rating.coerceIn(0, 5))
        }
        writableDatabase.update("reviews", values, "id=?", arrayOf(id.toString()))
    }

    @Synchronized fun deleteReview(id: Long) {
        writableDatabase.delete("reviews", "id=?", arrayOf(id.toString()))
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
            writableDatabase.delete("videos", "id=?", arrayOf(id.toString()))
            writableDatabase.setTransactionSuccessful()
        } finally { writableDatabase.endTransaction() }
    }
    @Synchronized fun clear() {
        writableDatabase.beginTransaction()
        try {
            writableDatabase.delete("reviews", null, null)
            writableDatabase.delete("videos", null, null)
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
            names.forEachIndexed { index, name ->
                writableDatabase.insert("categories", null, ContentValues().apply {
                    put("name", name); put("position", index)
                })
            }
            writableDatabase.setTransactionSuccessful()
        } finally { writableDatabase.endTransaction() }
    }
}
