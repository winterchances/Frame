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
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import android.graphics.BitmapFactory
import android.view.ViewGroup
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.text.Collator
import java.math.BigInteger
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt


private val FrameHomeIcon: ImageVector = ImageVector.Builder("FrameHome", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(3f, 9f); lineTo(12f, 2f); lineTo(21f, 9f); lineTo(21f, 20f); quadTo(21f, 22f, 19f, 22f); lineTo(5f, 22f); quadTo(3f, 22f, 3f, 20f); close()
        moveTo(9f, 22f); lineTo(9f, 12f); lineTo(15f, 12f); lineTo(15f, 22f)
    }
}.build()
private val FrameLibraryIcon: ImageVector = ImageVector.Builder("FrameLibrary", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(4.5f, 2f); lineTo(19.5f, 2f); quadTo(22f, 2f, 22f, 4.5f); lineTo(22f, 19.5f); quadTo(22f, 22f, 19.5f, 22f); lineTo(4.5f, 22f); quadTo(2f, 22f, 2f, 19.5f); lineTo(2f, 4.5f); quadTo(2f, 2f, 4.5f, 2f); close()
        moveTo(7f, 2f); lineTo(7f, 22f); moveTo(17f, 2f); lineTo(17f, 22f); moveTo(2f, 12f); lineTo(22f, 12f)
        moveTo(2f, 7f); lineTo(7f, 7f); moveTo(2f, 17f); lineTo(7f, 17f); moveTo(17f, 7f); lineTo(22f, 7f); moveTo(17f, 17f); lineTo(22f, 17f)
    }
}.build()
private val FrameRecentIcon: ImageVector = ImageVector.Builder("FrameRecent", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(12f, 2f)
        curveTo(17.52f, 2f, 22f, 6.48f, 22f, 12f)
        curveTo(22f, 17.52f, 17.52f, 22f, 12f, 22f)
        curveTo(6.48f, 22f, 2f, 17.52f, 2f, 12f)
        curveTo(2f, 6.48f, 6.48f, 2f, 12f, 2f)
        close()
        moveTo(12f, 6f); lineTo(12f, 12f); lineTo(16f, 14f)
    }
}.build()
private val FrameTimelineClockIcon: ImageVector = ImageVector.Builder("FrameTimelineClock", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color(0xFF888888)), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(12f, 3f); curveTo(16.97f, 3f, 21f, 7.03f, 21f, 12f)
        curveTo(21f, 16.97f, 16.97f, 21f, 12f, 21f)
        curveTo(7.03f, 21f, 3f, 16.97f, 3f, 12f)
        curveTo(3f, 7.03f, 7.03f, 3f, 12f, 3f); close()
        moveTo(12f, 7f); lineTo(12f, 12f); lineTo(15f, 15f)
    }
}.build()
private val FrameTimelinePlayIcon: ImageVector = ImageVector.Builder("FrameTimelinePlay", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color(0xFF888888)), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(5f, 3f); lineTo(19f, 12f); lineTo(5f, 21f); close()
    }
}.build()
private val FrameRecordsIcon: ImageVector = ImageVector.Builder("FrameRecords", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(4f, 19.5f); quadTo(4f, 17f, 6.5f, 17f); lineTo(20f, 17f); moveTo(6.5f, 2f); lineTo(20f, 2f); lineTo(20f, 22f); lineTo(6.5f, 22f); quadTo(4f, 22f, 4f, 19.5f); lineTo(4f, 4.5f); quadTo(4f, 2f, 6.5f, 2f); close()
    }
}.build()
private val FrameTagIcon: ImageVector = ImageVector.Builder("FrameTag", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(20.59f, 13.41f); lineTo(13.42f, 20.58f); quadTo(12.25f, 21.75f, 11.01f, 20.58f); lineTo(2f, 12f); lineTo(2f, 2f); lineTo(12f, 2f); lineTo(20.59f, 10.59f); quadTo(21.76f, 11.76f, 20.59f, 13.41f); close()
        moveTo(7f, 7f); lineTo(7.01f, 7f)
    }
}.build()
private val FrameSortIcon: ImageVector = ImageVector.Builder("FrameSort", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(7f, 3f); lineTo(7f, 21f)
        moveTo(3f, 7f); lineTo(7f, 3f); lineTo(11f, 7f)
        moveTo(17f, 21f); lineTo(17f, 3f)
        moveTo(13f, 17f); lineTo(17f, 21f); lineTo(21f, 17f)
    }
}.build()

private val FrameSortDirectionIcon: ImageVector = ImageVector.Builder("FrameSortDirection", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(12f, 20f); lineTo(12f, 4f)
        moveTo(6f, 10f); lineTo(12f, 4f); lineTo(18f, 10f)
    }
}.build()
private val FrameGridIcon: ImageVector = ImageVector.Builder("FrameGrid", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(3f, 3f); lineTo(10f, 3f); lineTo(10f, 10f); lineTo(3f, 10f); close()
        moveTo(14f, 3f); lineTo(21f, 3f); lineTo(21f, 10f); lineTo(14f, 10f); close()
        moveTo(14f, 14f); lineTo(21f, 14f); lineTo(21f, 21f); lineTo(14f, 21f); close()
        moveTo(3f, 14f); lineTo(10f, 14f); lineTo(10f, 21f); lineTo(3f, 21f); close()
    }
}.build()

private val FrameSearchIcon: ImageVector = ImageVector.Builder("FrameSearch", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(11f, 3f); curveTo(15.42f, 3f, 19f, 6.58f, 19f, 11f); curveTo(19f, 15.42f, 15.42f, 19f, 11f, 19f); curveTo(6.58f, 19f, 3f, 15.42f, 3f, 11f); curveTo(3f, 6.58f, 6.58f, 3f, 11f, 3f); close()
        moveTo(21f, 21f); lineTo(16.65f, 16.65f)
    }
}.build()
private val FrameTrashIcon: ImageVector = ImageVector.Builder("FrameTrash", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(3f, 6f); lineTo(21f, 6f); moveTo(19f, 6f); lineTo(19f, 20f); quadTo(19f, 22f, 17f, 22f); lineTo(7f, 22f); quadTo(5f, 22f, 5f, 20f); lineTo(5f, 6f)
        moveTo(8f, 6f); lineTo(8f, 4f); quadTo(8f, 2f, 10f, 2f); lineTo(14f, 2f); quadTo(16f, 2f, 16f, 4f); lineTo(16f, 6f)
    }
}.build()

private val FrameHamburgerIcon: ImageVector = ImageVector.Builder("FrameMenu", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color(0xFF333333)), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(5f, 8f); lineTo(19f, 8f); moveTo(5f, 16f); lineTo(19f, 16f)
    }
}.build()

private val PageBg = Color(0xFFF5F5F7)
private val MainText = Color(0xFF1A1A1A)
private val ThemeTeal = Color(0xFF7FB5B1)
private val MutedText = Color(0xFF888888)
private val Glass = Color.White.copy(alpha = .85f)

private val thumbnailBitmapCache = object : LruCache<String, Bitmap>(
    (Runtime.getRuntime().maxMemory() / 1024L / 4L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
) {
    override fun sizeOf(key: String, value: Bitmap): Int = (value.byteCount / 1024).coerceAtLeast(1)
}

private fun buildFrameStarIcon(name: String, filled: Boolean): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        path(
            fill = if (filled) SolidColor(ThemeTeal) else null,
            stroke = SolidColor(ThemeTeal),
            strokeLineWidth = 1.35f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 2.6f)
            curveTo(12.35f, 2.6f, 12.58f, 2.82f, 12.74f, 3.16f)
            lineTo(15.05f, 8.12f)
            curveTo(15.18f, 8.38f, 15.42f, 8.56f, 15.71f, 8.6f)
            lineTo(21.18f, 9.39f)
            curveTo(21.58f, 9.45f, 21.74f, 9.94f, 21.45f, 10.23f)
            lineTo(17.49f, 14.09f)
            curveTo(17.28f, 14.29f, 17.19f, 14.57f, 17.24f, 14.85f)
            lineTo(18.18f, 20.28f)
            curveTo(18.25f, 20.68f, 17.83f, 20.98f, 17.47f, 20.79f)
            lineTo(12.58f, 18.22f)
            curveTo(12.32f, 18.08f, 12.02f, 18.08f, 11.76f, 18.22f)
            lineTo(6.87f, 20.79f)
            curveTo(6.51f, 20.98f, 6.09f, 20.68f, 6.16f, 20.28f)
            lineTo(7.1f, 14.85f)
            curveTo(7.15f, 14.57f, 7.06f, 14.29f, 6.85f, 14.09f)
            lineTo(2.89f, 10.23f)
            curveTo(2.6f, 9.94f, 2.76f, 9.45f, 3.16f, 9.39f)
            lineTo(8.63f, 8.6f)
            curveTo(8.92f, 8.56f, 9.16f, 8.38f, 9.29f, 8.12f)
            lineTo(11.6f, 3.16f)
            curveTo(11.76f, 2.82f, 11.99f, 2.6f, 12f, 2.6f)
            close()
        }
    }.build()

private val FrameStarOutlineIcon = buildFrameStarIcon("FrameStarOutline", false)
private val FrameStarFilledIcon = buildFrameStarIcon("FrameStarFilled", true)

private enum class MainPage(val label: String) { HOME("首页"), LIBRARY("片库"), RECENT("最近"), RECORDS("记录") }
private enum class SheetType { NONE, IMPORT, SETTINGS, SEARCH, RECORD_SEARCH, RENAME, MOVE, NOTE, REVIEW_CREATE_OPTIONS, REVIEW_VIDEO_PICKER, CLEAR_RECENT, CLEAR_DATA, DELETE_RECENT, DELETE_CATEGORY, NEW_CATEGORY }
private data class SubtitleTrackOption(val uri: Uri, val name: String, val manual: Boolean = false)

private fun subtitleMimeType(uri: Uri, name: String? = null): String {
    val extension = (name ?: uri.lastPathSegment.orEmpty()).substringAfterLast('.', "").lowercase()
    return when (extension) {
        "srt" -> MimeTypes.APPLICATION_SUBRIP
        "ass", "ssa" -> MimeTypes.TEXT_SSA
        "vtt" -> MimeTypes.TEXT_VTT
        else -> MimeTypes.APPLICATION_SUBRIP
    }
}

private fun subtitleConfiguration(uri: Uri, name: String? = null): MediaItem.SubtitleConfiguration =
    MediaItem.SubtitleConfiguration.Builder(uri)
        .setMimeType(subtitleMimeType(uri, name))
        .setLanguage("und")
        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
        .build()

private enum class SortMode { ADDED, NAME }
private enum class SortDirection { ASCENDING, DESCENDING }
private enum class RecordSortMode { ADDED, RECORD_TIME, NAME, WATCHED, RATING }

class MainActivity : ComponentActivity() {
    companion object {
        const val ACTION_OPEN_PLAYER = "com.frame.cinema.OPEN_PLAYER"
    }

