package com.frame.cinema

import android.app.Activity
import android.content.Intent
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
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
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(12f, 20f); lineTo(12f, 4f); moveTo(6f, 10f); lineTo(12f, 4f); lineTo(18f, 10f)
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

private enum class MainPage(val label: String) { HOME("首页"), LIBRARY("片库"), RECENT("最近"), RECORDS("记录") }
private enum class SheetType { NONE, IMPORT, SETTINGS, SORT, GRID, CATEGORIES, SEARCH, RECORD_SEARCH, RECORD_SORT, RENAME, MOVE, NOTE, CLEAR_RECENT, CLEAR_DATA, DELETE_RECENT }
private enum class SortMode { ADDED, NAME, RATING, WATCHED }

class MainActivity : ComponentActivity() {
    private var player: Player? = null
    private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null
    private lateinit var repository: NativeLibraryRepository
    private var backgroundPlaybackAllowed = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                                onPlay = { item -> startNativePlayback(item) },
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
                                }
                            )
                        }
                    }
                }
            }
        }, java.util.concurrent.Executor { command -> command.run() })
    }

    private fun startNativePlayback(item: VideoItem) {
        val controller = player ?: return
        val iconBytes = runCatching {
            val drawable = packageManager.getApplicationIcon(packageName)
            val bitmap = android.graphics.Bitmap.createBitmap(
                256, 256, android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, 256, 256)
            drawable.draw(canvas)
            java.io.ByteArrayOutputStream().use { stream ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                stream.toByteArray()
            }
        }.getOrNull()
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(item.name)
        if (iconBytes != null) {
            metadataBuilder.setArtworkData(iconBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
        }
        val mediaItem = MediaItem.Builder()
            .setUri(item.contentUri)
            .setMediaMetadata(metadataBuilder.build())
            .build()
        controller.setMediaItem(mediaItem)
        controller.prepare()
        if (item.progress >= 99f || item.status == "watched") controller.seekTo(0L)
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
    onPlay: (VideoItem) -> Unit,
    onBackgroundPlaybackChanged: (Boolean) -> Unit,
    onOrientation: (Boolean) -> Unit,
    onKeepAwake: (Boolean) -> Unit,
    onSystemBarsVisible: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val hazeState = remember { HazeState() }
    val homeListState = rememberLazyListState()
    var page by remember { mutableStateOf(MainPage.HOME) }
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
    var weeklyWatchTimeMs by remember { mutableLongStateOf(repository.weeklyWatchTimeMs()) }
    var categories by remember { mutableStateOf(repository.categories().let { saved ->
        val ordered = listOf("我的最爱") + saved.filterNot { it == "全部分类" || it == "我的最爱" } + "全部分类"
        if (ordered != saved) repository.saveCategories(ordered)
        ordered
    }) }
    var status by remember { mutableStateOf("全部") }
    var category by remember { mutableStateOf("全部分类") }
    var compact by remember { mutableStateOf(true) }
    var showFab by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var recordSearch by remember { mutableStateOf("") }
    var recordSortByRating by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editReview by remember { mutableStateOf("") }
    var editRating by remember { mutableIntStateOf(0) }
    var newCategory by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(SortMode.ADDED) }
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
    val backdropBlur = maxOf(8.dp * maxOf(sheetProgress, clearDialogProgress, importDialogProgress), 12.dp * contextMenuProgress)
    var displayedSheet by remember { mutableStateOf(SheetType.NONE) }

    fun refresh() { videos = repository.videos(); categories = repository.categories(); weeklyWatchTimeMs = repository.weeklyWatchTimeMs() }
    fun notify(message: String) { toast = message }
    fun playVideo(item: VideoItem) {
        speedIndex = 0
        controlsVisible = true
        locked = false
        playing = item
        onSystemBarsVisible(false)
        onPlay(item)
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
        val count = repository.importUris(uris) { refresh() }
        refresh()
        notify("已导入 $count 个视频")
        sheet = SheetType.NONE
    }
    val galleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        val count = repository.importUris(uris) { refresh() }
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
    val exportDataLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                val json = com.google.gson.Gson().toJson(videos)
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
                val type = object : com.google.gson.reflect.TypeToken<List<VideoItem>>() {}.type
                val parsed: List<VideoItem> = com.google.gson.Gson().fromJson(json, type)
                    ?: throw com.google.gson.JsonSyntaxException("JSON 内容为空")
                require(parsed.all {
                    it.uri.isNotBlank() && it.name.isNotBlank() && it.date.isNotBlank() &&
                        it.size.length >= 0 && it.status.isNotBlank() && it.category.isNotBlank() &&
                        it.review.length >= 0 && it.progress.isFinite()
                })
                require(parsed.map { it.uri }.distinct().size == parsed.size)
                parsed
            } catch (_: Exception) {
                notify("文件格式错误")
                return
            }
            repository.replaceAll(imported)
            refresh()
            notify("数据导入成功，若部分视频无法播放，请重新导入视频源")
        } catch (_: Exception) {
            notify("数据导入失败")
        }
    }

    LaunchedEffect(toast) { if (toast.isNotBlank()) { delay(2100); toast = "" } }
    val sheetVisibility = remember { androidx.compose.animation.core.MutableTransitionState(false) }
    LaunchedEffect(sheet) {
        sheetVisibility.targetState = sheet != SheetType.NONE
        if (sheet == SheetType.NONE) { sheetKeyboardUp = false; parentSheet = SheetType.NONE } else displayedSheet = sheet
    }
    BackHandler(enabled = contextMenuVideo != null || showClearDataDialog || showImportDataDialog || clearDialogProgress > 0f || sheet != SheetType.NONE || sheetVisibility.currentState || sheetVisibility.targetState || playing != null || showFab) {
        when {
            contextMenuVideo != null -> { contextMenuOpen = false }
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
                Modifier.fillMaxSize().background(PageBg)
                    .graphicsLayer {
                        scaleX = 1f - .05f * sheetProgress
                        scaleY = 1f - .05f * sheetProgress
                    }
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
                            MainPage.LIBRARY -> LibraryPage(
                                hazeState = hazeState,
                                when (sort) { SortMode.NAME -> videos.sortedBy { it.name.lowercase(Locale.getDefault()) }; else -> videos.sortedByDescending { it.addedAt } }, categories, status, category, compact,
                                { status = it }, { category = it },
                                { sheet = SheetType.GRID }, { sheet = SheetType.SORT },
                                { sheet = SheetType.CATEGORIES },
                                { item, bounds -> selected = item; contextMenuVideo = item; contextMenuBounds = bounds; contextMenuStartBounds = bounds; contextMenuOpen = true; showFab = false },
                                { selected = it; playVideo(it) },
                                contextMenuVideo,
                                contextMenuReturnProgress,
                                { latestBounds -> if (contextMenuBounds != latestBounds) contextMenuBounds = latestBounds },
                                { viewport -> contextMenuViewportBounds = viewport },
                                { header -> contextMenuStickyHeaderBounds = header }
                            )
                            MainPage.RECENT -> RecentPage(
                                hazeState = hazeState,
                                videos,
                                onVideo = { selected = it; playVideo(it) },
                                onClear = { sheet = SheetType.CLEAR_RECENT },
                                onMore = { selected = it; sheet = SheetType.DELETE_RECENT }
                            )
                            MainPage.RECORDS -> RecordsPage(
                                hazeState = hazeState,
                                videos = videos.filter { it.status == "watched" || it.progress >= 100f }
                                    .let { list -> if (recordSortByRating) list.sortedByDescending { it.rating } else list.sortedByDescending { it.lastPlayedAt } }
                                    .filter { recordSearch.isBlank() || it.name.contains(recordSearch, true) || it.review.contains(recordSearch, true) },
                                onSearch = { sheet = SheetType.RECORD_SEARCH },
                                onSort = { sheet = SheetType.RECORD_SORT },
                                onEdit = {
                                    selected = it; editReview = it.review; editRating = it.rating
                                    parentSheet = SheetType.NONE
                                    sheet = SheetType.NOTE
                                }
                            )
                        }
                    }
            }

          } else {
            NativePlayerOverlay(
                item = targetPlaying,
                player = player,
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
                }
            )
          }
        }

        if (playing == null) {
            Box(Modifier.fillMaxSize().blur(12.dp * maxOf(contextMenuProgress, clearDialogProgress, importDialogProgress))) {
                Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().zIndex(1f)) {
                    BottomPillNavigation(page, hazeState, backdropBlur, onBoundsChanged = { contextMenuNavigationBounds = it }) { page = it; showFab = false }
                }
                Column(
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
                        .shadow(4.dp, CircleShape, ambientColor = Color.Black.copy(alpha = .06f), spotColor = Color.Black.copy(alpha = .06f))
                        .hazeEffect(state = hazeState, style = HazeStyle(backgroundColor = PageBg, tints = emptyList(), blurRadius = 20.dp, noiseFactor = 0f))
                        .clip(CircleShape)
                        .background(Glass)
                        .border(1.dp, Color.Black.copy(alpha = .05f), CircleShape)
                        .noRippleClickable { showFab = !showFab },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(FrameHamburgerIcon, null, tint = Color(0xFF333333), modifier = Modifier.size(20.dp).rotate(fabRotation))
                }
                }
        }
        }

        val sheetSlideOffsetPx = with(LocalDensity.current) { 24.dp.roundToPx() }

        AnimatedVisibility(
            visibleState = sheetVisibility,
            enter = fadeIn(tween(340, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f))),
            exit = fadeOut(tween(240, easing = androidx.compose.animation.core.CubicBezierEasing(.4f, 0f, 1f, 1f))),
            modifier = Modifier.zIndex(4f)
        ) {
            Box(
                Modifier.fillMaxSize().zIndex(2f).blur(8.dp * maxOf(clearDialogProgress, importDialogProgress))
                    .background(Color.Black.copy(alpha = .4f * (1f - maxOf(clearDialogProgress, importDialogProgress))))
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { sheet = SheetType.NONE }
            ) {
                Column(
                    Modifier.align(Alignment.BottomCenter)
                        .animateEnterExit(
                            enter = slideInVertically(
                                animationSpec = tween(340, easing = androidx.compose.animation.core.CubicBezierEasing(.34f, 1.56f, .64f, 1f))
                            ) { sheetSlideOffsetPx } + fadeIn(tween(340)),
                            exit = slideOutVertically(
                                animationSpec = tween(240, easing = androidx.compose.animation.core.CubicBezierEasing(.4f, 0f, 1f, 1f))
                            ) { sheetSlideOffsetPx } + fadeOut(tween(240))
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
                         .padding(top = 25.dp, bottom = if (sheetKeyboardUp) 400.dp else if (displayedSheet == SheetType.SETTINGS) 30.dp else 40.dp)
                        .navigationBarsPadding()
                ) {
                when (displayedSheet) {
                    SheetType.IMPORT -> SheetContent("导入本地视频") {
                        SheetAction("从相册导入") {
                            galleryPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                        }
                        SheetAction("从文件中选择视频") { importPicker.launch(arrayOf("video/*")) }
                        SheetAction("扫描整个文件夹") { folderPicker.launch(null) }
                    }
                    SheetType.SETTINGS -> Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        SheetHeading("设置")
                        Text("播放偏好", color = MutedText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 10.dp, bottom = 8.dp))
                        Column(Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(16.dp)).background(Color.White)) {
                            SettingRow("允许后台播放", allowBackground) { allowBackground = it; onBackgroundPlaybackChanged(it); preferences.edit().putBoolean("allow_background", it).apply() }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F0)))
                            SettingRow("保持屏幕常亮", keepAwake) { keepAwake = it; preferences.edit().putBoolean("keep_awake", it).apply(); onKeepAwake(it) }
                        }
                        Text("数据与隐私", color = MutedText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 10.dp, top = 20.dp, bottom = 8.dp))
                        Column(Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(16.dp)).background(Color.White)) {
                            Row(Modifier.fillMaxWidth().height(51.dp).noRippleClickable { notify("数据仅保存在本地") }.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("关于与隐私", Modifier.weight(1f), color = MainText, fontSize = 16.sp)
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F0)))
                            Row(Modifier.fillMaxWidth().height(51.dp).noRippleClickable { exportDataLauncher.launch("Frame-backup.json") }.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("导出所有数据", Modifier.weight(1f), color = MainText, fontSize = 16.sp)
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F0)))
                            Row(Modifier.fillMaxWidth().height(51.dp).noRippleClickable { importDataLauncher.launch(arrayOf("application/json")) }.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("导入所有数据", Modifier.weight(1f), color = MainText, fontSize = 16.sp)
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F0)))
                            Row(Modifier.fillMaxWidth().height(51.dp).noRippleClickable { showClearDataDialog = true; sheet = SheetType.NONE }.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("清空所有数据", color = Color(0xFFFF3B30), fontSize = 16.sp)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    SheetType.SORT -> SheetContent("视频排序") {
                        SheetAction("按添加时间排序") { sort = SortMode.ADDED; sheet = SheetType.NONE }
                        SheetAction("按文件名排序") { sort = SortMode.NAME; sheet = SheetType.NONE }
                    }
                    SheetType.GRID -> SheetContent("视图模式") {
                        SheetAction("2列大图（舒适）") { compact = false; sheet = SheetType.NONE }
                        SheetAction("3列小图（紧凑）") { compact = true; sheet = SheetType.NONE }
                    }
                    SheetType.CATEGORIES -> Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        SheetHeading("分类管理")
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                                .heightIn(max = 350.dp)
                                .shadow(5.dp, RoundedCornerShape(18.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White),
                            userScrollEnabled = false
                        ) {
                            itemsIndexed(categories, key = { _, cat -> cat }) { index, cat ->
                                val isDefault = cat == "全部分类" || cat == "我的最爱"
                                var dragRemainder by remember(cat) { mutableFloatStateOf(0f) }
                                val latestCategories = rememberUpdatedState(categories)
                                Row(
                                    Modifier.fillMaxWidth().height(50.dp)
                                        .animateItemPlacement(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                                        .then(
                                            Modifier.pointerInput(cat) {
                                                detectDragGesturesAfterLongPress(
                                                    onDragEnd = { dragRemainder = 0f },
                                                    onDragCancel = { dragRemainder = 0f }
                                                ) { change, dragAmount ->
                                                    change.consume()
                                                    dragRemainder += dragAmount.y
                                                    val threshold = 25.dp.toPx()
                                                    val step = when {
                                                        dragRemainder >= threshold -> 1
                                                        dragRemainder <= -threshold -> -1
                                                        else -> 0
                                                    }
                                                    if (step != 0) {
                                                        val currentCategories = latestCategories.value
                                                        val from = currentCategories.indexOf(cat)
                                                        val to = (from + step).coerceIn(0, currentCategories.lastIndex)
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
                                            }
                                        )
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.DragHandle,
                                        contentDescription = null,
                                        tint = Color(0xFF999999),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        if (cat == "我的最爱") "我的收藏" else cat,
                                        Modifier.weight(1f).padding(start = 12.dp),
                                        color = MainText,
                                        fontSize = 15.sp
                                    )
                                    if (isDefault) {
                                        Text(
                                            "默认",
                                            color = Color(0xFFAAAAAA),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp)
                                        )
                                    } else {
                                        Text(
                                            "删除",
                                            color = Color(0xFFE65C5C),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.noRippleClickable {
                                                repository.saveCategories(categories - cat)
                                                videos.filter { it.category == cat }.forEach { repository.update(it.copy(category = "全部分类")) }
                                                refresh()
                                            }.padding(start = 12.dp, top = 10.dp, bottom = 10.dp)
                                        )
                                    }
                                }
                                if (index < categories.lastIndex) {
                                    Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFFF0F0F0)))
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(
                            Modifier.fillMaxWidth()
                                .height(52.dp)
                                .shadow(5.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                newCategory,
                                { newCategory = it },
                                Modifier.weight(1f)
                                    .onFocusChanged { sheetKeyboardUp = it.isFocused }
                                    .padding(horizontal = 12.dp),
                                singleLine = true,
                                decorationBox = { inner -> if (newCategory.isEmpty()) Text("新分类名称", color = MutedText); inner() },
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    lineHeight = 18.sp,
                                    color = MainText,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                            Box(
                                Modifier.size(36.dp).clip(CircleShape).background(ThemeTeal)
                                    .noRippleClickable {
                                        val name = newCategory.trim()
                                        if (name.isNotEmpty() && name !in categories) {
                                            repository.saveCategories(categories + name); newCategory = ""; refresh()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Add, "添加分类", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                    SheetType.RECORD_SEARCH -> Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        SheetHeading("搜索观影记录")
                        BasicTextField(recordSearch, { recordSearch = it }, Modifier.fillMaxWidth().onFocusChanged { sheetKeyboardUp = it.isFocused }.background(Color.White, RoundedCornerShape(14.dp)).padding(15.dp), singleLine = true, decorationBox = { inner -> if (recordSearch.isEmpty()) Text("搜索视频名称或观影感想", color = MutedText); inner() })
                        videos.filter { (it.status == "watched" || it.progress >= 100f) && (recordSearch.isBlank() || it.name.contains(recordSearch, true) || it.review.contains(recordSearch, true)) }
                            .take(30).forEach { video ->
                                SheetAction(displayVideoName(video.name)) {
                                    selected = video
                                    editReview = video.review
                                    editRating = video.rating
                                    parentSheet = sheet
                                    sheet = SheetType.NOTE
                                }
                            }
                        Spacer(Modifier.height(24.dp))
                    }
                    SheetType.RECORD_SORT -> SheetContent("记录排序") {
                        SheetAction("按看完时间排序") { recordSortByRating = false; sheet = SheetType.NONE }
                        SheetAction("按我的评分排序") { recordSortByRating = true; sheet = SheetType.NONE }
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
                                .border(1.dp, Color.Black.copy(alpha = .05f), RoundedCornerShape(16.dp))
                                .padding(15.dp),
                            singleLine = true,
                            decorationBox = { inner ->
                                if (search.isEmpty()) Text("搜索本地视频...", color = MutedText, fontSize = 15.sp)
                                inner()
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, color = MainText)
                        )
                        val searchResults = remember(search, videos) {
                            val query = search.trim()
                            if (query.isBlank()) emptyList()
                            else videos.asSequence()
                                .filter { displayVideoName(it.name).contains(query, true) }
                                .distinctBy { displayVideoName(it.name).trim().lowercase(Locale.getDefault()) }
                                .take(30)
                                .toList()
                        }
                        searchResults.forEach { video ->
                            key(video.id) {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(tween(260, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f))) +
                                        androidx.compose.animation.slideInVertically(tween(260, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, 0f, .2f, 1f))) { it / 10 }
                                ) {
                                    Surface(
                                        Modifier.fillMaxWidth().padding(top = 12.dp)
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
                                            Text(
                                                displayVideoName(video.name),
                                                color = MainText,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                lineHeight = 21.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
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
                                    fontSize = 15.sp,
                                    lineHeight = 18.sp,
                                    color = MainText,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Box(
                            Modifier.fillMaxWidth()
                                .height(50.dp)
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
                        Spacer(Modifier.height(24.dp))
                    }
                    SheetType.MOVE -> SheetContent("移动到分类") {
                        categories.filter { it != "全部分类" }.forEach { cat ->
                            SheetAction(cat) { selected?.let { repository.moveCategory(it.id, cat) }; refresh(); sheet = SheetType.NONE }
                        }
                    }
                    SheetType.NOTE -> Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        SheetHeading("编辑观影记录")
                        Row(Modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                            (1..5).forEach { n ->
                                Text(if (n <= editRating) "★" else "★", color = if (n <= editRating) Color(0xFFFFB800) else Color(0xFFE0E0E0), fontSize = 32.sp, modifier = Modifier.noRippleClickable { editRating = n })
                            }
                        }
                        BasicTextField(editReview, { editReview = it }, Modifier.fillMaxWidth().padding(bottom = 20.dp).height(120.dp).onFocusChanged { sheetKeyboardUp = it.isFocused }.shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(16.dp)).background(Color.White).border(1.dp, Color.Black.copy(alpha = .05f), RoundedCornerShape(16.dp)).padding(15.dp), decorationBox = { inner -> if (editReview.isEmpty()) Text("写下你此刻的观影感受...", color = MutedText); inner() }, textStyle = TextStyle(fontSize = 15.sp, color = MainText, lineHeight = 22.sp))
                        Box(Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(16.dp)).background(ThemeTeal).noRippleClickable { selected?.let { repository.update(it.copy(rating = editRating, review = editReview)) }; refresh(); sheet = SheetType.NONE }.padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                            Text("保存记录", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                    SheetType.CLEAR_RECENT -> SheetContent("清空播放记录？") {
                        Text("只清除观看进度，不会删除片库中的视频。", color = MutedText, modifier = Modifier.padding(bottom = 12.dp))
                        SheetAction("确认清空", danger = true) { repository.clearRecent(); refresh(); sheet = SheetType.NONE }
                    }
                    SheetType.CLEAR_DATA -> Unit
                    SheetType.DELETE_RECENT -> SheetContent("删除这条播放记录？") {
                        SheetAction("删除播放记录", danger = true) { selected?.let { repository.deleteRecent(it.id) }; refresh(); sheet = SheetType.NONE }
                    }
                    SheetType.NONE -> Unit
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
                val menuHeight = 42.dp * 6 + 8.dp
                val menuX = (menuAnchorLeft + menuAnchorWidth / 2f - menuWidth / 2f)
                    .coerceIn(16.dp, (maxWidth - menuWidth - 16.dp).coerceAtLeast(16.dp))
                val safeTop = 60.dp
                val safeBottom = 80.dp
                val maxMenuY = (maxHeight - menuHeight - safeBottom).coerceAtLeast(safeTop)
                val menuY = if (menuAnchorBottom + menuHeight + safeBottom <= maxHeight) menuAnchorBottom + 8.dp
                    else (menuAnchorTop - menuHeight - 8.dp).coerceIn(safeTop, maxMenuY)
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .4f * contextMenuProgress)).noRippleClickable { contextMenuOpen = false })
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
                    ContextMenuAction(if (video.isFav) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, if (video.isFav) "取消收藏" else "加入收藏") { repository.favorite(video.id); refresh(); contextMenuOpen = false }
                    ContextMenuAction(Icons.Outlined.FolderOpen, "移动到分类") { selected = video; parentSheet = SheetType.NONE; contextMenuOpen = false; sheet = SheetType.MOVE }
                    ContextMenuAction(Icons.Outlined.Edit, "重命名") { selected = video; editName = displayVideoName(video.name); parentSheet = SheetType.NONE; contextMenuOpen = false; sheet = SheetType.RENAME }
                    ContextMenuAction(Icons.Outlined.Image, "更换封面") { selected = video; contextMenuOpen = false; coverPicker.launch("image/*") }
                    ContextMenuAction(Icons.Outlined.DeleteOutline, "移出片库", danger = true) { repository.delete(video.id); refresh(); contextMenuOpen = false }
                }
            }
        }
        if (showClearDataDialog || clearDialogProgress > 0f) {
            Box(Modifier.fillMaxSize().zIndex(5f).background(Color.Black.copy(alpha = .4f * clearDialogProgress)).noRippleClickable { showClearDataDialog = false }, contentAlignment = Alignment.Center) {
                androidx.compose.animation.AnimatedVisibility(visible = showClearDataDialog,
                    enter = androidx.compose.animation.scaleIn(initialScale = .8f, animationSpec = tween(340, easing = androidx.compose.animation.core.CubicBezierEasing(.34f, 1.56f, .64f, 1f))) + fadeIn(tween(340)),
                    exit = androidx.compose.animation.scaleOut(targetScale = .9f, animationSpec = tween(240, easing = androidx.compose.animation.core.CubicBezierEasing(.4f, 0f, 1f, 1f))) + fadeOut(tween(240))) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp).shadow(18.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(Color.White).noRippleClickable { }.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("清空全部数据？", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = MainText)
                        Text("此操作会删除片库条目、分类、封面和本地记录，且无法撤销。", fontSize = 14.sp, lineHeight = 21.sp, color = MutedText, modifier = Modifier.padding(top = 12.dp, bottom = 22.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF2F2F4)).noRippleClickable { showClearDataDialog = false }, contentAlignment = Alignment.Center) { Text("取消", color = MainText, fontWeight = FontWeight.Medium) }
                            Box(Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFFF3B30)).noRippleClickable { repository.clearAll(); refresh(); showClearDataDialog = false }, contentAlignment = Alignment.Center) { Text("确认清空", color = Color.White, fontWeight = FontWeight.SemiBold) }
                        }
                    }
                }
            }
        }
        if (showImportDataDialog || importDialogProgress > 0f) {
            Box(Modifier.fillMaxSize().zIndex(7f).background(Color.Black.copy(alpha = .4f * importDialogProgress)).noRippleClickable { showImportDataDialog = false; pendingImportUri = null }, contentAlignment = Alignment.Center) {
                androidx.compose.animation.AnimatedVisibility(visible = showImportDataDialog,
                    enter = androidx.compose.animation.scaleIn(initialScale = .8f, animationSpec = tween(340, easing = androidx.compose.animation.core.CubicBezierEasing(.34f, 1.56f, .64f, 1f))) + fadeIn(tween(340)),
                    exit = androidx.compose.animation.scaleOut(targetScale = .9f, animationSpec = tween(240, easing = androidx.compose.animation.core.CubicBezierEasing(.4f, 0f, 1f, 1f))) + fadeOut(tween(240))) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp).shadow(18.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(Color.White).noRippleClickable { }.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("导入并覆盖数据？", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = MainText)
                        Text("导入会覆盖当前片库中的全部数据，且无法撤销。确定继续吗？", fontSize = 14.sp, lineHeight = 21.sp, color = MutedText, modifier = Modifier.padding(top = 12.dp, bottom = 22.dp))
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
            Surface(Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp), shape = CircleShape, color = Color(0xDD222222)) {
                Text(toast, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp))
            }        }
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
            .shadow(8.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black.copy(alpha = .06f), spotColor = Color.Black.copy(alpha = .06f))
            .height(56.dp)
            .hazeEffect(state = hazeState, style = HazeStyle(backgroundColor = PageBg, tints = emptyList(), blurRadius = 20.dp, noiseFactor = 0f))
            .clip(RoundedCornerShape(28.dp))
            .background(Glass)
            .border(1.dp, Color.Black.copy(alpha = .05f), RoundedCornerShape(28.dp))
            .padding(5.dp)
    ) {
        val slot = maxWidth / 4
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomePage(
    videos: List<VideoItem>, weeklyWatchTimeMs: Long, hazeState: HazeState,
    listState: LazyListState,
    onSettings: () -> Unit, onVideo: (VideoItem) -> Unit
) {
    val continuing = remember(videos) { videos.filter { it.lastPlayedAt > 0L && it.progress < 100f }.take(4) }
    val recent = remember(videos) { videos.sortedByDescending { it.addedAt }.take(6) }
    var continueExpanded by remember { mutableStateOf(true) }
    var recentExpanded by remember { mutableStateOf(true) }
    val titleAlphaState = remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 0f
            else 1f - (listState.firstVisibleItemScrollOffset / 200f).coerceIn(0f, 1f)
        }
    }
    val showTopBar by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 20
        }
    }
    val homeHeaderHeightPx = remember { mutableIntStateOf(0) }
    val homeStatsHeightPx = remember { mutableIntStateOf(0) }
    val homeGapPx = with(LocalDensity.current) { 25.dp.toPx() }
    val homeOffscreenPx = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val homeScrollDistancePx = remember {
        derivedStateOf {
            when (listState.firstVisibleItemIndex) {
                0 -> listState.firstVisibleItemScrollOffset.toFloat()
                1 -> (homeHeaderHeightPx.intValue + listState.firstVisibleItemScrollOffset).toFloat()
                2 -> (homeHeaderHeightPx.intValue + homeStatsHeightPx.intValue + homeGapPx + listState.firstVisibleItemScrollOffset).toFloat()
                3 -> (homeHeaderHeightPx.intValue + homeStatsHeightPx.intValue + homeGapPx + listState.firstVisibleItemScrollOffset).toFloat()
                else -> (homeHeaderHeightPx.intValue + homeStatsHeightPx.intValue + homeGapPx + homeOffscreenPx).toFloat()
            }
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
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).clipToBounds(), contentPadding = PaddingValues(bottom = 112.dp)) {
            item(key = "home-large-title") {
                Box(
                    Modifier.zIndex(1f)
                        .onGloballyPositioned { homeHeaderHeightPx.intValue = it.size.height }
                        .graphicsLayer { alpha = 0f }
                ) {
                    Header("首页", onSettings)
                }
            }
            item(key = "home-stat-panel") {
                Column(
                    Modifier.fillMaxWidth()
                        .onGloballyPositioned { homeStatsHeightPx.intValue = it.size.height }
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
                            .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Glass)
                            .border(1.dp, Color.Black.copy(alpha = .05f), RoundedCornerShape(20.dp))
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
            item(key = "home-stat-panel-gap") { Spacer(Modifier.height(25.dp)) }
            item(key = "home-continue-heading") { SectionHeading("继续观看", continueExpanded, onToggle = { continueExpanded = !continueExpanded }) }
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
                        LazyRow(state = heroListState, flingBehavior = gentleSnapFling, modifier = Modifier.padding(top = 2.dp, bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                            itemsIndexed(continuing, key = { _, video -> video.id }) { _, video ->
                                Box(
                                    Modifier.width(280.dp).height(160.dp).shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = .10f), spotColor = Color.Black.copy(alpha = .10f))
                                        .clip(RoundedCornerShape(20.dp)).background(PosterBrush(video.bgIndex)).noRippleClickable { onVideo(video) }
                                ) {
                                    Thumbnail(video, Modifier.fillMaxSize())
                                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colorStops = arrayOf(0f to Color.Transparent, .4f to Color.Black.copy(alpha = .2f), 1f to Color.Black.copy(alpha = .8f)))))
                                    Column(Modifier.align(Alignment.BottomStart).padding(15.dp)) {
                                        Text(displayVideoName(video.name), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
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
                    chevronExpandDurationMs = 320,
                    chevronCollapseDurationMs = 240,
                    chevronExpandEasing = androidx.compose.animation.core.FastOutSlowInEasing,
                    chevronCollapseEasing = androidx.compose.animation.core.FastOutSlowInEasing,
                    onToggle = { recentExpanded = !recentExpanded }
                )
            }
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

        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                .graphicsLayer { translationY = -homeScrollDistancePx.value }
        ) {
            Box(Modifier.zIndex(1f).graphicsLayer { alpha = titleAlphaState.value }) {
                Header("首页", onSettings)
            }
            Column(
                Modifier.fillMaxWidth()
                    .background(PageBg)
                    .hazeEffect(state = hazeState, style = HazeStyle(backgroundColor = PageBg, tints = emptyList(), blurRadius = 10.dp, noiseFactor = 0f))
                    .drawBehind {
                        val bleed = 20.dp.toPx()
                        drawRect(PageBg, topLeft = Offset(-bleed, 0f), size = Size(size.width + bleed * 2f, size.height))
                    }
            ) {
                Row(
                    Modifier.fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Glass)
                        .border(1.dp, Color.Black.copy(alpha = .05f), RoundedCornerShape(20.dp))
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
            Spacer(Modifier.height(25.dp))
        }

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
                Text("首页", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MainText)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LibraryPage(
    hazeState: HazeState,
    videos: List<VideoItem>, categories: List<String>, status: String, category: String, compact: Boolean,
    onStatus: (String) -> Unit, onCategory: (String) -> Unit, onGrid: () -> Unit, onSort: () -> Unit,
    onCategories: () -> Unit, onLongVideo: (VideoItem, Rect) -> Unit, onPlay: (VideoItem) -> Unit,
    contextMenuVideo: VideoItem?, contextMenuReturnProgress: Float, onContextMenuBoundsChanged: (Rect) -> Unit,
    onViewportBoundsChanged: (Rect) -> Unit, onStickyHeaderBoundsChanged: (Rect) -> Unit
) {
    val listState = rememberLazyListState()
    val titleAlphaState = remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 0f
            else 1f - (listState.firstVisibleItemScrollOffset / 200f).coerceIn(0f, 1f)
        }
    }
    val filtered = remember(videos, status, category) {
        videos.filter {
            (status == "全部" || (status == "在看" && it.progress in .01f..99.99f) || (status == "已看" && it.progress >= 100f)) &&
                (category == "全部分类" || (category == "我的最爱" && it.isFav) || it.category == category)
        }
    }
    val columns = if (compact) 3 else 2
    val gap = if (compact) 10.dp else 15.dp
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
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().background(PageBg).clipToBounds(), contentPadding = PaddingValues(bottom = 112.dp)) {
            item {
                Row(
                    Modifier.fillMaxWidth()
                        .onGloballyPositioned { libraryTitleHeightPx.intValue = it.size.height }
                        .graphicsLayer { alpha = 0f }
                        .padding(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 15.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("片库", Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
                    ActionIcon(FrameTagIcon, "分类管理", onCategories)
                    ActionIcon(FrameSortIcon, "排序", onSort)
                    ActionIcon(FrameGridIcon, "布局管理", onGrid)
                }
            }
            stickyHeader {
                LibraryFilterHeader(
                    categories = categories, status = status, category = category,
                    onStatus = onStatus, onCategory = onCategory,
                    alpha = 0f,
                    onBoundsChanged = {}
                )
            }
            if (filtered.isEmpty()) {
                item { EmptyHint(if (videos.isEmpty()) "片库还是空的，点击右下角导入视频" else "没有符合条件的视频") }
            } else {
                item { Spacer(Modifier.height(5.dp)) }
                filtered.chunked(columns).forEach { rowVideos ->
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(gap),
                            verticalAlignment = Alignment.Top
                        ) {
                            rowVideos.forEach { video ->
                                PosterCard(
                                    video, compact, onPlay, { bounds -> onLongVideo(video, bounds) },
                                    Modifier.weight(1f),
                                    alpha = 1f,
                                    onBoundsChanged = if (contextMenuVideo?.id == video.id) onContextMenuBoundsChanged else null
                                )
                            }
                            repeat(columns - rowVideos.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                    item { Spacer(Modifier.height(gap)) }
                }
            }
        }
        Row(
            Modifier.fillMaxWidth()
                .graphicsLayer {
                    translationY = -libraryTitleScrollDistancePx.value
                    alpha = titleAlphaState.value
                }
                .padding(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("片库", Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
            ActionIcon(FrameTagIcon, "分类管理", onCategories)
            ActionIcon(FrameSortIcon, "排序", onSort)
            ActionIcon(FrameGridIcon, "布局管理", onGrid)
        }
        LibraryFilterHeader(
            categories = categories, status = status, category = category,
            onStatus = onStatus, onCategory = onCategory,
            alpha = 1f, onBoundsChanged = onStickyHeaderBoundsChanged,
            modifier = Modifier.align(Alignment.TopCenter).zIndex(2f)
                .graphicsLayer {
                    translationY = if (listState.firstVisibleItemIndex == 0) {
                        (libraryTitleHeightPx.intValue - listState.firstVisibleItemScrollOffset).coerceAtLeast(0).toFloat()
                    } else 0f
                }
        )
    }
}

@Composable
private fun LibraryFilterHeader(
    categories: List<String>, status: String, category: String,
    onStatus: (String) -> Unit, onCategory: (String) -> Unit,
    alpha: Float, onBoundsChanged: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
            .onGloballyPositioned { onBoundsChanged(it.boundsInRoot()) }
            .background(PageBg)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(top = 15.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            listOf("全部", "在看", "已看").forEach { value ->
                Column(Modifier.noRippleClickable { onStatus(value) }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value, color = if (status == value) MainText else MutedText, fontWeight = if (status == value) FontWeight.Bold else FontWeight.Medium, fontSize = 16.sp)
                    if (status == value) Spacer(Modifier.padding(top = 5.dp).width(20.dp).height(3.dp).clip(CircleShape).background(ThemeTeal))
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 10.dp).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            categories.forEach { value ->
                Surface(
                    Modifier.shadow(4.dp, CircleShape, ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                        .noRippleClickable { onCategory(value) },
                    shape = CircleShape,
                    color = if (category == value) ThemeTeal else Color.White,
                    shadowElevation = 0.dp,
                ) {
                    val label = when (value) { "全部分类" -> "全部分类"; "我的最爱" -> "收藏"; else -> value }
                    Text(label, color = if (category == value) Color.White else Color(0xFF666666), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecentPage(hazeState: HazeState, videos: List<VideoItem>, onVideo: (VideoItem) -> Unit, onClear: () -> Unit, onMore: (VideoItem) -> Unit) {
    val listState = rememberLazyListState()
    val titleAlphaState = remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 0f
            else 1f - (listState.firstVisibleItemScrollOffset / 200f).coerceIn(0f, 1f)
        }
    }
    val recentTitleHeightPx = remember { mutableIntStateOf(0) }
    val recentOffscreenPx = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val recentTitleScrollDistancePx = remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else {
                recentTitleHeightPx.intValue + recentOffscreenPx
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
    val nextHeaderOffsetState = remember(activeGroupIndex, groupHeaderIndices) {
        derivedStateOf {
            val nextIndex = groupHeaderIndices.getOrNull(activeGroupIndex + 1)
            if (nextIndex == null) Int.MAX_VALUE
            else listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == nextIndex }?.offset ?: Int.MAX_VALUE
        }
    }
    Box(Modifier.fillMaxSize().clipToBounds()) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().background(PageBg).clipToBounds(), contentPadding = PaddingValues(bottom = 112.dp)) {
            item(key = "recent-large-title") {
                Row(
                    Modifier.fillMaxWidth()
                        .onGloballyPositioned { recentTitleHeightPx.intValue = it.size.height }
                        .graphicsLayer { alpha = 0f }
                        .padding(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 15.dp),
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
                    stickyHeader {
                        RecentDateHeader(
                            label,
                            alpha = if (activeGroupIndex == groupIndex) 0f else 1f,
                            onHeightChanged = { headerHeightPx = it }
                        )
                    }
                    item { Spacer(Modifier.height(10.dp)) }
                    items(list, key = { it.id }) { video ->
                        RecentHistoryCard(
                            video,
                            onClick = { onVideo(video) },
                            onMore = { onMore(video) },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                    item { Spacer(Modifier.height(3.dp)) }
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
                .padding(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 15.dp),
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
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        Text(
            label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666),
            modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 15.dp, bottom = 5.dp)
        )
    }
}

@Composable
private fun RecentHistoryCard(video: VideoItem, onClick: () -> Unit, onMore: () -> Unit, modifier: Modifier = Modifier) {
    val playedMs = (video.durationMs * video.progress.coerceIn(0f, 100f) / 100f).toLong()
    val watchedDate = SimpleDateFormat("M/d/yyyy", Locale.getDefault()).format(Date(video.lastPlayedAt))
    Row(
        modifier.fillMaxWidth().padding(bottom = 12.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(16.dp))
            .background(Color.White).noRippleClickable(onClick).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp), verticalAlignment = Alignment.Top
    ) {
        Box(Modifier.size(120.dp, 84.dp).clip(RoundedCornerShape(10.dp)).background(PosterBrush(video.bgIndex))) {
            Thumbnail(video, Modifier.fillMaxSize())
        }
        Box(Modifier.weight(1f).height(84.dp)) {
            Column(Modifier.fillMaxSize().padding(end = 24.dp), verticalArrangement = Arrangement.SpaceBetween) {                Text(
                    displayVideoName(video.name),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF222222),
                    lineHeight = 21.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = FontFamily.SansSerif, platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(FrameTimelineClockIcon, null, tint = Color(0xFF888888), modifier = Modifier.size(12.dp))
                        Text(formatClock(video.durationMs), fontSize = 11.sp, lineHeight = 13.sp, color = Color(0xFF999999), maxLines = 1, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(FrameTimelinePlayIcon, null, tint = Color(0xFF888888), modifier = Modifier.size(12.dp))
                        Text("${formatClock(playedMs)} · ${watchedDate}", fontSize = 11.sp, lineHeight = 13.sp, color = Color(0xFF999999), maxLines = 2, overflow = TextOverflow.Clip, softWrap = true, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
                    }
                }
            }
            Box(Modifier.align(Alignment.BottomEnd).size(24.dp).noRippleClickable(onClick = onMore), contentAlignment = Alignment.BottomCenter) {
                Icon(Icons.Outlined.MoreVert, "更多", tint = Color(0xFFD0D0D0), modifier = Modifier.size(16.dp))
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecordsPage(
    hazeState: HazeState,
    videos: List<VideoItem>,
    onSearch: () -> Unit,
    onSort: () -> Unit,
    onEdit: (VideoItem) -> Unit
) {
    val listState = rememberLazyListState()
    val titleAlphaState = remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 0f
            else 1f - (listState.firstVisibleItemScrollOffset / 200f).coerceIn(0f, 1f)
        }
    }
    val showTopBar by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 20
        }
    }
    val watchedCount = remember(videos) { videos.size }
    val totalHours = remember(videos) { videos.sumOf { it.durationMs } / 3600000 }
    val reviewedCount = remember(videos) { videos.count { it.review.isNotBlank() } }
    val recordsTitleHeightPx = remember { mutableIntStateOf(0) }
    val recordsStatsHeightPx = remember { mutableIntStateOf(0) }
    val recordsGapPx = with(LocalDensity.current) { 26.dp.toPx() }
    val recordsOffscreenPx = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val recordsScrollDistancePx = remember(videos.isNotEmpty()) {
        derivedStateOf {
            when (listState.firstVisibleItemIndex) {
                0 -> listState.firstVisibleItemScrollOffset.toFloat()
                1 -> (recordsTitleHeightPx.intValue + listState.firstVisibleItemScrollOffset).toFloat()
                2 -> (recordsTitleHeightPx.intValue + recordsStatsHeightPx.intValue +
                    (if (videos.isEmpty()) 0f else recordsGapPx) + listState.firstVisibleItemScrollOffset).toFloat()
                else -> recordsTitleHeightPx.intValue + recordsStatsHeightPx.intValue + recordsOffscreenPx
            }
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
                Text("记录", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MainText, modifier = Modifier.graphicsLayer { alpha = 0f })
            }
        }
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().clipToBounds(), contentPadding = PaddingValues(bottom = 112.dp)) {
            item(key = "records-large-title") {
                Row(
                    Modifier.fillMaxWidth().zIndex(1f)
                        .onGloballyPositioned { recordsTitleHeightPx.intValue = it.size.height }
                        .graphicsLayer { alpha = 0f }
                        .padding(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("记录", Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ActionIcon(FrameSearchIcon, "搜索观影感想", onSearch)
                        ActionIcon(FrameSortIcon, "记录排序", onSort)
                    }
                }
            }
        item {
            Column(
                Modifier.fillMaxWidth()
                    .onGloballyPositioned { recordsStatsHeightPx.intValue = it.size.height }
                    .graphicsLayer { alpha = 0f }
                    .background(PageBg.copy(alpha = .95f))
            ) {
                Row(
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 10.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Glass)
                        .border(1.dp, Color.Black.copy(alpha = .05f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Stat("已看完", watchedCount.toString(), "部", Modifier.weight(1f))
                    Box(Modifier.width(1.dp).height(30.dp).background(Color.Black.copy(alpha = .05f)))
                    Stat("累计时长", totalHours.toString(), "h", Modifier.weight(1f))
                    Box(Modifier.width(1.dp).height(30.dp).background(Color.Black.copy(alpha = .05f)))
                    Stat("写下感想", reviewedCount.toString(), "篇", Modifier.weight(1f))
                }
            }
        }
        if (videos.isEmpty()) {
            item {
                EmptyHint("暂无已看完的视频")
                Spacer(Modifier.height(20.dp))
            }
        } else {
            item { Spacer(Modifier.height(26.dp)) }
            items(videos, key = { it.id }) { video ->
                Column(
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp)
                        .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .noRippleClickable { onEdit(video) }
                        .padding(15.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(75.dp, 100.dp)
                                .shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = Color.Black.copy(alpha = .10f), spotColor = Color.Black.copy(alpha = .10f))
                                .clip(RoundedCornerShape(12.dp))
                                .background(PosterBrush(video.bgIndex))
                        ) { Thumbnail(video, Modifier.fillMaxSize()) }
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                displayVideoName(video.name),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF222222),
                                lineHeight = 22.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(video.date + " 看完", fontSize = 12.sp, color = Color(0xFF999999))
                            Text(
                                "★".repeat(video.rating.coerceIn(0, 5)) + "☆".repeat(5 - video.rating.coerceIn(0, 5)),
                                color = if (video.rating > 0) Color(0xFFFFB800) else Color(0xFFE0E0E0),
                                fontSize = 14.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                    val hasReview = video.review.isNotBlank()
                    Row(
                        Modifier.fillMaxWidth()
                            .height(androidx.compose.foundation.layout.IntrinsicSize.Min)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF9F9FB))
                    ) {
                        Box(
                            Modifier.width(3.dp).fillMaxHeight()
                                .background(if (hasReview) ThemeTeal else Color(0xFFE0E0E0))
                        )
                        Text(
                            video.review.ifBlank { "点击添加你的观影感想..." },
                            color = if (hasReview) Color(0xFF555555) else Color(0xFFB0B0B0),
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            fontStyle = if (hasReview) androidx.compose.ui.text.font.FontStyle.Normal else androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.weight(1f).padding(12.dp)
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
        }

        Column(
            Modifier.fillMaxWidth()
                .graphicsLayer { translationY = -recordsScrollDistancePx.value }
        ) {
            Row(
                Modifier.fillMaxWidth().zIndex(1f).graphicsLayer { alpha = titleAlphaState.value }
                    .padding(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("记录", Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionIcon(FrameSearchIcon, "搜索观影感想", onSearch)
                    ActionIcon(FrameSortIcon, "记录排序", onSort)
                }
            }
            Column(Modifier.fillMaxWidth().background(PageBg.copy(alpha = .95f))) {
                Row(
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 10.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Glass)
                        .border(1.dp, Color.Black.copy(alpha = .05f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Stat("已看完", watchedCount.toString(), "部", Modifier.weight(1f))
                    Box(Modifier.width(1.dp).height(30.dp).background(Color.Black.copy(alpha = .05f)))
                    Stat("累计时长", totalHours.toString(), "h", Modifier.weight(1f))
                    Box(Modifier.width(1.dp).height(30.dp).background(Color.Black.copy(alpha = .05f)))
                    Stat("写下感想", reviewedCount.toString(), "篇", Modifier.weight(1f))
                }
            }
            if (videos.isNotEmpty()) Spacer(Modifier.height(26.dp))
        }

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
                Text("记录", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MainText)
            }
        }
    }
}
@Composable
private fun NativePlayerOverlay(
    item: VideoItem, player: Player, speeds: List<Float>, speedIndex: Int, locked: Boolean, controlsVisible: Boolean,
    onClose: () -> Unit, onToggleControls: () -> Unit, onAutoHideControls: () -> Unit, onToggleLock: () -> Unit,
    onSpeed: () -> Unit, onOrientation: () -> Unit, onEnded: () -> Unit,
    onProgress: (Long, Long) -> Unit,
    onWatchTime: (Long) -> Unit
) {
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
    LaunchedEffect(controlsVisible, isPlaying, locked) {
        if (controlsVisible && isPlaying && !locked) {
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
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { it.player = player }
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
                    }
                }
            }
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
            visible = centerMessage.isNotEmpty() && !locked,
            modifier = Modifier.align(Alignment.Center).zIndex(10f),
            enter = fadeIn(tween(180)), exit = fadeOut(tween(180))
        ) {
            Box(Modifier.clip(CircleShape).background(Color.Black.copy(alpha = .68f)).padding(horizontal = 22.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
                Text(centerMessage, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
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

private fun displayVideoName(name: String): String = name.replace(
    Regex("\\.(mp4|m4v|mkv|avi|mov|wmv|flv|webm|3gp|3g2|ts|mts|m2ts|mpg|mpeg|mpe|mpv|ogv|ogg|asf|rm|rmvb|vob|divx|f4v|mxf|nsv|wtv|dvr-ms|amv|hevc|h264|h265)$", RegexOption.IGNORE_CASE), ""
)

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
private fun ActionIcon(icon: ImageVector, description: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(36.dp).noRippleClickable(onClick),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 1.dp,
        
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, description, Modifier.size(20.dp), tint = Color(0xFF333333))
        }
    }
}

@Composable
private fun Header(title: String, onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 60.dp, bottom = 15.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText, letterSpacing = .5.sp)
        Box(Modifier.size(36.dp).shadow(2.dp, CircleShape, ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(CircleShape).background(Color.White).noRippleClickable { onSettings() }, contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Settings, "设置", Modifier.size(20.dp), tint = MainText)
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
    chevronCollapseEasing: androidx.compose.animation.core.Easing = androidx.compose.animation.core.CubicBezierEasing(.4f, 0f, 1f, 1f)
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
        Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp)
            .then(if (onToggle != null) Modifier.clickable(interactionSource = interactionSource, indication = null) { onToggle() } else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(4.dp).height(16.dp).clip(CircleShape).background(ThemeTeal))
        Text(
            text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MainText,
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
    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp).shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(16.dp)).background(Color.White).noRippleClickable(onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(100.dp, 65.dp).clip(RoundedCornerShape(10.dp)).background(PosterBrush(video.bgIndex)), contentAlignment = Alignment.Center) {
            Thumbnail(video, Modifier.fillMaxSize())
            if (video.progress > 0f) Box(Modifier.align(Alignment.BottomStart).fillMaxWidth((video.progress / 100f).coerceIn(0f, 1f)).height(3.dp).background(ThemeTeal))
        }
        Column(Modifier.weight(1f).padding(start = 15.dp)) {
            Text(displayVideoName(video.name), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF222222), maxLines = 1, overflow = TextOverflow.Ellipsis)
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
    var cardBounds by remember { mutableStateOf(Rect.Zero) }
    Column(
        modifier.graphicsLayer { this.alpha = alpha }
            .onGloballyPositioned {
                cardBounds = it.boundsInRoot()
                onBoundsChanged?.invoke(cardBounds)
            }
            .noRippleCombinedClickable(onClick = { onPlay(video) }, onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onLong(cardBounds) })
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
        Text(displayVideoName(video.name), fontSize = if (compact) 12.sp else 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF222222), maxLines = if (compact) 1 else 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun Thumbnail(video: VideoItem, modifier: Modifier = Modifier) {
    val bitmap = remember(video.thumbPath) {
        video.thumbPath?.let { path -> runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull() }
    }
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
private fun EmptyHint(text: String, horizontalPadding: androidx.compose.ui.unit.Dp = 20.dp) {
    Text(text, color = MutedText, fontSize = 14.sp, modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding).padding(top = 16.dp, bottom = 20.dp))
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
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 0.dp, bottom = 25.dp)
    )
}

@Composable
private fun SheetAction(text: String, danger: Boolean = false, onClick: () -> Unit) {
    Surface(
        Modifier.fillMaxWidth().padding(bottom = 12.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
            .clip(RoundedCornerShape(16.dp)).noRippleClickable(onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 0.dp,
        ) {
        Text(
            text,
            color = if (danger) Color(0xFFFF3B30) else MainText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(18.dp)
        )
    }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().height(51.dp).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MainText, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Box(Modifier.size(44.dp, 24.dp).clip(CircleShape).background(if (checked) ThemeTeal else Color(0xFFE0E0E0)).noRippleClickable { onChecked(!checked) }) {
            val knobOffset by animateDpAsState(if (checked) 22.dp else 2.dp, tween(300), label = "setting-switch")
            Box(Modifier.align(Alignment.CenterStart).offset(x = knobOffset).size(20.dp).clip(CircleShape).background(Color.White))
        }
    }
}