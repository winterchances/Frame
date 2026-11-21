package com.frame.cinema

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/** Persistent thumbnail/cover files; the database stores only their private file paths. */
data class VideoMediaDetails(val thumbnailPath: String?, val durationMs: Long)

object ThumbnailStore {
    fun createVideoMetadata(context: Context, uri: Uri, videoId: Long): VideoMediaDetails {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            val frame = retriever.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime(0)
            val path = if (frame != null) {
                val target = File(directory(context), "$videoId.jpg")
                try {
                    val compressed = FileOutputStream(target).use {
                        frame.compress(Bitmap.CompressFormat.JPEG, 78, it)
                    }
                    if (compressed) target.absolutePath else {
                        target.delete()
                        null
                    }
                } finally {
                    frame.recycle()
                }
            } else null
            VideoMediaDetails(path, duration)
        } catch (_: Exception) {
            VideoMediaDetails(null, 0L)
        } finally {
            runCatching { retriever.release() }
        }
    }
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
            try {
                val compressed = FileOutputStream(target).use {
                    frame.compress(Bitmap.CompressFormat.JPEG, 78, it)
                }
                if (compressed) target.absolutePath else {
                    target.delete()
                    null
                }
            } finally {
                frame.recycle()
            }
        } catch (_: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    fun saveCustomCover(context: Context, source: Uri, videoId: Long): String? {
        return try {
            val target = File(directory(context), "$videoId-custom-${System.currentTimeMillis()}.jpg")
            val input = context.contentResolver.openInputStream(source) ?: return null
            input.use { stream -> target.outputStream().use { output -> stream.copyTo(output) } }
            target.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun delete(context: Context, path: String?) {
        if (!path.isNullOrBlank()) runCatching { File(path).delete() }
    }

    fun clear(context: Context) {
        directory(context).listFiles()?.forEach { it.delete() }
    }
}