    private var player: Player? = null
    private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null
    private lateinit var repository: NativeLibraryRepository
    private var backgroundPlaybackAllowed = true
    private val openPlayerRequest = mutableIntStateOf(0)
    private val appIconBytes: ByteArray? by lazy {
        runCatching {
            val drawable = packageManager.getApplicationIcon(packageName)
            val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
            try {
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, 256, 256)
                drawable.draw(canvas)
                java.io.ByteArrayOutputStream().use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.toByteArray()
                }
            } finally {
                bitmap.recycle()
            }
        }.getOrNull()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action == ACTION_OPEN_PLAYER) {
            openPlayerRequest.intValue++
            intent.action = null
        }
        window.statusBarColor = android.graphics.Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        repository = NativeLibraryRepository(this)
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val future = MediaController.Builder(this, token).buildAsync()
        controllerFuture = future
        future.addListener({
            runOnUiThread {
                runCatching { future.get() }.onSuccess { controller ->
                    player = controller
                    setContent {
                        MaterialTheme(colorScheme = lightColorScheme(background = PageBg, surface = PageBg, primary = ThemeTeal, onBackground = MainText)) {
                            FrameNativeApp(
                                repository = repository,
                                player = controller,
                                openPlayerRequest = openPlayerRequest,
                                onPlay = { item -> startNativePlayback(item) },
                                onPlayAt = { item, positionMs -> startNativePlayback(item, positionMs) },
                                onBackgroundPlaybackChanged = { backgroundPlaybackAllowed = it },
                                onOrientation = { requested ->
                                    requestedOrientation = if (requested) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                },
                                onKeepAwake = { enabled ->
                                    if (enabled) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                    else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                },
                                onSystemBarsVisible = { visible ->
                                    val controller = WindowInsetsControllerCompat(window, window.decorView)
                                    if (visible) controller.show(WindowInsetsCompat.Type.statusBars()) else controller.hide(WindowInsetsCompat.Type.statusBars())
                                },
                                onCategoryDrawerOverlay = { progress ->
                                    val channel = (255f * (1f - 0.28f * progress.coerceIn(0f, 1f))).toInt().coerceIn(0, 255)
                                    window.statusBarColor = android.graphics.Color.rgb(channel, channel, channel)
                                }
                            )
                        }
                    }
                }
            }
        }, java.util.concurrent.Executor { command -> command.run() })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == ACTION_OPEN_PLAYER) {
            openPlayerRequest.intValue++
            intent.action = null
        }
        setIntent(intent)
    }

    private fun startNativePlayback(item: VideoItem, positionMs: Long? = null) {
        val controller = player ?: return
        val iconBytes = appIconBytes
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(item.name)
        if (iconBytes != null) {
            metadataBuilder.setArtworkData(iconBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
        }
        val subtitlePreference = repository.subtitlePreference(item.id)
        val matchedSubtitles = repository.findSiblingSubtitles(item.id, item.name)
        val selectedSubtitle = subtitlePreference.selectedUri?.let { saved ->
            matchedSubtitles.firstOrNull { it.first.toString() == saved }
                ?: runCatching {
                    val uri = Uri.parse(saved)
                    val name = uri.lastPathSegment?.substringAfterLast('/') ?: "外部字幕"
                    uri to name
                }.getOrNull()
        } ?: if (subtitlePreference.enabled) matchedSubtitles.firstOrNull() else null
        val readableSubtitle = selectedSubtitle?.takeIf { candidate ->
            runCatching {
                contentResolver.openFileDescriptor(candidate.first, "r")?.use { true } ?: false
            }.getOrDefault(false)
        }
        if (subtitlePreference.selectedUri == null && subtitlePreference.enabled && readableSubtitle != null) {
            repository.saveSubtitlePreference(item.id, SubtitlePreference(readableSubtitle.first.toString(), true))
        }
        val mediaItemBuilder = MediaItem.Builder()
            .setMediaId(item.id.toString())
            .setUri(item.contentUri)
            .setMediaMetadata(metadataBuilder.build())
        if (subtitlePreference.enabled && readableSubtitle != null) {
            mediaItemBuilder.setSubtitleConfigurations(listOf(subtitleConfiguration(readableSubtitle.first, readableSubtitle.second)))
        }
        val mediaItem = mediaItemBuilder.build()
        controller.setMediaItem(mediaItem)
        controller.prepare()
        if (positionMs != null) controller.seekTo(positionMs.coerceAtLeast(0L))
        else if (item.progress >= 99f || item.status == "watched") controller.seekTo(0L)
        else if (item.durationMs > 0 && item.progress > 0f) controller.seekTo((item.durationMs * item.progress / 100f).toLong())
        controller.play()
    }

    override fun onStop() {
        if (!backgroundPlaybackAllowed) player?.pause()
        super.onStop()
    }

    override fun onDestroy() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        player = null
        if (::repository.isInitialized) repository.close()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun FrameNativeApp(
    repository: NativeLibraryRepository,
    player: Player,
    openPlayerRequest: State<Int>,
    onPlay: (VideoItem) -> Unit,
    onPlayAt: (VideoItem, Long) -> Unit,
    onBackgroundPlaybackChanged: (Boolean) -> Unit,
    onOrientation: (Boolean) -> Unit,
    onKeepAwake: (Boolean) -> Unit,
    onSystemBarsVisible: (Boolean) -> Unit,
    onCategoryDrawerOverlay: (Float) -> Unit
) {
    val context = LocalContext.current
    val openPlayerRequestValue by openPlayerRequest
    val appVersion = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }
    val hazeState = remember { HazeState() }
    val homeListState = rememberLazyListState()
    val libraryListState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    val libraryStickyCategoryScrollState = rememberScrollState()
    val libraryFloatingCategoryScrollState = rememberScrollState()
    val recentListState = rememberLazyListState()
    val recordsListState = rememberLazyListState()
    var page by remember { mutableStateOf(MainPage.HOME) }
    var libraryMultiSelectMode by remember { mutableStateOf(false) }
    var showCategoryDrawer by remember { mutableStateOf(false) }
    var categoryEditMode by remember { mutableStateOf(false) }
    var editingCategoryName by remember { mutableStateOf<String?>(null) }
    var editingCategoryDraft by remember { mutableStateOf("") }
    var pendingCategoryDelete by remember { mutableStateOf<String?>(null) }
    val categoryRenameFocusRequester = remember { FocusRequester() }
    LaunchedEffect(editingCategoryName) { if (editingCategoryName != null) categoryRenameFocusRequester.requestFocus() }
    var sheet by remember { mutableStateOf(SheetType.NONE) }
    var parentSheet by remember { mutableStateOf(SheetType.NONE) }
    var sheetKeyboardUp by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<VideoItem?>(null) }
    var contextMenuVideo by remember { mutableStateOf<VideoItem?>(null) }
    var contextMenuBounds by remember { mutableStateOf<Rect?>(null) }
    var contextMenuStartBounds by remember { mutableStateOf<Rect?>(null) }
    var contextMenuViewportBounds by remember { mutableStateOf<Rect?>(null) }
    var contextMenuStickyHeaderBounds by remember { mutableStateOf<Rect?>(null) }
    var contextMenuNavigationBounds by remember { mutableStateOf<Rect?>(null) }
    var contextMenuFabBounds by remember { mutableStateOf<Rect?>(null) }
    var contextMenuOpen by remember { mutableStateOf(false) }
    var videos by remember { mutableStateOf(repository.videos()) }
    var reviewEntries by remember { mutableStateOf(repository.reviews()) }
    var weeklyWatchTimeMs by remember { mutableLongStateOf(repository.weeklyWatchTimeMs()) }
    var categories by remember { mutableStateOf(repository.categories().let { saved ->
        val ordered = listOf("我的最爱") + saved.filterNot { it == "全部" || it == "全部分类" || it == "我的最爱" }.distinct() + "全部分类"
        if (ordered != saved) repository.saveCategories(ordered)
        ordered
    }) }
    var status by remember { mutableStateOf("全部") }
    var category by remember { mutableStateOf("全部") }
    var compact by remember { mutableStateOf(true) }
    var showFab by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var recordSearch by remember { mutableStateOf("") }
    var reviewVideoSearch by remember { mutableStateOf("") }
    var targetReviewId by remember { mutableStateOf<Long?>(null) }
    var targetRecordVideoId by remember { mutableStateOf<Long?>(null) }
    var recordSortMode by remember { mutableStateOf(RecordSortMode.WATCHED) }
    var recordSortDirection by remember { mutableStateOf(SortDirection.DESCENDING) }
    var editName by remember { mutableStateOf("") }
    var editReview by remember { mutableStateOf("") }
    var editRating by remember { mutableFloatStateOf(0f) }
    var editReviewTitle by remember { mutableStateOf("") }
    var editReviewCoverUri by remember { mutableStateOf<Uri?>(null) }
    var editReviewCoverPath by remember { mutableStateOf<String?>(null) }
    var editReviewId by remember { mutableStateOf<Long?>(null) }
    var editReviewVideoId by remember { mutableStateOf<Long?>(null) }
    var editStandaloneGroupId by remember { mutableStateOf<Long?>(null) }
    var showReviewVideoActions by remember { mutableStateOf(true) }
    var newCategoryName by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(SortMode.ADDED) }
    var sortDirection by remember { mutableStateOf(SortDirection.DESCENDING) }
    val videoById = remember(videos) { videos.associateBy { it.id } }
    val sortedLibraryVideos = remember(videos, sort, sortDirection) {
        videos.sortedWith { a, b ->
            val primary = when (sort) {
                SortMode.NAME -> compareNaturalVideoNames(a.name, b.name)
                SortMode.ADDED -> a.addedAt.compareTo(b.addedAt)
            }
            val directed = if (sortDirection == SortDirection.ASCENDING) primary else -primary
            if (directed != 0) directed else {
                val addedComparison = a.addedAt.compareTo(b.addedAt)
                if (addedComparison != 0) addedComparison else a.id.compareTo(b.id)
            }
        }
    }
    var allowBackground by remember { mutableStateOf(true) }
    var keepAwake by remember { mutableStateOf(true) }
    val preferences = remember { context.getSharedPreferences("frame_preferences", android.content.Context.MODE_PRIVATE) }
    LaunchedEffect(Unit) {
        allowBackground = preferences.getBoolean("allow_background", true)
        keepAwake = preferences.getBoolean("keep_awake", true)
        onBackgroundPlaybackChanged(allowBackground)
        onKeepAwake(keepAwake)
    }
    var playing by remember { mutableStateOf<VideoItem?>(null) }
    var isLandscape by remember { mutableStateOf(false) }
    var speedIndex by remember { mutableIntStateOf(0) }
    val speeds = listOf(1f, 1.25f, 1.5f, 2f, .75f)
    var controlsVisible by remember { mutableStateOf(true) }
    var locked by remember { mutableStateOf(false) }
    LaunchedEffect(openPlayerRequestValue) {
        if (openPlayerRequestValue > 0) {
            val mediaId = player.currentMediaItem?.mediaId
            val currentVideo = mediaId?.let { id -> repository.videos().firstOrNull { it.id.toString() == id } }
            if (currentVideo != null) {
                playing = currentVideo
                controlsVisible = true
                locked = false
                onSystemBarsVisible(false)
            }
        }
    }
    var toast by remember { mutableStateOf("") }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showImportDataDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    val sheetProgress by animateFloatAsState(
        targetValue = if (sheet == SheetType.NONE) 0f else 1f,
        animationSpec = tween(if (sheet == SheetType.NONE) 240 else 340, easing = if (sheet == SheetType.NONE) androidx.compose.animation.core.CubicBezierEasing(.4f, 0f, 1f, 1f) else androidx.compose.animation.core.CubicBezierEasing(.34f, 1.56f, .64f, 1f)),
        label = "sheet-background-progress"
    )
    val clearDialogProgress by animateFloatAsState(
        targetValue = if (showClearDataDialog) 1f else 0f,
        animationSpec = tween(if (showClearDataDialog) 300 else 240, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f)),
        label = "clear-data-backdrop-progress"
    )
    val importDialogProgress by animateFloatAsState(
        targetValue = if (showImportDataDialog) 1f else 0f,
        animationSpec = tween(if (showImportDataDialog) 300 else 240, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f)),
        label = "import-data-backdrop-progress"
    )
    val contextMenuProgress by animateFloatAsState(if (contextMenuOpen) 1f else 0f, tween(200), label = "library-context-backdrop")
    val categoryDrawerProgress by animateFloatAsState(
        targetValue = if (showCategoryDrawer) 1f else 0f,
        animationSpec = tween(if (showCategoryDrawer) 300 else 220, easing = if (showCategoryDrawer) androidx.compose.animation.core.FastOutSlowInEasing else androidx.compose.animation.core.FastOutLinearInEasing),
        label = "category-drawer-backdrop-progress"
    )
    SideEffect { onCategoryDrawerOverlay(categoryDrawerProgress) }
    val contextMenuReturnProgress by animateFloatAsState(
        targetValue = if (contextMenuVideo != null && !contextMenuOpen) 1f else 0f,
        animationSpec = tween(260, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "library-context-card-return"
    )
    LaunchedEffect(contextMenuOpen) {
        if (!contextMenuOpen && contextMenuVideo != null) {
            delay(260)
            contextMenuVideo = null
            contextMenuBounds = null
            contextMenuStartBounds = null
            contextMenuViewportBounds = null
            contextMenuStickyHeaderBounds = null
            contextMenuNavigationBounds = null
            contextMenuFabBounds = null
        }
    }
    val backdropBlur = maxOf(5.dp * maxOf(sheetProgress, clearDialogProgress, importDialogProgress, categoryDrawerProgress), 5.dp * contextMenuProgress)
    var displayedSheet by remember { mutableStateOf(SheetType.NONE) }

    fun refresh() {
        videos = repository.videos()
        reviewEntries = repository.reviews()
        categories = repository.categories()
        weeklyWatchTimeMs = repository.weeklyWatchTimeMs()
    }
    fun startNewReview(video: VideoItem? = null, showVideoActions: Boolean = true) {
        showReviewVideoActions = showVideoActions
        selected = video
        editReviewId = null
        editReviewVideoId = video?.id
        editStandaloneGroupId = null
        editReview = ""
        editRating = video?.rating ?: 0f
        editReviewTitle = ""
        editReviewCoverUri = null
        editReviewCoverPath = null
        parentSheet = SheetType.NONE
        sheet = SheetType.NOTE
    }
    fun openReviewEditor(entry: ReviewEntry) {
        editReviewId = entry.id
        editReviewVideoId = entry.videoId
        editStandaloneGroupId = entry.standaloneGroupId
        selected = videos.firstOrNull { it.id == entry.videoId }
        editReview = entry.content
        editRating = entry.rating
        editReviewTitle = entry.title
        editReviewCoverUri = null
        editReviewCoverPath = entry.coverPath
        parentSheet = SheetType.NONE
        sheet = SheetType.NOTE
    }
    fun startStandaloneChildReview(groupId: Long) {
        val parent = reviewEntries.firstOrNull { it.id == groupId && it.videoId == null && it.standaloneGroupId == null } ?: return
        showReviewVideoActions = false
        selected = null
        editReviewId = null
        editReviewVideoId = null
        editStandaloneGroupId = parent.id
        editReview = ""
        editRating = 0f
        editReviewTitle = parent.title
        editReviewCoverUri = null
        editReviewCoverPath = null
        parentSheet = SheetType.NONE
        sheet = SheetType.NOTE
    }
    fun notify(message: String) { toast = message }
    fun playVideo(item: VideoItem) {
        speedIndex = 0
        controlsVisible = true
        locked = false
        playing = item
        onSystemBarsVisible(false)
        onPlay(item)
    }
    fun playVideoAt(item: VideoItem, positionMs: Long) {
        speedIndex = 0
        controlsVisible = true
        locked = false
        playing = item
        onSystemBarsVisible(false)
        onPlayAt(item, positionMs)
    }
    fun closePlayer(completed: Boolean = player.playbackState == Player.STATE_ENDED) {
        playing?.let { repository.updatePlayback(it.id, player.currentPosition, player.duration.coerceAtLeast(0L), completed) }
        player.pause()
        playing = null
        locked = false
        controlsVisible = true
        onSystemBarsVisible(true)
        if (isLandscape) { isLandscape = false; onOrientation(false) }
        refresh()
    }

    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        val count = repository.importUris(uris, onMetadataReady = { refresh() })
        refresh()
        notify("已导入 $count 个视频")
        sheet = SheetType.NONE
    }
    val galleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        val count = repository.importUris(uris, onMetadataReady = { refresh() })
        refresh()
        notify("已从相册导入 $count 个视频")
        sheet = SheetType.NONE
    }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            val count = repository.importFolder(uri) { refresh() }
            refresh()
            notify("已扫描并导入 $count 个视频")
        }
        sheet = SheetType.NONE
    }
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val item = selected
        if (uri != null && item != null && repository.updateCover(item.id, uri)) {
            refresh(); notify("封面已更新")
        }
        sheet = SheetType.NONE
    }
    val reviewCoverPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            editReviewCoverUri = uri
        }
    }
    val exportDataLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                val appPreferences = context.getSharedPreferences("frame_preferences", Context.MODE_PRIVATE)
                val json = com.google.gson.Gson().toJson(
                    LibraryBackup(
                        videos = repository.backupVideos(),
                        reviews = repository.reviews(),
                        categories = repository.categories(),
                        watchTimes = repository.backupWatchTimes(),
                        folderSources = repository.backupFolderSources(),
                        subtitlePreferences = repository.backupSubtitlePreferences(),
                        subtitleTracks = repository.backupSubtitleTracks(),
                        assets = repository.backupAssets(),
                        appPreferences = BackupAppPreferences(
                            allowBackground = appPreferences.getBoolean("allow_background", true),
                            keepAwake = appPreferences.getBoolean("keep_awake", true),
                            subtitleTextSizePercent = appPreferences.getInt("subtitle_text_size_percent", 100).coerceIn(50, 100)
                        )
                    )
                )
                val output = context.contentResolver.openOutputStream(uri)
                    ?: throw java.io.IOException("无法打开目标文件")
                output.bufferedWriter(Charsets.UTF_8).use { it.write(json) }
                notify("数据导出成功")
                sheet = SheetType.NONE
            } catch (_: Exception) {
                notify("数据导出失败")
            }
        }
    }
    val importDataLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportDataDialog = true
            sheet = SheetType.NONE
        }
    }

    fun importBackup(uri: Uri) {
        try {
            val json = context.contentResolver.openInputStream(uri)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: throw java.io.IOException("无法读取文件")
            val imported = try {
                val gson = com.google.gson.Gson()
                val root = com.google.gson.JsonParser.parseString(json)
                val backup = if (root.isJsonArray) {
                    val type = object : com.google.gson.reflect.TypeToken<List<VideoItem>>() {}.type
                    run {
                        val legacyVideos = gson.fromJson<List<VideoItem>>(root, type)
                        LibraryBackup(
                            legacyVideos,
                            legacyVideos.filter { it.review.isNotBlank() }.map {
                                ReviewEntry(
                                    id = 0L,
                                    videoId = it.id,
                                    content = it.review,
                                    rating = it.rating,
                                    createdAt = it.lastPlayedAt.takeIf { time -> time > 0L } ?: it.addedAt
                                )
                            }
                        )
                    }
                } else {
                    gson.fromJson(root, LibraryBackup::class.java)
                        ?: throw com.google.gson.JsonSyntaxException("JSON 内容为空")
                }
                require(backup.videos.all {
                    it.uri.isNotBlank() && it.name.isNotBlank() && it.date.isNotBlank() &&
                        it.status.isNotBlank() && it.category.isNotBlank() &&
                        it.progress.isFinite()
                })
                require(backup.videos.map { it.uri }.distinct().size == backup.videos.size)
                val ids = backup.videos.map { it.id }.toSet()
                require(backup.reviews.all { it.content.isNotBlank() && (it.videoId == null || it.videoId in ids) })
                backup
            } catch (_: Exception) {
                notify("文件格式错误")
                return
            }
            repository.replaceAll(
                imported.videos,
                imported.reviews,
                imported.categories,
                imported.watchTimes.orEmpty(),
                imported.folderSources.orEmpty(),
                imported.subtitlePreferences.orEmpty(),
                imported.subtitleTracks.orEmpty(),
                imported.assets.orEmpty()
            )
            imported.appPreferences?.let { saved ->
                preferences.edit()
                    .putBoolean("allow_background", saved.allowBackground)
                    .putBoolean("keep_awake", saved.keepAwake)
                    .putInt("subtitle_text_size_percent", saved.subtitleTextSizePercent.coerceIn(50, 100))
                    .apply()
                allowBackground = saved.allowBackground
                keepAwake = saved.keepAwake
                onBackgroundPlaybackChanged(saved.allowBackground)
                onKeepAwake(saved.keepAwake)
            }
            refresh()
            notify("数据导入成功；如视频或外挂字幕无法访问，请重新授权文件/文件夹")
        } catch (_: Exception) {
            notify("数据导入失败")
        }
    }

    LaunchedEffect(toast) { if (toast.isNotBlank()) { delay(2100); toast = "" } }
    val sheetVisibility = remember { androidx.compose.animation.core.MutableTransitionState(false) }
    val showNestedCategorySheet = showCategoryDrawer && (sheet == SheetType.DELETE_CATEGORY || sheet == SheetType.NEW_CATEGORY)
    LaunchedEffect(sheet, showCategoryDrawer) {
        sheetVisibility.targetState = sheet != SheetType.NONE && !showNestedCategorySheet
        if (sheet == SheetType.NONE) { sheetKeyboardUp = false; parentSheet = SheetType.NONE } else displayedSheet = sheet
    }
    BackHandler(enabled = showCategoryDrawer || contextMenuOpen || showClearDataDialog || showImportDataDialog || clearDialogProgress > 0f || sheet != SheetType.NONE || sheetVisibility.currentState || sheetVisibility.targetState || playing != null || showFab) {
        when {
            showNestedCategorySheet -> sheet = SheetType.NONE
            showCategoryDrawer -> { showCategoryDrawer = false; categoryEditMode = false }
            contextMenuOpen -> { contextMenuOpen = false }
            showClearDataDialog || clearDialogProgress > 0f -> showClearDataDialog = false
            showImportDataDialog -> { showImportDataDialog = false; pendingImportUri = null }
            sheet != SheetType.NONE && parentSheet != SheetType.NONE -> { sheet = parentSheet; parentSheet = SheetType.NONE }
            sheet != SheetType.NONE -> sheet = SheetType.NONE
            sheetVisibility.currentState || sheetVisibility.targetState -> Unit
            playing != null -> closePlayer()
            showFab -> showFab = false
        }
    }

    Box(
        Modifier.fillMaxSize().background(PageBg)
            .pointerInput(page, sheet, playing, contextMenuVideo) {
                var horizontalDistance = 0f
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        if (sheet == SheetType.NONE && playing == null && contextMenuVideo == null) {
                            horizontalDistance += dragAmount
                        }
                    },
                    onDragEnd = {
                        if (sheet == SheetType.NONE && playing == null && contextMenuVideo == null && kotlin.math.abs(horizontalDistance) > 50f) {
                            val next = when {
                                horizontalDistance < 0f -> MainPage.values().getOrNull(MainPage.values().indexOf(page) + 1)
                                else -> MainPage.values().getOrNull(MainPage.values().indexOf(page) - 1)
                            }
                            if (next != null) { page = next; showFab = false }
                        }
                        horizontalDistance = 0f
                    },
                    onDragCancel = { horizontalDistance = 0f }
                )
            }
    ) {
        Box(
            Modifier.fillMaxSize().graphicsLayer {
                scaleX = 1f - .02f * sheetProgress
                scaleY = 1f - .02f * sheetProgress
            }
        ) {
        androidx.compose.animation.AnimatedContent(
            targetState = playing,
            contentKey = { it },
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center,
            transitionSpec = {
                if (targetState != null) {
                    (androidx.compose.animation.slideInVertically(
                        initialOffsetY = { it / 10 },
                        animationSpec = tween(350, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                    ) + fadeIn(tween(350))) togetherWith
                        fadeOut(tween(350))
                } else {
                    fadeIn(tween(250)) togetherWith
                        (androidx.compose.animation.slideOutVertically(
                            targetOffsetY = { it / 10 },
                            animationSpec = tween(250, easing = androidx.compose.animation.core.FastOutLinearInEasing)
                        ) + fadeOut(tween(250)))
                }
            },
            label = "player-card-transition"
        ) { targetPlaying ->
          if (targetPlaying == null) {
            Box(
                Modifier.fillMaxSize().background(PageBg).blur(backdropBlur)
            ) {
                androidx.compose.animation.AnimatedContent(
                    modifier = Modifier.fillMaxSize().statusBarsPadding().hazeSource(hazeState, zIndex = 0f),
                    targetState = page,
                    transitionSpec = {
                        val forward = targetState.ordinal > initialState.ordinal
                        (androidx.compose.animation.slideInHorizontally(
                            animationSpec = tween(350, easing = androidx.compose.animation.core.CubicBezierEasing(.25f, 1f, .5f, 1f))
                        ) { fullWidth -> if (forward) fullWidth else -fullWidth } ) togetherWith
                            androidx.compose.animation.slideOutHorizontally(
                                animationSpec = tween(350, easing = androidx.compose.animation.core.CubicBezierEasing(.25f, 1f, .5f, 1f))
                            ) { fullWidth -> if (forward) -fullWidth else fullWidth }
                    },
                    label = "frame-page"
                ) { target ->
                    when (target) {
                            MainPage.HOME -> HomePage(
                                videos = videos,
                                weeklyWatchTimeMs = weeklyWatchTimeMs,
                                hazeState = hazeState,
                                listState = homeListState,
                                onSettings = { sheet = SheetType.SETTINGS },
                                onVideo = { selected = it; playVideo(it) }
                            )
                            MainPage.LIBRARY -> LibraryPage(
                                hazeState = hazeState,
                                listState = libraryListState,
                                stickyCategoryScrollState = libraryStickyCategoryScrollState,
                                floatingCategoryScrollState = libraryFloatingCategoryScrollState,
                                videos = sortedLibraryVideos,
                                categories = categories,
                                status = status,
                                category = category,
                                compact = compact,
                                sort = sort,
                                sortDirection = sortDirection,
                                onSortModeChange = { sort = it },
                                onSortDirectionChange = { sortDirection = it },
                                onCompactChange = { compact = it },
                                onStatus = { status = it },
                                onCategory = { category = it },
                                categoryDrawerVisible = showCategoryDrawer,
                                onOpenCategoryDrawer = { showCategoryDrawer = true },
                                onLongVideo = { item, bounds -> selected = item; contextMenuVideo = item; contextMenuBounds = bounds; contextMenuStartBounds = bounds; contextMenuOpen = true; showFab = false },
                                onPlay = { selected = it; playVideo(it) },
                                contextMenuVideo = contextMenuVideo,
                                contextMenuReturnProgress = contextMenuReturnProgress,
                                onContextMenuBoundsChanged = { latestBounds -> if (contextMenuBounds != latestBounds) contextMenuBounds = latestBounds },
                                onViewportBoundsChanged = { viewport -> contextMenuViewportBounds = viewport },
                                onStickyHeaderBoundsChanged = { header -> contextMenuStickyHeaderBounds = header },
                                multiSelectMode = libraryMultiSelectMode,
                                onMultiSelectModeChange = { libraryMultiSelectMode = it },
                                onBatchMove = { ids, target -> ids.forEach { id -> videos.firstOrNull { it.id == id }?.let { repository.update(it.copy(category = target)) } }; refresh() },
                                onBatchFavorite = { ids, value -> ids.forEach { id -> videos.firstOrNull { it.id == id }?.let { repository.update(it.copy(isFav = value)) } }; refresh() },
                                onBatchStatus = { ids, target -> ids.forEach { id -> videos.firstOrNull { it.id == id }?.let { item ->
                                    repository.update(when (target) {
                                        "未看" -> item.copy(progress = 0f, status = "all")
                                        "在看" -> item.copy(status = "playing")
                                        else -> item.copy(progress = 100f, status = "watched")
                                    })
                                } }; refresh() },
                                onBatchRemove = { ids -> ids.forEach(repository::removeFromLibrary); refresh() }
                            )
                            MainPage.RECENT -> RecentPage(
                                hazeState = hazeState,
                                listState = recentListState,
                                videos,
                                onVideo = { selected = it; playVideo(it) },
                                onClear = { sheet = SheetType.CLEAR_RECENT },
                                onMore = { selected = it; sheet = SheetType.DELETE_RECENT }
                            )
                            MainPage.RECORDS -> RecordsPage(
                                hazeState = hazeState,
                                listState = recordsListState,
                                videos = videos,
                                reviews = reviewEntries,
                                targetReviewId = targetReviewId,
                                targetVideoId = targetRecordVideoId,
                                sortMode = recordSortMode,
                                sortDirection = recordSortDirection,
                                onRecordSortModeChange = { recordSortMode = it },
                                onRecordSortDirectionChange = { recordSortDirection = it },
                                onSearch = { recordSearch = ""; sheet = SheetType.RECORD_SEARCH },
                                onCreate = { video ->
                                    if (video == null) {
                                        selected = null
                                        editReviewId = null
                                        editReviewVideoId = null
                                        editReview = ""
                                        editRating = 0f
                                        parentSheet = SheetType.NONE
                                        sheet = SheetType.REVIEW_CREATE_OPTIONS
                                    } else {
                                        startNewReview(video, showVideoActions = false)
                                    }
                                },
                                onCreateStandalone = { groupId -> startStandaloneChildReview(groupId) },
                                onEdit = { openReviewEditor(it) },
                                onRateVideo = { video, rating -> repository.updateVideoRating(video.id, rating); refresh() },
                                onPlayVideo = { video -> playVideo(video) },
                                onSeekToTimestamp = { video, positionMs -> playVideoAt(video, positionMs) },
                                onTargetHandled = { targetReviewId = null; targetRecordVideoId = null }
                            )
                        }
                    }
            }

          } else {
            NativePlayerOverlay(
                item = targetPlaying,
                player = player,
                repository = repository,
                speeds = speeds,
                speedIndex = speedIndex,
                locked = locked,
                controlsVisible = controlsVisible,
                onClose = { closePlayer() },
                onToggleControls = { controlsVisible = !controlsVisible },
                onAutoHideControls = { controlsVisible = false },
                onToggleLock = { locked = !locked; controlsVisible = !locked },
                onSpeed = {
                    speedIndex = (speedIndex + 1) % speeds.size
                },
                onOrientation = {
                    isLandscape = !isLandscape
                    onOrientation(isLandscape)
                },
                onEnded = {
                    controlsVisible = true
                    playing?.let { repository.updatePlayback(it.id, player.currentPosition.coerceAtLeast(0L), player.duration.coerceAtLeast(0L), ended = true) }
                    refresh()
                },
                onProgress = { pos, duration ->
                    playing?.let { repository.updatePlayback(it.id, pos, duration) }
                },
                onWatchTime = { deltaMs ->
                    playing?.let { repository.recordWatchTime(it.id, deltaMs) }
                },
                onSaveTimestampRecord = { positionMs, content ->
                    playing?.let { currentVideo ->
                        if (repository.addTimestampReview(currentVideo.id, content, positionMs)) {
                            reviewEntries = repository.reviews()
                        } else {
                            notify("记录内容不能为空")
                        }
                    }
                }
            )
          }
        }

        if (playing == null) {
            Box(Modifier.fillMaxSize().blur(5.dp * maxOf(contextMenuProgress, clearDialogProgress, importDialogProgress, categoryDrawerProgress))) {
                AnimatedVisibility(
                    visible = !(page == MainPage.LIBRARY && libraryMultiSelectMode),
                    enter = fadeIn(tween(180)) + slideInVertically(tween(180)) { it / 3 },
                    exit = fadeOut(tween(140)) + slideOutVertically(tween(140)) { it / 3 },
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().zIndex(1f)
                ) {
                    BottomPillNavigation(page, hazeState, backdropBlur, onBoundsChanged = { contextMenuNavigationBounds = it }) { page = it; showFab = false }
                }
                if (!(page == MainPage.LIBRARY && libraryMultiSelectMode)) Column(
                    Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val fabEasing = androidx.compose.animation.core.CubicBezierEasing(.4f, 0f, .2f, 1f)
                val searchProgress by animateFloatAsState(
                    if (showFab) 1f else 0f,
                    tween(300, delayMillis = 50, easing = fabEasing),
                    label = "fab-search-progress"
                )
                val importProgress by animateFloatAsState(
                    if (showFab) 1f else 0f,
                    tween(300, delayMillis = 100, easing = fabEasing),
                    label = "fab-import-progress"
                )
                val fabRotation by animateFloatAsState(
                    if (showFab) 45f else 0f,
                    tween(300, easing = fabEasing),
                    label = "fab-rotation"
                )
                Box(
                    Modifier.size(48.dp)
                        .graphicsLayer {
                            alpha = searchProgress
                            scaleX = .5f + .5f * searchProgress
                            scaleY = .5f + .5f * searchProgress
                            translationY = 20.dp.toPx() * (1f - searchProgress)
                        }
                        .then(if (showFab) Modifier.noRippleClickable { search = ""; sheet = SheetType.SEARCH; showFab = false } else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(48.dp).shadow(4.dp, CircleShape, ambientColor = ThemeTeal.copy(alpha = .4f), spotColor = ThemeTeal.copy(alpha = .4f)).clip(CircleShape).background(ThemeTeal), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Search, "搜索", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Box(
                    Modifier.size(48.dp)
                        .graphicsLayer {
                            alpha = importProgress
                            scaleX = .5f + .5f * importProgress
                            scaleY = .5f + .5f * importProgress
                            translationY = 20.dp.toPx() * (1f - importProgress)
                        }
                        .then(if (showFab) Modifier.noRippleClickable { sheet = SheetType.IMPORT; showFab = false } else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(48.dp).shadow(4.dp, CircleShape, ambientColor = ThemeTeal.copy(alpha = .4f), spotColor = ThemeTeal.copy(alpha = .4f)).clip(CircleShape).background(ThemeTeal), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Add, "导入视频", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Box(
                    Modifier.size(56.dp)
                        .onGloballyPositioned { contextMenuFabBounds = it.boundsInRoot() }
                        .shadow(8.dp, CircleShape, ambientColor = Color.Black.copy(alpha = .10f), spotColor = Color.Black.copy(alpha = .10f))
                        .hazeEffect(state = hazeState, style = HazeStyle(backgroundColor = PageBg, tints = emptyList(), blurRadius = 20.dp, noiseFactor = 0f))
                        .clip(CircleShape)
                        .background(Glass)
                        .border(0.3.dp, Color(0xFFE6E6E6), CircleShape)
                        .noRippleClickable { showFab = !showFab },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(FrameHamburgerIcon, null, tint = Color(0xFF333333), modifier = Modifier.size(20.dp).rotate(fabRotation))
                }
                }
        }
        }
        }

        // The shared sheet uses its measured full height for a complete entrance/exit.
        // The complete background layer (page, bottom navigation and adjacent FAB) scales together.

        Box(Modifier.fillMaxSize().zIndex(4f)) {
            AnimatedVisibility(
                visibleState = sheetVisibility,
                enter = fadeIn(tween(320, easing = androidx.compose.animation.core.FastOutSlowInEasing)),
                exit = fadeOut(tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing)),
                modifier = Modifier.zIndex(2f)
            ) {
                Box(
                    Modifier.fillMaxSize().blur(5.dp * maxOf(clearDialogProgress, importDialogProgress))
                        .background(Color.Black.copy(alpha = .28f * (1f - maxOf(clearDialogProgress, importDialogProgress))))
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) { sheet = SheetType.NONE }
                )
            }
            Box(Modifier.fillMaxSize().zIndex(3f)) {
                AnimatedVisibility(
                    visibleState = sheetVisibility,
                    enter = androidx.compose.animation.EnterTransition.None,
                    exit = androidx.compose.animation.ExitTransition.None,
                    modifier = Modifier.fillMaxSize().zIndex(3f)
                ) {
                    Box(Modifier.fillMaxSize()) {
                    Column(
                    Modifier.align(Alignment.BottomCenter)
                        .animateEnterExit(
                            enter = slideInVertically(
                                animationSpec = tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                            ) { fullHeight -> fullHeight } + fadeIn(tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)),
                            exit = slideOutVertically(
                                animationSpec = tween(250, easing = androidx.compose.animation.core.FastOutLinearInEasing)
                            ) { fullHeight -> fullHeight }
                        )
                        .fillMaxWidth()
                        .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * .85f)
                        .shadow(10.dp, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), ambientColor = Color.Black.copy(alpha = .10f), spotColor = Color.Black.copy(alpha = .10f))
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(PageBg)
                        .verticalScroll(rememberScrollState())
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) { }
                         .padding(top = 26.dp, bottom = if (sheetKeyboardUp) 400.dp else 50.dp)
                        .navigationBarsPadding()
                ) {
                when (displayedSheet) {
                    SheetType.IMPORT -> SheetContent("导入本地视频") {
                        SheetAction("从相册导入") {
                            galleryPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                        }
                        SheetAction("从文件中选择视频") { importPicker.launch(arrayOf("video/*")) }
                        SheetAction("扫描整个文件夹", bottomPadding = 0.dp) { folderPicker.launch(null) }
                    }
                    SheetType.SETTINGS -> Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        SheetHeading("设置")
                        Text("播放偏好", color = MutedText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 10.dp, bottom = 8.dp))
                        Column(Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(16.dp)).background(Color.White)) {
                            SettingRow("允许后台播放", allowBackground) { allowBackground = it; onBackgroundPlaybackChanged(it); preferences.edit().putBoolean("allow_background", it).apply() }
                            Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFFF0F0F0)))
                            SettingRow("保持屏幕常亮", keepAwake) { keepAwake = it; preferences.edit().putBoolean("keep_awake", it).apply(); onKeepAwake(it) }
                        }
                        Text("数据与隐私", color = MutedText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 10.dp, top = 20.dp, bottom = 8.dp))
                        Column(Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(16.dp)).background(Color.White)) {
                            Row(Modifier.fillMaxWidth().height(52.dp).noRippleClickable { notify("数据仅保存在本地") }.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("关于与隐私", Modifier.weight(1f), color = MainText, fontSize = 16.sp)
                                Text(
                                    if (appVersion.isBlank()) "" else "v$appVersion",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 11.sp
                                )
                            }
                            Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFFF0F0F0)))
                            Row(Modifier.fillMaxWidth().height(52.dp).noRippleClickable { exportDataLauncher.launch("Frame-backup.json") }.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("导出数据", Modifier.weight(1f), color = MainText, fontSize = 16.sp)
                            }
                            Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFFF0F0F0)))
                            Row(Modifier.fillMaxWidth().height(52.dp).noRippleClickable { importDataLauncher.launch(arrayOf("application/json")) }.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("导入数据", Modifier.weight(1f), color = MainText, fontSize = 16.sp)
                            }
                            Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFFF0F0F0)))
                            Row(Modifier.fillMaxWidth().height(52.dp).noRippleClickable { showClearDataDialog = true; sheet = SheetType.NONE }.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("清空所有数据", color = Color(0xFFFF3B30), fontSize = 16.sp)
                            }
                        }
                    }
                    SheetType.RECORD_SEARCH -> Column(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .animateContentSize(
                                tween(320, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f))
                            )
                    ) {
                        SheetHeading("搜索")
                        BasicTextField(
                            recordSearch,
                            { recordSearch = it },
                            Modifier.fillMaxWidth().heightIn(min = 50.dp)
                                .onFocusChanged { sheetKeyboardUp = it.isFocused }
                                .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(15.dp),
                            singleLine = true,
                            decorationBox = { inner ->
                                if (recordSearch.isEmpty()) Text("搜索视频名称或观影感想...", color = MutedText, fontSize = 16.sp)
                                inner()
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = MainText)
                        )
                        val recordQuery = recordSearch.trim()
                        val matchingReviewEntries = remember(recordQuery, reviewEntries, videoById) {
                            if (recordQuery.isBlank()) emptyList() else reviewEntries.filter { entry ->
                                val linkedVideo = videoById[entry.videoId]
                                entry.content.contains(recordQuery, true) || entry.title.contains(recordQuery, true) ||
                                    linkedVideo?.let { matchesVideoFilenameSearch(it, recordQuery) } == true
                            }.take(30)
                        }
                        val matchingUnreviewedVideos = remember(recordQuery, videos, reviewEntries) {
                            if (recordQuery.isBlank()) emptyList() else {
                                val reviewedVideoIds = reviewEntries.mapNotNull { it.videoId }.toSet()
                                videos.filter { video ->
                                    (video.status == "watched" || video.progress >= 100f) &&
                                        video.id !in reviewedVideoIds &&
                                        matchesVideoFilenameSearch(video, recordQuery)
                                }.take(30)
                            }
                        }
                        val recordSearchResultCount = matchingReviewEntries.size + matchingUnreviewedVideos.size
                        var recordSearchBottomSpacerPx by remember(recordSearch) { mutableFloatStateOf(0f) }
                        val searchSheetMidpointPx = with(LocalDensity.current) {
                            LocalConfiguration.current.screenHeightDp.dp.toPx() * .55f
                        }
                        matchingReviewEntries.forEachIndexed { index, entry ->
                            val isLastShortResult = index == matchingReviewEntries.lastIndex && matchingUnreviewedVideos.isEmpty() && recordSearchResultCount in 1..4
                            key("review_search_${entry.id}") {
                                val video = videoById[entry.videoId]
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(tween(260, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f))) +
                                        androidx.compose.animation.slideInVertically(tween(260, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f))) { it / 10 }
                                ) {
                                    Surface(
                                        Modifier.fillMaxWidth().padding(top = 12.dp)
                                            .then(
                                                if (isLastShortResult) Modifier.onGloballyPositioned { coordinates ->
                                                    if (!sheetKeyboardUp) {
                                                        val desired = (coordinates.boundsInRoot().bottom + recordSearchBottomSpacerPx - searchSheetMidpointPx)
                                                            .coerceIn(0f, searchSheetMidpointPx * .9f)
                                                        if (abs(recordSearchBottomSpacerPx - desired) > 1f) recordSearchBottomSpacerPx = desired
                                                    }
                                                } else Modifier
                                            )
                                            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                            .clip(RoundedCornerShape(16.dp))
                                            .noRippleClickable {
                                                targetRecordVideoId = null
                                                targetReviewId = entry.id
                                                recordSearch = ""
                                                sheet = SheetType.NONE
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White,
                                        shadowElevation = 0.dp
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth().padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                Modifier.size(width = 84.dp, height = 58.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (video != null) PosterBrush(video.bgIndex) else Brush.linearGradient(listOf(Color(0xFFF1F4F4), Color(0xFFF1F4F4)))),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (video != null) Thumbnail(video, Modifier.fillMaxSize())
                                                else Text("感想", color = ThemeTeal, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                            }
                                            Column(Modifier.weight(1f)) {
                                                Text(
                                                    video?.let { displayVideoName(it.name) } ?: "独立感想",
                                                    color = MainText,
                                                    fontSize = if (video == null) 12.sp else 16.sp,
                                                    fontWeight = if (video == null) FontWeight.Normal else FontWeight.Medium,
                                                    lineHeight = if (video == null) 17.sp else 21.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    SimpleDateFormat("M/d HH:mm", Locale.getDefault()).format(Date(entry.createdAt)),
                                                    color = MutedText,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                                if (entry.content.isNotBlank()) {
                                                    Text(
                                                        entry.content,
                                                        color = Color(0xFF777777),
                                                        fontSize = 13.sp,
                                                        lineHeight = 19.sp,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.padding(top = 3.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        matchingUnreviewedVideos.forEachIndexed { index, video ->
                            val isLastShortResult = index == matchingUnreviewedVideos.lastIndex && recordSearchResultCount in 1..4
                            key("video_search_${video.id}") {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(tween(260, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f))) +
                                        androidx.compose.animation.slideInVertically(tween(260, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f))) { it / 10 }
                                ) {
                                    Surface(
                                        Modifier.fillMaxWidth().padding(top = 12.dp)
                                            .then(
                                                if (isLastShortResult) Modifier.onGloballyPositioned { coordinates ->
                                                    if (!sheetKeyboardUp) {
                                                        val desired = (coordinates.boundsInRoot().bottom + recordSearchBottomSpacerPx - searchSheetMidpointPx)
                                                            .coerceIn(0f, searchSheetMidpointPx * .9f)
                                                        if (abs(recordSearchBottomSpacerPx - desired) > 1f) recordSearchBottomSpacerPx = desired
                                                    }
                                                } else Modifier
                                            )
                                            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                            .clip(RoundedCornerShape(16.dp))
                                            .noRippleClickable {
                                                targetReviewId = null
                                                targetRecordVideoId = video.id
                                                recordSearch = ""
                                                sheet = SheetType.NONE
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White,
                                        shadowElevation = 0.dp
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth().padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                Modifier.size(width = 84.dp, height = 58.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(PosterBrush(video.bgIndex))
                                            ) {
                                                Thumbnail(video, Modifier.fillMaxSize())
                                            }
                                            VideoCardTitle(
                                                video.name,
                                                color = MainText,
                                                fontSize = 16.sp,
                                                englishFontSize = 14.sp,
                                                englishColor = Color(0xFF888888),
                                                fontWeight = FontWeight.Medium,
                                                lineHeight = 21.sp,
                                                maxLines = 1,
                                                showEnglish = true,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (!sheetKeyboardUp && recordSearchResultCount in 1..4) {
                            Spacer(Modifier.height(with(LocalDensity.current) { recordSearchBottomSpacerPx.toDp() }))
                        }
                    }
                    SheetType.SEARCH -> Column(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .animateContentSize(
                                tween(320, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f))
                            )
                    ) {
                        SheetHeading("搜索")
                        BasicTextField(
                            search,
                            { search = it },
                            Modifier.fillMaxWidth().heightIn(min = 50.dp)
                                .onFocusChanged { sheetKeyboardUp = it.isFocused }
                                .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(15.dp),
                            singleLine = true,
                            decorationBox = { inner ->
                                if (search.isEmpty()) Text("搜索本地视频...", color = MutedText, fontSize = 16.sp)
                                inner()
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = MainText)
                        )
                        val searchResults = remember(search, videos) {
                            val query = search.trim()
                            if (query.isBlank()) emptyList()
                            else videos.asSequence()
                                .filter { matchesVideoFilenameSearch(it, query) }
                                .distinctBy { it.id }
                                .take(30)
                                .toList()
                        }
                        var videoSearchBottomSpacerPx by remember(search) { mutableFloatStateOf(0f) }
                        val searchSheetMidpointPx = with(LocalDensity.current) {
                            LocalConfiguration.current.screenHeightDp.dp.toPx() * .55f
                        }
                        searchResults.forEachIndexed { index, video ->
                            val isLastShortResult = index == searchResults.lastIndex && searchResults.size <= 4
                            key(video.id) {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(tween(260, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f))) +
                                        androidx.compose.animation.slideInVertically(tween(260, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f))) { it / 10 }
                                ) {
                                    Surface(
                                        Modifier.fillMaxWidth().padding(top = 12.dp)
                                            .then(
                                                if (isLastShortResult) Modifier.onGloballyPositioned { coordinates ->
                                                    if (!sheetKeyboardUp) {
                                                        val desired = (coordinates.boundsInRoot().bottom + videoSearchBottomSpacerPx - searchSheetMidpointPx)
                                                            .coerceIn(0f, searchSheetMidpointPx * .9f)
                                                        if (abs(videoSearchBottomSpacerPx - desired) > 1f) videoSearchBottomSpacerPx = desired
                                                    }
                                                } else Modifier
                                            )
                                            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                            .clip(RoundedCornerShape(16.dp))
                                            .noRippleClickable {
                                                selected = video
                                                sheet = SheetType.NONE
                                                playVideo(video)
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White,
                                        shadowElevation = 0.dp
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth().padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                Modifier.size(width = 84.dp, height = 58.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(PosterBrush(video.bgIndex))
                                            ) {
                                                Thumbnail(video, Modifier.fillMaxSize())
                                            }
                                            VideoCardTitle(
                                                video.name,
                                                color = MainText,
                                                fontSize = 16.sp,
                                                englishFontSize = 14.sp,
                                                englishColor = Color(0xFF888888),
                                                fontWeight = FontWeight.Medium,
                                                lineHeight = 21.sp,
                                                maxLines = 1,
                                                showEnglish = true,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (!sheetKeyboardUp && searchResults.size in 1..4) {
                            Spacer(Modifier.height(with(LocalDensity.current) { videoSearchBottomSpacerPx.toDp() }))
                        }
                    }
                    SheetType.RENAME -> Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        SheetHeading("重命名")
                        Row(
                            Modifier.fillMaxWidth()
                                .height(52.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                editName,
                                { editName = it },
                                Modifier.fillMaxWidth()
                                    .onFocusChanged { sheetKeyboardUp = it.isFocused }
                                    .padding(horizontal = 16.dp),
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    lineHeight = 18.sp,
                                    color = MainText,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Box(
                            Modifier.fillMaxWidth()
                                .height(52.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .noRippleClickable {
                                    val newName = editName.trim()
                                    if (newName.isEmpty()) notify("视频名称不能为空")
                                    else { selected?.let { repository.rename(it.id, newName) }; refresh(); sheet = SheetType.NONE }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("保存", color = MainText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    SheetType.MOVE -> SheetContent("移动到分类") {
                        categories.filter { it != "全部分类" }.forEach { cat ->
                            SheetAction(cat, bottomPadding = if (cat == categories.lastOrNull()) 0.dp else 12.dp) { selected?.let { repository.moveCategory(it.id, cat) }; refresh(); sheet = SheetType.NONE }
                        }
                    }
                    SheetType.REVIEW_CREATE_OPTIONS -> Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        SheetHeading("新增观影感想")
                        Column(
                            Modifier.fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().height(52.dp)
                                    .noRippleClickable {
                                        selected = null
                                        editReviewId = null
                                        editReviewVideoId = null
                                        editReview = ""
                                        editRating = 0f
                                        editReviewTitle = ""
                                        editReviewCoverUri = null
                                        editReviewCoverPath = null
                                        reviewVideoSearch = ""
                                        parentSheet = SheetType.NONE
                                        sheet = SheetType.REVIEW_VIDEO_PICKER
                                    }
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("关联视频", color = MainText, fontSize = 16.sp)
                            }
                            Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFFF0F0F0)))
                            Row(
                                Modifier.fillMaxWidth().height(52.dp)
                                    .noRippleClickable { startNewReview(null) }
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("不关联视频", color = MainText, fontSize = 16.sp)
                            }
                        }
                    }
                    SheetType.NOTE -> Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        SheetHeading(if (editStandaloneGroupId != null) { if (editReviewId == null) "新增感想" else "编辑感想" } else if (editReviewVideoId == null) { if (editReviewId == null) "新增记录" else "编辑记录" } else { if (editReviewId == null) "新增观影感想" else "编辑观影感想" })
                        val linkedVideo = videos.firstOrNull { it.id == editReviewVideoId }
                        if (editReviewVideoId == null) {
                            if (editStandaloneGroupId != null) {
                                val parentTitle = reviewEntries.firstOrNull { it.id == editStandaloneGroupId }?.title?.ifBlank { "未命名" } ?: "未命名"
                                Text(
                                    parentTitle,
                                    color = MainText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                )
                                BasicTextField(
                                    editReview, { editReview = it },
                                    Modifier.fillMaxWidth().padding(bottom = 20.dp).height(120.dp)
                                        .onFocusChanged { sheetKeyboardUp = it.isFocused }
                                        .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                        .clip(RoundedCornerShape(16.dp)).background(Color.White).padding(15.dp),
                                    decorationBox = { inner -> if (editReview.isEmpty()) Text("写下你的感想...", color = MutedText, fontSize = 16.sp); inner() },
                                    textStyle = TextStyle(fontSize = 16.sp, color = MainText, lineHeight = 24.sp)
                                )
                                val canSaveChild = editReview.isNotBlank()
                                Box(
                                    Modifier.fillMaxWidth().padding(top = 10.dp).height(52.dp)
                                        .shadow(if (canSaveChild) 4.dp else 0.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (canSaveChild) ThemeTeal else Color(0xFFE5E5E5))
                                        .then(if (canSaveChild) Modifier.noRippleClickable {
                                            val groupId = editStandaloneGroupId!!
                                            val saved = if (editReviewId == null) repository.addStandaloneChildReview(groupId, editReview)
                                                else repository.updateStandaloneChildReview(editReviewId!!, groupId, editReview)
                                            if (saved) { refresh(); sheet = SheetType.NONE }
                                        } else Modifier),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("保存记录", color = if (canSaveChild) Color.White else Color(0xFF999999), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
                                }
                                if (editReviewId != null) {
                                    Box(Modifier.fillMaxWidth().padding(top = 12.dp).height(52.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).noRippleClickable {
                                        editReviewId?.let { repository.deleteReview(it) }; refresh(); sheet = SheetType.NONE
                                    }, contentAlignment = Alignment.Center) {
                                        Text("删除这条感想", color = Color(0xFFE5484D), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            } else {
                            val coverBitmap = remember(editReviewCoverPath, editReviewCoverUri) {
                                runCatching {
                                    when {
                                        editReviewCoverUri != null -> context.contentResolver.openInputStream(editReviewCoverUri!!)?.use { BitmapFactory.decodeStream(it) }
                                        editReviewCoverPath != null -> BitmapFactory.decodeFile(editReviewCoverPath)
                                        else -> null
                                    }
                                }.getOrNull()
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                                Box(
                                    Modifier.size(75.dp, 100.dp).clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFECEDEF))
                                        .noRippleClickable {
                                            reviewCoverPicker.launch(
                                                androidx.activity.result.PickVisualMediaRequest(
                                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                                )
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (coverBitmap != null) Image(coverBitmap.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    else Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Icon(Icons.Outlined.Image, null, tint = Color(0xFF999999), modifier = Modifier.size(22.dp))
                                        Text("添加封面", color = Color(0xFF999999), fontSize = 10.sp)
                                    }
                                }
                                Column(Modifier.weight(1f).height(100.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                    BasicTextField(
                                        editReviewTitle,
                                        { editReviewTitle = it },
                                        Modifier.fillMaxWidth().heightIn(min = 42.dp)
                                            .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                            .clip(RoundedCornerShape(12.dp)).background(Color.White).padding(11.dp),
                                        singleLine = true,
                                        decorationBox = { inner -> if (editReviewTitle.isEmpty()) Text("输入标题（选填）", color = MutedText, fontSize = 14.sp); inner() },
                                        textStyle = TextStyle(fontSize = 14.sp, color = MainText, fontWeight = FontWeight.Medium)
                                    )
                                    RatingStars(editRating, 20.dp, onRate = { editRating = it }, spacing = 3.dp)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            BasicTextField(
                                editReview, { editReview = it },
                                Modifier.fillMaxWidth().padding(bottom = 20.dp).height(120.dp)
                                    .onFocusChanged { sheetKeyboardUp = it.isFocused }
                                    .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                    .clip(RoundedCornerShape(16.dp)).background(Color.White).padding(15.dp),
                                decorationBox = { inner -> if (editReview.isEmpty()) Text("写下你的感想...", color = MutedText, fontSize = 16.sp); inner() },
                                textStyle = TextStyle(fontSize = 16.sp, color = MainText, lineHeight = 24.sp)
                            )
                            val canSaveStandalone = editReview.isNotBlank()
                            Box(
                                Modifier.fillMaxWidth().padding(top = 10.dp).height(52.dp)
                                    .shadow(if (canSaveStandalone) 4.dp else 0.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (canSaveStandalone) ThemeTeal else Color(0xFFE5E5E5))
                                    .then(if (canSaveStandalone) Modifier.noRippleClickable {
                                        val saved = if (editReviewId == null) {
                                            repository.addStandaloneReview(editReviewTitle, editReview, editRating, editReviewCoverUri)
                                        } else {
                                            repository.updateStandaloneReview(editReviewId!!, editReviewTitle, editReview, editRating, editReviewCoverUri, editReviewCoverPath)
                                        }
                                        if (saved) { refresh(); sheet = SheetType.NONE }
                                    } else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("保存记录", color = if (canSaveStandalone) Color.White else Color(0xFF999999), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
                            }
                            if (editReviewId != null) {
                                Box(Modifier.fillMaxWidth().padding(top = 12.dp).height(52.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).noRippleClickable {
                                    editReviewId?.let { repository.deleteReview(it) }; refresh(); sheet = SheetType.NONE
                                }, contentAlignment = Alignment.Center) {
                                    Text("删除这条感想", color = Color(0xFFE5484D), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            }
                        } else {
                            val canSetVideoRating = editReviewId == null &&
                                editReviewVideoId != null &&
                                reviewEntries.none { it.videoId == editReviewVideoId }
                        if (editReviewId == null && showReviewVideoActions) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Box(
                                    Modifier.weight(1f).height(48.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (linkedVideo != null) ThemeTeal else Color.White)
                                        .noRippleClickable {
                                            reviewVideoSearch = ""
                                            sheet = SheetType.REVIEW_VIDEO_PICKER
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        linkedVideo?.let { displayVideoName(it.name) } ?: "关联视频",
                                        color = if (linkedVideo != null) Color.White else MainText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                                    )
                                }
                                Box(
                                    Modifier.weight(1f).height(48.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (linkedVideo == null) ThemeTeal else Color.White)
                                        .noRippleClickable {
                                            editReviewVideoId = null
                                            selected = null
                                            editRating = 0f
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "不关联视频",
                                        color = if (linkedVideo == null) Color.White else MainText,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        } else if (linkedVideo != null) {
                            VideoCardTitle(
                                linkedVideo.name,
                                color = MainText,
                                fontSize = 14.sp,
                                englishFontSize = 12.sp,
                                englishColor = Color(0xFF888888),
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                showEnglish = true,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                        if (canSetVideoRating) {
                            Row(
                                Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RatingStars(editRating, 30.dp, onRate = { editRating = it }, spacing = 5.dp)
                            }
                        }
                        BasicTextField(editReview, { editReview = it }, Modifier.fillMaxWidth().padding(bottom = 20.dp).height(120.dp).onFocusChanged { sheetKeyboardUp = it.isFocused }.shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(16.dp)).background(Color.White).padding(15.dp), decorationBox = { inner -> if (editReview.isEmpty()) Text("写下你此刻的观影感受...", color = MutedText, fontSize = 16.sp); inner() }, textStyle = TextStyle(fontSize = 16.sp, color = MainText, lineHeight = 24.sp))
                        Box(Modifier.fillMaxWidth().padding(top = 10.dp).height(52.dp).shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(16.dp)).background(ThemeTeal).noRippleClickable {
                            if (editReview.isBlank()) {
                                notify("先写下一些感想再保存")
                            } else if (canSetVideoRating && editRating == 0f) {
                                notify("请先为视频评分")
                            } else {
                                val entryId = editReviewId
                                if (entryId == null) {
                                    repository.addReview(editReviewVideoId, editReview, if (canSetVideoRating) editRating else 0f)
                                } else {
                                    repository.updateReview(entryId, editReviewVideoId, editReview, 0f)
                                }
                                refresh()
                                sheet = SheetType.NONE
                            }
                        }, contentAlignment = Alignment.Center) {
                            Text("保存记录", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
                        }
                        if (editReviewId != null) {
                            Box(
                                Modifier.fillMaxWidth().padding(top = 12.dp).height(52.dp)
                                    .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .noRippleClickable {
                                        editReviewId?.let { repository.deleteReview(it) }
                                        refresh()
                                        sheet = SheetType.NONE
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("删除这条感想", color = Color(0xFFE5484D), fontSize = 16.sp, fontWeight = FontWeight.Medium, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
                            }
                        }
                        }
                    }
                    SheetType.REVIEW_VIDEO_PICKER -> Column(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                            .animateContentSize(tween(320, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f)))
                    ) {
                        SheetHeading("关联到")
                        BasicTextField(
                            reviewVideoSearch,
                            { reviewVideoSearch = it },
                            Modifier.fillMaxWidth().heightIn(min = 50.dp)
                                .onFocusChanged { sheetKeyboardUp = it.isFocused }
                                .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                
                                .padding(15.dp),
                            singleLine = true,
                            decorationBox = { inner ->
                                if (reviewVideoSearch.isEmpty()) Text("搜索要关联的视频...", color = MutedText, fontSize = 16.sp)
                                inner()
                            },
                            textStyle = TextStyle(fontSize = 16.sp, color = MainText)
                        )
                        val query = reviewVideoSearch.trim()
                        val matchingVideos = remember(query, videos) {
                            if (query.isBlank()) emptyList() else videos.filter {
                                matchesVideoFilenameSearch(it, query)
                            }.take(30)
                        }
                        matchingVideos.forEach { video ->
                            key("review_link_${video.id}") {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(tween(260, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f))) +
                                        slideInVertically(tween(260, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f))) { it / 10 }
                                ) {
                                    Surface(
                                        Modifier.fillMaxWidth().padding(top = 12.dp)
                                            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                            .clip(RoundedCornerShape(16.dp))
                                            .noRippleClickable {
                                                editReviewVideoId = video.id
                                                editRating = video.rating
                                                reviewVideoSearch = ""
                                                parentSheet = SheetType.NONE
                                                sheet = SheetType.NOTE
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White,
                                        shadowElevation = 0.dp
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth().padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                Modifier.size(width = 84.dp, height = 58.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(PosterBrush(video.bgIndex))
                                            ) { Thumbnail(video, Modifier.fillMaxSize()) }
                                            VideoCardTitle(
                                                video.name,
                                                color = MainText,
                                                fontSize = 15.sp,
                                                englishFontSize = 13.sp,
                                                englishColor = Color(0xFF888888),
                                                fontWeight = FontWeight.Medium,
                                                lineHeight = 21.sp,
                                                maxLines = 1,
                                                showEnglish = true,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    SheetType.CLEAR_RECENT -> SheetContent("清空播放记录？") {
                        Text("只清除观看进度，不会删除片库中的视频。", fontSize = 14.sp, lineHeight = 21.sp, color = MutedText, modifier = Modifier.padding(bottom = 12.dp))
                        SheetAction("确认清空", danger = true, bottomPadding = 0.dp) { repository.clearRecent(); refresh(); sheet = SheetType.NONE }
                    }
                    SheetType.CLEAR_DATA -> Unit
                    SheetType.DELETE_RECENT -> SheetContent("删除这条播放记录？") {
                        SheetAction("删除播放记录", danger = true, bottomPadding = 0.dp) { selected?.let { repository.deleteRecent(it.id) }; refresh(); sheet = SheetType.NONE }
                    }
                    SheetType.DELETE_CATEGORY -> SheetContent("确定删除该分类？") {
                        SheetAction("删除该分类", danger = true, bottomPadding = 0.dp) {
                            val target = pendingCategoryDelete
                            if (target != null) {
                                repository.saveCategories(categories - target)
                                videos.filter { it.category == target }.forEach { repository.update(it.copy(category = "全部分类")) }
                                if (category == target) category = "全部"
                                pendingCategoryDelete = null
                                refresh()
                            }
                            sheet = SheetType.NONE
                        }
                    }
                    SheetType.NEW_CATEGORY -> Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        SheetHeading("新建分类")
                        BasicTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                                .onFocusChanged { sheetKeyboardUp = it.isFocused }
                                .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                .clip(RoundedCornerShape(16.dp)).background(Color.White).padding(horizontal = 20.dp),
                            singleLine = true,
                            decorationBox = { inner ->
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                                    if (newCategoryName.isEmpty()) Text("输入分类名称", color = MutedText, fontSize = 16.sp)
                                    inner()
                                }
                            },
                            textStyle = TextStyle(fontSize = 16.sp, lineHeight = 18.sp, color = MainText, platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                        Spacer(Modifier.height(12.dp))
                        SheetActionPair(
                            onConfirm = {
                                val name = newCategoryName.trim()
                                when {
                                    name.isEmpty() -> toast = "分类名称不能为空"
                                    name in categories -> toast = "分类名称已存在"
                                    else -> {
                                        repository.saveCategories(categories + name)
                                        newCategoryName = ""
                                        refresh()
                                        sheet = SheetType.NONE
                                    }
                                }
                            },
                            onCancel = {
                                newCategoryName = ""
                                sheet = SheetType.NONE
                            }
                        )
                    }
                    SheetType.NONE -> Unit
                }
                }
                    }
                }
            }
            }
        }

        if (contextMenuVideo != null && contextMenuBounds != null && contextMenuStartBounds != null) {
            val video = contextMenuVideo!!
            val targetBounds = contextMenuBounds!!
            val startBounds = contextMenuStartBounds!!
            val p = contextMenuReturnProgress
            val bounds = Rect(
                startBounds.left + (targetBounds.left - startBounds.left) * p,
                startBounds.top + (targetBounds.top - startBounds.top) * p,
                startBounds.right + (targetBounds.right - startBounds.right) * p,
                startBounds.bottom + (targetBounds.bottom - startBounds.bottom) * p
            )
            BoxWithConstraints(Modifier.fillMaxSize().zIndex(20f)) {
                val density = LocalDensity.current
                val cardLeft = with(density) { bounds.left.toDp() }
                val cardTop = with(density) { bounds.top.toDp() }
                val cardWidth = with(density) { bounds.width.toDp() }
                val menuAnchorLeft = with(density) { startBounds.left.toDp() }
                val menuAnchorTop = with(density) { startBounds.top.toDp() }
                val menuAnchorWidth = with(density) { startBounds.width.toDp() }
                val menuAnchorBottom = with(density) { startBounds.bottom.toDp() }
                val menuWidth = minOf(196.dp, maxWidth - 32.dp)
                val menuHeight = 42.dp * 7 + 8.dp
                val menuX = (menuAnchorLeft + menuAnchorWidth / 2f - menuWidth / 2f)
                    .coerceIn(16.dp, (maxWidth - menuWidth - 16.dp).coerceAtLeast(16.dp))
                val safeTop = 60.dp
                val safeBottom = 80.dp
                val maxMenuY = (maxHeight - menuHeight - safeBottom).coerceAtLeast(safeTop)
                val menuY = if (menuAnchorBottom + menuHeight + safeBottom <= maxHeight) menuAnchorBottom + 8.dp
                    else (menuAnchorTop - menuHeight - 8.dp).coerceIn(safeTop, maxMenuY)
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .28f * contextMenuProgress)).noRippleClickable { contextMenuOpen = false })
                Box(
                    Modifier.offset(x = cardLeft, y = cardTop)
                        .width(cardWidth)
                        .graphicsLayer {
                            alpha = contextMenuProgress
                            compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                        }
                        .drawWithContent {
                            drawContent()
                            fun clearRootRect(rect: Rect) {
                                val left = maxOf(rect.left, bounds.left)
                                val top = maxOf(rect.top, bounds.top)
                                val right = minOf(rect.right, bounds.right)
                                val bottom = minOf(rect.bottom, bounds.bottom)
                                if (right > left && bottom > top) {
                                    drawRect(
                                        color = Color.Transparent,
                                        topLeft = Offset(left - bounds.left, top - bounds.top),
                                        size = Size(right - left, bottom - top),
                                        blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                                    )
                                }
                            }
                            fun clearRootRoundRect(rect: Rect, radius: Float) {
                                drawRoundRect(
                                    color = Color.Transparent,
                                    topLeft = Offset(rect.left - bounds.left, rect.top - bounds.top),
                                    size = Size(rect.width, rect.height),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                                    blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                                )
                            }
                            fun clearRootCircle(rect: Rect) {
                                drawCircle(
                                    color = Color.Transparent,
                                    radius = minOf(rect.width, rect.height) / 2f,
                                    center = Offset(
                                        rect.center.x - bounds.left,
                                        rect.center.y - bounds.top
                                    ),
                                    blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                                )
                            }
                            val viewport = contextMenuViewportBounds
                            if (viewport != null) {
                                if (bounds.top < viewport.top) clearRootRect(Rect(bounds.left, bounds.top, bounds.right, viewport.top))
                                if (bounds.bottom > viewport.bottom) clearRootRect(Rect(bounds.left, viewport.bottom, bounds.right, bounds.bottom))
                                if (bounds.left < viewport.left) clearRootRect(Rect(bounds.left, bounds.top, viewport.left, bounds.bottom))
                                if (bounds.right > viewport.right) clearRootRect(Rect(viewport.right, bounds.top, bounds.right, bounds.bottom))
                            }
                            contextMenuStickyHeaderBounds?.let { clearRootRect(it) }
                            contextMenuNavigationBounds?.let { clearRootRoundRect(it, 28.dp.toPx()) }
                            contextMenuFabBounds?.let { clearRootCircle(it) }
                        },
                    content = { PosterCardVisual(video, compact) }
                )
                Column(
                    Modifier.offset(x = menuX, y = menuY).width(menuWidth)
                        .graphicsLayer {
                            val p = contextMenuProgress
                            alpha = p
                            scaleX = .9f + .1f * p
                            scaleY = .9f + .1f * p
                            transformOrigin = TransformOrigin(.5f, 0f)
                        }
                        .shadow(16.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color.Black.copy(alpha = .05f), RoundedCornerShape(18.dp))
                        .padding(vertical = 4.dp)
                        .zIndex(2f)
                ) {
                    ContextMenuAction(Icons.Outlined.PlayArrow, "立即播放") { contextMenuOpen = false; playVideo(video) }
                    ContextMenuAction(Icons.Outlined.Edit, "记录感想") { contextMenuOpen = false; startNewReview(video, showVideoActions = false) }
                    ContextMenuAction(if (video.isFav) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, if (video.isFav) "取消收藏" else "加入收藏") { repository.favorite(video.id); refresh(); contextMenuOpen = false }
                    ContextMenuAction(Icons.Outlined.FolderOpen, "移动到分类") { selected = video; parentSheet = SheetType.NONE; contextMenuOpen = false; sheet = SheetType.MOVE }
                    ContextMenuAction(Icons.Outlined.Edit, "重命名") { selected = video; editName = displayVideoName(video.name); parentSheet = SheetType.NONE; contextMenuOpen = false; sheet = SheetType.RENAME }
                    ContextMenuAction(Icons.Outlined.Image, "更换封面") { selected = video; contextMenuOpen = false; coverPicker.launch("image/*") }
                    ContextMenuAction(Icons.Outlined.DeleteOutline, "移出片库", danger = true) { repository.removeFromLibrary(video.id); refresh(); contextMenuOpen = false }
                }
            }
        }
        if (showClearDataDialog || clearDialogProgress > 0f) {
            Box(Modifier.fillMaxSize().zIndex(5f).background(Color.Black.copy(alpha = .28f * clearDialogProgress)).noRippleClickable { showClearDataDialog = false }, contentAlignment = Alignment.Center) {
                androidx.compose.animation.AnimatedVisibility(visible = showClearDataDialog,
                    enter = androidx.compose.animation.scaleIn(initialScale = .8f, animationSpec = tween(340, easing = androidx.compose.animation.core.CubicBezierEasing(.34f, 1.56f, .64f, 1f))) + fadeIn(tween(340)),
                    exit = androidx.compose.animation.scaleOut(targetScale = .9f, animationSpec = tween(240, easing = androidx.compose.animation.core.CubicBezierEasing(.4f, 0f, 1f, 1f))) + fadeOut(tween(240))) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp).shadow(18.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(Color.White).noRippleClickable { }.padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("清空全部数据？", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MainText)
                        Text("此操作会删除片库条目、分类、封面和本地记录，且无法撤销。", fontSize = 14.sp, lineHeight = 21.sp, color = MutedText, modifier = Modifier.padding(top = 26.dp, bottom = 22.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF2F2F4)).noRippleClickable { showClearDataDialog = false }, contentAlignment = Alignment.Center) { Text("取消", color = MainText, fontWeight = FontWeight.Medium) }
                            Box(Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFFF3B30)).noRippleClickable { repository.clearAll(); refresh(); showClearDataDialog = false }, contentAlignment = Alignment.Center) { Text("确认清空", color = Color.White, fontWeight = FontWeight.SemiBold) }
                        }
                    }
                }
            }
        }
        if (showImportDataDialog || importDialogProgress > 0f) {
            Box(Modifier.fillMaxSize().zIndex(7f).background(Color.Black.copy(alpha = .28f * importDialogProgress)).noRippleClickable { showImportDataDialog = false; pendingImportUri = null }, contentAlignment = Alignment.Center) {
                androidx.compose.animation.AnimatedVisibility(visible = showImportDataDialog,
                    enter = androidx.compose.animation.scaleIn(initialScale = .8f, animationSpec = tween(340, easing = androidx.compose.animation.core.CubicBezierEasing(.34f, 1.56f, .64f, 1f))) + fadeIn(tween(340)),
                    exit = androidx.compose.animation.scaleOut(targetScale = .9f, animationSpec = tween(240, easing = androidx.compose.animation.core.CubicBezierEasing(.4f, 0f, 1f, 1f))) + fadeOut(tween(240))) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp).shadow(18.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(Color.White).noRippleClickable { }.padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("导入并覆盖数据？", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MainText)
                        Text("导入会覆盖当前片库中的全部数据，且无法撤销。确定继续吗？", fontSize = 14.sp, lineHeight = 21.sp, color = MutedText, modifier = Modifier.padding(top = 26.dp, bottom = 22.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF2F2F4)).noRippleClickable { showImportDataDialog = false; pendingImportUri = null }, contentAlignment = Alignment.Center) { Text("取消", color = MainText, fontWeight = FontWeight.Medium) }
                            Box(Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(14.dp)).background(ThemeTeal).noRippleClickable {
                                val uri = pendingImportUri
                                showImportDataDialog = false
                                pendingImportUri = null
                                if (uri != null) importBackup(uri)
                            }, contentAlignment = Alignment.Center) { Text("确认导入", color = Color.White, fontWeight = FontWeight.SemiBold) }
                        }
                    }
                }
            }
        }
        if (toast.isNotBlank()) {
            Box(Modifier.fillMaxSize()) {
                Surface(Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp), shape = CircleShape, color = Color(0xDD222222)) {
                    Text(toast, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp))
                }
            }
        }

    Box(Modifier.fillMaxSize().zIndex(if (sheet == SheetType.DELETE_CATEGORY || sheet == SheetType.NEW_CATEGORY) 2f else 10f)) {
            AnimatedVisibility(
                visible = showCategoryDrawer,
                enter = fadeIn(tween(320, easing = androidx.compose.animation.core.FastOutSlowInEasing)),
                exit = fadeOut(tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing)),
                modifier = Modifier.fillMaxSize().zIndex(0f)
            ) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .28f)).noRippleClickable {
                    showCategoryDrawer = false
                    categoryEditMode = false
                })
            }
            AnimatedVisibility(
                visible = showCategoryDrawer,
                enter = slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(320, easing = androidx.compose.animation.core.FastOutSlowInEasing)) + fadeIn(tween(320, easing = androidx.compose.animation.core.FastOutSlowInEasing)),
                exit = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing)) + fadeOut(tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing)),
                modifier = Modifier.align(Alignment.CenterStart).zIndex(1f)
            ) {
                BoxWithConstraints(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                    Column(
                        Modifier.width(maxWidth * 0.75f).padding(start = 12.dp).fillMaxHeight().padding(vertical = 40.dp)
                            .shadow(12.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp))
                            .background(PageBg).noRippleClickable { }.padding(horizontal = 18.dp, vertical = 20.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(bottom = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("分类管理", Modifier.weight(1f), color = MainText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            ActionIcon(if (categoryEditMode) Icons.Outlined.Check else Icons.Outlined.Edit, if (categoryEditMode) "完成编辑" else "编辑分类", { categoryEditMode = !categoryEditMode; editingCategoryName = null })
                            Spacer(Modifier.width(8.dp))
                            ActionIcon(Icons.Outlined.Add, "新建分类", { newCategoryName = ""; sheet = SheetType.NEW_CATEGORY })
                            Spacer(Modifier.width(8.dp))
                            ActionIcon(Icons.Outlined.Close, "关闭分类管理", { showCategoryDrawer = false; categoryEditMode = false; editingCategoryName = null })
                        }
                        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            items(listOf("全部", "全部分类", "我的最爱"), key = { it }) { value ->
                                val count = when (value) {
                                    "全部" -> videos.size
                                    "全部分类" -> videos.count { it.category == "全部分类" }
                                    else -> videos.count { it.isFav }
                                }
                                val label = when (value) {
                                    "全部" -> "全部"
                                    "全部分类" -> "未分类"
                                    else -> "收藏"
                                }
                                val itemIcon = when (value) {
                                    "全部" -> Icons.Outlined.Apps
                                    "全部分类" -> Icons.Outlined.FolderOpen
                                    else -> Icons.Outlined.FavoriteBorder
                                }
                                Row(
                                    Modifier.fillMaxWidth().height(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (!categoryEditMode && category == value) Color.White else Color.Transparent)
                                        .then(if (!categoryEditMode) Modifier.noRippleClickable { category = value; showCategoryDrawer = false } else Modifier)
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(itemIcon, null, tint = MainText, modifier = Modifier.size(21.dp))
                                    Text(label, Modifier.weight(1f).padding(start = 12.dp), color = MainText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                    Text(count.toString(), color = MutedText, fontSize = 13.sp)
                                }
                            }
                            item(key = "category-fixed-divider") {
                                Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                    Spacer(Modifier.fillMaxWidth().padding(horizontal = 12.dp).height(0.5.dp).background(Color(0xFFE6E6E6)))
                                }
                            }
                            items(categories.filterNot { it == "全部" || it == "全部分类" || it == "我的最爱" }.distinct(), key = { it }) { value ->
                                var dragRemainder by remember(value) { mutableFloatStateOf(0f) }
                                val latestCategories = rememberUpdatedState(categories)
                                val count = videos.count { it.category == value }
                                Row(
                                    Modifier.fillMaxWidth().height(52.dp).animateItemPlacement(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                                        .then(if (categoryEditMode && editingCategoryName != value) Modifier.pointerInput(value) {
                                            detectDragGesturesAfterLongPress(onDragEnd = { dragRemainder = 0f }, onDragCancel = { dragRemainder = 0f }) { change, dragAmount ->
                                                change.consume()
                                                dragRemainder += dragAmount.y
                                                val threshold = 25.dp.toPx()
                                                val step = when { dragRemainder >= threshold -> 1; dragRemainder <= -threshold -> -1; else -> 0 }
                                                if (step != 0) {
                                                    val currentCategories = latestCategories.value
                                                    val from = currentCategories.indexOf(value)
                                                    val to = (from + step).coerceIn(1, currentCategories.lastIndex - 1)
                                                    if (from != to) {
                                                        val reordered = currentCategories.toMutableList()
                                                        val moved = reordered.removeAt(from)
                                                        reordered.add(to, moved)
                                                        categories = reordered
                                                        repository.saveCategories(reordered)
                                                    }
                                                    dragRemainder = 0f
                                                }
                                            }
                                        } else Modifier)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (!categoryEditMode && category == value) Color.White else Color.Transparent)
                                        .then(if (!categoryEditMode) Modifier.noRippleClickable { category = value; showCategoryDrawer = false } else Modifier)
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(Modifier.size(21.dp), contentAlignment = Alignment.Center) {
                                        if (categoryEditMode) Icon(Icons.Outlined.Menu, "拖动排序", tint = Color(0xFF999999), modifier = Modifier.size(18.dp))
                                        else Icon(Icons.Outlined.Folder, null, tint = MainText, modifier = Modifier.size(21.dp))
                                    }
                                    if (editingCategoryName == value) {
                                        BasicTextField(
                                            value = editingCategoryDraft,
                                            onValueChange = { editingCategoryDraft = it },
                                            modifier = Modifier.weight(1f).padding(start = 12.dp).focusRequester(categoryRenameFocusRequester),
                                            singleLine = true,
                                            textStyle = TextStyle(fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, color = MainText, platformStyle = PlatformTextStyle(includeFontPadding = false))
                                        )
                                        Box(Modifier.size(28.dp).noRippleClickable {
                                            val renamed = editingCategoryDraft.trim()
                                            when {
                                                renamed.isEmpty() -> toast = "分类名称不能为空"
                                                renamed != value && renamed in categories -> toast = "分类名称已存在"
                                                else -> {
                                                    val renamedCategories = categories.map { if (it == value) renamed else it }
                                                    repository.saveCategories(renamedCategories)
                                                    videos.filter { it.category == value }.forEach { repository.update(it.copy(category = renamed)) }
                                                    if (category == value) category = renamed
                                                    editingCategoryName = null
                                                    editingCategoryDraft = ""
                                                    refresh()
                                                }
                                            }
                                        }, contentAlignment = Alignment.CenterEnd) {
                                            Icon(Icons.Outlined.Check, "保存分类名称", tint = ThemeTeal, modifier = Modifier.size(18.dp))
                                        }
                                        Box(Modifier.size(28.dp).noRippleClickable {
                                            editingCategoryName = null
                                            editingCategoryDraft = ""
                                        }, contentAlignment = Alignment.CenterEnd) {
                                            Icon(Icons.Outlined.Close, "取消重命名", tint = Color(0xFF999999), modifier = Modifier.size(18.dp))
                                        }
                                    } else {
                                        Text(
                                            value,
                                            Modifier.weight(1f).padding(start = 12.dp).then(
                                                if (categoryEditMode) Modifier.pointerInput(value) {
                                                    detectTapGestures(onDoubleTap = {
                                                        editingCategoryName = value
                                                        editingCategoryDraft = value
                                                    })
                                                } else Modifier
                                            ),
                                            color = MainText, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                                            maxLines = 1, overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (categoryEditMode && editingCategoryName != value) {
                                        Box(Modifier.size(28.dp).noRippleClickable {
                                            pendingCategoryDelete = value
                                            sheet = SheetType.DELETE_CATEGORY
                                        }, contentAlignment = Alignment.CenterEnd) {
                                            Icon(Icons.Outlined.DeleteOutline, "删除分类", tint = Color(0xFF999999), modifier = Modifier.size(18.dp))
                                        }
                                    } else if (editingCategoryName != value) {
                                        Text(count.toString(), color = MutedText, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showNestedCategorySheet) {
                Box(Modifier.fillMaxSize().zIndex(2f)) {
                    Box(
                        Modifier.fillMaxSize()
                            .background(Color.Black.copy(alpha = .28f))
                            .noRippleClickable { sheet = SheetType.NONE }
                    )
                    Column(
                        Modifier.align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .imePadding()
                            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * .85f)
                            .shadow(10.dp, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), ambientColor = Color.Black.copy(alpha = .10f), spotColor = Color.Black.copy(alpha = .10f))
                            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                            .background(PageBg)
                            .verticalScroll(rememberScrollState())
                            .padding(start = 20.dp, top = 26.dp, end = 20.dp, bottom = 30.dp)
                    ) {
                        if (sheet == SheetType.DELETE_CATEGORY) {
                            SheetHeading("确定删除该分类？")
                            SheetAction("删除该分类", danger = true, bottomPadding = 0.dp) {
                                val target = pendingCategoryDelete
                                if (target != null) {
                                    repository.saveCategories(categories - target)
                                    videos.filter { it.category == target }.forEach { repository.update(it.copy(category = "全部分类")) }
                                    if (category == target) category = "全部"
                                    pendingCategoryDelete = null
                                    refresh()
                                }
                                sheet = SheetType.NONE
                            }
                        } else {
                            SheetHeading("新建分类")
                            BasicTextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                                    .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                    .clip(RoundedCornerShape(16.dp)).background(Color.White).padding(horizontal = 20.dp),
                                singleLine = true,
                                decorationBox = { inner ->
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                                        if (newCategoryName.isEmpty()) Text("输入分类名称", color = MutedText, fontSize = 16.sp)
                                        inner()
                                    }
                                },
                                textStyle = TextStyle(fontSize = 16.sp, lineHeight = 18.sp, color = MainText, platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Spacer(Modifier.height(12.dp))
                            SheetActionPair(
                                onConfirm = {
                                    val name = newCategoryName.trim()
                                    when {
                                        name.isEmpty() -> toast = "分类名称不能为空"
                                        name in categories -> toast = "分类名称已存在"
                                        else -> {
                                            repository.saveCategories(categories + name)
                                            newCategoryName = ""
                                            refresh()
                                            sheet = SheetType.NONE
                                        }
                                    }
                                },
                                onCancel = {
                                    newCategoryName = ""
                                    sheet = SheetType.NONE
                                }
                            )
                        }
                    }
                }
            }
        }

    }

@Composable
private fun BottomPillNavigation(current: MainPage, hazeState: HazeState, sheetBlur: androidx.compose.ui.unit.Dp, onBoundsChanged: ((Rect) -> Unit)? = null, onSelect: (MainPage) -> Unit) {
    val entries = listOf(
        MainPage.HOME to FrameHomeIcon,
        MainPage.LIBRARY to FrameLibraryIcon,
        MainPage.RECENT to FrameRecentIcon,
        MainPage.RECORDS to FrameRecordsIcon
    )
    BoxWithConstraints(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 78.dp, bottom = 16.dp)
            .onGloballyPositioned { onBoundsChanged?.invoke(it.boundsInRoot()) }
            .height(56.dp)
            .shadow(12.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black.copy(alpha = .18f), spotColor = Color.Black.copy(alpha = .28f))
    ) {
        val navWidth = maxWidth
        Box(
            Modifier.fillMaxSize()
                .hazeEffect(state = hazeState, style = HazeStyle(backgroundColor = PageBg, tints = emptyList(), blurRadius = 20.dp, noiseFactor = 0f))
                .clip(RoundedCornerShape(28.dp))
                .background(Glass)
                .border(0.3.dp, Color(0xFFE6E6E6), RoundedCornerShape(28.dp))
                .padding(5.dp)
        ) {
            val slot = (navWidth - 10.dp) / 4
            val target = slot * entries.indexOfFirst { it.first == current }
            val offset by animateDpAsState(target, tween(300, easing = androidx.compose.animation.core.CubicBezierEasing(.25f, 1f, .5f, 1f)), label = "nav-indicator")
            Box(Modifier.offset(x = offset).width(slot).fillMaxHeight().clip(CircleShape).background(Color.Black.copy(alpha = .06f)))
            Row(Modifier.fillMaxSize()) {
                entries.forEach { (item, icon) ->
                    Column(Modifier.weight(1f).fillMaxHeight().noRippleClickable { onSelect(item) }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(icon, item.label, Modifier.size(20.dp), tint = MainText)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            item.label,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MainText,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomePage(
    videos: List<VideoItem>, weeklyWatchTimeMs: Long, hazeState: HazeState,
    listState: LazyListState,
    onSettings: () -> Unit, onVideo: (VideoItem) -> Unit
) {
    val context = LocalContext.current
    val continuing = remember(videos) { videos.filter { it.lastPlayedAt > 0L && it.progress < 100f }.sortedByDescending { it.lastPlayedAt }.take(4) }
    val recent = remember(videos) { videos.sortedByDescending { it.addedAt }.take(10) }
    val homeExpansionPreferences = remember {
        context.getSharedPreferences("frame_home_expansion", Context.MODE_PRIVATE)
    }
    var continueExpanded by remember {
        mutableStateOf(homeExpansionPreferences.getBoolean("continue_expanded", true))
    }
    var recentExpanded by remember {
        mutableStateOf(homeExpansionPreferences.getBoolean("recent_expanded", true))
    }
    val showTopBar by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 20
        }
    }
    val density = LocalDensity.current
    val statusBarBottomPx = WindowInsets.statusBars.getTop(density).toFloat()
    val titleSafetyGapPx = with(density) { 4.dp.toPx() }
    val homeTopFadeStartPx = statusBarBottomPx + with(density) { 48.dp.toPx() }
    val homeBottomFadeEndPx = with(density) { (LocalConfiguration.current.screenHeightDp.dp - 72.dp).toPx() }
    val homeFadeLengthPx = with(density) { 48.dp.toPx() }
    val homeTitleTopPx = remember { mutableFloatStateOf(0f) }
    val homeTitleAlphaState = remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 0f
            else titleFadeAlpha(
                listState.firstVisibleItemScrollOffset.toFloat(),
                homeTitleTopPx.floatValue,
                statusBarBottomPx,
                titleSafetyGapPx
            )
        }
    }
    val homeTitleHeightPx = remember { mutableIntStateOf(0) }
    val homeOffscreenPx = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val homeTitleScrollDistancePx = remember {
        derivedStateOf {
            when (listState.firstVisibleItemIndex) {
                0 -> listState.firstVisibleItemScrollOffset.toFloat()
                1 -> (homeTitleHeightPx.intValue + listState.firstVisibleItemScrollOffset).toFloat()
                else -> homeTitleHeightPx.intValue + homeOffscreenPx
            }
        }
    }
    val homeOverlayOffsets by remember(listState) {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.associate { it.key to it.offset.toFloat() }
        }
    }
    Box(Modifier.fillMaxSize().clipToBounds()) {
        AnimatedVisibility(
            visible = showTopBar,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                Modifier.fillMaxWidth()
                    .background(PageBg)
                    .statusBarsPadding()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("首页", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MainText, modifier = Modifier.graphicsLayer { alpha = 0f })
            }
        }
        LazyColumn(state = listState, flingBehavior = rememberSoftListFlingBehavior(), modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).clipToBounds(), contentPadding = PaddingValues(bottom = 88.dp)) {
            item(key = "home-large-title") {
                Box(
                    Modifier.zIndex(1f)
                        .onGloballyPositioned {
                            homeTitleHeightPx.intValue = it.size.height
                            if (homeTitleTopPx.floatValue == 0f && listState.firstVisibleItemIndex == 0) {
                                homeTitleTopPx.floatValue = it.positionInWindow().y + listState.firstVisibleItemScrollOffset + with(density) { 40.dp.toPx() }
                            }
                        }
                        .graphicsLayer { alpha = 0f }
                ) {
                    Header("首页", onSettings)
                }
            }
            item(key = "home-stat-panel") {
                Column(
                    Modifier.fillMaxWidth()
                        .graphicsLayer { alpha = 0f }
                        .background(PageBg)
                        .hazeEffect(state = hazeState, style = HazeStyle(backgroundColor = PageBg, tints = emptyList(), blurRadius = 10.dp, noiseFactor = 0f))
                        .drawBehind {
                            val bleed = 20.dp.toPx()
                            drawRect(PageBg, topLeft = Offset(-bleed, 0f), size = Size(size.width + bleed * 2f, size.height))

                        }
                ) {
                    Row(
                        Modifier.fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = .07f), spotColor = Color.Black.copy(alpha = .05f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Glass)
                            .padding(horizontal = 10.dp, vertical = 15.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Stat("视频总数", videos.size.toString(), "个", Modifier.weight(1f))
                        Box(Modifier.width(1.dp).height(30.dp).background(Color.Black.copy(alpha = .05f)))
                        Stat("已看完", videos.count { it.progress >= 100f || it.status == "watched" }.toString(), "个", Modifier.weight(1f))
                        Box(Modifier.width(1.dp).height(30.dp).background(Color.Black.copy(alpha = .05f)))
                        Stat("本周时长", (weeklyWatchTimeMs / 3600000L).toString(), "h", Modifier.weight(1f))
                    }
                }
            }
            item(key = "home-stat-panel-gap") { Spacer(Modifier.height(14.dp)) }
            item(key = "home-continue-heading") {
                SectionHeading("继续观看", continueExpanded, onToggle = {
                    continueExpanded = !continueExpanded
                    homeExpansionPreferences.edit().putBoolean("continue_expanded", continueExpanded).apply()
                })
            }
            item(key = "home-continue-content") {
                AnimatedVisibility(
                    visible = continueExpanded,
                    enter = androidx.compose.animation.expandVertically(tween(300, easing = androidx.compose.animation.core.CubicBezierEasing(.34f, 1.12f, .64f, 1f))) + fadeIn(tween(300)),
                    exit = androidx.compose.animation.shrinkVertically(tween(250, easing = androidx.compose.animation.core.CubicBezierEasing(.4f, 0f, 1f, 1f))) + fadeOut(tween(250))
                ) {
                    if (continuing.isEmpty()) EmptyHint("暂无观看记录", horizontalPadding = 0.dp) else {
                        val heroListState = rememberLazyListState()
                        val snapFling = rememberSnapFlingBehavior(heroListState, SnapPosition.Start)
                        val gentleSnapFling = remember(snapFling) {
                            object : FlingBehavior {
                                override suspend fun ScrollScope.performFling(initialVelocity: Float): Float =
                                    with(snapFling) { performFling(initialVelocity * .58f) }
                            }
                        }
                        LazyRow(state = heroListState, flingBehavior = gentleSnapFling, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            itemsIndexed(continuing, key = { _, video -> video.id }) { _, video ->
                                Box(
                                    Modifier.width(280.dp).height(160.dp).shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .10f), spotColor = Color.Black.copy(alpha = .10f))
                                        .clip(RoundedCornerShape(16.dp)).background(PosterBrush(video.bgIndex)).noRippleClickable { onVideo(video) }
                                ) {
                                    Thumbnail(video, Modifier.fillMaxSize())
                                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colorStops = arrayOf(0f to Color.Transparent, .4f to Color.Black.copy(alpha = .2f), 1f to Color.Black.copy(alpha = .8f)))))
                                    Column(Modifier.align(Alignment.BottomStart).padding(15.dp)) {
                                        VideoCardTitle(
                                            video.name,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            englishFontSize = 14.sp,
                                            englishFontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            showEnglish = true
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text("播放至 ${formatTime((video.durationMs * video.progress.coerceIn(0f, 100f) / 100f).toLong())}", color = Color.White.copy(alpha = .8f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                                        Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = .2f))) {
                                            Box(Modifier.fillMaxWidth((video.progress / 100f).coerceIn(0f, 1f)).fillMaxHeight().background(ThemeTeal))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item(key = "home-recent-heading") {
                SectionHeading(
                    "最近添加",
                    recentExpanded,
                    chevronExpandDurationMs = 300 + (recent.size - 1).coerceAtLeast(0) * 60,
                    chevronCollapseDurationMs = 300 + (recent.size - 1).coerceAtLeast(0) * 60,
                    chevronExpandEasing = androidx.compose.animation.core.FastOutSlowInEasing,
                    chevronCollapseEasing = androidx.compose.animation.core.FastOutSlowInEasing,
                    onToggle = {
                        recentExpanded = !recentExpanded
                        homeExpansionPreferences.edit().putBoolean("recent_expanded", recentExpanded).apply()
                    }
                )
            }
            item(key = "home-recent-heading-gap-bottom") { Spacer(Modifier.height(4.dp)) }
            item(key = "home-recent-content") {
                Column(Modifier.fillMaxWidth()) {
                    if (recent.isEmpty()) {
                        AnimatedVisibility(
                            visible = recentExpanded,
                            enter = expandVertically(tween(300)) + fadeIn(tween(220)),
                            exit = shrinkVertically(tween(300)) + fadeOut(tween(220))
                        ) {
                            EmptyHint("暂无视频，点击右下角添加", horizontalPadding = 0.dp)
                        }
                    } else {
                        recent.forEachIndexed { index, video ->
                            val collapseDelay = (recent.size - index - 1) * 60
                            AnimatedVisibility(
                                visible = recentExpanded,
                                enter = expandVertically(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        delayMillis = index * 60,
                                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                                    )
                                ) + fadeIn(tween(durationMillis = 220, delayMillis = index * 60)) +
                                    slideInVertically(
                                        initialOffsetY = { it / 8 },
                                        animationSpec = tween(
                                            durationMillis = 300,
                                            delayMillis = index * 60,
                                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                                        )
                                    ),
                                exit = shrinkVertically(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        delayMillis = collapseDelay,
                                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                                    )
                                ) + fadeOut(tween(durationMillis = 220, delayMillis = collapseDelay)) +
                                    slideOutVertically(
                                        targetOffsetY = { it / 8 },
                                        animationSpec = tween(
                                            durationMillis = 300,
                                            delayMillis = collapseDelay,
                                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                                        )
                                    )
                            ) {
                                VideoRow(video, "${video.date} 添加") { onVideo(video) }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }

        Box(
            Modifier.fillMaxWidth().zIndex(1f)
                .graphicsLayer {
                    translationY = -homeTitleScrollDistancePx.value
                    alpha = homeTitleAlphaState.value
                }
                .padding(horizontal = 20.dp)
        ) {
            Header("首页", onSettings)
        }
        val homeStatsOffset = homeOverlayOffsets["home-stat-panel"]
        if (homeStatsOffset != null) {
            Column(
                Modifier.fillMaxWidth()
                    .graphicsLayer { translationY = homeStatsOffset }
                    .background(PageBg)
                    .hazeEffect(state = hazeState, style = HazeStyle(backgroundColor = PageBg, tints = emptyList(), blurRadius = 10.dp, noiseFactor = 0f))
                    .drawBehind {
                        val bleed = 20.dp.toPx()
                        drawRect(PageBg, topLeft = Offset(-bleed, 0f), size = Size(size.width + bleed * 2f, size.height))
                    }
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                        .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = .07f), spotColor = Color.Black.copy(alpha = .05f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Glass)
                        .padding(horizontal = 10.dp, vertical = 15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Stat("视频总数", videos.size.toString(), "个", Modifier.weight(1f))
                    Box(Modifier.width(1.dp).height(30.dp).background(Color.Black.copy(alpha = .05f)))
                    Stat("已看完", videos.count { it.progress >= 100f || it.status == "watched" }.toString(), "个", Modifier.weight(1f))
                    Box(Modifier.width(1.dp).height(30.dp).background(Color.Black.copy(alpha = .05f)))
                    Stat("本周时长", (weeklyWatchTimeMs / 3600000L).toString(), "h", Modifier.weight(1f))
                }
            }
        }

        val showStickyTitle = showTopBar && homeTitleAlphaState.value <= 0f
        val stickyTitleVisibility = remember { MutableTransitionState(false) }
        stickyTitleVisibility.targetState = showStickyTitle
        Box(
            Modifier.align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    if (stickyTitleVisibility.currentState || stickyTitleVisibility.targetState) PageBg
                    else Color.Transparent
                )
                .statusBarsPadding()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visibleState = stickyTitleVisibility,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {
                Text("首页", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MainText)
            }
        }
    }
}

@Composable
private fun rememberSoftListFlingBehavior(): FlingBehavior {
    val decay = remember { exponentialDecay<Float>(frictionMultiplier = 0.8f) }
    return remember(decay) {
        object : FlingBehavior {
            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                var lastValue = 0f
                val animation = AnimationState(initialValue = 0f, initialVelocity = initialVelocity)
                animation.animateDecay(decay) {
                    val delta = value - lastValue
                    val consumed = scrollBy(delta)
                    lastValue = value
                    if (abs(delta - consumed) > 0.5f) cancelAnimation()
                }
                return animation.velocity
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LibraryPage(
    hazeState: HazeState,
    listState: androidx.compose.foundation.lazy.grid.LazyGridState,
    stickyCategoryScrollState: androidx.compose.foundation.ScrollState,
    floatingCategoryScrollState: androidx.compose.foundation.ScrollState,
    videos: List<VideoItem>, categories: List<String>, status: String, category: String, compact: Boolean,
    sort: SortMode, sortDirection: SortDirection,
    onSortModeChange: (SortMode) -> Unit, onSortDirectionChange: (SortDirection) -> Unit,
    onCompactChange: (Boolean) -> Unit,
    onStatus: (String) -> Unit, onCategory: (String) -> Unit,
    categoryDrawerVisible: Boolean, onOpenCategoryDrawer: () -> Unit, onLongVideo: (VideoItem, Rect) -> Unit, onPlay: (VideoItem) -> Unit,
    contextMenuVideo: VideoItem?, contextMenuReturnProgress: Float, onContextMenuBoundsChanged: (Rect) -> Unit,
    onViewportBoundsChanged: (Rect) -> Unit, onStickyHeaderBoundsChanged: (Rect) -> Unit,
    multiSelectMode: Boolean, onMultiSelectModeChange: (Boolean) -> Unit,
    onBatchMove: (Set<Long>, String) -> Unit,
    onBatchFavorite: (Set<Long>, Boolean) -> Unit,
    onBatchStatus: (Set<Long>, String) -> Unit,
    onBatchRemove: (Set<Long>) -> Unit
) {
    var selectedVideoIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val selectedCount = selectedVideoIds.size
    BackHandler(enabled = multiSelectMode && !categoryDrawerVisible) {
        selectedVideoIds = emptySet()
        onMultiSelectModeChange(false)
    }
    val selectedVideos = videos.filter { it.id in selectedVideoIds }
    val selectedAreAllFavorite = selectedVideos.let { it.isNotEmpty() && it.all { video -> video.isFav } }
    val commonSelectedCategory = selectedVideos.map { it.category }.distinct().singleOrNull()
    val commonSelectedStatus = selectedVideos.map { it.status }.distinct().singleOrNull()
    val availableMoveCategories = categories.filterNot { it == "我的最爱" }
    val softListFlingBehavior = rememberSoftListFlingBehavior()
    val density = LocalDensity.current
    val statusBarBottomPx = WindowInsets.statusBars.getTop(density).toFloat()
    val libraryStickyHeaderHeightPx = remember { mutableIntStateOf(0) }
    val libraryBottomFadeEndPx = with(density) { (LocalConfiguration.current.screenHeightDp.dp - 72.dp).toPx() }
    val libraryFadeLengthPx = with(density) { 48.dp.toPx() }
    val titleSafetyGapPx = with(density) { 4.dp.toPx() }
    val libraryTitleTopPx = remember { mutableFloatStateOf(0f) }
    val titleAlphaState = remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 0f
            else titleFadeAlpha(
                listState.firstVisibleItemScrollOffset.toFloat(),
                libraryTitleTopPx.floatValue,
                statusBarBottomPx,
                titleSafetyGapPx
            )
        }
    }
    val filtered = remember(videos, status, category) {
        videos.filter {
            (status == "全部" || (status == "未看" && it.status == "all" && it.progress <= 0f) || (status == "在看" && (it.status == "playing" || it.progress in .01f..99.99f)) || (status == "已看" && (it.status == "watched" || it.progress >= 100f))) &&
                (category == "全部" || (category == "全部分类" && it.category == "全部分类") || (category == "我的最爱" && it.isFav) || (category != "全部" && category != "全部分类" && category != "我的最爱" && it.category == category))
        }
    }
    val columns = if (compact) 3 else 2
    val gap = if (compact) 10.dp else 16.dp
    val libraryTitleHeightPx = remember { mutableIntStateOf(0) }
    val libraryOffscreenPx = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val libraryTitleScrollDistancePx = remember {
        derivedStateOf {
            when (listState.firstVisibleItemIndex) {
                0 -> listState.firstVisibleItemScrollOffset.toFloat()
                1 -> (libraryTitleHeightPx.intValue + listState.firstVisibleItemScrollOffset).toFloat()
                else -> libraryTitleHeightPx.intValue + libraryOffscreenPx
            }
        }
    }
    Box(Modifier.fillMaxSize().clipToBounds().onGloballyPositioned { onViewportBoundsChanged(it.boundsInRoot()) }) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            state = listState,
            flingBehavior = softListFlingBehavior,
            modifier = Modifier.fillMaxSize().background(PageBg).clipToBounds(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = if (compact) 114.dp else 108.dp),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap)
        ) {
            item(key = "library-title", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Row(
                    Modifier.fillMaxWidth()
                        .onGloballyPositioned {
                            libraryTitleHeightPx.intValue = it.size.height
                            if (libraryTitleTopPx.floatValue == 0f && listState.firstVisibleItemIndex == 0) {
                                libraryTitleTopPx.floatValue = it.positionInWindow().y + listState.firstVisibleItemScrollOffset + with(density) { 40.dp.toPx() }
                            }
                        }
                        .graphicsLayer { alpha = 0f }
                        .padding(top = 40.dp, bottom = 15.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("片库", Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
                    Spacer(Modifier.size(36.dp))
                    Spacer(Modifier.size(36.dp))
                    Spacer(Modifier.size(36.dp))
                }
            }
            item(key = "library-filter-header", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                LibraryFilterHeader(
                    categories = categories, status = status, category = category,
                    categoryScrollState = stickyCategoryScrollState,
                    onStatus = onStatus, onCategory = onCategory,
                    onOpenCategoryDrawer = onOpenCategoryDrawer,
                    alpha = 0f,
                    onBoundsChanged = { libraryStickyHeaderHeightPx.intValue = it.height.roundToInt() },
                    horizontalPadding = 0.dp,
                    categoryBottomPadding = 0.dp,
                    containerBottomPadding = if (compact) 6.dp else 0.dp
                )
            }
            if (filtered.isEmpty()) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    EmptyHint(
                        if (videos.isEmpty()) "片库还是空的，点击右下角导入视频" else "没有符合条件的视频",
                        horizontalPadding = 0.dp,
                        topPadding = (16.dp - gap).coerceAtLeast(0.dp)
                    )
                }
            } else {
                items(filtered, key = { it.id }) { video ->
                    Box(Modifier.fillMaxWidth()) {
                        PosterCard(
                            video, compact,
                            onPlay = { if (multiSelectMode) {
                                selectedVideoIds = if (video.id in selectedVideoIds) selectedVideoIds - video.id else selectedVideoIds + video.id
                            } else onPlay(video) },
                            onLong = { bounds -> onLongVideo(video, bounds) },
                            modifier = Modifier.fillMaxWidth(),
                            alpha = 1f,
                            onBoundsChanged = if (contextMenuVideo?.id == video.id) onContextMenuBoundsChanged else null
                        )
                        if (multiSelectMode) {
                            Box(
                                Modifier.align(Alignment.TopEnd).padding(7.dp).size(25.dp)
                                    .clip(CircleShape)
                                    .background(if (video.id in selectedVideoIds) ThemeTeal else Color.Black.copy(alpha = .42f))
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (video.id in selectedVideoIds) Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(17.dp))
                            }
                        }
                    }
                }
            }
        }
        Row(
            Modifier.fillMaxWidth()
                .background(PageBg)
                .graphicsLayer {
                    translationY = if (multiSelectMode) 0f else -libraryTitleScrollDistancePx.value
                    alpha = if (multiSelectMode) 1f else titleAlphaState.value
                }
                .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (multiSelectMode) {
                TextButton(onClick = { selectedVideoIds = emptySet(); onMultiSelectModeChange(false) }, contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Text("取消", color = ThemeTeal, fontSize = 16.sp)
                }
                Text("已选 $selectedCount 项", Modifier.weight(1f), fontSize = 19.sp, fontWeight = FontWeight.Bold, color = MainText, textAlign = TextAlign.Center, maxLines = 1)
                TextButton(onClick = {
                    val visibleIds = filtered.map { it.id }.toSet()
                    selectedVideoIds = if (visibleIds.isNotEmpty() && visibleIds.all { it in selectedVideoIds }) selectedVideoIds - visibleIds else selectedVideoIds + visibleIds
                }, contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Text(if (filtered.isNotEmpty() && filtered.all { it.id in selectedVideoIds }) "取消全选" else "全选", color = ThemeTeal, fontSize = 16.sp)
                }
            } else {
                Text("片库", Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
                ActionIcon(Icons.Outlined.Checklist, "批量选择", {
                    selectedVideoIds = emptySet()
                    onMultiSelectModeChange(true)
                })
                Box {
                    ActionIcon(FrameGridIcon, "视图与排序", { showSortMenu = true })
                    AnimatedDropdownPopup(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        offset = androidx.compose.ui.unit.IntOffset.Zero,
                        modifier = Modifier.width(192.dp)
                    ) {
                        CustomSortMenuItem("2列大图", !compact) { onCompactChange(false); showSortMenu = false }
                        CustomSortMenuItem("3列小图", compact) { onCompactChange(true); showSortMenu = false }
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))
                        CustomSortMenuItem("按添加时间", sort == SortMode.ADDED) { onSortModeChange(SortMode.ADDED); showSortMenu = false }
                        CustomSortMenuItem("按名称", sort == SortMode.NAME) { onSortModeChange(SortMode.NAME); showSortMenu = false }
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))
                        CustomSortMenuItem("正序", sortDirection == SortDirection.ASCENDING) { onSortDirectionChange(SortDirection.ASCENDING); showSortMenu = false }
                        CustomSortMenuItem("倒序", sortDirection == SortDirection.DESCENDING) { onSortDirectionChange(SortDirection.DESCENDING); showSortMenu = false }
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = multiSelectMode && selectedCount > 0,
            enter = slideInVertically(tween(260, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it } + fadeIn(tween(220)),
            exit = slideOutVertically(tween(200, easing = androidx.compose.animation.core.FastOutLinearInEasing)) { it } + fadeOut(tween(160)),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().zIndex(4f)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, bottom = 16.dp)
                    .height(58.dp)
                    .shadow(8.dp, RoundedCornerShape(90.dp), ambientColor = Color.Black.copy(alpha = .08f), spotColor = Color.Black.copy(alpha = .10f))
                    .clip(RoundedCornerShape(90.dp)).background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f).fillMaxHeight().noRippleClickable {
                    onBatchFavorite(selectedVideoIds, !selectedAreAllFavorite)
                    selectedVideoIds = emptySet()
                    onMultiSelectModeChange(false)
                }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Outlined.FavoriteBorder, null, tint = MainText, modifier = Modifier.size(19.dp))
                    Text(if (selectedAreAllFavorite) "取消收藏" else "添加收藏", fontSize = 11.sp, color = MainText, modifier = Modifier.padding(top = 1.dp))
                }
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Column(Modifier.fillMaxSize().noRippleClickable { showMoveDialog = true }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Outlined.FolderOpen, null, tint = MainText, modifier = Modifier.size(19.dp))
                        Text("移动分类", fontSize = 11.sp, color = MainText, modifier = Modifier.padding(top = 1.dp))
                    }
                    AnimatedDropdownPopup(
                        expanded = showMoveDialog,
                        onDismissRequest = { showMoveDialog = false },
                        alignment = Alignment.BottomCenter,
                        offset = with(LocalDensity.current) { androidx.compose.ui.unit.IntOffset(0, -66.dp.roundToPx()) },
                        modifier = Modifier.width(160.dp)
                    ) {
                        availableMoveCategories.forEach { target ->
                            CustomSortMenuItem(
                                if (target == "全部分类") "未分类" else target,
                                commonSelectedCategory == target
                            ) {
                                onBatchMove(selectedVideoIds, target)
                                selectedVideoIds = emptySet()
                                onMultiSelectModeChange(false)
                                showMoveDialog = false
                            }
                        }
                    }
                }
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Column(Modifier.fillMaxSize().noRippleClickable { showStatusDialog = true }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Outlined.CheckCircleOutline, null, tint = MainText, modifier = Modifier.size(19.dp))
                        Text("修改状态", fontSize = 11.sp, color = MainText, modifier = Modifier.padding(top = 1.dp))
                    }
                    AnimatedDropdownPopup(
                        expanded = showStatusDialog,
                        onDismissRequest = { showStatusDialog = false },
                        alignment = Alignment.BottomCenter,
                        offset = with(LocalDensity.current) { androidx.compose.ui.unit.IntOffset(0, -66.dp.roundToPx()) },
                        modifier = Modifier.width(160.dp)
                    ) {
                        listOf("未看", "在看", "已看").forEach { target ->
                            val targetStatus = when (target) {
                                "未看" -> "all"
                                "在看" -> "playing"
                                else -> "watched"
                            }
                            CustomSortMenuItem(target, commonSelectedStatus == targetStatus) {
                                onBatchStatus(selectedVideoIds, target)
                                selectedVideoIds = emptySet()
                                onMultiSelectModeChange(false)
                                showStatusDialog = false
                            }
                        }
                    }
                }
                Column(Modifier.weight(1f).fillMaxHeight().noRippleClickable { showDeleteDialog = true }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Outlined.DeleteOutline, null, tint = Color(0xFFFF3B30), modifier = Modifier.size(19.dp))
                    Text("批量移除", fontSize = 11.sp, color = Color(0xFFFF3B30), modifier = Modifier.padding(top = 1.dp))
                }
            }
        }
        LibraryFilterHeader(
            categories = categories, status = status, category = category,
            categoryScrollState = floatingCategoryScrollState,
            onStatus = onStatus, onCategory = onCategory,
            onOpenCategoryDrawer = onOpenCategoryDrawer,
            alpha = 1f, onBoundsChanged = { rect -> libraryStickyHeaderHeightPx.intValue = rect.height.roundToInt(); onStickyHeaderBoundsChanged(rect) },
            modifier = Modifier.align(Alignment.TopCenter).zIndex(2f)
                .graphicsLayer {
                    translationY = if (multiSelectMode) {
                        libraryTitleHeightPx.intValue.toFloat()
                    } else if (listState.firstVisibleItemIndex == 0) {
                        (libraryTitleHeightPx.intValue - listState.firstVisibleItemScrollOffset).coerceAtLeast(0).toFloat()
                    } else 0f
                }
        )
        if (showDeleteDialog) {
            Dialog(onDismissRequest = { showDeleteDialog = false }) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    color = PageBg,
                    shadowElevation = 0.dp
                ) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp)) {
                        Text(
                            "移出所选视频？",
                            modifier = Modifier.fillMaxWidth(),
                            color = MainText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "确定将 $selectedCount 个视频移出片库吗？视频感想、评分、观看时长和封面缓存都会保留。",
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 22.dp),
                            color = MainText,
                            fontSize = 15.sp,
                            lineHeight = 23.sp,
                            textAlign = TextAlign.Center
                        )
                        SheetActionPair(
                            confirmText = "移出片库",
                            confirmDanger = true,
                            confirmOnRight = true,
                            spacing = 12.dp,
                            onConfirm = {
                                onBatchRemove(selectedVideoIds)
                                selectedVideoIds = emptySet()
                                onMultiSelectModeChange(false)
                                showDeleteDialog = false
                            },
                            onCancel = { showDeleteDialog = false }
                        )
                    }
                }
            }
        }

        }
    }

