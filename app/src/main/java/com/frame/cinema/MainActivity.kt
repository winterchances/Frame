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


internal enum class MainPage(val label: String) { HOME("首页"), LIBRARY("片库"), RECENT("最近"), RECORDS("记录") }
private enum class SheetType { NONE, IMPORT, SETTINGS, SEARCH, RECORD_SEARCH, RENAME, MOVE, NOTE, REVIEW_CREATE_OPTIONS, REVIEW_VIDEO_PICKER, CLEAR_RECENT, CLEAR_DATA, DELETE_RECENT, DELETE_MOVIE_REVIEW, DELETE_CATEGORY, NEW_CATEGORY }
private enum class MovieReviewScreen { LIST, DETAIL, EDITOR }
internal enum class SortMode { ADDED, NAME }
internal enum class SortDirection { ASCENDING, DESCENDING }
internal enum class RecordSortMode { ADDED, RECORD_TIME, NAME, WATCHED, RATING }

class MainActivity : ComponentActivity() {
    companion object {
        const val ACTION_OPEN_PLAYER = "com.frame.cinema.OPEN_PLAYER"
    }

    private var player: Player? = null
    private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null
    private var fallbackPlayer: ExoPlayer? = null
    private lateinit var repository: NativeLibraryRepository
    private var backgroundPlaybackAllowed = true
    private var appearanceMode by mutableStateOf("system")
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
        appearanceMode = when (getSharedPreferences("frame_preferences", Context.MODE_PRIVATE)
            .getString("appearance_mode", "system")) {
            "dark" -> "dark"
            "light" -> "light"
            else -> "system"
        }
        repository = NativeLibraryRepository(this)
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val future = MediaController.Builder(this, token).buildAsync()
        controllerFuture = future
        future.addListener({
            runOnUiThread {
                if (isFinishing || isDestroyed || controllerFuture !== future) return@runOnUiThread

                try {
                    val controller = future.get()
                    if (isFinishing || isDestroyed || controllerFuture !== future) return@runOnUiThread
                    installContent(controller)
                } catch (error: Exception) {
                    Log.e("Frame", "MediaController connection failed", error)
                    if (isFinishing || isDestroyed || controllerFuture !== future) return@runOnUiThread

                    val fallback = runCatching { ExoPlayer.Builder(this).build() }
                        .onFailure { Log.e("Frame", "Fallback player creation failed", it) }
                        .getOrNull() ?: return@runOnUiThread
                    fallbackPlayer = fallback
                    installContent(fallback)
                }
            }
        }, java.util.concurrent.Executor { command -> command.run() })
    }

    private fun installContent(controller: Player) {
        player = controller
        setContent {
                        val systemDark = isSystemInDarkTheme()
                        val useDarkTheme = when (appearanceMode) {
                            "dark" -> true
                            "light" -> false
                            else -> systemDark
                        }
                        val palette = if (useDarkTheme) FrameThemeColors.Dark else FrameThemeColors.Light
                        LaunchedEffect(useDarkTheme) {
                            window.statusBarColor = if (useDarkTheme) android.graphics.Color.rgb(23, 27, 27) else android.graphics.Color.WHITE
                            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = !useDarkTheme
                        }
                        CompositionLocalProvider(LocalFramePalette provides palette) {
                            MaterialTheme(
                                colorScheme = if (useDarkTheme) {
                                    darkColorScheme(
                                        background = palette.pageBackground,
                                        surface = palette.cardBackground,
                                        primary = palette.accent,
                                        onBackground = palette.primaryText,
                                        onSurface = palette.primaryText
                                    )
                                } else {
                                    lightColorScheme(
                                        background = palette.pageBackground,
                                        surface = palette.cardBackground,
                                        primary = palette.accent,
                                        onBackground = palette.primaryText,
                                        onSurface = palette.primaryText
                                    )
                                }
                            ) {
                            FrameNativeApp(
                                repository = repository,
                                player = controller,
                                openPlayerRequest = openPlayerRequest,
                                appearanceMode = appearanceMode,
                                onAppearanceModeChange = { mode ->
                                    appearanceMode = mode
                                    getSharedPreferences("frame_preferences", Context.MODE_PRIVATE)
                                        .edit().putString("appearance_mode", mode).apply()
                                },
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
                                    val factor = 1f - 0.28f * progress.coerceIn(0f, 1f)
                                    window.statusBarColor = if (useDarkTheme) {
                                        android.graphics.Color.rgb(
                                            (23f * factor).toInt(),
                                            (27f * factor).toInt(),
                                            (27f * factor).toInt()
                                        )
                                    } else {
                                        val channel = (255f * factor).toInt().coerceIn(0, 255)
                                        android.graphics.Color.rgb(channel, channel, channel)
                                    }
                                }
                            )
                            }
                        }
                    
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == ACTION_OPEN_PLAYER) {
            openPlayerRequest.intValue++
            intent.action = null
        }
        setIntent(intent)
    }

    private fun startNativePlayback(requestedItem: VideoItem, positionMs: Long? = null) {
        val controller = player ?: return
        // Page state can lag behind SQLite after playback progress is saved.
        // Always resolve the latest persisted video before choosing the start position.
        val item = repository.backupVideos().firstOrNull { it.id == requestedItem.id } ?: requestedItem
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
        val startPositionMs = when {
            positionMs != null -> positionMs.coerceAtLeast(0L)
            item.playbackCompleted -> 0L
            item.playbackPositionMs != null -> item.playbackPositionMs.coerceAtLeast(0L)
            item.durationMs > 0L && item.progress > 0f ->
                (item.durationMs * item.progress / 100f).toLong().coerceAtLeast(0L)
            else -> 0L
        }
        // Set the media item and its start position together so the initial prepare
        // cannot briefly expose position 0 and overwrite the saved position.
        controller.setMediaItem(mediaItem, startPositionMs)
        controller.prepare()
        controller.play()
    }

    override fun onStop() {
        val activePlayer = player
        val mediaId = activePlayer?.currentMediaItem?.mediaId
        val videoId = mediaId?.toLongOrNull()
        if (videoId != null &&
            (activePlayer.playbackState == Player.STATE_READY ||
                activePlayer.playbackState == Player.STATE_ENDED) &&
            (activePlayer.currentPosition > 0L || activePlayer.playbackState == Player.STATE_ENDED)
        ) {
            repository.updatePlayback(
                videoId,
                activePlayer.currentPosition.coerceAtLeast(0L),
                activePlayer.duration.coerceAtLeast(0L),
                activePlayer.playbackState == Player.STATE_ENDED
            )
        }
        if (!backgroundPlaybackAllowed) activePlayer?.pause()
        super.onStop()
    }

    override fun onDestroy() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        player = null
        fallbackPlayer?.release()
        fallbackPlayer = null
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
    appearanceMode: String,
    onAppearanceModeChange: (String) -> Unit,
    onPlay: (VideoItem) -> Unit,
    onPlayAt: (VideoItem, Long) -> Unit,
    onBackgroundPlaybackChanged: (Boolean) -> Unit,
    onOrientation: (Boolean) -> Unit,
    onKeepAwake: (Boolean) -> Unit,
    onSystemBarsVisible: (Boolean) -> Unit,
    onCategoryDrawerOverlay: (Float) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val openPlayerRequestValue by openPlayerRequest
    val appVersion = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }
    val hazeState = remember { HazeState() }
    val dropdownScrimTargets = remember { mutableStateMapOf<Any, Boolean>() }
    val dropdownScrimReporter: (Any, Boolean) -> Unit = remember {
        { key, visible -> dropdownScrimTargets[key] = visible }
    }
    val dropdownScrimVisible = dropdownScrimTargets.values.any { it }
    val dropdownScrimAlpha by animateFloatAsState(
        targetValue = if (dropdownScrimVisible) 0.06f else 0f,
        animationSpec = tween(
            durationMillis = if (dropdownScrimVisible) 320 else 280,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "dropdown-background-scrim"
    )
    val homeListState = rememberLazyListState()
    val libraryListState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    val libraryStickyCategoryScrollState = rememberScrollState()
    val libraryFloatingCategoryScrollState = rememberScrollState()
    val recentListState = rememberLazyListState()
    val recordsListState = rememberLazyListState()
    var page by rememberSaveable { mutableStateOf(MainPage.HOME) }
    var libraryMultiSelectMode by remember { mutableStateOf(false) }
    val librarySelectedVideoIds = remember { mutableStateOf<Set<Long>>(emptySet()) }
    LaunchedEffect(page) {
        if (page != MainPage.LIBRARY) librarySelectedVideoIds.value = emptySet()
    }
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
    var movieReviewEntries by remember { mutableStateOf(repository.movieReviews()) }
    var movieReviewScreen by rememberSaveable { mutableStateOf<MovieReviewScreen?>(null) }
    var movieReviewVideoId by rememberSaveable { mutableStateOf<Long?>(null) }
    var movieReviewStandaloneId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedMovieReviewId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editingMovieReviewId by rememberSaveable { mutableStateOf<Long?>(null) }
    var movieReviewEditorSession by rememberSaveable { mutableIntStateOf(0) }
    var movieReviewEditorReturnToList by rememberSaveable { mutableStateOf(true) }
    var movieReviewDraftTitle by rememberSaveable { mutableStateOf("") }
    var movieReviewDraftContent by rememberSaveable { mutableStateOf("") }
    var weeklyWatchTimeMs by remember { mutableLongStateOf(repository.weeklyWatchTimeMs()) }
    var categories by remember { mutableStateOf(repository.categories().let { saved ->
        val ordered = listOf("我的最爱") + saved.filterNot { it == "全部" || it == "全部分类" || it == "我的最爱" }.distinct() + "全部分类"
        if (ordered != saved) repository.saveCategories(ordered)
        ordered
    }) }
    var status by rememberSaveable { mutableStateOf("全部") }
    var category by rememberSaveable { mutableStateOf("全部") }
    var compact by rememberSaveable { mutableStateOf(true) }
    var showFab by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var recordSearch by remember { mutableStateOf("") }
    var reviewVideoSearch by remember { mutableStateOf("") }
    var targetReviewId by remember { mutableStateOf<Long?>(null) }
    var targetRecordVideoId by remember { mutableStateOf<Long?>(null) }
    var recordSortMode by rememberSaveable { mutableStateOf(RecordSortMode.WATCHED) }
    var recordSortDirection by rememberSaveable { mutableStateOf(SortDirection.DESCENDING) }
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
    var sort by rememberSaveable { mutableStateOf(SortMode.ADDED) }
    var sortDirection by rememberSaveable { mutableStateOf(SortDirection.DESCENDING) }
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
        movieReviewEntries = repository.movieReviews()
        categories = repository.categories()
        weeklyWatchTimeMs = repository.weeklyWatchTimeMs()
    }
    fun openMovieReviewList(video: VideoItem) {
        showFab = false
        movieReviewVideoId = video.id
        movieReviewStandaloneId = null
        val existing = movieReviewEntries
            .filter { it.videoId == video.id }
            .maxWithOrNull(compareBy<MovieReviewEntry> { it.createdAt }.thenBy { it.id })
        selectedMovieReviewId = existing?.id
        editingMovieReviewId = null
        movieReviewDraftTitle = ""
        movieReviewDraftContent = existing?.content.orEmpty()
        movieReviewEditorReturnToList = false
        movieReviewScreen = MovieReviewScreen.DETAIL
    }
    fun openStandaloneMovieReview(root: ReviewEntry) {
        showFab = false
        movieReviewVideoId = null
        movieReviewStandaloneId = root.id
        val existing = movieReviewEntries.firstOrNull { it.standaloneReviewId == root.id }
        selectedMovieReviewId = existing?.id
        editingMovieReviewId = null
        movieReviewDraftTitle = root.title
        movieReviewDraftContent = existing?.content.orEmpty()
        movieReviewEditorReturnToList = false
        movieReviewScreen = MovieReviewScreen.DETAIL
    }
    fun openMovieReviewEditor(entry: MovieReviewEntry? = null) {
        movieReviewEditorReturnToList = true
        movieReviewEditorSession++
        editingMovieReviewId = entry?.id
        selectedMovieReviewId = entry?.id ?: selectedMovieReviewId
        movieReviewDraftTitle = entry?.title.orEmpty()
        movieReviewDraftContent = entry?.content.orEmpty()
        movieReviewScreen = MovieReviewScreen.EDITOR
    }
    fun saveMovieReview(title: String, content: String) {
        val standaloneId = movieReviewStandaloneId
        val workTitle = if (standaloneId != null) {
            reviewEntries.firstOrNull { it.id == standaloneId }?.title.orEmpty()
        } else ""
        val editingId = selectedMovieReviewId
        if (editingId == null) {
            val newId = if (standaloneId != null) {
                repository.addStandaloneMovieReview(standaloneId, content)
            } else {
                val videoId = movieReviewVideoId ?: return
                repository.addMovieReview(videoId, workTitle, content)
            } ?: return
            selectedMovieReviewId = newId
        } else {
            if (!repository.updateMovieReview(editingId, workTitle, content)) return
        }
        movieReviewDraftContent = content
        refresh()
        movieReviewEditorReturnToList = true
        editingMovieReviewId = null
    }
    fun closeMovieReviewPages() {
        movieReviewScreen = null
        movieReviewVideoId = null
        movieReviewStandaloneId = null
        selectedMovieReviewId = null
        editingMovieReviewId = null
        movieReviewEditorReturnToList = true
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
                        movieReviews = repository.backupMovieReviews(),
                        appPreferences = BackupAppPreferences(
                            allowBackground = appPreferences.getBoolean("allow_background", true),
                            keepAwake = appPreferences.getBoolean("keep_awake", true),
                            subtitleTextSizePercent = appPreferences.getInt("subtitle_text_size_percent", 100).coerceIn(50, 100),
                            appearanceMode = appPreferences.getString("appearance_mode", "system") ?: "system"
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
                require(backup.movieReviews.orEmpty().all { it.content.isNotBlank() && it.videoId in ids })
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
                imported.assets.orEmpty(),
                imported.movieReviews
            )
            imported.appPreferences?.let { saved ->
                preferences.edit()
                    .putBoolean("allow_background", saved.allowBackground)
                    .putBoolean("keep_awake", saved.keepAwake)
                    .putInt("subtitle_text_size_percent", saved.subtitleTextSizePercent.coerceIn(50, 100))
                    .apply()
                saved.appearanceMode?.takeIf { it == "system" || it == "light" || it == "dark" }?.let { mode ->
                    onAppearanceModeChange(mode)
                }
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
    BackHandler(enabled = movieReviewScreen != null || showCategoryDrawer || contextMenuOpen || showClearDataDialog || showImportDataDialog || clearDialogProgress > 0f || sheet != SheetType.NONE || sheetVisibility.currentState || sheetVisibility.targetState || playing != null || showFab) {
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
            movieReviewScreen == MovieReviewScreen.EDITOR -> {
                when {
                    editingMovieReviewId != null -> movieReviewScreen = MovieReviewScreen.DETAIL
                    movieReviewEditorReturnToList -> movieReviewScreen = MovieReviewScreen.LIST
                    else -> closeMovieReviewPages()
                }
                editingMovieReviewId = null
            }
            movieReviewScreen == MovieReviewScreen.DETAIL -> closeMovieReviewPages()
            movieReviewScreen == MovieReviewScreen.LIST -> closeMovieReviewPages()
            showFab -> showFab = false
        }
    }

    Box(
        Modifier.fillMaxSize().background(PageBg)
            .pointerInput(page, sheet, playing, contextMenuVideo, movieReviewScreen) {
                var horizontalDistance = 0f
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        if (sheet == SheetType.NONE && playing == null && contextMenuVideo == null && movieReviewScreen == null) {
                            horizontalDistance += dragAmount
                        }
                    },
                    onDragEnd = {
                        if (sheet == SheetType.NONE && playing == null && contextMenuVideo == null && movieReviewScreen == null && kotlin.math.abs(horizontalDistance) > 50f) {
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
        Box(Modifier.fillMaxSize().background(PageBg)) {
            Box(
                Modifier.fillMaxSize().graphicsLayer {
                    scaleX = 1f - .02f * sheetProgress
                    scaleY = 1f - .02f * sheetProgress
                }
            ) {
        androidx.compose.animation.AnimatedContent(
            targetState = playing,
            contentKey = { it },
            modifier = Modifier.fillMaxSize().background(if (playing == null) Color.Transparent else Color.Black),
            contentAlignment = Alignment.Center,
            transitionSpec = {
                if (targetState != null) {
                    (androidx.compose.animation.slideInVertically(
                        initialOffsetY = { it / 10 },
                        animationSpec = tween(350, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                    ) + fadeIn(tween(350))) togetherWith
                        fadeOut(tween(0))
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
                Modifier.fillMaxSize()
                    .graphicsLayer { alpha = if (playing == null) 1f else 0f }
                    .blur(backdropBlur)
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
                            MainPage.LIBRARY -> CompositionLocalProvider(LocalDropdownScrimReporter provides dropdownScrimReporter) {
                                LibraryPage(
                                hazeState = hazeState,
                                selectedVideoIdsState = librarySelectedVideoIds,
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
                                onMultiSelectModeChange = { libraryMultiSelectMode = it }
                            )
                            }
                            MainPage.RECENT -> RecentPage(
                                hazeState = hazeState,
                                listState = recentListState,
                                videos,
                                onVideo = { selected = it; playVideo(it) },
                                onClear = { sheet = SheetType.CLEAR_RECENT },
                                onMore = { selected = it; sheet = SheetType.DELETE_RECENT }
                            )
                            MainPage.RECORDS -> CompositionLocalProvider(LocalDropdownScrimReporter provides dropdownScrimReporter) {
                                androidx.compose.animation.AnimatedContent(
                                targetState = movieReviewScreen,
                                transitionSpec = {
                                    val initialDepth = when (initialState) {
                                        null -> 0
                                        MovieReviewScreen.LIST -> 1
                                        MovieReviewScreen.DETAIL -> 2
                                        MovieReviewScreen.EDITOR -> 3
                                    }
                                    val targetDepth = when (targetState) {
                                        null -> 0
                                        MovieReviewScreen.LIST -> 1
                                        MovieReviewScreen.DETAIL -> 2
                                        MovieReviewScreen.EDITOR -> 3
                                    }
                                    val forward = targetDepth > initialDepth
                                    if (forward) {
                                        (slideInHorizontally(tween(300)) { it / 12 } + fadeIn(tween(300))) togetherWith
                                            (slideOutHorizontally(tween(260)) { -it / 12 } + fadeOut(tween(260)))
                                    } else {
                                        (slideInHorizontally(tween(300)) { -it / 12 } + fadeIn(tween(300))) togetherWith
                                            (slideOutHorizontally(tween(260)) { it / 12 } + fadeOut(tween(260)))
                                    }
                                },
                                label = "movie-review-page"
                            ) { reviewScreen ->
                                val standaloneReviewRoot = reviewEntries.firstOrNull {
                                    it.id == movieReviewStandaloneId && it.videoId == null && it.standaloneGroupId == null
                                }
                                val reviewVideo = videos.firstOrNull { it.id == movieReviewVideoId } ?: standaloneReviewRoot?.let { root ->
                                    VideoItem(
                                        id = root.id,
                                        uri = "",
                                        name = root.title.ifBlank { "未命名" },
                                        size = "",
                                        date = "",
                                        addedAt = root.createdAt,
                                        rating = root.rating,
                                        thumbPath = root.coverPath
                                    )
                                }
                                val selectedMovieReview = movieReviewEntries.firstOrNull { it.id == selectedMovieReviewId }
                                when (reviewScreen) {
                                    null -> RecordsPage(
                                        hazeState = hazeState,
                                        listState = recordsListState,
                                        videos = videos,
                                        reviews = reviewEntries,
                                        movieReviews = movieReviewEntries,
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
                                        onOpenMovieReviews = { video -> openMovieReviewList(video) },
                                        onOpenStandaloneMovieReview = { root -> openStandaloneMovieReview(root) },
                                        onPlayVideo = { video -> playVideo(video) },
                                        onSeekToTimestamp = { video, positionMs -> playVideoAt(video, positionMs) },
                                        onTargetHandled = { targetReviewId = null; targetRecordVideoId = null }
                                    )
                                    MovieReviewScreen.LIST -> Box(Modifier.fillMaxSize())
                                    MovieReviewScreen.DETAIL -> if (reviewVideo != null) {
                                        MovieReviewDetailPage(
                                            hazeState = hazeState,
                                            video = reviewVideo,
                                            entry = selectedMovieReview,
                                            stateKey = movieReviewStandaloneId?.let { "standalone_$it" } ?: "video_${reviewVideo.id}",
                                            initialContent = movieReviewDraftContent,
                                            onBack = { closeMovieReviewPages() },
                                            onSave = { content -> saveMovieReview("", content) },
                                            onDelete = { sheet = SheetType.DELETE_MOVIE_REVIEW },
                                            onRate = { rating ->
                                                if (movieReviewStandaloneId != null) {
                                                    repository.updateStandaloneReviewRating(movieReviewStandaloneId!!, rating)
                                                } else {
                                                    repository.updateVideoRating(reviewVideo.id, rating)
                                                }
                                                refresh()
                                            },
                                            onExport = {
                                                scope.launch {
                                                    var bitmap: android.graphics.Bitmap? = null
                                                    val saved = try {
                                                        bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                                                            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                                                                textSize = 32f
                                                                typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
                                                            }
                                                            createUnifiedReviewExportBitmap(
                                                                title = displayVideoName(reviewVideo.name),
                                                                coverPath = reviewVideo.thumbPath,
                                                                rating = reviewVideo.rating,
                                                                reviews = emptyList(),
                                                                singleReviewBody = wrapReviewExportText(selectedMovieReview?.content ?: movieReviewDraftContent, paint, 936f),
                                                                eyebrowLabel = "REVIEW"
                                                            )
                                                        }
                                                        saveVideoScreenshot(context, bitmap!!)
                                                    } catch (_: Exception) {
                                                        false
                                                    } finally {
                                                        bitmap?.recycle()
                                                    }
                                                    notify(if (saved) "影评已导出到相册" else "影评导出失败")
                                                }
                                            },
                                            onCopy = {
                                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("影评", selectedMovieReview?.content ?: movieReviewDraftContent))
                                                notify("已复制影评")
                                            }
                                        )
                                    } else Box(Modifier.fillMaxSize())
                                    MovieReviewScreen.EDITOR -> Box(Modifier.fillMaxSize())
                                }
                                }
                            }
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
                onWatchTime = { deltaMs ->
                    playing?.let { repository.recordWatchTime(it.id, deltaMs) }
                },
                onFlushWatchTime = { repository.flushWatchTime() },
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
            val bottomFadePageBg = PageBg
            Box(Modifier.fillMaxSize().blur(5.dp * maxOf(contextMenuProgress, clearDialogProgress, importDialogProgress, categoryDrawerProgress))) {
                if (page == MainPage.LIBRARY && libraryMultiSelectMode && movieReviewScreen == null) {
                    CompositionLocalProvider(LocalDropdownScrimReporter provides dropdownScrimReporter) {
                        LibraryBatchActionBar(
                        hazeState = hazeState,
                        videos = sortedLibraryVideos,
                        categories = categories,
                        selectedVideoIdsState = librarySelectedVideoIds,
                        multiSelectMode = libraryMultiSelectMode,
                        onMultiSelectModeChange = { libraryMultiSelectMode = it },
                        onBatchMove = { ids, target -> ids.forEach { id -> videos.firstOrNull { it.id == id }?.let { repository.update(it.copy(category = target)) } }; refresh() },
                        onBatchFavorite = { ids, value -> ids.forEach { id -> videos.firstOrNull { it.id == id }?.let { repository.update(it.copy(isFav = value)) } }; refresh() },
                        onBatchStatus = { ids, target -> ids.forEach { id -> videos.firstOrNull { it.id == id }?.let { item ->
                            repository.update(when (target) {
                                "未看" -> item.copy(progress = 0f, status = "all", playbackPositionMs = 0L, playbackCompleted = false)
                                "在看" -> item.copy(status = "playing")
                                else -> item.copy(
                                    progress = 100f,
                                    status = "watched",
                                    playbackCompleted = true,
                                    playbackPositionMs = item.playbackPositionMs
                                        ?: if (item.durationMs > 0L) (item.durationMs * item.progress / 100f).toLong() else null
                                )
                            })
                        } }; refresh() },
                        onBatchRemove = { ids -> ids.forEach(repository::removeFromLibrary); refresh() }
                        )
                    }
                }
                if (!(page == MainPage.LIBRARY && libraryMultiSelectMode) && movieReviewScreen == null) {
                    Box(
                        Modifier.align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(145.dp)
                            .drawWithCache {
                                val verticalFade = Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    0.20f to Color.Transparent,
                                    0.48f to bottomFadePageBg.copy(alpha = 0.04f),
                                    0.76f to bottomFadePageBg.copy(alpha = 0.18f),
                                    1f to bottomFadePageBg.copy(alpha = 0.42f),
                                    startY = 0f,
                                    endY = size.height
                                )
                                onDrawWithContent {
                                    drawContent()
                                    drawRect(verticalFade)
                                }
                            }
                    )
                }
                AnimatedVisibility(
                    visible = !(page == MainPage.LIBRARY && libraryMultiSelectMode) && movieReviewScreen == null,
                    enter = fadeIn(tween(180)) + slideInVertically(tween(180)) { it / 3 },
                    exit = fadeOut(tween(140)) + slideOutVertically(tween(140)) { it / 3 },
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().zIndex(1f)
                ) {
                    BottomPillNavigation(page, hazeState, backdropBlur, onBoundsChanged = { contextMenuNavigationBounds = it }) { page = it; showFab = false }
                }
                if (!(page == MainPage.LIBRARY && libraryMultiSelectMode) && movieReviewScreen == null) Column(
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
                    Box(Modifier.size(48.dp).shadow(4.dp, CircleShape, ambientColor = ActiveAccent.copy(alpha = .4f), spotColor = ActiveAccent.copy(alpha = .4f)).clip(CircleShape).background(ActiveAccent), contentAlignment = Alignment.Center) {
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
                    Box(Modifier.size(48.dp).shadow(4.dp, CircleShape, ambientColor = ActiveAccent.copy(alpha = .4f), spotColor = ActiveAccent.copy(alpha = .4f)).clip(CircleShape).background(ActiveAccent), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Add, "导入视频", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Box(
                    Modifier.size(56.dp)
                        .onGloballyPositioned { contextMenuFabBounds = it.boundsInRoot() }
                        .shadow(8.dp, CircleShape, ambientColor = Color.Black.copy(alpha = .10f), spotColor = Color.Black.copy(alpha = .10f))
                        .frameGlassSurface(hazeState, CircleShape)
                        .noRippleClickable { showFab = !showFab },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(FrameHamburgerIcon, null, tint = MainText, modifier = Modifier.size(20.dp).rotate(fabRotation))
                }
                }
        }
            }
        }
        }

        if (dropdownScrimAlpha > 0f) {
            Box(
                Modifier.fillMaxSize()
                    .zIndex(3f)
                    .background(Color.Black.copy(alpha = dropdownScrimAlpha))
            )
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
                        .then(
                            when (displayedSheet) {
                                SheetType.SETTINGS ->
                                    Modifier.height(LocalConfiguration.current.screenHeightDp.dp * .5f)
                                SheetType.SEARCH, SheetType.RECORD_SEARCH ->
                                    Modifier.fillMaxHeight(.85f)
                                else ->
                                    Modifier.heightIn(max = LocalConfiguration.current.screenHeightDp.dp * .85f)
                            }
                        )
                        .shadow(10.dp, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), ambientColor = Color.Black.copy(alpha = .10f), spotColor = Color.Black.copy(alpha = .10f))
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(PageBg)
                        .then(
                            if (displayedSheet == SheetType.SETTINGS ||
                                displayedSheet == SheetType.RECORD_SEARCH ||
                                displayedSheet == SheetType.SEARCH ||
                                displayedSheet == SheetType.MOVE
                            ) Modifier
                            else Modifier.verticalScroll(rememberScrollState())
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) { }
                         .padding(
                            top = 26.dp,
                            bottom = when {
                                sheetKeyboardUp &&
                                    displayedSheet != SheetType.SEARCH &&
                                    displayedSheet != SheetType.RECORD_SEARCH -> 400.dp
                                displayedSheet == SheetType.SETTINGS ||
                                    displayedSheet == SheetType.SEARCH ||
                                    displayedSheet == SheetType.RECORD_SEARCH ||
                                    displayedSheet == SheetType.MOVE -> 24.dp
                                else -> 50.dp
                            }
                        )
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
                        Column(
                            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                        ) {
                        Text("播放偏好", color = MutedText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 10.dp, bottom = 8.dp))
                        Column(Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface)) {
                            SettingRow("允许后台播放", allowBackground) { allowBackground = it; onBackgroundPlaybackChanged(it); preferences.edit().putBoolean("allow_background", it).apply() }
                            Box(Modifier.fillMaxWidth().height(0.5.dp).background(LocalFramePalette.current.divider))
                            SettingRow("保持屏幕常亮", keepAwake) { keepAwake = it; preferences.edit().putBoolean("keep_awake", it).apply(); onKeepAwake(it) }
                        }
                        Text("外观", color = MutedText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 10.dp, top = 20.dp, bottom = 8.dp))
                        Column(Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface)) {
                            listOf("system" to "跟随系统", "dark" to "深色模式").forEachIndexed { index, option ->
                                val selected = appearanceMode == option.first
                                Row(
                                    Modifier.fillMaxWidth().height(52.dp).noRippleClickable {
                                        onAppearanceModeChange(if (selected) "light" else option.first)
                                    }.padding(horizontal = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(option.second, Modifier.weight(1f), color = MainText, fontSize = 16.sp)
                                    Box(
                                        Modifier.size(44.dp, 24.dp)
                                            .clip(CircleShape)
                                            .background(if (selected) ActiveAccent else LocalFramePalette.current.secondarySurface)
                                    ) {
                                        val knobOffset by animateDpAsState(if (selected) 22.dp else 2.dp, tween(300), label = "setting-switch")
                                        Box(
                                            Modifier.align(Alignment.CenterStart)
                                                .offset(x = knobOffset)
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    }
                                }
                                if (index < 1) {
                                    Box(Modifier.fillMaxWidth().height(0.5.dp).background(LocalFramePalette.current.divider))
                                }
                            }
                        }
                        Text("数据与隐私", color = MutedText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 10.dp, top = 20.dp, bottom = 8.dp))
                        Column(Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface)) {
                            Row(Modifier.fillMaxWidth().height(52.dp).noRippleClickable { notify("数据仅保存在本地") }.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("关于与隐私", Modifier.weight(1f), color = MainText, fontSize = 16.sp)
                                Text(
                                    if (appVersion.isBlank()) "" else "v$appVersion",
                                    color = LocalFramePalette.current.tertiaryText,
                                    fontSize = 11.sp
                                )
                            }
                            Box(Modifier.fillMaxWidth().height(0.5.dp).background(LocalFramePalette.current.divider))
                            Row(Modifier.fillMaxWidth().height(52.dp).noRippleClickable { exportDataLauncher.launch("Frame-backup.json") }.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("导出数据", Modifier.weight(1f), color = MainText, fontSize = 16.sp)
                            }
                            Box(Modifier.fillMaxWidth().height(0.5.dp).background(LocalFramePalette.current.divider))
                            Row(Modifier.fillMaxWidth().height(52.dp).noRippleClickable { importDataLauncher.launch(arrayOf("application/json")) }.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("导入数据", Modifier.weight(1f), color = MainText, fontSize = 16.sp)
                            }
                            Box(Modifier.fillMaxWidth().height(0.5.dp).background(LocalFramePalette.current.divider))
                            Row(Modifier.fillMaxWidth().height(52.dp).noRippleClickable { showClearDataDialog = true; sheet = SheetType.NONE }.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("清空所有数据", color = LocalFramePalette.current.danger, fontSize = 16.sp)
                            }
                        }
                        }
                    }
                    SheetType.RECORD_SEARCH -> Column(
                        Modifier.fillMaxHeight().fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .animateContentSize(
                                tween(320, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f))
                            )
                    ) {
                        SheetHeading("搜索")
                        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                        BasicTextField(
                            recordSearch,
                            { recordSearch = it },
                            Modifier.fillMaxWidth().heightIn(min = 50.dp)
                                .onFocusChanged { sheetKeyboardUp = it.isFocused }
                                .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
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
                                        color = MaterialTheme.colorScheme.surface,
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
                                                    .background(if (video != null) PosterBrush(video.bgIndex) else Brush.linearGradient(listOf(LocalFramePalette.current.secondarySurface, LocalFramePalette.current.secondarySurface))),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (video != null) Thumbnail(video, Modifier.fillMaxSize())
                                                else Text("感想", color = ActiveAccent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                            }
                                            Column(Modifier.weight(1f)) {
                                                if (video != null && entry.content.isNotBlank()) {
                                                    VideoCardTitle(
                                                        name = video.name,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 16.sp,
                                                        color = MainText,
                                                        englishFontSize = 14.sp,
                                                        englishColor = MutedText,
                                                        lineHeight = 21.sp,
                                                        maxLines = 2,
                                                        showEnglish = true,
                                                        textAlign = TextAlign.Start
                                                    )
                                                } else {
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
                                                }
                                                if (entry.content.isNotBlank()) {
                                                    Text(
                                                        entry.content,
                                                        color = MutedText,
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
                                        color = MaterialTheme.colorScheme.surface,
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
                                                englishColor = MutedText,
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
                    }
                    SheetType.SEARCH -> Column(
                        Modifier.fillMaxHeight().fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .animateContentSize(
                                tween(320, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f))
                            )
                    ) {
                        SheetHeading("搜索")
                        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                        BasicTextField(
                            search,
                            { search = it },
                            Modifier.fillMaxWidth().heightIn(min = 50.dp)
                                .onFocusChanged { sheetKeyboardUp = it.isFocused }
                                .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
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
                                        color = MaterialTheme.colorScheme.surface,
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
                                                englishColor = MutedText,
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
                    }
                    SheetType.RENAME -> Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        SheetHeading("重命名")
                        Row(
                            Modifier.fillMaxWidth()
                                .height(52.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface),
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
                                .background(MaterialTheme.colorScheme.surface)
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
                    SheetType.MOVE -> Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        SheetHeading("移动到分类")
                        val moveCategories = categories.filterNot { it == "全部分类" || it == "我的最爱" }
                        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                            moveCategories.forEachIndexed { index, cat ->
                                SheetAction(cat, bottomPadding = if (index == moveCategories.lastIndex) 0.dp else 12.dp) { selected?.let { repository.moveCategory(it.id, cat) }; refresh(); sheet = SheetType.NONE }
                            }
                        }
                    }
                    SheetType.REVIEW_CREATE_OPTIONS -> Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        SheetHeading("新增观影感想")
                        Column(
                            Modifier.fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Box(
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
                                contentAlignment = Alignment.Center
                            ) {
                                Text("关联视频", color = MainText, fontSize = 16.sp)
                            }
                            Box(Modifier.fillMaxWidth().height(0.5.dp).background(LocalFramePalette.current.divider))
                            Box(
                                Modifier.fillMaxWidth().height(52.dp)
                                    .noRippleClickable { startNewReview(null) }
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.Center
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
                                        .clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(15.dp),
                                    decorationBox = { inner -> if (editReview.isEmpty()) Text("写下你的感想...", color = MutedText, fontSize = 16.sp); inner() },
                                    textStyle = TextStyle(fontSize = 16.sp, color = MainText, lineHeight = 24.sp)
                                )
                                val canSaveChild = editReview.isNotBlank()
                                Box(
                                    Modifier.fillMaxWidth().padding(top = 10.dp).height(52.dp)
                                        .shadow(if (canSaveChild) 4.dp else 0.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (canSaveChild) ActiveAccent else LocalFramePalette.current.secondarySurface)
                                        .then(if (canSaveChild) Modifier.noRippleClickable {
                                            val groupId = editStandaloneGroupId!!
                                            val saved = if (editReviewId == null) repository.addStandaloneChildReview(groupId, editReview)
                                                else repository.updateStandaloneChildReview(editReviewId!!, groupId, editReview)
                                            if (saved) { refresh(); sheet = SheetType.NONE }
                                        } else Modifier),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("保存记录", color = if (canSaveChild) Color.White else LocalFramePalette.current.tertiaryText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
                                }
                                if (editReviewId != null) {
                                    Box(Modifier.fillMaxWidth().padding(top = 12.dp).height(52.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).noRippleClickable {
                                        editReviewId?.let { repository.deleteReview(it) }; refresh(); sheet = SheetType.NONE
                                    }, contentAlignment = Alignment.Center) {
                                        Text("删除这条感想", color = LocalFramePalette.current.danger, fontSize = 16.sp, fontWeight = FontWeight.Medium)
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
                                        .background(LocalFramePalette.current.secondarySurface)
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
                                        Icon(Icons.Outlined.Image, null, tint = LocalFramePalette.current.tertiaryText, modifier = Modifier.size(22.dp))
                                        Text("添加封面", color = LocalFramePalette.current.tertiaryText, fontSize = 10.sp)
                                    }
                                }
                                Column(Modifier.weight(1f).height(100.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                    BasicTextField(
                                        editReviewTitle,
                                        { editReviewTitle = it },
                                        Modifier.fillMaxWidth().heightIn(min = 42.dp)
                                            .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                            .clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface).padding(11.dp),
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
                                    .clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(15.dp),
                                decorationBox = { inner -> if (editReview.isEmpty()) Text("写下你的感想...", color = MutedText, fontSize = 16.sp); inner() },
                                textStyle = TextStyle(fontSize = 16.sp, color = MainText, lineHeight = 24.sp)
                            )
                            val canSaveStandalone = editReview.isNotBlank()
                            Box(
                                Modifier.fillMaxWidth().padding(top = 10.dp).height(52.dp)
                                    .shadow(if (canSaveStandalone) 4.dp else 0.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (canSaveStandalone) ActiveAccent else LocalFramePalette.current.secondarySurface)
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
                                Text("保存记录", color = if (canSaveStandalone) Color.White else LocalFramePalette.current.tertiaryText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
                            }
                            if (editReviewId != null) {
                                Box(Modifier.fillMaxWidth().padding(top = 12.dp).height(52.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).noRippleClickable {
                                    editReviewId?.let { repository.deleteReview(it) }; refresh(); sheet = SheetType.NONE
                                }, contentAlignment = Alignment.Center) {
                                    Text("删除这条感想", color = LocalFramePalette.current.danger, fontSize = 16.sp, fontWeight = FontWeight.Medium)
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
                                        .background(if (linkedVideo != null) ActiveAccent else MaterialTheme.colorScheme.surface)
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
                                        .background(if (linkedVideo == null) ActiveAccent else MaterialTheme.colorScheme.surface)
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
                                englishColor = MutedText,
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
                        BasicTextField(editReview, { editReview = it }, Modifier.fillMaxWidth().padding(bottom = 20.dp).height(120.dp).onFocusChanged { sheetKeyboardUp = it.isFocused }.shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(15.dp), decorationBox = { inner -> if (editReview.isEmpty()) Text("写下你此刻的观影感受...", color = MutedText, fontSize = 16.sp); inner() }, textStyle = TextStyle(fontSize = 16.sp, color = MainText, lineHeight = 24.sp))
                        Box(Modifier.fillMaxWidth().padding(top = 10.dp).height(52.dp).shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(16.dp)).background(ActiveAccent).noRippleClickable {
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
                                    .background(MaterialTheme.colorScheme.surface)
                                    .noRippleClickable {
                                        editReviewId?.let { repository.deleteReview(it) }
                                        refresh()
                                        sheet = SheetType.NONE
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("删除这条感想", color = LocalFramePalette.current.danger, fontSize = 16.sp, fontWeight = FontWeight.Medium, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
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
                                .background(MaterialTheme.colorScheme.surface)
                                
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
                                        color = MaterialTheme.colorScheme.surface,
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
                                                englishColor = MutedText,
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
                    SheetType.DELETE_MOVIE_REVIEW -> SheetContent("删除这篇影评？") {
                        Text("删除后无法恢复，但不会影响作品评分和随记。", fontSize = 14.sp, lineHeight = 21.sp, color = MutedText, modifier = Modifier.padding(bottom = 16.dp))
                        SheetActionPair(
                            confirmText = "删除",
                            cancelText = "取消",
                            confirmDanger = true,
                            confirmOnRight = true,
                            onConfirm = {
                                selectedMovieReviewId?.let { repository.deleteMovieReview(it) }
                                refresh()
                                sheet = SheetType.NONE
                                closeMovieReviewPages()
                            },
                            onCancel = { sheet = SheetType.NONE }
                        )
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
                                .clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 20.dp),
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
                            confirmOnRight = true,
                            confirmTheme = true,
                            spacing = 12.dp,
                            onConfirm = {
                                val name = newCategoryName.trim()
                                when {
                                    name.isEmpty() -> toast = "分类名称不能为空"
                                    name in categories -> toast = "分类名称已存在"
                                    else -> {
                                        repository.saveCategories(if (categories.lastOrNull() == "全部分类") categories.dropLast(1) + name + "全部分类" else categories + name)
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
                        .background(MaterialTheme.colorScheme.surface)
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
                    Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp).shadow(18.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).noRippleClickable { }.padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("清空全部数据？", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MainText)
                        Text("此操作会删除片库条目、分类、封面和本地记录，且无法撤销。", fontSize = 14.sp, lineHeight = 21.sp, color = MutedText, modifier = Modifier.padding(top = 26.dp, bottom = 22.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(14.dp)).background(LocalFramePalette.current.secondarySurface).noRippleClickable { showClearDataDialog = false }, contentAlignment = Alignment.Center) { Text("取消", color = MainText, fontWeight = FontWeight.Medium) }
                            Box(Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(14.dp)).background(LocalFramePalette.current.danger).noRippleClickable { repository.clearAll(); refresh(); showClearDataDialog = false }, contentAlignment = Alignment.Center) { Text("确认清空", color = Color.White, fontWeight = FontWeight.SemiBold) }
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
                    Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp).shadow(18.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).noRippleClickable { }.padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("导入并覆盖数据？", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MainText)
                        Text("导入会覆盖当前片库中的全部数据，且无法撤销。确定继续吗？", fontSize = 14.sp, lineHeight = 21.sp, color = MutedText, modifier = Modifier.padding(top = 26.dp, bottom = 22.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(14.dp)).background(LocalFramePalette.current.secondarySurface).noRippleClickable { showImportDataDialog = false; pendingImportUri = null }, contentAlignment = Alignment.Center) { Text("取消", color = MainText, fontWeight = FontWeight.Medium) }
                            Box(Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(14.dp)).background(ActiveAccent).noRippleClickable {
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
                                    Spacer(Modifier.fillMaxWidth().padding(horizontal = 12.dp).height(0.5.dp).background(LocalFramePalette.current.divider))
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
                                        if (categoryEditMode) Icon(Icons.Outlined.Menu, "拖动排序", tint = LocalFramePalette.current.tertiaryText, modifier = Modifier.size(18.dp))
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
                                            Icon(Icons.Outlined.Check, "保存分类名称", tint = ActiveAccent, modifier = Modifier.size(18.dp))
                                        }
                                        Box(Modifier.size(28.dp).noRippleClickable {
                                            editingCategoryName = null
                                            editingCategoryDraft = ""
                                        }, contentAlignment = Alignment.CenterEnd) {
                                            Icon(Icons.Outlined.Close, "取消重命名", tint = LocalFramePalette.current.tertiaryText, modifier = Modifier.size(18.dp))
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
                                            Icon(Icons.Outlined.DeleteOutline, "删除分类", tint = LocalFramePalette.current.tertiaryText, modifier = Modifier.size(18.dp))
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
                                    .clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 20.dp),
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
                                confirmOnRight = true,
                                confirmTheme = true,
                                spacing = 12.dp,
                                onConfirm = {
                                    val name = newCategoryName.trim()
                                    when {
                                        name.isEmpty() -> toast = "分类名称不能为空"
                                        name in categories -> toast = "分类名称已存在"
                                        else -> {
                                            repository.saveCategories(if (categories.lastOrNull() == "全部分类") categories.dropLast(1) + name + "全部分类" else categories + name)
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



