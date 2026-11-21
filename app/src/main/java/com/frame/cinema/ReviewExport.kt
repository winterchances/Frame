package com.frame.cinema

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.view.TextureView
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.Activity
import android.content.Intent
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.util.LruCache
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.zIndex
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.C
import androidx.media3.common.MimeTypes
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import android.graphics.BitmapFactory
import android.view.ViewGroup
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt



internal fun wrapReviewExportText(
    text: String,
    paint: android.graphics.Paint,
    maxWidth: Float
): List<String> {
    val lines = mutableListOf<String>()
    text.split('\n').forEach { paragraph ->
        if (paragraph.isEmpty()) {
            lines.add("")
        } else {
            var remaining = paragraph
            while (remaining.isNotEmpty()) {
                val count = paint.breakText(remaining, true, maxWidth, null).coerceAtLeast(1)
                var end = count
                if (count < remaining.length && remaining.substring(0, count).any { it.isLetterOrDigit() }) {
                    val boundary = remaining.substring(0, count).lastIndexOf(' ')
                    if (boundary > 0) end = boundary
                }
                lines.add(remaining.substring(0, end).trimEnd())
                remaining = remaining.substring(end).trimStart()
            }
        }
    }
    return lines.ifEmpty { listOf("") }
}



internal fun splitReviewExportTitle(title: String): Pair<String, String?> =
    splitChineseEnglishTitle(title)

internal fun createReviewExportBitmap(
    video: VideoItem,
    entries: List<ReviewEntry>
): Bitmap {
    val dateFormat = SimpleDateFormat("M/d/yyyy HH:mm", Locale.getDefault())
    val bodyPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(68, 75, 73)
        textSize = 32f
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
    }
    val layouts = entries.map { entry ->
        (dateFormat.format(Date(entry.createdAt)) +
            (entry.playbackPositionMs?.let { " | ${formatTime(it)}" } ?: "")) to
            wrapReviewExportText(entry.content, bodyPaint, 850f)
    }
    return createUnifiedReviewExportBitmap(
        title = displayVideoName(video.name),
        coverPath = video.thumbPath,
        rating = video.rating,
        reviews = layouts
    )
}

internal fun createStandaloneReviewExportBitmap(
    entry: ReviewEntry,
    timelineEntries: List<ReviewEntry>
): Bitmap {
    val dateFormat = SimpleDateFormat("M/d/yyyy HH:mm", Locale.getDefault())
    val bodyPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(68, 75, 73)
        textSize = 32f
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
    }
    val layouts = timelineEntries.map { item ->
        dateFormat.format(Date(item.createdAt)) to
            wrapReviewExportText(item.content, bodyPaint, 850f)
    }
    return createUnifiedReviewExportBitmap(
        title = entry.title.ifBlank { "未命名" },
        coverPath = entry.coverPath,
        rating = entry.rating,
        reviews = layouts
    )
}