@Composable
private fun LibraryFilterHeader(
    categories: List<String>, status: String, category: String,
    categoryScrollState: androidx.compose.foundation.ScrollState,
    onStatus: (String) -> Unit, onCategory: (String) -> Unit,
    onOpenCategoryDrawer: () -> Unit,
    alpha: Float, onBoundsChanged: (Rect) -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: androidx.compose.ui.unit.Dp = 20.dp,
    categoryBottomPadding: androidx.compose.ui.unit.Dp = 10.dp,
    containerBottomPadding: androidx.compose.ui.unit.Dp = 1.dp
) {
    Column(
        modifier.fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
            .onGloballyPositioned { onBoundsChanged(it.boundsInRoot()) }
            .background(PageBg)
            .statusBarsPadding()
            .padding(start = horizontalPadding, end = horizontalPadding, top = 9.dp, bottom = containerBottomPadding)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(top = 0.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            listOf("全部", "未看", "在看", "已看").forEach { value ->
                Column(Modifier.noRippleClickable { onStatus(value) }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value, color = if (status == value) MainText else MutedText, fontWeight = FontWeight.Medium, fontSize = 17.sp)
                    if (status == value) Spacer(Modifier.padding(top = 5.dp).width(20.dp).height(3.dp).clip(CircleShape).background(ThemeTeal))
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 2.dp, bottom = categoryBottomPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionIcon(Icons.Outlined.FolderOpen, "分类总览", onOpenCategoryDrawer)
            Spacer(Modifier.width(10.dp))
            Row(
                Modifier.weight(1f).horizontalScroll(categoryScrollState),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                (categories.filterNot { it == "全部分类" } + listOf("全部分类", "全部")).forEach { value ->
                    Surface(
                        Modifier.shadow(4.dp, CircleShape, ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                            .noRippleClickable { onCategory(value) },
                        shape = CircleShape,
                        color = if (category == value) ThemeTeal else Color.White,
                        shadowElevation = 0.dp,
                    ) {
                        val label = when (value) { "全部" -> "全部"; "全部分类" -> "未分类"; "我的最爱" -> "收藏"; else -> value }
                        Text(label, color = if (category == value) Color.White else Color(0xFF666666), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecentPage(hazeState: HazeState, listState: LazyListState, videos: List<VideoItem>, onVideo: (VideoItem) -> Unit, onClear: () -> Unit, onMore: (VideoItem) -> Unit) {
    val softListFlingBehavior = rememberSoftListFlingBehavior()
    val density = LocalDensity.current
    val statusBarBottomPx = WindowInsets.statusBars.getTop(density).toFloat()
    val titleSafetyGapPx = with(density) { 4.dp.toPx() }
    val recentTitleTopPx = remember { mutableFloatStateOf(0f) }
    val titleAlphaState = remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 0f
            else titleFadeAlpha(
                listState.firstVisibleItemScrollOffset.toFloat(),
                recentTitleTopPx.floatValue,
                statusBarBottomPx,
                titleSafetyGapPx
            )
        }
    }
    val recentTitleHeightPx = remember { mutableIntStateOf(0) }
    val recentOffscreenPx = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val recentTitleScrollDistancePx = remember {
        derivedStateOf {
            when (listState.firstVisibleItemIndex) {
                0 -> listState.firstVisibleItemScrollOffset.toFloat()
                1 -> (recentTitleHeightPx.intValue + listState.firstVisibleItemScrollOffset).toFloat()
                else -> recentTitleHeightPx.intValue + recentOffscreenPx
            }
        }
    }
    val recentItems = remember(videos) { videos.filter { it.lastPlayedAt > 0L }.sortedByDescending { it.lastPlayedAt } }
    val dateFormat = remember { SimpleDateFormat("M/d/yyyy", Locale.getDefault()) }
    val today = dateFormat.format(Date())
    val yesterday = dateFormat.format(Date(System.currentTimeMillis() - 86400000L))
    val groups = remember(recentItems, today, yesterday) {
        listOf(
            "今天" to recentItems.filter { dateFormat.format(Date(it.lastPlayedAt)) == today },
            "昨天" to recentItems.filter { dateFormat.format(Date(it.lastPlayedAt)) == yesterday },
            "更早" to recentItems.filter { dateFormat.format(Date(it.lastPlayedAt)) != today && dateFormat.format(Date(it.lastPlayedAt)) != yesterday }
        ).filter { it.second.isNotEmpty() }
    }
    val groupHeaderIndices = remember(groups) {
        buildList {
            var index = 1
            groups.forEach { (_, items) ->
                add(index)
                index += items.size + 3
            }
        }
    }
    val activeGroupIndex by remember(groupHeaderIndices) {
        derivedStateOf {
            groupHeaderIndices.indexOfLast { listState.firstVisibleItemIndex >= it }
        }
    }
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val recentBottomFadeEndPx = with(density) { (LocalConfiguration.current.screenHeightDp.dp - 72.dp).toPx() }
    val recentFadeLengthPx = with(density) { 48.dp.toPx() }
    val recentTopFadeStartPx = if (activeGroupIndex >= 0) headerHeightPx.toFloat() else statusBarBottomPx + with(density) { 48.dp.toPx() }
    val nextHeaderOffsetState = remember(activeGroupIndex, groupHeaderIndices) {
        derivedStateOf {
            val nextIndex = groupHeaderIndices.getOrNull(activeGroupIndex + 1)
            if (nextIndex == null) Int.MAX_VALUE
            else listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == nextIndex }?.offset ?: Int.MAX_VALUE
        }
    }
    Box(Modifier.fillMaxSize().clipToBounds()) {
        LazyColumn(state = listState, flingBehavior = softListFlingBehavior, modifier = Modifier.fillMaxSize().background(PageBg).clipToBounds(), contentPadding = PaddingValues(bottom = 112.dp)) {
            item(key = "recent-large-title") {
                Row(
                    Modifier.fillMaxWidth()
                        .onGloballyPositioned {
                            recentTitleHeightPx.intValue = it.size.height
                            if (recentTitleTopPx.floatValue == 0f && listState.firstVisibleItemIndex == 0) {
                                recentTitleTopPx.floatValue = it.positionInWindow().y + listState.firstVisibleItemScrollOffset + with(density) { 40.dp.toPx() }
                            }
                        }
                        .graphicsLayer { alpha = 0f }
                        .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("最近", Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
                    ActionIcon(FrameTrashIcon, "清空播放记录", onClear)
                }
            }
            if (recentItems.isEmpty()) {
                item { EmptyHint("暂无播放记录") }
            } else {
                groups.forEachIndexed { groupIndex, (label, list) ->
                    if (groupIndex > 0) item { Spacer(Modifier.height(7.dp)) }
                    stickyHeader {
                        RecentDateHeader(
                            label,
                            alpha = if (activeGroupIndex == groupIndex) 0f else 1f,
                            onHeightChanged = { headerHeightPx = it }
                        )
                    }
                    item { Spacer(Modifier.height(1.dp)) }
                    itemsIndexed(list, key = { _, video -> video.id }) { itemIndex, video ->
                        RecentHistoryCard(
                            video,
                            onClick = { onVideo(video) },
                            onMore = { onMore(video) },
                            isLastInGroup = itemIndex == list.lastIndex,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                    item { Spacer(Modifier.height(0.dp)) }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
        Row(
            Modifier.fillMaxWidth()
                .graphicsLayer {
                    translationY = -recentTitleScrollDistancePx.value
                    alpha = titleAlphaState.value
                }
                .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("最近", Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
            ActionIcon(FrameTrashIcon, "清空播放记录", onClear)
        }
        if (activeGroupIndex >= 0) {
            Box(
                Modifier.fillMaxWidth().offset {
                    val nextOffset = nextHeaderOffsetState.value
                    val pushOffset = if (activeGroupIndex >= 0 && nextOffset != Int.MAX_VALUE) {
                        minOf(0, nextOffset - headerHeightPx)
                    } else 0
                    androidx.compose.ui.unit.IntOffset(0, pushOffset)
                }.zIndex(2f)
            ) {
                RecentDateHeader(
                    groups[activeGroupIndex].first,
                    alpha = 1f,
                    onHeightChanged = { headerHeightPx = it }
                )
            }
        }
    }
}

@Composable
private fun RecentDateHeader(label: String, alpha: Float, onHeightChanged: (Int) -> Unit) {
    Box(
        Modifier.fillMaxWidth().graphicsLayer { this.alpha = alpha }
            .onGloballyPositioned { onHeightChanged(it.size.height) }
            .background(PageBg)
            .statusBarsPadding()
            .padding(top = 9.dp, bottom = 10.dp)
    ) {
        Text(
            label,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 5.dp)
        )
    }
}

@Composable
private fun RecentHistoryCard(video: VideoItem, onClick: () -> Unit, onMore: () -> Unit, isLastInGroup: Boolean = false, modifier: Modifier = Modifier) {
    val playedMs = (video.durationMs * video.progress.coerceIn(0f, 100f) / 100f).toLong()
    val watchedDate = SimpleDateFormat("M/d/yyyy", Locale.getDefault()).format(Date(video.lastPlayedAt))
    Row(
        modifier.fillMaxWidth().padding(bottom = if (isLastInGroup) 0.dp else 16.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(16.dp))
            .background(Color.White).noRippleClickable(onClick).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp), verticalAlignment = Alignment.Top
    ) {
        Box(Modifier.size(120.dp, 84.dp).clip(RoundedCornerShape(10.dp)).background(PosterBrush(video.bgIndex))) {
            Thumbnail(video, Modifier.fillMaxSize())
        }
        Box(Modifier.weight(1f).height(84.dp)) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {                VideoCardTitle(
                    video.name,
                    fontSize = 15.sp,
                    color = Color(0xFF222222),
                    englishFontSize = 13.sp,
                    englishColor = Color(0xFF888888),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    showEnglish = true,
                    lineHeight = 21.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("总时长${formatClock(video.durationMs)}", fontSize = 11.sp, lineHeight = 13.sp, color = Color(0xFF999999), maxLines = 1, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
                    Text("播放至${formatClock(playedMs)} · ${watchedDate}", fontSize = 11.sp, lineHeight = 13.sp, color = Color(0xFF999999), maxLines = 1, overflow = TextOverflow.Clip, softWrap = false, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
                }
            }
            Box(Modifier.align(Alignment.BottomEnd).offset(x = 4.dp).size(24.dp).noRippleClickable(onClick = onMore), contentAlignment = Alignment.BottomEnd) {
                Icon(Icons.Outlined.MoreVert, "更多", tint = Color(0xFFDDDDDD), modifier = Modifier.size(14.dp).offset(x = 4.dp))
            }
        }
    }
}
private enum class RecordActionGlyphType { Download, Add, Expand }

@Composable
private fun RecordActionGlyph(
    type: RecordActionGlyphType,
    description: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier
            .size(if (type == RecordActionGlyphType.Expand) 17.dp else 15.dp)
            .semantics { contentDescription = description }
    ) {
        val scale = size.width / 24f
        val stroke = 1.3.dp.toPx()
        fun point(x: Float, y: Float) = Offset(x * scale, y * scale)
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
            drawLine(
                color = tint,
                start = point(x1, y1),
                end = point(x2, y2),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
        when (type) {
            RecordActionGlyphType.Download -> {
                line(12f, 4.5f, 12f, 14.5f)
                line(7.5f, 10.5f, 12f, 15f)
                line(12f, 15f, 16.5f, 10.5f)
                line(5f, 17.5f, 5f, 20f)
                line(5f, 20f, 19f, 20f)
                line(19f, 20f, 19f, 17.5f)
            }
            RecordActionGlyphType.Add -> {
                line(12f, 5f, 12f, 19f)
                line(5f, 12f, 19f, 12f)
            }
            RecordActionGlyphType.Expand -> {
                line(6.5f, 9.5f, 12f, 15f)
                line(12f, 15f, 17.5f, 9.5f)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecordsPage(
    hazeState: HazeState,
    listState: LazyListState,
    videos: List<VideoItem>,
    reviews: List<ReviewEntry>,
    targetReviewId: Long?,
    targetVideoId: Long?,
    sortMode: RecordSortMode,
    sortDirection: SortDirection,
    onRecordSortModeChange: (RecordSortMode) -> Unit,
    onRecordSortDirectionChange: (SortDirection) -> Unit,
    onSearch: () -> Unit,
    onCreate: (VideoItem?) -> Unit,
    onCreateStandalone: (Long) -> Unit,
    onEdit: (ReviewEntry) -> Unit,
    onRateVideo: (VideoItem, Float) -> Unit,
    onPlayVideo: (VideoItem) -> Unit,
    onSeekToTimestamp: (VideoItem, Long) -> Unit,
    onTargetHandled: () -> Unit
) {
    val softListFlingBehavior = rememberSoftListFlingBehavior()
    var showRecordSortMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val exportScope = rememberCoroutineScope()
    var exportingReviewVideoId by remember { mutableStateOf<Long?>(null) }
    var exportingStandaloneReviewId by remember { mutableStateOf<Long?>(null) }
    val recordExpansionPreferences = remember {
        context.getSharedPreferences("frame_record_expansion", Context.MODE_PRIVATE)
    }
    val expandedVideoReviews = remember {
        mutableStateMapOf<Long, Boolean>().apply {
            recordExpansionPreferences.all.forEach { (key, value) ->
                if (key.startsWith("video_") && value is Boolean) {
                    key.removePrefix("video_").toLongOrNull()?.let { this[it] = value }
                }
            }
        }
    }
    val expandedStandaloneReviews = remember {
        mutableStateMapOf<Long, Boolean>().apply {
            recordExpansionPreferences.all.forEach { (key, value) ->
                if (key.startsWith("standalone_") && value is Boolean) {
                    key.removePrefix("standalone_").toLongOrNull()?.let { this[it] = value }
                }
            }
        }
    }
    fun setVideoReviewsExpanded(id: Long, expanded: Boolean) {
        expandedVideoReviews[id] = expanded
        recordExpansionPreferences.edit().putBoolean("video_$id", expanded).apply()
    }
    fun setStandaloneReviewExpanded(id: Long, expanded: Boolean) {
        expandedStandaloneReviews[id] = expanded
        recordExpansionPreferences.edit().putBoolean("standalone_$id", expanded).apply()
    }
    val showTopBar by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 20
        }
    }
    val density = LocalDensity.current
    val statusBarBottomPx = WindowInsets.statusBars.getTop(density).toFloat()
    val titleSafetyGapPx = with(density) { 4.dp.toPx() }
    val recordsTopFadeStartPx = statusBarBottomPx + with(density) { 48.dp.toPx() }
    val recordsBottomFadeEndPx = with(density) { (LocalConfiguration.current.screenHeightDp.dp - 72.dp).toPx() }
    val recordsFadeLengthPx = with(density) { 48.dp.toPx() }
    val recordsTitleTopPx = remember { mutableFloatStateOf(0f) }
    val recordsTitleAlphaState = remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 0f
            else titleFadeAlpha(
                listState.firstVisibleItemScrollOffset.toFloat(),
                recordsTitleTopPx.floatValue,
                statusBarBottomPx,
                titleSafetyGapPx
            )
        }
    }
    val recordsTitleHeightPx = remember { mutableIntStateOf(0) }
    val recordsOffscreenPx = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val recordsTitleScrollDistancePx = remember {
        derivedStateOf {
            when (listState.firstVisibleItemIndex) {
                0 -> listState.firstVisibleItemScrollOffset.toFloat()
                1 -> (recordsTitleHeightPx.intValue + listState.firstVisibleItemScrollOffset).toFloat()
                else -> recordsTitleHeightPx.intValue + recordsOffscreenPx
            }
        }
    }
    val recordsOverlayOffsets by remember(listState) {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.associate { it.key to it.offset.toFloat() }
        }
    }
    val completedVideos = remember(videos) {
        videos.filter { it.status == "watched" || it.progress >= 100f }
    }
    val reviewsByVideo = remember(reviews) {
        reviews.filter { it.videoId != null }.groupBy { it.videoId!! }
    }
    val sortedReviewsByVideo = remember(reviewsByVideo) {
        reviewsByVideo.mapValues { (_, entries) -> entries.sortedByDescending { it.createdAt } }
    }
    val displayVideos = remember(videos, reviewsByVideo, sortMode, sortDirection) {
        videos.filter { video ->
            val completed = video.status == "watched" || video.progress >= 100f
            val hasReviews = reviewsByVideo[video.id].orEmpty().isNotEmpty()
            completed || hasReviews
        }.sortedWith { a, b ->
            val primary = when (sortMode) {
                RecordSortMode.ADDED -> a.addedAt.compareTo(b.addedAt)
                RecordSortMode.RECORD_TIME -> {
                    val aRecordTime = reviewsByVideo[a.id].orEmpty().maxOfOrNull { it.createdAt } ?: a.lastPlayedAt
                    val bRecordTime = reviewsByVideo[b.id].orEmpty().maxOfOrNull { it.createdAt } ?: b.lastPlayedAt
                    aRecordTime.compareTo(bRecordTime)
                }
                RecordSortMode.NAME -> compareNaturalVideoNames(a.name, b.name)
                RecordSortMode.RATING -> a.rating.compareTo(b.rating)
                RecordSortMode.WATCHED -> a.lastPlayedAt.compareTo(b.lastPlayedAt)
            }
            val directed = if (sortMode == RecordSortMode.RATING && (a.rating == 0f || b.rating == 0f)) {
                when {
                    a.rating == 0f && b.rating > 0f -> 1
                    a.rating > 0f && b.rating == 0f -> -1
                    else -> 0
                }
            } else if (sortDirection == SortDirection.ASCENDING) primary else -primary
            if (directed != 0) directed else {
                val addedComparison = a.addedAt.compareTo(b.addedAt)
                if (addedComparison != 0) addedComparison else a.id.compareTo(b.id)
            }
        }
    }
    val standaloneReviews = remember(reviews, sortMode, sortDirection) {
        val roots = reviews.filter { it.videoId == null && it.standaloneGroupId == null }
        if (sortMode == RecordSortMode.RECORD_TIME) {
            roots.sortedWith { a, b ->
                val comparison = a.createdAt.compareTo(b.createdAt)
                if (sortDirection == SortDirection.ASCENDING) comparison else -comparison
            }
        } else {
            roots.sortedByDescending { it.createdAt }
        }
    }
    val hasRecords = displayVideos.isNotEmpty() || standaloneReviews.isNotEmpty()
    LaunchedEffect(targetReviewId, targetVideoId, displayVideos, standaloneReviews) {
        val videoTargetId = targetVideoId
        if (videoTargetId != null) {
            val videoIndex = displayVideos.indexOfFirst { it.id == videoTargetId }
            if (videoIndex >= 0) {
                listState.animateScrollToItem(5 + videoIndex)
                onTargetHandled()
            } else onTargetHandled()
            return@LaunchedEffect
        }
        val targetId = targetReviewId ?: return@LaunchedEffect
        val videoIndex = displayVideos.indexOfFirst { video -> reviewsByVideo[video.id].orEmpty().any { it.id == targetId } }
        if (videoIndex >= 0) {
            setVideoReviewsExpanded(displayVideos[videoIndex].id, true)
            listState.animateScrollToItem(5 + videoIndex)
        } else {
            val standaloneRootId = standaloneReviews.firstOrNull { root ->
                root.id == targetId || reviews.any { it.id == targetId && it.standaloneGroupId == root.id }
            }?.id
            val standaloneIndex = standaloneReviews.indexOfFirst { it.id == standaloneRootId }
            if (standaloneIndex >= 0) {
                setStandaloneReviewExpanded(standaloneRootId!!, true)
                listState.animateScrollToItem(5 + displayVideos.size + standaloneIndex)
            } else onTargetHandled()
        }
    }
    val watchedCount = completedVideos.size
    val totalHours = completedVideos.sumOf { it.durationMs } / 3600000
    val reviewedCount = reviews.size
    Box(Modifier.fillMaxSize().clipToBounds()) {
        AnimatedVisibility(
            visible = showTopBar,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                Modifier.fillMaxWidth()
                    .background(PageBg)
                    .statusBarsPadding()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("记录", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MainText, modifier = Modifier.graphicsLayer { alpha = 0f })
            }
        }
        LazyColumn(state = listState, flingBehavior = softListFlingBehavior, modifier = Modifier.fillMaxSize().clipToBounds(), contentPadding = PaddingValues(bottom = 88.dp)) {
            item(key = "records-large-title") {
                Row(
                    Modifier.fillMaxWidth().zIndex(1f)
                    .onGloballyPositioned {
                        recordsTitleHeightPx.intValue = it.size.height
                        if (recordsTitleTopPx.floatValue == 0f && listState.firstVisibleItemIndex == 0) {
                            recordsTitleTopPx.floatValue = it.positionInWindow().y + listState.firstVisibleItemScrollOffset + with(density) { 40.dp.toPx() }
                        }
                    }
                    .graphicsLayer { alpha = 0f }
                    .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("记录", Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ActionIcon(FrameSearchIcon, "搜索观影感想", onSearch)
                        ActionIcon(Icons.Outlined.Add, "新增感想", { onCreate(null) }, iconSize = 24.dp)
                    }
                }
            }
            item(key = "records-stat-panel") {
                Column(
                    Modifier.fillMaxWidth()
                        .graphicsLayer { alpha = 0f }
                        .background(PageBg.copy(alpha = .95f))
                ) {
                    Row(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 10.dp)
                            .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = .07f), spotColor = Color.Black.copy(alpha = .05f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Glass)
                            .padding(horizontal = 10.dp, vertical = 15.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Stat("已看完", watchedCount.toString(), "个", Modifier.weight(1f))
                        Box(Modifier.width(1.dp).height(30.dp).background(Color.Black.copy(alpha = .05f)))
                        Stat("累计时长", totalHours.toString(), "h", Modifier.weight(1f))
                        Box(Modifier.width(1.dp).height(30.dp).background(Color.Black.copy(alpha = .05f)))
                        Stat("写下感想", reviewedCount.toString(), "篇", Modifier.weight(1f))
                    }
                }
            }
            if (!hasRecords) {
                item {
                    EmptyHint("暂无观影记录，点击右上角＋新增感想")
                    Spacer(Modifier.height(20.dp))
                }
            } else {
                item(key = "records-list-heading-gap-top") { Spacer(Modifier.height(4.dp)) }
                item(key = "records-list-heading") { SectionHeading("观看记录", horizontalPadding = 20.dp) }
                item(key = "records-list-heading-gap-bottom") { Spacer(Modifier.height(4.dp)) }
                items(displayVideos, key = { "video_${it.id}" }) { video ->
                    val entries = sortedReviewsByVideo[video.id].orEmpty()
                    val completed = video.status == "watched" || video.progress >= 100f
                    val expanded = expandedVideoReviews[video.id] ?: true
                    Column(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 16.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                Modifier.size(80.dp, 106.dp)
                                    .shadow(2.dp, RoundedCornerShape(10.dp), ambientColor = Color.Black.copy(alpha = .10f), spotColor = Color.Black.copy(alpha = .10f))
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PosterBrush(video.bgIndex))
                                    .noRippleClickable { onPlayVideo(video) }
                            ) { Thumbnail(video, Modifier.fillMaxSize()) }
                            Column(
                                Modifier.weight(1f).height(106.dp)
                            ) {
                                VideoCardTitle(
                                    video.name,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = if (displayVideoName(video.name) == "未命名") MutedText else Color(0xFF222222),
                                    englishFontSize = 14.sp,
                                    englishColor = Color(0xFF888888),
                                    lineHeight = 22.sp,
                                    maxLines = 1,
                                    showEnglish = true
                                )
                                Spacer(Modifier.weight(1f))
                                RatingStars(
                                    video.rating,
                                    14.dp,
                                    onRate = { onRateVideo(video, it) },
                                    spacing = 1.dp,
                                    modifier = Modifier.graphicsLayer { translationX = -4.dp.toPx() }
                                )
                                Spacer(Modifier.height(4.dp))
                                Spacer(Modifier.weight(1f))
                                Text(
                                    if (completed) video.date + " · 已看完" else "尚未看完 · ${video.progress.toInt()}%",
                                    color = Color(0xFF999999),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                )
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFFF0F0F0)))
                        Spacer(Modifier.height(16.dp))
                        Row(
                            Modifier.fillMaxWidth().padding(end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "随记",
                                color = Color(0xFF555555),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${entries.size} 条",
                                modifier = Modifier,
                                color = Color(0xFFDDDDDD),
                                fontSize = 13.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                "导出",
                                modifier = Modifier.then(
                                    if (exportingReviewVideoId == null) Modifier.noRippleClickable {
                                        exportingReviewVideoId = video.id
                                        exportScope.launch {
                                            var bitmap: Bitmap? = null
                                            val saved = try {
                                                bitmap = withContext(Dispatchers.Default) {
                                                    createReviewExportBitmap(video, entries)
                                                }
                                                saveVideoScreenshot(context, bitmap!!)
                                            } catch (_: Exception) {
                                                false
                                            } finally {
                                                bitmap?.recycle()
                                                exportingReviewVideoId = null
                                            }
                                            Toast.makeText(
                                                context,
                                                if (saved) "观影记录图片已保存到图片/一帧" else "观影记录图片保存失败",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else Modifier
                                ),
                                color = Color(0xFFDDDDDD),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Text(" 〡 ", color = Color(0xFFDDDDDD), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "添加",
                                modifier = Modifier.noRippleClickable { onCreate(video) },
                                color = Color(0xFFDDDDDD),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Text(" 〡 ", color = Color(0xFFDDDDDD), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(
                                if (expanded) "关闭" else "展开",
                                modifier = Modifier.then(
                                    if (entries.isNotEmpty()) Modifier.noRippleClickable {
                                        setVideoReviewsExpanded(video.id, !expanded)
                                    } else Modifier
                                ),
                                color = Color(0xFFDDDDDD),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                        }
                        val timelineTopSpacing by animateDpAsState(
                            targetValue = if (entries.isNotEmpty() && expanded) 16.dp else 0.dp,
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = androidx.compose.animation.core.FastOutSlowInEasing
                            ),
                            label = "record-timeline-spacing-${video.id}"
                        )
                        Spacer(Modifier.height(timelineTopSpacing))
                        entries.forEachIndexed { index, entry ->
                            val entryBringIntoView = remember(entry.id) { BringIntoViewRequester() }
                            val entryHighlight = entry.id == targetReviewId || reviews.any { it.id == targetReviewId && it.standaloneGroupId == entry.id }
                            val expandDelay = index * 60
                            val collapseDelay = (entries.size - index - 1) * 60
                            LaunchedEffect(targetReviewId, expanded) {
                                if (entryHighlight && expanded) {
                                    delay(360 + expandDelay.toLong())
                                    entryBringIntoView.bringIntoView()
                                    delay(1500)
                                    onTargetHandled()
                                }
                            }
                            val entryTextColor by androidx.compose.animation.animateColorAsState(
                                if (entryHighlight) ThemeTeal else Color(0xFF555555),
                                animationSpec = tween(420),
                                label = "record-review-highlight-${entry.id}"
                            )
                            AnimatedVisibility(
                                visible = expanded,
                                enter = expandVertically(
                                    animationSpec = tween(300, delayMillis = expandDelay, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                                ) + fadeIn(tween(220, delayMillis = expandDelay)) +
                                    slideInVertically(
                                        initialOffsetY = { it / 8 },
                                        animationSpec = tween(300, delayMillis = expandDelay, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                                    ),
                                exit = shrinkVertically(
                                    animationSpec = tween(300, delayMillis = collapseDelay, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                                ) + fadeOut(tween(220, delayMillis = collapseDelay)) +
                                    slideOutVertically(
                                        targetOffsetY = { it / 8 },
                                        animationSpec = tween(300, delayMillis = collapseDelay, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                                    ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().drawBehind {
                                        val axisX = 7.dp.toPx()
                                        val dotCenterY = 8.dp.toPx()
                                        val lineWidth = 1.dp.toPx()
                                        val lineColor = ThemeTeal
                                        if (entries.size > 1) {
                                            when (index) {
                                                0 -> drawLine(
                                                    lineColor,
                                                    Offset(axisX, dotCenterY),
                                                    Offset(axisX, size.height),
                                                    strokeWidth = lineWidth
                                                )
                                                entries.lastIndex -> drawLine(
                                                    lineColor,
                                                    Offset(axisX, 0f),
                                                    Offset(axisX, dotCenterY),
                                                    strokeWidth = lineWidth
                                                )
                                                else -> drawLine(
                                                    lineColor,
                                                    Offset(axisX, 0f),
                                                    Offset(axisX, size.height),
                                                    strokeWidth = lineWidth
                                                )
                                            }
                                        }
                                        drawCircle(
                                            color = ThemeTeal,
                                            radius = 4.dp.toPx(),
                                            center = Offset(axisX, dotCenterY)
                                        )
                                    }
                                ) {
                                    Spacer(Modifier.width(14.dp))
                                    Column(
                                        Modifier.weight(1f).padding(start = 10.dp, top = 1.dp, bottom = 8.dp)
                                            .bringIntoViewRequester(entryBringIntoView)
                                            .noRippleClickable { onEdit(entry) }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Text(
                                                SimpleDateFormat("M/d/yyyy HH:mm", Locale.getDefault()).format(Date(entry.createdAt)),
                                                color = Color(0xFF999999),
                                                fontSize = 11.sp,
                                                lineHeight = 14.sp,
                                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                            )
                                            entry.playbackPositionMs?.let { positionMs ->
                                                Text(
                                                    "|",
                                                    color = Color(0xFF999999),
                                                    fontSize = 11.sp,
                                                    lineHeight = 14.sp,
                                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                                )
                                                Text(
                                                    formatTime(positionMs),
                                                    modifier = Modifier.noRippleClickable { onSeekToTimestamp(video, positionMs) },
                                                    color = ThemeTeal,
                                                    fontSize = 11.sp,
                                                    lineHeight = 14.sp,
                                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                                )
                                            }
                                        }
                                        Text(entry.content, color = entryTextColor, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 5.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                items(standaloneReviews, key = { "review_${it.id}" }) { entry ->
                    val entryBringIntoView = remember(entry.id) { BringIntoViewRequester() }
                    val entryHighlight = entry.id == targetReviewId
                    val expanded = expandedStandaloneReviews[entry.id] ?: true
                    LaunchedEffect(targetReviewId, expanded) {
                        if (entryHighlight && expanded) {
                            delay(360)
                            entryBringIntoView.bringIntoView()
                            delay(1500)
                            onTargetHandled()
                        }
                    }
                    val standaloneTitle = entry.title.ifBlank { "未命名" }
                    val coverBitmap = remember(entry.coverPath) {
                        entry.coverPath?.let { runCatching { BitmapFactory.decodeFile(it) }.getOrNull() }
                    }
                    val entryTextColor by androidx.compose.animation.animateColorAsState(
                        if (entryHighlight) ThemeTeal else Color(0xFF555555),
                        animationSpec = tween(420),
                        label = "standalone-review-text-highlight-${entry.id}"
                    )
                    Column(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 16.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .noRippleClickable { onEdit(entry) }
                            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                Modifier.size(80.dp, 106.dp)
                                    .shadow(2.dp, RoundedCornerShape(10.dp), ambientColor = Color.Black.copy(alpha = .10f), spotColor = Color.Black.copy(alpha = .10f))
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFECEDEF)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (coverBitmap != null) {
                                    Image(coverBitmap.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else {
                                    Icon(Icons.Outlined.Image, null, tint = Color(0xFFB5B5B5), modifier = Modifier.size(25.dp))
                                }
                            }
                            Column(Modifier.weight(1f).height(106.dp)) {
                                VideoCardTitle(
                                    standaloneTitle,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = if (entry.title.isBlank()) MutedText else Color(0xFF222222),
                                    englishFontSize = 14.sp,
                                    englishColor = Color(0xFF888888),
                                    lineHeight = 22.sp,
                                    maxLines = 2,
                                    showEnglish = true
                                )
                                Spacer(Modifier.weight(1f))
                                RatingStars(
                                    entry.rating,
                                    14.dp,
                                    onRate = {},
                                    spacing = 1.dp,
                                    modifier = Modifier.graphicsLayer { translationX = -4.dp.toPx() }
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    SimpleDateFormat("M/d/yyyy HH:mm", Locale.getDefault()).format(Date(entry.createdAt)),
                                    color = Color(0xFF999999),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                )
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFFF0F0F0)))
                        Spacer(Modifier.height(16.dp))
                        Row(
                            Modifier.fillMaxWidth().padding(end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "随记",
                                color = Color(0xFF555555),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${1 + reviews.count { it.standaloneGroupId == entry.id }} 条",
                                modifier = Modifier,
                                color = Color(0xFFDDDDDD),
                                fontSize = 13.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                "导出",
                                modifier = Modifier.then(
                                    if (exportingStandaloneReviewId == null) Modifier.noRippleClickable {
                                        exportingStandaloneReviewId = entry.id
                                        exportScope.launch {
                                            var bitmap: Bitmap? = null
                                            val timelineEntries = listOf(entry) + reviews.filter { it.standaloneGroupId == entry.id }.sortedByDescending { it.createdAt }
                                            val saved = try {
                                                bitmap = withContext(Dispatchers.Default) {
                                                    createStandaloneReviewExportBitmap(entry, timelineEntries)
                                                }
                                                saveVideoScreenshot(context, bitmap!!)
                                            } catch (_: Exception) {
                                                false
                                            } finally {
                                                bitmap?.recycle()
                                                exportingStandaloneReviewId = null
                                            }
                                            Toast.makeText(context, if (saved) "观影记录图片已保存到图片/一帧" else "观影记录图片保存失败", Toast.LENGTH_SHORT).show()
                                        }
                                    } else Modifier
                                ),
                                color = Color(0xFFDDDDDD),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Text(" 〡 ", color = Color(0xFFDDDDDD), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "添加",
                                modifier = Modifier.noRippleClickable { onCreateStandalone(entry.id) },
                                color = Color(0xFFDDDDDD),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Text(" 〡 ", color = Color(0xFFDDDDDD), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(
                                if (expanded) "关闭" else "展开",
                                modifier = Modifier.noRippleClickable {
                                    setStandaloneReviewExpanded(entry.id, !expanded)
                                },
                                color = Color(0xFFDDDDDD),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                        }
                        val timelineTopSpacing by animateDpAsState(
                            targetValue = if (expanded) 16.dp else 0.dp,
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = androidx.compose.animation.core.FastOutSlowInEasing
                            ),
                            label = "standalone-timeline-spacing-${entry.id}"
                        )
                        Spacer(Modifier.height(timelineTopSpacing))
                        AnimatedVisibility(
                            visible = expanded,
                            enter = expandVertically(
                                animationSpec = tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                            ) + fadeIn(tween(220)) +
                                slideInVertically(
                                    initialOffsetY = { it / 8 },
                                    animationSpec = tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                                ),
                            exit = shrinkVertically(
                                animationSpec = tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                            ) + fadeOut(tween(220)) +
                                slideOutVertically(
                                    targetOffsetY = { it / 8 },
                                    animationSpec = tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                                ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                (listOf(entry) + reviews.filter { it.standaloneGroupId == entry.id }.sortedByDescending { it.createdAt })
                                    .forEach { timelineEntry ->
                                        Row(
                                            Modifier.fillMaxWidth()
                                                .drawBehind {
                                                    val axisX = 7.dp.toPx()
                                                    val dotCenterY = 8.dp.toPx()
                                                    drawCircle(
                                                        color = ThemeTeal,
                                                        radius = 4.dp.toPx(),
                                                        center = Offset(axisX, dotCenterY)
                                                    )
                                                }
                                        ) {
                                            Spacer(Modifier.width(14.dp))
                                            Column(
                                                Modifier.weight(1f)
                                                    .padding(start = 10.dp, top = 1.dp, bottom = 8.dp)
                                                    .then(if (timelineEntry.id == entry.id) Modifier.bringIntoViewRequester(entryBringIntoView) else Modifier)
                                                    .noRippleClickable { onEdit(timelineEntry) }
                                            ) {
                                                Text(
                                                    SimpleDateFormat("M/d/yyyy HH:mm", Locale.getDefault()).format(Date(timelineEntry.createdAt)),
                                                    color = Color(0xFF999999),
                                                    fontSize = 11.sp,
                                                    lineHeight = 14.sp,
                                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                                )
                                                Text(
                                                    timelineEntry.content,
                                                    color = entryTextColor,
                                                    fontSize = 14.sp,
                                                    lineHeight = 21.sp,
                                                    modifier = Modifier.padding(top = 5.dp)
                                                )
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }

        Row(
            Modifier.fillMaxWidth().zIndex(1f)
                .graphicsLayer {
                    translationY = -recordsTitleScrollDistancePx.value
                    alpha = recordsTitleAlphaState.value
                }
                .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("记录", Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionIcon(FrameSearchIcon, "搜索观影感想", onSearch)
                ActionIcon(Icons.Outlined.Add, "新增感想", { onCreate(null) }, iconSize = 24.dp)
                Box {
                    ActionIcon(FrameGridIcon, "记录排序与视图", { showRecordSortMenu = true })
                    AnimatedDropdownPopup(
                        expanded = showRecordSortMenu,
                        onDismissRequest = { showRecordSortMenu = false },
                        offset = androidx.compose.ui.unit.IntOffset.Zero,
                        modifier = Modifier.width(192.dp)
                    ) {
                        CustomSortMenuItem("按添加时间", sortMode == RecordSortMode.ADDED) {
                            onRecordSortModeChange(RecordSortMode.ADDED)
                            showRecordSortMenu = false
                        }
                        CustomSortMenuItem("按记录时间", sortMode == RecordSortMode.RECORD_TIME) {
                            onRecordSortModeChange(RecordSortMode.RECORD_TIME)
                            showRecordSortMenu = false
                        }
                        CustomSortMenuItem("按名称", sortMode == RecordSortMode.NAME) {
                            onRecordSortModeChange(RecordSortMode.NAME)
                            showRecordSortMenu = false
                        }
                        CustomSortMenuItem("按看完时间", sortMode == RecordSortMode.WATCHED) {
                            onRecordSortModeChange(RecordSortMode.WATCHED)
                            showRecordSortMenu = false
                        }
                        CustomSortMenuItem("按评分", sortMode == RecordSortMode.RATING) {
                            onRecordSortModeChange(RecordSortMode.RATING)
                            showRecordSortMenu = false
                        }
                        HorizontalDivider(
                            color = Color(0xFFF0F0F0),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        CustomSortMenuItem("正序", sortDirection == SortDirection.ASCENDING) {
                            onRecordSortDirectionChange(SortDirection.ASCENDING)
                            showRecordSortMenu = false
                        }
                        CustomSortMenuItem("倒序", sortDirection == SortDirection.DESCENDING) {
                            onRecordSortDirectionChange(SortDirection.DESCENDING)
                            showRecordSortMenu = false
                        }
                    }
                }
            }
        }
        val recordsStatsOffset = recordsOverlayOffsets["records-stat-panel"]
        if (recordsStatsOffset != null) {
            Column(
                Modifier.fillMaxWidth()
                    .graphicsLayer { translationY = recordsStatsOffset }
                    .background(PageBg.copy(alpha = .95f))
            ) {
                Row(
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 10.dp)
                        .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = .07f), spotColor = Color.Black.copy(alpha = .05f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Glass)
                        .padding(horizontal = 10.dp, vertical = 15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Stat("已看完", watchedCount.toString(), "个", Modifier.weight(1f))
                    Box(Modifier.width(1.dp).height(30.dp).background(Color.Black.copy(alpha = .05f)))
                    Stat("累计时长", totalHours.toString(), "h", Modifier.weight(1f))
                    Box(Modifier.width(1.dp).height(30.dp).background(Color.Black.copy(alpha = .05f)))
                    Stat("写下感想", reviewedCount.toString(), "篇", Modifier.weight(1f))
                }
            }
        }

        val showStickyTitle = showTopBar && recordsTitleAlphaState.value <= 0f
        val stickyTitleVisibility = remember { MutableTransitionState(false) }
        stickyTitleVisibility.targetState = showStickyTitle
        Box(
            Modifier.align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    if (stickyTitleVisibility.currentState || stickyTitleVisibility.targetState) PageBg
                    else Color.Transparent
                )
                .statusBarsPadding()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visibleState = stickyTitleVisibility,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {
                Text("记录", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MainText)
            }
        }
    }
}
@Composable
private fun NativePlayerOverlay(
    item: VideoItem, player: Player, repository: NativeLibraryRepository, speeds: List<Float>, speedIndex: Int, locked: Boolean, controlsVisible: Boolean,
    onClose: () -> Unit, onToggleControls: () -> Unit, onAutoHideControls: () -> Unit, onToggleLock: () -> Unit,
    onSpeed: () -> Unit, onOrientation: () -> Unit, onEnded: () -> Unit,
    onProgress: (Long, Long) -> Unit,
    onWatchTime: (Long) -> Unit,
    onSaveTimestampRecord: (Long, String) -> Unit
) {
    val context = LocalContext.current
    val screenshotScope = rememberCoroutineScope()
    var videoTextureView by remember(item.id) { mutableStateOf<TextureView?>(null) }
    var pendingScreenshot by remember(item.id) { mutableStateOf<Bitmap?>(null) }

    fun saveCapturedScreenshot(bitmap: Bitmap) {
        screenshotScope.launch {
            val saved = runCatching { saveVideoScreenshot(context, bitmap) }.getOrDefault(false)
            bitmap.recycle()
            Toast.makeText(context, if (saved) "截图已保存到图片/一帧" else "截图保存失败", Toast.LENGTH_SHORT).show()
        }
    }

    val screenshotPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val bitmap = pendingScreenshot
        pendingScreenshot = null
        if (granted && bitmap != null) {
            saveCapturedScreenshot(bitmap)
        } else {
            bitmap?.recycle()
            Toast.makeText(context, "需要存储权限才能保存截图", Toast.LENGTH_SHORT).show()
        }
    }

    fun captureVideoFrame() {
        val texture = videoTextureView
        if (texture == null || !texture.isAvailable) {
            Toast.makeText(context, "当前视频画面暂不可截图", Toast.LENGTH_SHORT).show()
            return
        }
        val bitmap = runCatching { texture.getBitmap() }.getOrNull()
        if (bitmap == null) {
            Toast.makeText(context, "获取视频画面失败，请稍后重试", Toast.LENGTH_SHORT).show()
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingScreenshot?.recycle()
            pendingScreenshot = bitmap
            screenshotPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            saveCapturedScreenshot(bitmap)
        }
    }

    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var playbackEnded by remember(item.id) { mutableStateOf(player.playbackState == Player.STATE_ENDED) }
    var current by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(item.durationMs) }
    var isSeeking by remember { mutableStateOf(false) }
    var brightnessMask by remember { mutableFloatStateOf(0f) }
    var volume by remember { mutableFloatStateOf(player.volume) }
    var unlockVisible by remember { mutableStateOf(false) }
    var centerMessage by remember { mutableStateOf("") }
    var dragMode by remember { mutableStateOf("none") }
    var dragStartX by remember { mutableFloatStateOf(0f) }
    var dragStartY by remember { mutableFloatStateOf(0f) }
    var dragStartPosition by remember { mutableLongStateOf(0L) }
    var dragStartVolume by remember { mutableFloatStateOf(player.volume) }
    var dragStartBrightness by remember { mutableFloatStateOf(0f) }
    var timestampNoteSheetVisible by remember(item.id) { mutableStateOf(false) }
    var timestampNoteText by remember(item.id) { mutableStateOf("") }
    var timestampPositionMs by remember(item.id) { mutableLongStateOf(0L) }
    var subtitleSheetVisible by remember(item.id) { mutableStateOf(false) }
    val subtitleTextSizePreferences = remember {
        context.getSharedPreferences("frame_preferences", Context.MODE_PRIVATE)
    }
    var subtitleTextSizePercent by remember(item.id) {
        mutableIntStateOf(subtitleTextSizePreferences.getInt("subtitle_text_size_percent", 100).coerceIn(50, 100))
    }
    val playerViewReference = remember(item.id) { arrayOfNulls<PlayerView>(1) }
    fun applySubtitleTextSize(view: PlayerView?) {
        view?.subtitleView?.setFractionalTextSize(0.0533f * subtitleTextSizePercent / 100f)
    }
    var subtitleTracks by remember(item.id) { mutableStateOf<List<SubtitleTrackOption>>(emptyList()) }
    var subtitlePreference by remember(item.id) { mutableStateOf(repository.subtitlePreference(item.id)) }
    var hasSubtitleFolderAccess by remember(item.id) { mutableStateOf(repository.hasVideoFolderSource(item.id)) }
    val subtitlePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val name = context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
                ?: uri.lastPathSegment?.substringAfterLast('/') ?: "外部字幕"
            if (name.substringAfterLast('.', "").lowercase() in setOf("srt", "ass", "ssa", "vtt")) {
                val option = SubtitleTrackOption(uri, name, true)
                subtitleTracks = (subtitleTracks.filterNot { it.uri == uri } + option)
                repository.saveSubtitleTrack(item.id, SubtitleTrackEntry(uri.toString(), name, true))
                val updated = SubtitlePreference(uri.toString(), true)
                subtitlePreference = updated
                repository.saveSubtitlePreference(item.id, updated)
                val wasPlaying = player.isPlaying
                val position = player.currentPosition.coerceAtLeast(0L)
                val mediaItem = MediaItem.Builder()
                    .setMediaId(item.id.toString())
                    .setUri(item.contentUri)
                    .setMediaMetadata(player.currentMediaItem?.mediaMetadata ?: MediaMetadata.EMPTY)
                    .setSubtitleConfigurations(listOf(subtitleConfiguration(uri, name)))
                    .build()
                player.setMediaItem(mediaItem, position)
                player.prepare()
                if (wasPlaying) player.play() else player.pause()
            } else {
                Toast.makeText(context, "请选择 SRT、ASS、SSA 或 VTT 字幕文件", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val timestampNoteFocusRequester = remember(item.id) { FocusRequester() }

    LaunchedEffect(item.id) {
        subtitlePreference = repository.subtitlePreference(item.id)
        val automatic = repository.findSiblingSubtitles(item.id, item.name).map { SubtitleTrackOption(it.first, it.second) }
        val manual = repository.manualSubtitleTracks(item.id).mapNotNull { entry ->
            runCatching { SubtitleTrackOption(Uri.parse(entry.uri), entry.name, true) }.getOrNull()
        }
        val selected = subtitlePreference.selectedUri?.let { saved ->
            (automatic + manual).firstOrNull { it.uri.toString() == saved } ?: runCatching {
                val uri = Uri.parse(saved)
                SubtitleTrackOption(uri, uri.lastPathSegment?.substringAfterLast('/') ?: "已挂载字幕", true)
            }.getOrNull()
        }
        subtitleTracks = (automatic + manual + listOfNotNull(selected)).distinctBy { it.uri }
    }

    fun selectSubtitle(option: SubtitleTrackOption?) {
        if (option != null) {
            val readable = runCatching {
                context.contentResolver.openFileDescriptor(option.uri, "r")?.use { true } ?: false
            }.getOrDefault(false)
            if (!readable) {
                Toast.makeText(context, "无法读取字幕文件，请重新选择", Toast.LENGTH_SHORT).show()
                return
            }
        }
        val updated = if (option == null) subtitlePreference.copy(enabled = false)
            else SubtitlePreference(option.uri.toString(), true)
        subtitlePreference = updated
        repository.saveSubtitlePreference(item.id, updated)
        val wasPlaying = player.isPlaying
        val position = player.currentPosition.coerceAtLeast(0L)
        val builder = MediaItem.Builder()
            .setMediaId(item.id.toString())
            .setUri(item.contentUri)
            .setMediaMetadata(player.currentMediaItem?.mediaMetadata ?: MediaMetadata.EMPTY)
        if (updated.enabled && option != null) {
            builder.setSubtitleConfigurations(listOf(subtitleConfiguration(option.uri, option.name)))
        }
        player.setMediaItem(builder.build(), position)
        player.prepare()
        if (wasPlaying) player.play() else player.pause()
    }

    val subtitleFolderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { treeUri ->
        if (treeUri != null) {
            val registered = repository.registerVideoFolderSource(item.id, item.contentUri, treeUri)
            hasSubtitleFolderAccess = registered
            if (registered) {
                val automatic = repository.findSiblingSubtitles(item.id, item.name).map { SubtitleTrackOption(it.first, it.second) }
                val savedManual = repository.manualSubtitleTracks(item.id).map { SubtitleTrackOption(Uri.parse(it.uri), it.name, true) }
                subtitleTracks = (automatic + savedManual).distinctBy { it.uri }
                if (subtitlePreference.selectedUri == null && subtitlePreference.enabled) {
                    automatic.firstOrNull()?.let { selectSubtitle(it) }
                }
                if (automatic.isEmpty()) Toast.makeText(context, "文件夹中没有找到同名字幕", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "未在所选文件夹中找到当前视频", Toast.LENGTH_SHORT).show()
            }
        }
    }

    BackHandler(enabled = timestampNoteSheetVisible || subtitleSheetVisible) {
        if (timestampNoteSheetVisible) timestampNoteSheetVisible = false else subtitleSheetVisible = false
    }
    LaunchedEffect(timestampNoteSheetVisible) {
        if (timestampNoteSheetVisible) {
            delay(120)
            timestampNoteFocusRequester.requestFocus()
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(value: Boolean) {
                isPlaying = value
                Log.d("FramePlayback", "isPlaying=$value playWhenReady=${player.playWhenReady}")
            }
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                Log.d("FramePlayback", "playWhenReady=$playWhenReady reason=$reason")
            }
            override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
                Log.d("FramePlayback", "suppressionReason=$playbackSuppressionReason")
            }
            override fun onPlaybackStateChanged(state: Int) {
                playbackEnded = state == Player.STATE_ENDED
                Log.d("FramePlayback", "state=$state position=${player.currentPosition} duration=${player.duration}")
                if (state == Player.STATE_ENDED) onEnded()
            }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e("FramePlayback", "Playback error code=${error.errorCodeName}: ${error.message}", error)
                centerMessage = "播放失败"
            }
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    current = newPosition.positionMs.coerceAtLeast(0L)
                    isSeeking = false
                }
                Log.d("FramePlayback", "positionDiscontinuity reason=$reason from=${oldPosition.positionMs} to=${newPosition.positionMs}")
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                Log.d("FramePlayback", "mediaItemTransition reason=$reason item=${mediaItem?.mediaId}")
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }
    LaunchedEffect(player) {
        var lastPersistedAt = 0L
        var previousTick = SystemClock.elapsedRealtime()
        var wasPlaying = player.isPlaying
        while (true) {
            if (!isSeeking) current = player.currentPosition.coerceAtLeast(0L)
            if (player.duration > 0L) duration = player.duration
            val now = System.currentTimeMillis()
            val elapsedNow = SystemClock.elapsedRealtime()
            val elapsed = (elapsedNow - previousTick).coerceAtLeast(0L)
            val playingNow = player.isPlaying
            if (playingNow && wasPlaying) onWatchTime(elapsed)
            wasPlaying = playingNow
            previousTick = elapsedNow
            if (!isSeeking && now - lastPersistedAt >= 5000L && player.playbackState != Player.STATE_ENDED) {
                onProgress(current, duration)
                lastPersistedAt = now
            }
            delay(500)
        }
    }
    LaunchedEffect(centerMessage) {
        if (centerMessage.isNotEmpty()) {
            delay(800)
            centerMessage = ""
        }
    }
    LaunchedEffect(speedIndex) { player.setPlaybackParameters(PlaybackParameters(speeds[speedIndex])) }
    LaunchedEffect(controlsVisible, isPlaying, locked, subtitleSheetVisible, timestampNoteSheetVisible) {
        if (controlsVisible && isPlaying && !locked && !subtitleSheetVisible && !timestampNoteSheetVisible) {
            delay(3000)
            onAutoHideControls()
        }
    }

    Box(
        Modifier.fillMaxSize().background(Color.Black)
            .pointerInput(locked, controlsVisible, speedIndex, duration) {
                detectTapGestures(
                    onTap = {
                        if (locked) {
                            unlockVisible = !unlockVisible
                        } else if (!controlsVisible) {
                            onToggleControls()
                        } else {
                            if (player.isPlaying) player.pause() else player.play()
                        }
                    },
                    onDoubleTap = { offset ->
                        if (!locked) {
                            val forward = offset.x > size.width / 2f
                            val target = (player.currentPosition + if (forward) 10_000L else -10_000L)
                                .coerceIn(0L, duration.coerceAtLeast(0L))
                            player.seekTo(target)
                            current = target
                            centerMessage = if (forward) "快进 10s" else "快退 10s"
                        }
                    },
                    onLongPress = {
                        if (!locked) {
                            player.setPlaybackParameters(PlaybackParameters(2f))
                            centerMessage = "2.0x 播放中"
                        }
                    },
                    onPress = {
                        tryAwaitRelease()
                        if (!locked) player.setPlaybackParameters(PlaybackParameters(speeds[speedIndex]))
                    }
                )
            }
            .pointerInput(locked, duration) {
                detectDragGestures(
                    onDragStart = { start ->
                        if (!locked) {
                            dragStartX = start.x
                            dragStartY = start.y
                            dragStartPosition = player.currentPosition.coerceAtLeast(0L)
                            dragStartVolume = player.volume
                            dragStartBrightness = brightnessMask
                            dragMode = when {
                                start.y > size.height * .78f -> "none"
                                start.x < size.width / 3f -> "brightness"
                                start.x > size.width * 2f / 3f -> "volume"
                                else -> "progress"
                            }
                            if (dragMode == "progress") isSeeking = true
                        }
                    },
                    onDragEnd = {
                        if (dragMode == "progress") player.seekTo(current)
                        dragMode = "none"
                    },
                    onDragCancel = {
                        if (dragMode == "progress") isSeeking = false
                        dragMode = "none"
                    },
                    onDrag = { change, _ ->
                        if (!locked && dragMode != "none") {
                            change.consume()
                            val dx = change.position.x - dragStartX
                            val dy = change.position.y - dragStartY
                            when {
                                dragMode == "brightness" && abs(dy) > abs(dx) -> {
                                    brightnessMask = (dragStartBrightness - dy / 200f).coerceIn(0f, .8f)
                                    centerMessage = "亮度 ${((1f - brightnessMask) * 100).toInt()}%"
                                }
                                dragMode == "volume" && abs(dy) > abs(dx) -> {
                                    volume = (dragStartVolume - dy / 200f).coerceIn(0f, 1f)
                                    player.volume = volume
                                    centerMessage = "音量 ${(volume * 100).toInt()}%"
                                }
                                dragMode == "progress" && abs(dx) > abs(dy) -> {
                                    val changeMs = (dx / (size.width * .8f) * duration.coerceAtLeast(0L)).toLong()
                                    current = (dragStartPosition + changeMs).coerceIn(0L, duration.coerceAtLeast(0L))
                                    centerMessage = "${formatTime(current)} / ${formatTime(duration)}"
                                }
                            }
                        }
                    }
                )
            }
    ) {
        AndroidView(
            factory = { ctx ->
                (android.view.LayoutInflater.from(ctx)
                    .inflate(R.layout.player_view, null, false) as PlayerView).apply {
                    this.player = player
                    useController = false
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    layoutParams = ViewGroup.LayoutParams(-1, -1)
                    playerViewReference[0] = this
                    applySubtitleTextSize(this)
                    videoTextureView = findVideoTextureView(this)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = {
                it.player = player
                playerViewReference[0] = it
                applySubtitleTextSize(it)
                videoTextureView = findVideoTextureView(it)
            }
        )
        if (brightnessMask > 0f) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = brightnessMask)))
        }
        AnimatedVisibility(
            visible = controlsVisible && !locked,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(
                    Modifier.fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .8f), Color.Transparent)))
                        .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    PlayerIconButton(Icons.Outlined.ArrowBack, "返回", onClose)
                    Text(displayVideoName(item.name), color = Color.White, fontSize = 16.sp,
                        fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    PlayerIconButton(Icons.Outlined.ScreenRotation, "旋转屏幕", onOrientation)
                    PlayerIconButton(Icons.Outlined.Lock, "锁定屏幕", onToggleLock)
                }
                Column(
                    Modifier.fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .8f))))
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 30.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                        Text(formatTime(current), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        PlayerProgressBar(
                            progress = if (duration > 0L) (current.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f,
                            modifier = Modifier.weight(1f),
                            onSeekStart = { isSeeking = true },
                            onSeek = { fraction ->
                                current = (duration * fraction).toLong().coerceIn(0L, duration.coerceAtLeast(0L))
                            },
                            onSeekEnd = { fraction ->
                                current = (duration * fraction).toLong().coerceIn(0L, duration.coerceAtLeast(0L))
                                player.seekTo(current)
                            }
                        )
                        Text(formatClock(duration), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        PlayerIconButton(
                            icon = when {
                                playbackEnded || player.playbackState == Player.STATE_ENDED -> Icons.Outlined.Replay
                                isPlaying -> Icons.Outlined.Pause
                                else -> Icons.Outlined.PlayArrow
                            },
                            description = when {
                                playbackEnded || player.playbackState == Player.STATE_ENDED -> "重播"
                                isPlaying -> "暂停"
                                else -> "播放"
                            }
                        ) {
                            if (playbackEnded || player.playbackState == Player.STATE_ENDED) {
                                player.seekTo(0L)
                                current = 0L
                                playbackEnded = false
                                player.play()
                            } else if (player.isPlaying) player.pause() else player.play()
                        }
                        Spacer(Modifier.weight(1f))
                        Box(
                            Modifier.clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = .2f))
                                .noRippleClickable { onSpeed() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("${speeds[speedIndex]}x", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.width(15.dp))
                        PlayerIconButton(Icons.Outlined.ClosedCaption, "字幕") {
                            subtitleSheetVisible = true
                        }
                        Spacer(Modifier.width(15.dp))
                        PlayerIconButton(Icons.Outlined.AddComment, "记录此刻") {
                            timestampPositionMs = player.currentPosition.coerceAtLeast(0L)
                            player.pause()
                            timestampNoteText = ""
                            timestampNoteSheetVisible = true
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = controlsVisible && !locked,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp),
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            PlayerIconButton(Icons.Outlined.PhotoCamera, "截图", ::captureVideoFrame)
        }
        AnimatedVisibility(
            visible = locked && unlockVisible,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                Modifier.fillMaxSize().zIndex(9f), contentAlignment = Alignment.Center
            ) {
                Box(Modifier.size(60.dp).clip(CircleShape).background(Color.Black.copy(alpha = .5f))
                    .border(1.dp, Color.White.copy(alpha = .2f), CircleShape)
                    .noRippleClickable { unlockVisible = false; onToggleLock() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.LockOpen, contentDescription = "解锁", tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
        AnimatedVisibility(
            visible = centerMessage.isNotEmpty() && !locked && !timestampNoteSheetVisible && !subtitleSheetVisible,
            modifier = Modifier.align(Alignment.Center).zIndex(10f),
            enter = fadeIn(tween(180)), exit = fadeOut(tween(180))
        ) {
            Box(Modifier.clip(CircleShape).background(Color.Black.copy(alpha = .68f)).padding(horizontal = 22.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
                Text(centerMessage, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        if (subtitleSheetVisible) {
            Box(Modifier.fillMaxSize().zIndex(19f)) {
                Box(
                    Modifier.fillMaxSize()
                        .background(Color.Black.copy(alpha = .32f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { subtitleSheetVisible = false }
                )
                Column(
                    Modifier.align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * .75f)
                        .shadow(10.dp, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), ambientColor = Color.Black.copy(alpha = .10f), spotColor = Color.Black.copy(alpha = .10f))
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(PageBg)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 20.dp, top = 26.dp, end = 20.dp, bottom = 50.dp)
                ) {
                    SheetHeading("字幕")
                    Row(
                        Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("字幕大小", color = MainText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.weight(1f))
                        Text("${subtitleTextSizePercent}%", color = ThemeTeal, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Slider(
                        value = subtitleTextSizePercent.toFloat(),
                        onValueChange = { value ->
                            val percent = value.roundToInt().coerceIn(50, 100)
                            subtitleTextSizePercent = percent
                            subtitleTextSizePreferences.edit().putInt("subtitle_text_size_percent", percent).apply()
                            applySubtitleTextSize(playerViewReference[0])
                        },
                        valueRange = 50f..100f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.32f),
                            activeTickColor = Color.White,
                            inactiveTickColor = Color.White.copy(alpha = 0.32f)
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    if (subtitleTracks.isEmpty()) {
                        Text("还没有找到字幕，可以从文件中选择，或授权视频文件夹进行自动匹配。", color = MutedText, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(bottom = 16.dp))
                    }
                    SheetAction(if (subtitlePreference.enabled) "关闭字幕" else "字幕已关闭", bottomPadding = 12.dp) {
                        selectSubtitle(null)
                        subtitleSheetVisible = false
                    }
                    subtitleTracks.forEach { option ->
                        val selected = subtitlePreference.enabled && subtitlePreference.selectedUri == option.uri.toString()
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = 12.dp).height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .noRippleClickable {
                                    selectSubtitle(option)
                                    subtitleSheetVisible = false
                                }
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                option.name,
                                modifier = Modifier.weight(1f),
                                color = MainText,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (selected) Icon(Icons.Outlined.Check, contentDescription = "当前字幕", tint = ThemeTeal)
                        }
                    }
                    SheetAction("从文件中选择字幕") {
                        subtitlePicker.launch(arrayOf("*/*"))
                    }
                    if (!hasSubtitleFolderAccess) {
                        SheetAction("授权视频文件夹以自动匹配", bottomPadding = 0.dp) {
                            subtitleFolderPicker.launch(null)
                        }
                    }
                }
            }
        }
        if (timestampNoteSheetVisible) {
            Box(Modifier.fillMaxSize().zIndex(20f)) {
                Box(
                    Modifier.fillMaxSize()
                        .background(Color.Black.copy(alpha = .32f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { timestampNoteSheetVisible = false }
                )
                Column(
                    Modifier.align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .imePadding()
                        .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * .85f)
                        .shadow(10.dp, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), ambientColor = Color.Black.copy(alpha = .10f), spotColor = Color.Black.copy(alpha = .10f))
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(PageBg)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 20.dp, top = 26.dp, end = 20.dp, bottom = 30.dp)
                ) {
                    SheetHeading("记录此刻")
                    BasicTextField(
                        value = timestampNoteText,
                        onValueChange = { timestampNoteText = it },
                        modifier = Modifier.fillMaxWidth()
                            .heightIn(min = 110.dp)
                            .focusRequester(timestampNoteFocusRequester)
                            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(15.dp),
                        textStyle = TextStyle(fontSize = 16.sp, color = MainText, lineHeight = 24.sp),
                        decorationBox = { inner ->
                            Box {
                                if (timestampNoteText.isEmpty()) {
                                    Text("写下此刻的想法……", color = MutedText, fontSize = 16.sp)
                                }
                                inner()
                            }
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    SheetActionPair(
                        confirmOnRight = true,
                        confirmTheme = true,
                        spacing = 12.dp,
                        onConfirm = {
                            val content = timestampNoteText.trim()
                            if (content.isEmpty()) {
                                Toast.makeText(context, "请输入记录内容", Toast.LENGTH_SHORT).show()
                            } else {
                                onSaveTimestampRecord(timestampPositionMs, content)
                                timestampNoteSheetVisible = false
                                timestampNoteText = ""
                            }
                        },
                        onCancel = {
                            timestampNoteSheetVisible = false
                            timestampNoteText = ""
                        }
                    )
                }
            }
        }
    }
}

private fun findVideoTextureView(view: android.view.View): TextureView? {
    if (view is TextureView) return view
    if (view is ViewGroup) {
        for (index in 0 until view.childCount) {
            findVideoTextureView(view.getChildAt(index))?.let { return it }
        }
    }
    return null
}

private fun wrapReviewExportText(
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

private val ChineseEnglishTitleRegex = Regex(
    "^(.+?)\\s+([\\p{IsLatin}\\p{M}\\p{N}\\[（(「『“‘][\\p{IsLatin}\\p{M}\\p{N}\\s&＆'’.,:;!?()\\[\\]{}+_\\-/]*)$"
)

private fun splitChineseEnglishTitle(title: String): Pair<String, String?> {
    val cleanTitle = title.trim()
    val match = ChineseEnglishTitleRegex.matchEntire(cleanTitle)
    return if (match != null) {
        match.groupValues[1].trim() to match.groupValues[2].trim().takeIf { it.isNotEmpty() }
    } else {
        cleanTitle to null
    }
}

private fun splitReviewExportTitle(title: String): Pair<String, String?> =
    splitChineseEnglishTitle(title)

private fun createReviewExportBitmap(
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

private fun createStandaloneReviewExportBitmap(
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

private fun createUnifiedReviewExportBitmap(
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

private suspend fun saveVideoScreenshot(context: android.content.Context, bitmap: Bitmap): Boolean =
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
private fun PlayerIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(CircleShape)
            .background(Color.White.copy(alpha = .2f))
            .noRippleClickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun PlayerProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    onSeekStart: () -> Unit,
    onSeek: (Float) -> Unit,
    onSeekEnd: (Float) -> Unit
) {
    var dragFraction by remember { mutableFloatStateOf(progress) }
    BoxWithConstraints(
        modifier.height(30.dp).pointerInput(Unit) {
            detectTapGestures {
                val fraction = (it.x / size.width).coerceIn(0f, 1f)
                onSeek(fraction)
                onSeekEnd(fraction)
            }
        }.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    onSeekStart()
                    dragFraction = (it.x / size.width).coerceIn(0f, 1f)
                    onSeek(dragFraction)
                },
                onDragEnd = { onSeekEnd(dragFraction) },
                onDragCancel = { onSeekEnd(dragFraction) },
                onDrag = { change, _ ->
                    change.consume()
                    dragFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                    onSeek(dragFraction)
                }
            )
        },
        contentAlignment = Alignment.CenterStart
    ) {
        val fraction = progress.coerceIn(0f, 1f)
        Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = .3f)))
        Box(Modifier.fillMaxWidth(fraction).height(4.dp).clip(RoundedCornerShape(2.dp)).background(ThemeTeal))
        Box(
            Modifier.align(Alignment.CenterStart).offset(x = maxWidth * fraction - 6.dp)
                .size(12.dp).clip(CircleShape).background(Color.White)
        )
    }
}

private fun formatTime(ms: Long): String {
    val seconds = (ms / 1000).coerceAtLeast(0)
    return String.format(
        Locale.getDefault(), "%02d:%02d:%02d",
        seconds / 3600, (seconds % 3600) / 60, seconds % 60
    )
}

private val ChineseNameCollator: Collator by lazy {
    Collator.getInstance(Locale.CHINA).apply { strength = Collator.PRIMARY }
}

private fun compareNaturalVideoNames(left: String, right: String): Int {
    val leftName = displayVideoName(left)
    val rightName = displayVideoName(right)
    val tokenRegex = Regex("\\d+|\\D+")
    val leftTokens = tokenRegex.findAll(leftName).map { it.value }.toList()
    val rightTokens = tokenRegex.findAll(rightName).map { it.value }.toList()
    for (index in 0 until minOf(leftTokens.size, rightTokens.size)) {
        val a = leftTokens[index]
        val b = rightTokens[index]
        val aNumeric = a.all { it.isDigit() }
        val bNumeric = b.all { it.isDigit() }
        val comparison = when {
            aNumeric && bNumeric -> {
                val numberComparison = BigInteger(a).compareTo(BigInteger(b))
                if (numberComparison != 0) numberComparison else a.length.compareTo(b.length)
            }
            aNumeric != bNumeric -> if (aNumeric) -1 else 1
            else -> ChineseNameCollator.compare(a, b)
        }
        if (comparison != 0) return comparison
    }
    return leftTokens.size.compareTo(rightTokens.size)
}

private fun displayVideoName(name: String): String {
    val displayName = name.replace(
        Regex("\\.(mp4|m4v|mkv|avi|mov|wmv|flv|webm|3gp|3g2|ts|mts|m2ts|mpg|mpeg|mpe|mpv|ogv|ogg|asf|rm|rmvb|vob|divx|f4v|mxf|nsv|wtv|dvr-ms|amv|hevc|h264|h265)$", RegexOption.IGNORE_CASE), ""
    )
    return displayName.ifBlank { "未命名" }
}

private fun splitDisplayVideoTitle(title: String): Pair<String, String?> {
    val cleanTitle = displayVideoName(title).trim()
    return splitChineseEnglishTitle(cleanTitle)
}

@Composable
private fun VideoCardTitle(
    name: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    color: Color,
    englishFontSize: androidx.compose.ui.unit.TextUnit = fontSize,
    englishColor: Color = color,
    fontWeight: FontWeight,
    englishFontWeight: FontWeight = fontWeight,
    maxLines: Int,
    showEnglish: Boolean,
    modifier: Modifier = Modifier,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    textAlign: TextAlign = TextAlign.Start,
    combineEnglish: Boolean = false
) {
    val (primaryTitle, englishTitle) = splitDisplayVideoTitle(name)
    Column(modifier, horizontalAlignment = if (textAlign == TextAlign.Center) Alignment.CenterHorizontally else Alignment.Start) {
        if (combineEnglish) {
            Text(
                listOfNotNull(primaryTitle, englishTitle?.takeIf { it.isNotBlank() }).joinToString(" "),
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight,
                lineHeight = lineHeight,
                textAlign = textAlign,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = FontFamily.SansSerif, platformStyle = PlatformTextStyle(includeFontPadding = false))
            )
        } else {
            Text(
                primaryTitle,
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight,
                lineHeight = lineHeight,
                textAlign = textAlign,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = FontFamily.SansSerif, platformStyle = PlatformTextStyle(includeFontPadding = false))
            )
        }
        if (!combineEnglish && showEnglish && !englishTitle.isNullOrBlank()) {
            Text(
                englishTitle,
                color = englishColor,
                fontSize = englishFontSize,
                fontWeight = englishFontWeight,
                lineHeight = lineHeight,
                textAlign = textAlign,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = FontFamily.Serif, platformStyle = PlatformTextStyle(includeFontPadding = false))
            )
        }
    }
}

@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun Modifier.noRippleCombinedClickable(onClick: () -> Unit, onLongClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.combinedClickable(interactionSource = interactionSource, indication = null, onClick = onClick, onLongClick = onLongClick)
}

private fun formatClock(ms: Long): String {
    if (ms <= 0L) return "--:--:--"
    val seconds = ms / 1000
    return String.format(
        Locale.getDefault(), "%02d:%02d:%02d",
        seconds / 3600, (seconds % 3600) / 60, seconds % 60
    )
}


@Composable
private fun ActionIcon(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
    iconModifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier.size(36.dp)
            .shadow(2.dp, CircleShape, ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
            .clip(CircleShape)
            .background(Color.White)
            .noRippleClickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, iconModifier.size(iconSize), tint = Color(0xFF333333))
    }
}

private fun titleFadeAlpha(
    scrollOffsetPx: Float,
    titleTopAtRestPx: Float,
    statusBarBottomPx: Float,
    safetyGapPx: Float
): Float {
    val fadeDistancePx = (titleTopAtRestPx - statusBarBottomPx - safetyGapPx).coerceAtLeast(1f)
    return (1f - scrollOffsetPx / fadeDistancePx).coerceIn(0f, 1f)
}

@Composable
private fun Header(title: String, onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText, letterSpacing = .5.sp)
        Box(Modifier.size(36.dp).shadow(2.dp, CircleShape, ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(CircleShape).background(Color.White).noRippleClickable { onSettings() }, contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Settings, "设置", Modifier.size(22.dp), tint = MainText)
        }
    }
}

@Composable
private fun SectionHeading(
    text: String,
    expanded: Boolean? = null,
    onToggle: (() -> Unit)? = null,
    chevronExpandDurationMs: Int = 300,
    chevronCollapseDurationMs: Int = 250,
    chevronExpandEasing: androidx.compose.animation.core.Easing = androidx.compose.animation.core.CubicBezierEasing(.34f, 1.12f, .64f, 1f),
    chevronCollapseEasing: androidx.compose.animation.core.Easing = androidx.compose.animation.core.CubicBezierEasing(.4f, 0f, 1f, 1f),
    horizontalPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val rotation by animateFloatAsState(
        if (expanded == false) -90f else 0f,
        tween(durationMillis = if (expanded == false) chevronCollapseDurationMs else chevronExpandDurationMs, easing = if (expanded == false) chevronCollapseEasing else chevronExpandEasing),
        label = "section-chevron"
    )
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val titleScale by animateFloatAsState(
        if (pressed) .98f else 1f,
        spring(dampingRatio = .72f, stiffness = 650f),
        label = "section-title-press"
    )
    Row(
        Modifier.fillMaxWidth().padding(horizontal = horizontalPadding).padding(top = 10.dp, bottom = 12.dp)
            .then(if (onToggle != null) Modifier.clickable(interactionSource = interactionSource, indication = null) { onToggle() } else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(4.dp).height(16.dp).clip(CircleShape).background(ThemeTeal))
        Text(
            text, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = MainText,
            modifier = Modifier.padding(start = 8.dp).weight(1f).graphicsLayer { scaleX = titleScale; scaleY = titleScale }
        )
        if (expanded != null) Icon(Icons.Outlined.ExpandMore, null, tint = MutedText, modifier = Modifier.rotate(rotation).size(20.dp))
    }
}

@Composable
private fun Stat(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = MutedText)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value, fontSize = 22.sp, lineHeight = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111111),
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
            )
            Text(
                unit, fontSize = 12.sp, lineHeight = 12.sp, color = MutedText, modifier = Modifier.padding(start = 3.dp, bottom = 2.dp),
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
            )
        }
    }
}

@Composable
private fun VideoRow(video: VideoItem, subtitle: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 16.dp).shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(16.dp)).background(Color.White).noRippleClickable(onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(100.dp, 65.dp).clip(RoundedCornerShape(10.dp)).background(PosterBrush(video.bgIndex)), contentAlignment = Alignment.Center) {
            Thumbnail(video, Modifier.fillMaxSize())
            if (video.progress > 0f) Box(Modifier.align(Alignment.BottomStart).fillMaxWidth((video.progress / 100f).coerceIn(0f, 1f)).height(3.dp).background(ThemeTeal))
        }
        Column(Modifier.weight(1f).padding(start = 15.dp)) {
            VideoCardTitle(video.name, fontSize = 15.sp, color = Color(0xFF222222), englishFontSize = 13.sp, englishColor = Color(0xFF888888), fontWeight = FontWeight.Medium, maxLines = 1, showEnglish = true)
            Text(subtitle, fontSize = 12.sp, color = MutedText, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PosterCard(
    video: VideoItem, compact: Boolean, onPlay: (VideoItem) -> Unit, onLong: (Rect) -> Unit,
    modifier: Modifier = Modifier, highlighted: Boolean = false, alpha: Float = 1f,
    onBoundsChanged: ((Rect) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val cardBounds = remember { arrayOf(Rect.Zero) }
    Column(
        modifier.graphicsLayer { this.alpha = alpha }
            .onGloballyPositioned {
                cardBounds[0] = it.boundsInRoot()
                onBoundsChanged?.invoke(cardBounds[0])
            }
            .noRippleCombinedClickable(onClick = { onPlay(video) }, onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onLong(cardBounds[0]) })
    ) {
        PosterCardVisual(video, compact, highlighted)
    }
}

@Composable
private fun PosterCardVisual(video: VideoItem, compact: Boolean, highlighted: Boolean = false) {
    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f / 1.4f).shadow(if (highlighted) 6.dp else 4.dp, RoundedCornerShape(if (compact) 12.dp else 16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(if (compact) 12.dp else 16.dp)).background(PosterBrush(video.bgIndex))) {
            Thumbnail(video, Modifier.fillMaxSize())
            if (video.progress > 0f) {
                Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().height(3.dp).background(Color.White.copy(alpha = .22f)))
                Box(Modifier.align(Alignment.BottomStart).fillMaxWidth((video.progress / 100f).coerceIn(0f, 1f)).height(3.dp).background(ThemeTeal))
            }
            if (video.isFav) Text("♥", color = Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(7.dp))
        }
        VideoCardTitle(
            video.name,
            fontSize = 13.sp,
            color = Color(0xFF222222),
            fontWeight = FontWeight.Medium,
            maxLines = if (compact) 2 else 1,
            showEnglish = !compact,
            combineEnglish = compact,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun Thumbnail(video: VideoItem, modifier: Modifier = Modifier) {
    val bitmap = produceState<ImageBitmap?>(
        initialValue = video.thumbPath?.let { thumbnailBitmapCache.get(it)?.asImageBitmap() },
        key1 = video.thumbPath
    ) {
        val path = video.thumbPath
        value = if (path.isNullOrBlank()) {
            null
        } else {
            val decoded = withContext(Dispatchers.IO) {
                thumbnailBitmapCache.get(path) ?: runCatching {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(path, bounds)
                    val maxDimension = maxOf(bounds.outWidth, bounds.outHeight)
                    val sampleSize = generateSequence(1) { it * 2 }
                        .takeWhile { maxDimension / it > 600 }
                        .lastOrNull() ?: 1
                    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                    BitmapFactory.decodeFile(path, options)?.also { thumbnailBitmapCache.put(path, it) }
                }.getOrNull()
            }
            decoded?.asImageBitmap()
        }
    }.value
    if (bitmap != null) {
        Image(
            bitmap,
            contentDescription = displayVideoName(video.name),
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(modifier)
    }
}

private fun PosterBrush(index: Int): Brush {
    val colors = when (((index - 1).coerceAtLeast(0)) % 4) {
        0 -> listOf(Color(0xFF667EEA), Color(0xFF764BA2))
        1 -> listOf(Color(0xFFF093FB), Color(0xFFF5576C))
        2 -> listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))
        else -> listOf(Color(0xFF43E97B), Color(0xFF38F9D7))
    }
    return Brush.linearGradient(colors)
}

@Composable
private fun EmptyHint(
    text: String,
    horizontalPadding: androidx.compose.ui.unit.Dp = 20.dp,
    topPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    Text(text, color = MutedText, fontSize = 14.sp, modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding).padding(top = topPadding, bottom = 20.dp))
}

@Composable
private fun FabAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(shape = CircleShape, color = ThemeTeal, shadowElevation = 4.dp, modifier = Modifier.size(48.dp).noRippleClickable(onClick)) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, label, tint = Color.White) }
    }
}

@Composable
private fun ContextMenuAction(icon: ImageVector, label: String, danger: Boolean = false, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(42.dp).noRippleClickable(onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = if (danger) Color(0xFFFF3B30) else Color(0xFF666666), modifier = Modifier.size(18.dp))
        Text(label, color = if (danger) Color(0xFFFF3B30) else MainText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RatingStars(
    rating: Float,
    size: androidx.compose.ui.unit.Dp,
    onRate: ((Float) -> Unit)? = null,
    spacing: androidx.compose.ui.unit.Dp = 2.dp,
    modifier: Modifier = Modifier
) {
    val normalizedRating = rating.coerceIn(0f, 5f)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(spacing), verticalAlignment = Alignment.CenterVertically) {
        (1..5).forEach { value ->
            val fullStar = normalizedRating >= value.toFloat()
            val halfStar = !fullStar && normalizedRating >= value - 0.5f
            Box(
                Modifier.size(size + 8.dp).then(
                    if (onRate != null) Modifier.pointerInput(onRate, value) {
                        detectTapGestures(
                            onTap = { onRate(value.toFloat()) },
                            onDoubleTap = { onRate(value - 0.5f) }
                        )
                    } else Modifier
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (fullStar) FrameStarFilledIcon else FrameStarOutlineIcon,
                    contentDescription = "评分 ${value - if (halfStar) 0.5f else 0f} 星",
                    tint = ThemeTeal,
                    modifier = Modifier.size(size)
                )
                if (halfStar) {
                    Box(
                        Modifier.width(size / 2f).height(size).clipToBounds(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Icon(
                            FrameStarFilledIcon,
                            contentDescription = null,
                            tint = ThemeTeal,
                            modifier = Modifier.size(size)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun AnimatedDropdownPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: androidx.compose.ui.unit.IntOffset = androidx.compose.ui.unit.IntOffset.Zero,
    alignment: Alignment = Alignment.TopEnd,
    content: @Composable ColumnScope.() -> Unit
) {
    val visibilityState = remember { MutableTransitionState(false) }
    visibilityState.targetState = expanded
    val cornerRadius by animateDpAsState(
        targetValue = if (expanded) 16.dp else 180.dp,
        animationSpec = tween(280),
        label = "sortMenuCornerRadius"
    )
    val menuShape = RoundedCornerShape(cornerRadius)

    if (visibilityState.currentState || visibilityState.targetState) {
        Popup(
            alignment = alignment,
            offset = offset,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true)
        ) {
            AnimatedVisibility(
                visibleState = visibilityState,
                enter = fadeIn(animationSpec = tween(280)) +
                    scaleIn(
                        initialScale = 0.10f,
                        transformOrigin = TransformOrigin(0.5f, 0.5f),
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
                    ),
                exit = fadeOut(animationSpec = tween(180)) +
                    scaleOut(
                        targetScale = 0.10f,
                        transformOrigin = TransformOrigin(0.5f, 0.5f),
                        animationSpec = tween(180)
                    )
            ) {
                Column(
                    modifier = modifier
                        .shadow(6.dp, menuShape, ambientColor = Color.Black.copy(alpha = .06f), spotColor = Color.Black.copy(alpha = .05f))
                        .clip(menuShape)
                        .background(Color.White)
                        .border(0.7.dp, Color(0xFFEEEEEE), menuShape),
                    content = content
                )
            }
        }
    }
}

@Composable
private fun CustomSortMenuItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().height(42.dp)
            .noRippleClickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text,
            color = if (isSelected) ThemeTeal else MainText,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
        if (isSelected) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = ThemeTeal,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Spacer(Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SortOptionRow(
    title: String,
    isSelected: Boolean,
    direction: SortDirection,
    onAscending: () -> Unit,
    onDescending: () -> Unit
) {
    val ascendingSelected = isSelected && direction == SortDirection.ASCENDING
    val indicatorOffset by animateDpAsState(
        targetValue = if (ascendingSelected) 0.dp else 50.dp,
        animationSpec = tween(
            300,
            easing = androidx.compose.animation.core.CubicBezierEasing(.25f, 1f, .5f, 1f)
        ),
        label = "sort-direction-indicator"
    )
    val indicatorColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) ThemeTeal else Color.Transparent,
        animationSpec = tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "sort-direction-indicator-color"
    )
    val ascendingText by androidx.compose.animation.animateColorAsState(
        targetValue = if (ascendingSelected) Color.White else Color(0xFFAAAAAA),
        animationSpec = tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "sort-ascending-text"
    )
    val descendingText by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected && direction == SortDirection.DESCENDING) Color.White else Color(0xFFAAAAAA),
        animationSpec = tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "sort-descending-text"
    )
    Row(
        Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            Modifier.weight(1f),
            color = if (isSelected) MainText else MutedText,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
        Box(
            Modifier.width(96.dp).height(28.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                Modifier.offset(x = indicatorOffset)
                    .size(width = 46.dp, height = 28.dp)
                    .clip(RoundedCornerShape(90.dp))
                    .background(indicatorColor)
            )
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(width = 46.dp, height = 28.dp).noRippleClickable { onAscending() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("正序", color = ascendingText, fontSize = 13.sp, fontWeight = if (ascendingSelected) FontWeight.Bold else FontWeight.Normal)
                }
                Spacer(Modifier.width(4.dp))
                Box(
                    Modifier.size(width = 46.dp, height = 28.dp).noRippleClickable { onDescending() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("倒序", color = descendingText, fontSize = 13.sp, fontWeight = if (isSelected && direction == SortDirection.DESCENDING) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}
@Composable
private fun SheetContent(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        SheetHeading(title); content()
    }
}

@Composable
private fun SheetHeading(title: String) {
    Text(
        title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MainText,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 0.dp, bottom = 26.dp)
    )
}

@Composable
private fun SheetAction(
    text: String,
    danger: Boolean = false,
    bottomPadding: androidx.compose.ui.unit.Dp = 12.dp,
    onClick: () -> Unit
) {
    Surface(
        Modifier.fillMaxWidth().padding(bottom = bottomPadding).height(52.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
            .clip(RoundedCornerShape(16.dp)).noRippleClickable(onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text,
                color = if (danger) Color(0xFFFF3B30) else MainText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SheetActionPair(
    confirmText: String = "确认",
    cancelText: String = "取消",
    confirmDanger: Boolean = false,
    confirmOnRight: Boolean = false,
    confirmTheme: Boolean = false,
    spacing: androidx.compose.ui.unit.Dp = 20.dp,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
        if (confirmOnRight) {
            SheetActionButton(cancelText, MainText, Color.White, onCancel, Modifier.weight(1f))
            SheetActionButton(
                confirmText,
                if (confirmTheme) Color.White else if (confirmDanger) Color(0xFFFF3B30) else MainText,
                if (confirmTheme) ThemeTeal else Color.White,
                onConfirm,
                Modifier.weight(1f)
            )
        } else {
            SheetActionButton(
                confirmText,
                if (confirmTheme) Color.White else if (confirmDanger) Color(0xFFFF3B30) else MainText,
                if (confirmTheme) ThemeTeal else Color.White,
                onConfirm,
                Modifier.weight(1f)
            )
            SheetActionButton(cancelText, MainText, Color.White, onCancel, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SheetActionButton(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier.height(52.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
            .clip(RoundedCornerShape(16.dp)).noRippleClickable(onClick),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        shadowElevation = 0.dp
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MainText, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Box(Modifier.size(44.dp, 24.dp).clip(CircleShape).background(if (checked) ThemeTeal else Color(0xFFE0E0E0)).noRippleClickable { onChecked(!checked) }) {
            val knobOffset by animateDpAsState(if (checked) 22.dp else 2.dp, tween(300), label = "setting-switch")
            Box(Modifier.align(Alignment.CenterStart).offset(x = knobOffset).size(20.dp).clip(CircleShape).background(Color.White))
        }
    }
}