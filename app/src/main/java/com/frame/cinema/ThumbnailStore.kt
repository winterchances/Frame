package com.frame.cinema

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/** Persistent thumbnail/cover files; the database stores only their private file paths. */
object ThumbnailStore {
    private fun directory(context: Context): File =
        File(context.filesDir, "video-thumbnails").apply { mkdirs() }

    fun createVideoThumbnail(context: Context, uri: Uri, videoId: Long): String? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val frame = retriever.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime(0)
                ?: return null
            val target = File(directory(context), "$videoId.jpg")
            FileOutputStream(target).use { frame.compress(Bitmap.CompressFormat.JPEG, 78, it) }
            frame.recycle()
            target.absolutePath
        } catch (_: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    fun saveCustomCover(context: Context, source: Uri, videoId: Long): String? = try {
        val target = File(directory(context), "$videoId.jpg")
        context.contentResolver.openInputStream(source)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        target.absolutePath
    } catch (_: Exception) {
        null
    }

    fun delete(context: Context, path: String?) {
        if (!path.isNullOrBlank()) runCatching { File(path).delete() }
    }

    fun clear(context: Context) {
        directory(context).listFiles()?.forEach { it.delete() }
    }
}