internal fun createUnifiedReviewExportBitmap(
    title: String,
    coverPath: String?,
    rating: Float,
    reviews: List<Pair<String, List<String>>>
): Bitmap {
    val width = 1080
    val margin = 72f
    val coverTop = 190f
    val coverWidth = 292f
    val coverHeight = 390f
    val sectionTop = coverTop + coverHeight + 66f
    val firstReviewTop = sectionTop + 42f

    val titlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(38, 43, 42)
        textSize = 46f
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
    }
    val bodyPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(68, 75, 73)
        textSize = 32f
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
    }
    val metaPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(137, 149, 145)
        textSize = 23f
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
    }
    val accentColor = android.graphics.Color.rgb(127, 181, 177)
    val accentPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = accentColor
        strokeWidth = 5f
        strokeCap = android.graphics.Paint.Cap.ROUND
    }
    val (chineseTitle, englishTitle) = splitReviewExportTitle(title)
    val titleLines = wrapReviewExportText(chineseTitle, titlePaint, 560f)
    val englishTitlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(68, 75, 73)
        textSize = 34f
        typeface = android.graphics.Typeface.create("serif", android.graphics.Typeface.NORMAL)
    }
    val englishTitleLines = englishTitle?.let { wrapReviewExportText(it, englishTitlePaint, 560f) }.orEmpty()
    val timelineEntryHeights = reviews.map { (_, lines) ->
        76f + lines.size * 48f
    }
    var contentBottom = firstReviewTop
    timelineEntryHeights.forEach { entryHeight ->
        contentBottom += entryHeight + 12f
    }
    val contentHeight = (contentBottom + 150f).toInt().coerceAtLeast(1050)
    val exportHeight = maxOf(1440, contentHeight)

    val bitmap = Bitmap.createBitmap(width, exportHeight, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.rgb(246, 248, 247))

    val cardPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
    }
    canvas.drawRoundRect(
        android.graphics.RectF(28f, 28f, width - 28f, exportHeight - 28f),
        32f, 32f, cardPaint
    )


    val eyebrowPaint = android.graphics.Paint(metaPaint).apply {
        color = android.graphics.Color.rgb(112, 151, 143)
        textSize = 23f
        letterSpacing = 2.2f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    }
    val eyebrow = "FRAME  〡  JOURNAL"
    val eyebrowWidth = eyebrowPaint.measureText(eyebrow) +
        eyebrowPaint.letterSpacing * (eyebrow.length - 1).coerceAtLeast(0)
    val eyebrowScale = minOf(1f, (width - margin * 2f) / eyebrowWidth)
    canvas.save()
    canvas.scale(eyebrowScale, 1f, margin, 108f)
    canvas.drawText(eyebrow, margin, 108f, eyebrowPaint)
    canvas.restore()
    canvas.drawLine(margin, 137f, margin + 76f, 137f, accentPaint)

    val coverRect = android.graphics.RectF(
        margin, coverTop, margin + coverWidth, coverTop + coverHeight
    )
    val coverClipPath = android.graphics.Path().apply {
        addRoundRect(coverRect, 22f, 22f, android.graphics.Path.Direction.CW)
    }
    canvas.save()
    canvas.clipPath(coverClipPath)
    canvas.drawColor(android.graphics.Color.rgb(237, 242, 240))
    val cover = coverPath?.let { path ->
        runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
    }
    if (cover != null) {
        val scale = maxOf(coverRect.width() / cover.width, coverRect.height() / cover.height)
        val matrix = android.graphics.Matrix().apply {
            setScale(scale, scale)
            postTranslate(
                coverRect.left + (coverRect.width() - cover.width * scale) / 2f,
                coverRect.top + (coverRect.height() - cover.height * scale) / 2f
            )
        }
        canvas.drawBitmap(
            cover, matrix,
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
        )
        cover.recycle()
    } else {
        val placeholderPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(151, 171, 165)
            textSize = 27f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        }
        canvas.drawText("一帧", coverRect.centerX(), coverRect.centerY(), placeholderPaint)
    }
    canvas.restore()

    val textLeft = margin + coverWidth + 48f
    var titleY = coverTop + 58f
    titleLines.forEach { line ->
        canvas.drawText(line, textLeft, titleY, titlePaint)
        titleY += 60f
    }
    if (englishTitleLines.isNotEmpty()) {
        titleY += 2f
        englishTitleLines.forEach { line ->
            canvas.drawText(line, textLeft, titleY, englishTitlePaint)
            titleY += 46f
        }
    }
    if (rating > 0f) {
        val ratingPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 34f
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        }
        canvas.drawText(
            "★  ${String.format(Locale.getDefault(), "%.1f", rating)}",
            textLeft, maxOf(titleY + 42f, coverTop + 250f), ratingPaint
        )
    }
    val notePaint = android.graphics.Paint(metaPaint).apply {
        color = android.graphics.Color.rgb(155, 164, 161)
        textSize = 27f
        typeface = android.graphics.Typeface.create("serif", android.graphics.Typeface.NORMAL)
    }
    canvas.drawText("A frame, A moment.", textLeft, coverTop + coverHeight - 24f, notePaint)

    val dividerPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(232, 238, 235)
        strokeWidth = 2f
    }
    canvas.drawLine(margin, sectionTop, width - margin, sectionTop, dividerPaint)

    var cursorY = firstReviewTop
    reviews.forEachIndexed { index, (date, lines) ->
        val entryHeight = timelineEntryHeights[index]
        val dotPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawCircle(margin + 22f, cursorY + 24f, 7f, dotPaint)
        canvas.drawText(date, margin + 48f, cursorY + 31f, metaPaint)
        var bodyY = cursorY + 76f
        lines.forEach { line ->
            canvas.drawText(line, margin + 48f, bodyY, bodyPaint)
            bodyY += 48f
        }
        cursorY += entryHeight + 12f
    }

    val footerPaint = android.graphics.Paint(metaPaint).apply {
        textSize = 22f
        textAlign = android.graphics.Paint.Align.CENTER
        color = android.graphics.Color.rgb(151, 163, 159)
        letterSpacing = 1.1f
    }
    canvas.drawText("一帧  ·  FRAME", width / 2f, exportHeight - 96f, footerPaint)
    return bitmap
}

internal suspend fun saveVideoScreenshot(context: android.content.Context, bitmap: Bitmap): Boolean =
    withContext(Dispatchers.IO) {
        val fileName = "一帧_${SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())}.png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(
                    android.provider.MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/一帧"
                )
                put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return@withContext false
            val written = runCatching {
                resolver.openOutputStream(uri)?.use { output ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                } ?: false
            }.getOrDefault(false)
            if (written) {
                val completed = ContentValues().apply {
                    put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
                }
                resolver.update(uri, completed, null, null)
                true
            } else {
                resolver.delete(uri, null, null)
                false
            }
        } else {
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "一帧"
            )
            if (!directory.exists() && !directory.mkdirs()) return@withContext false
            val file = File(directory, fileName)
            val written = runCatching {
                java.io.FileOutputStream(file).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                }
            }.getOrDefault(false)
            if (written) {
                MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
            } else {
                file.delete()
            }
            written
        }
    }

@Composable
