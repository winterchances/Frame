package com.frame.cinema

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes

internal data class SubtitleTrackOption(val uri: Uri, val name: String, val manual: Boolean = false)

private fun subtitleMimeType(uri: Uri, name: String? = null): String {
    val extension = (name ?: uri.lastPathSegment.orEmpty()).substringAfterLast('.', "").lowercase()
    return when (extension) {
        "srt" -> MimeTypes.APPLICATION_SUBRIP
        "ass", "ssa" -> MimeTypes.TEXT_SSA
        "vtt" -> MimeTypes.TEXT_VTT
        else -> MimeTypes.APPLICATION_SUBRIP
    }
}

internal fun subtitleConfiguration(uri: Uri, name: String? = null): MediaItem.SubtitleConfiguration =
    MediaItem.SubtitleConfiguration.Builder(uri)
        .setMimeType(subtitleMimeType(uri, name))
        .setLanguage("und")
        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
        .build()

