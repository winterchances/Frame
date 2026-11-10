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

class VideoStore(context: Context) : SQLiteOpenHelper(context, "frame_library.db", null, 1) {
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
        listOf("全部分类", "我的最爱").forEachIndexed { index, name ->
            val values = ContentValues().apply { put("name", name); put("position", index) }
            db.insert("categories", null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

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

    @Synchronized fun delete(id: Long) { writableDatabase.delete("videos", "id=?", arrayOf(id.toString())) }
    @Synchronized fun clear() { writableDatabase.delete("videos", null, null) }

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
