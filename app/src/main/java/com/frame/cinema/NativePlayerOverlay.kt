package com.frame.cinema

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.provider.Settings
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



@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NativePlayerOverlay(
    item: VideoItem, player: Player, repository: NativeLibraryRepository, speeds: List<Float>, speedIndex: Int, locked: Boolean, controlsVisible: Boolean,
    onClose: () -> Unit, onToggleControls: () -> Unit, onAutoHideControls: () -> Unit, onToggleLock: () -> Unit,
    onSpeed: () -> Unit, onOrientation: () -> Unit, onEnded: () -> Unit,
    onWatchTime: (Long) -> Unit,
    onFlushWatchTime: () -> Unit,
    onSaveTimestampRecord: (Long, String) -> Unit
) {
    val context = LocalContext.current
    val playerActivity = remember(context) {
        var currentContext: Context? = context
        while (currentContext != null && currentContext !is Activity) {
            currentContext = (currentContext as? ContextWrapper)?.baseContext
        }
        currentContext as? Activity
    }
    val screenshotScope = rememberCoroutineScope()
    val playerWindowInsetsController = remember(playerActivity) {
        playerActivity?.window?.let { WindowInsetsControllerCompat(it, it.decorView) }
    }
    val previousLightStatusBarAppearance = remember(playerWindowInsetsController) {
        playerWindowInsetsController?.isAppearanceLightStatusBars
    }
    LaunchedEffect(controlsVisible, locked) {
        if (controlsVisible && !locked) {
            playerWindowInsetsController?.isAppearanceLightStatusBars = false
        }
    }
    DisposableEffect(playerWindowInsetsController) {
        onDispose {
            previousLightStatusBarAppearance?.let {
                playerWindowInsetsController?.isAppearanceLightStatusBars = it
            }
        }
    }
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
    val initialBrightness = remember(playerActivity) {
        val windowBrightness = playerActivity?.window?.attributes?.screenBrightness ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        if (windowBrightness >= 0f) {
            windowBrightness.coerceIn(0f, 1f)
        } else {
            (Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128) / 255f).coerceIn(0f, 1f)
        }
    }
    var brightnessLevel by remember(playerActivity) { mutableFloatStateOf(initialBrightness) }
    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
    }
    var volume by remember(audioManager) {
        val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        mutableFloatStateOf(audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC).toFloat() / maxVolume)
    }
    var unlockVisible by remember { mutableStateOf(false) }
    var centerMessage by remember { mutableStateOf("") }
    var centerMessageRevision by remember { mutableIntStateOf(0) }
    fun showCenterMessage(message: String) {
        centerMessage = message
        centerMessageRevision++
    }
    var isLevelAdjusting by remember { mutableStateOf(false) }
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

    DisposableEffect(playerActivity) {
        val window = playerActivity?.window
        val originalBrightness = window?.attributes?.screenBrightness
        onDispose {
            if (window != null && originalBrightness != null) {
                window.attributes = window.attributes.apply {
                    screenBrightness = originalBrightness
                }
            }
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
                showCenterMessage("播放失败")
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
    LaunchedEffect(player, item.id) {
        var lastPersistedAt = 0L
        var previousTick = SystemClock.elapsedRealtime()
        var wasPlaying = player.isPlaying
        try {
            while (true) {
                if (!isSeeking) current = player.currentPosition.coerceAtLeast(0L)
                if (player.duration > 0L) duration = player.duration
                val now = System.currentTimeMillis()
                val elapsedNow = SystemClock.elapsedRealtime()
                val elapsed = (elapsedNow - previousTick).coerceAtLeast(0L)
                val playingNow = player.isPlaying
                if (playingNow && wasPlaying) onWatchTime(elapsed)
                if (wasPlaying && !playingNow) onFlushWatchTime()
                wasPlaying = playingNow
                previousTick = elapsedNow
                delay(500)
            }
        } finally {
            onFlushWatchTime()
        }
    }
    LaunchedEffect(centerMessageRevision, isLevelAdjusting) {
        if (centerMessage.isNotEmpty() && !isLevelAdjusting) {
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
                        } else {
                            onToggleControls()
                        }
                    },
                    onDoubleTap = {
                        if (!locked) {
                            if (playbackEnded || player.playbackState == Player.STATE_ENDED) {
                                player.seekTo(0L)
                                current = 0L
                                playbackEnded = false
                                player.play()
                            } else if (player.isPlaying) {
                                player.pause()
                            } else {
                                player.play()
                            }
                        }
                    },
                    onLongPress = {
                        if (!locked) {
                            player.setPlaybackParameters(PlaybackParameters(2f))
                            showCenterMessage("2.0x 播放中")
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
                            val maxMusicVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC).coerceAtLeast(1)
                            dragStartVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC).toFloat() / maxMusicVolume
                            dragStartBrightness = brightnessLevel
                            dragMode = when {
                                start.y > size.height * .78f -> "none"
                                start.x < size.width / 3f -> "brightness"
                                start.x > size.width * 2f / 3f -> "volume"
                                else -> "progress"
                            }
                            if (dragMode == "progress") isSeeking = true
                            isLevelAdjusting = dragMode == "brightness" || dragMode == "volume"
                        }
                    },
                    onDragEnd = {
                        if (dragMode == "progress") {
                            player.seekTo(current)
                            isSeeking = false
                        }
                        isLevelAdjusting = false
                        dragMode = "none"
                    },
                    onDragCancel = {
                        if (dragMode == "progress") isSeeking = false
                        isLevelAdjusting = false
                        dragMode = "none"
                    },
                    onDrag = { change, _ ->
                        if (!locked && dragMode != "none") {
                            change.consume()
                            val dx = change.position.x - dragStartX
                            val dy = change.position.y - dragStartY
                            when {
                                dragMode == "brightness" && abs(dy) > abs(dx) -> {
                                    brightnessLevel = (dragStartBrightness - dy / 200f).coerceIn(0f, 1f)
                                    playerActivity?.window?.let { window ->
                                        window.attributes = window.attributes.apply {
                                            screenBrightness = brightnessLevel
                                        }
                                    }
                                    showCenterMessage("亮度 ${(brightnessLevel * 100).toInt()}%")
                                }
                                dragMode == "volume" && abs(dy) > abs(dx) -> {
                                    volume = (dragStartVolume - dy / 200f).coerceIn(0f, 1f)
                                    val maxMusicVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC).coerceAtLeast(1)
                                    val targetMusicVolume = (volume * maxMusicVolume).toInt().coerceIn(0, maxMusicVolume)
                                    audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, targetMusicVolume, 0)
                                    volume = targetMusicVolume.toFloat() / maxMusicVolume
                                    showCenterMessage("音量 ${(volume * 100).toInt()}%")
                                }
                                dragMode == "progress" && abs(dx) > abs(dy) -> {
                                    val changeMs = (dx / (size.width * .8f) * duration.coerceAtLeast(0L) * .05f).toLong()
                                    current = (dragStartPosition + changeMs).coerceIn(0L, duration.coerceAtLeast(0L))
                                    showCenterMessage("${formatTime(current)} / ${formatTime(duration)}")
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
                    PlayerIconButton(Icons.Outlined.ScreenRotation, "旋转屏幕", 18, onOrientation)
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
                                isSeeking = false
                            }
                        )
                        Text(formatClock(duration), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                            PlayerIconButton(Icons.Outlined.FastRewind, "快退 10 秒") {
                                val target = (player.currentPosition - 10_000L).coerceAtLeast(0L)
                                player.seekTo(target)
                                current = target
                                playbackEnded = false
                                showCenterMessage("快退 10s")
                            }
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
                            PlayerIconButton(Icons.Outlined.FastForward, "快进 10 秒") {
                                val target = (player.currentPosition + 10_000L).coerceAtMost(duration.coerceAtLeast(0L))
                                player.seekTo(target)
                                current = target
                                playbackEnded = false
                                showCenterMessage("快进 10s")
                            }
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
                        PlayerIconButton(Icons.Outlined.ClosedCaption, "字幕", 22) {
                            subtitleSheetVisible = true
                        }
                        Spacer(Modifier.width(15.dp))
                        PlayerIconButton(Icons.Outlined.AddComment, "记录此刻", 18) {
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
                Box(Modifier.size(40.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = .2f))
                    .noRippleClickable { unlockVisible = false; onToggleLock() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.LockOpen, contentDescription = "解锁", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
        AnimatedVisibility(
            visible = centerMessage.isNotEmpty() && !locked && !timestampNoteSheetVisible && !subtitleSheetVisible,
            modifier = Modifier.align(Alignment.Center).zIndex(10f),
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)) +
                slideOutVertically(
                    targetOffsetY = { it / 12 },
                    animationSpec = tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                )
        ) {
            Box(
                Modifier.clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = .2f))
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
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
                        Text("${subtitleTextSizePercent}%", color = ActiveAccent, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
                        if (subtitlePreference.enabled) selectSubtitle(null)
                        subtitleSheetVisible = false
                    }
                    subtitleTracks.forEach { option ->
                        val selected = subtitlePreference.enabled && subtitlePreference.selectedUri == option.uri.toString()
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = 12.dp).height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
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
                            if (selected) Icon(Icons.Outlined.Check, contentDescription = "当前字幕", tint = ActiveAccent)
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
            val timestampNoteLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
            val timestampNoteShape = if (timestampNoteLandscape) RoundedCornerShape(24.dp) else RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
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
                    Modifier.align(if (timestampNoteLandscape) Alignment.CenterStart else Alignment.BottomCenter)
                        .then(
                            if (timestampNoteLandscape) {
                                Modifier.padding(
                                    start = 16.dp,
                                    top = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding()
                                )
                                    .width(LocalConfiguration.current.screenWidthDp.dp * 0.5f - 16.dp)
                            } else {
                                Modifier.fillMaxWidth()
                            }
                        )
                        .imePadding()
                        .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * if (timestampNoteLandscape) .72f else .85f)
                        .shadow(10.dp, timestampNoteShape, ambientColor = Color.Black.copy(alpha = .10f), spotColor = Color.Black.copy(alpha = .10f))
                        .clip(timestampNoteShape)
                        .background(PageBg)
                        .verticalScroll(rememberScrollState())
                        .padding(
                            start = 20.dp,
                            top = if (timestampNoteLandscape) 18.dp else 26.dp,
                            end = 20.dp,
                            bottom = if (timestampNoteLandscape) 18.dp else 30.dp
                        )
                ) {
                    SheetHeading("记录此刻")
                    BasicTextField(
                        value = timestampNoteText,
                        onValueChange = { timestampNoteText = it },
                        modifier = Modifier.fillMaxWidth()
                            .heightIn(min = if (timestampNoteLandscape) 72.dp else 110.dp)
                            .focusRequester(timestampNoteFocusRequester)
                            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
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
                    Spacer(Modifier.height(if (timestampNoteLandscape) 8.dp else 12.dp))
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

