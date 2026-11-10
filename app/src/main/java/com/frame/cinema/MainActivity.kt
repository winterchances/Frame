package com.frame.cinema

import android.app.Activity
import android.content.Intent
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
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
private val Glass = Color.White.copy(alpha = .88f)

private enum class MainPage(val label: String) { HOME("首页"), LIBRARY("片库"), RECENT("最近"), RECORDS("记录") }
private enum class SheetType { NONE, IMPORT, SETTINGS, SORT, GRID, CATEGORIES, SEARCH, RECORD_SEARCH, RECORD_SORT, VIDEO_ACTIONS, RENAME, MOVE, NOTE, CLEAR_RECENT, CLEAR_DATA, DELETE_RECENT }
private enum class SortMode { ADDED, NAME, RATING, WATCHED }

class MainActivity : ComponentActivity() {
    private var player: Player? = null
    private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null
    private lateinit var repository: NativeLibraryRepository
    private var backgroundPlaybackAllowed = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        controller.setMediaItem(MediaItem.fromUri(item.contentUri))
        controller.prepare()
        if (item.durationMs > 0 && item.progress > 0f) controller.seekTo((item.durationMs * item.progress / 100f).toLong())
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FrameNativeApp(
    repository: NativeLibraryRepository,
    player: Player,
    onPlay: (VideoItem) -> Unit,
    onBackgroundPlaybackChanged: (Boolean) -> Unit,
    onOrientation: (Boolean) -> Unit,
    onKeepAwake: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var page by remember { mutableStateOf(MainPage.HOME) }
    var sheet by remember { mutableStateOf(SheetType.NONE) }
    var sheetKeyboardUp by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<VideoItem?>(null) }
    var videos by remember { mutableStateOf(repository.videos()) }
    var weeklyWatchTimeMs by remember { mutableLongStateOf(repository.weeklyWatchTimeMs()) }
    var categories by remember { mutableStateOf(repository.categories()) }
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
        onKeepAwake(keepAwake)
    }
    var playing by remember { mutableStateOf<VideoItem?>(null) }
    var isLandscape by remember { mutableStateOf(false) }
    var speedIndex by remember { mutableIntStateOf(0) }
    val speeds = listOf(1f, 1.25f, 1.5f, 2f, .75f)
    var controlsVisible by remember { mutableStateOf(true) }
    var locked by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf("") }
    val sheetBlur by animateDpAsState(if (sheet == SheetType.NONE) 0.dp else 5.dp, tween(300), label = "sheet-backdrop-blur")

    fun refresh() { videos = repository.videos(); categories = repository.categories(); weeklyWatchTimeMs = repository.weeklyWatchTimeMs() }
    fun notify(message: String) { toast = message }
    fun playVideo(item: VideoItem) {
        speedIndex = 0
        controlsVisible = true
        locked = false
        playing = item
        onPlay(item)
    }

    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        val count = repository.importUris(uris) { refresh() }
        refresh()
        notify("已导入 $count 个视频")
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

    LaunchedEffect(toast) { if (toast.isNotBlank()) { delay(2100); toast = "" } }
    LaunchedEffect(sheet) { if (sheet == SheetType.NONE) sheetKeyboardUp = false }

    Box(
        Modifier.fillMaxSize().background(PageBg)
            .pointerInput(page, sheet, playing) {
                var horizontalDistance = 0f
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        if (sheet == SheetType.NONE && playing == null) {
                            horizontalDistance += dragAmount
                        }
                    },
                    onDragEnd = {
                        if (sheet == SheetType.NONE && playing == null && kotlin.math.abs(horizontalDistance) > 50f) {
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
        if (playing == null) {
            Scaffold(
                modifier = Modifier.blur(sheetBlur),
                containerColor = PageBg,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = { BottomPillNavigation(page) { page = it; showFab = false } }
            ) { insets ->
                Box(Modifier.fillMaxSize().padding(bottom = insets.calculateBottomPadding())) {
                    androidx.compose.animation.AnimatedContent(
                        targetState = page,
                        transitionSpec = {
                            androidx.compose.animation.slideInHorizontally(animationSpec = tween(260)) { it / 5 } +
                                androidx.compose.animation.fadeIn(tween(220)) togetherWith
                                androidx.compose.animation.slideOutHorizontally(animationSpec = tween(220)) { -it / 5 } +
                                androidx.compose.animation.fadeOut(tween(180))
                        },
                        label = "frame-page"
                    ) { target ->
                        when (target) {
                            MainPage.HOME -> HomePage(
                                videos = videos,
                                weeklyWatchTimeMs = weeklyWatchTimeMs,
                                onSettings = { sheet = SheetType.SETTINGS },
                                onVideo = { selected = it; playVideo(it) }
                            )
                            MainPage.LIBRARY -> LibraryPage(
                                when (sort) { SortMode.NAME -> videos.sortedBy { it.name.lowercase(Locale.getDefault()) }; else -> videos.sortedByDescending { it.addedAt } }, categories, status, category, compact,
                                { status = it }, { category = it },
                                { sheet = SheetType.GRID }, { sheet = SheetType.SORT },
                                { sheet = SheetType.CATEGORIES },
                                { selected = it; sheet = SheetType.VIDEO_ACTIONS },
                                { selected = it; playVideo(it) }
                            )
                            MainPage.RECENT -> RecentPage(
                                videos,
                                onVideo = { selected = it; playVideo(it) },
                                onClear = { sheet = SheetType.CLEAR_RECENT },
                                onMore = { selected = it; sheet = SheetType.DELETE_RECENT }
                            )
                            MainPage.RECORDS -> RecordsPage(
                                videos = videos.filter { it.status == "watched" || it.progress >= 100f }
                                    .let { list -> if (recordSortByRating) list.sortedByDescending { it.rating } else list.sortedByDescending { it.lastPlayedAt } }
                                    .filter { recordSearch.isBlank() || it.name.contains(recordSearch, true) || it.review.contains(recordSearch, true) },
                                onSearch = { sheet = SheetType.RECORD_SEARCH },
                                onSort = { sheet = SheetType.RECORD_SORT },
                                onEdit = {
                                    selected = it; editReview = it.review; editRating = it.rating
                                    sheet = SheetType.NOTE
                                }
                            )
                        }
                    }
                }
            }
            Column(
                Modifier.align(Alignment.BottomEnd).blur(sheetBlur).padding(end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(showFab) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FabAction(Icons.Outlined.Search, "搜索") { sheet = SheetType.SEARCH; showFab = false }
                        FabAction(Icons.Outlined.Add, "导入视频") { sheet = SheetType.IMPORT; showFab = false }
                    }
                }
                val fabRotation by animateFloatAsState(if (showFab) 45f else 0f, tween(300), label = "fab-rotation")
                Surface(
                    modifier = Modifier.size(56.dp).clickable { showFab = !showFab },
                    shape = CircleShape, color = Glass, shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(FrameHamburgerIcon, null, tint = Color.Unspecified, modifier = Modifier.size(20.dp).rotate(fabRotation))
                    }
                }
            }
        } else {
            NativePlayerOverlay(
                item = playing!!,
                player = player,
                speeds = speeds,
                speedIndex = speedIndex,
                locked = locked,
                controlsVisible = controlsVisible,
                onClose = {
                    playing?.let { repository.updatePlayback(it.id, player.currentPosition, player.duration.coerceAtLeast(0L)) }
                    player.pause()
                    playing = null
                    locked = false
                    controlsVisible = true
                    if (isLandscape) { isLandscape = false; onOrientation(false) }
                    refresh()
                },
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
                    playing?.let { repository.updatePlayback(it.id, player.currentPosition, player.duration.coerceAtLeast(0L), true) }
                    player.pause()
                    playing = null
                    locked = false
                    controlsVisible = true
                    if (isLandscape) { isLandscape = false; onOrientation(false) }
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

        AnimatedVisibility(
            visible = sheet != SheetType.NONE,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = .4f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { sheet = SheetType.NONE }
            ) {
                Column(
                    Modifier.align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * .85f)
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(PageBg)
                        .verticalScroll(rememberScrollState())
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) { }
                        .animateEnterExit(
                            enter = slideInVertically(
                                animationSpec = tween(300, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, .8f, .2f, 1f))
                            ) { it },
                            exit = slideOutVertically(
                                animationSpec = tween(300, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, .8f, .2f, 1f))
                            ) { it }
                        )
                        .padding(top = 25.dp, bottom = if (sheetKeyboardUp) 400.dp else 40.dp)
                ) {
                when (sheet) {
                    SheetType.IMPORT -> SheetContent("导入本地视频") {
                        SheetAction("从文件中选择视频") { importPicker.launch(arrayOf("video/*")) }
                        SheetAction("扫描整个文件夹") { folderPicker.launch(null) }
                    }
                    SheetType.SETTINGS -> Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
                        SheetHeading("设置")
                        Text("播放偏好", color = MutedText, fontSize = 13.sp)
                        SettingRow("允许后台播放", allowBackground) { allowBackground = it; onBackgroundPlaybackChanged(it); preferences.edit().putBoolean("allow_background", it).apply() }
                        SettingRow("保持屏幕常亮", keepAwake) { keepAwake = it; preferences.edit().putBoolean("keep_awake", it).apply(); onKeepAwake(it) }
                        SheetAction("清空全部本地数据", danger = true) { sheet = SheetType.CLEAR_DATA }
                        Spacer(Modifier.height(26.dp))
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
                        categories.forEach { cat ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cat, Modifier.weight(1f).padding(vertical = 10.dp), color = MainText)
                                if (cat !in listOf("全部分类", "我的最爱")) {
                                    TextButton(onClick = {
                                        repository.saveCategories(categories - cat)
                                        videos.filter { it.category == cat }.forEach { repository.update(it.copy(category = "全部分类")) }
                                        refresh()
                                    }) { Text("删除", color = Color(0xFFE65C5C)) }
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BasicTextField(newCategory, { newCategory = it }, Modifier.weight(1f).onFocusChanged { sheetKeyboardUp = it.isFocused }.background(Color.White, RoundedCornerShape(12.dp)).padding(14.dp), singleLine = true, decorationBox = { inner -> if (newCategory.isEmpty()) Text("新分类名称", color = MutedText); inner() })
                            TextButton(onClick = {
                                val name = newCategory.trim()
                                if (name.isNotEmpty() && name !in categories) {
                                    repository.saveCategories(categories + name); newCategory = ""; refresh()
                                }
                            }) { Text("添加") }
                        }
                        Spacer(Modifier.height(28.dp))
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
                                    sheet = SheetType.NOTE
                                }
                            }
                        Spacer(Modifier.height(24.dp))
                    }
                    SheetType.RECORD_SORT -> SheetContent("记录排序") {
                        SheetAction("按看完时间排序") { recordSortByRating = false; sheet = SheetType.NONE }
                        SheetAction("按我的评分排序") { recordSortByRating = true; sheet = SheetType.NONE }
                    }
                    SheetType.SEARCH -> Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        SheetHeading("搜索视频")
                        BasicTextField(search, { search = it }, Modifier.fillMaxWidth().onFocusChanged { sheetKeyboardUp = it.isFocused }.background(Color.White, RoundedCornerShape(14.dp)).padding(15.dp), singleLine = true, decorationBox = { inner -> if (search.isEmpty()) Text("输入视频名称", color = MutedText); inner() })
                        videos.filter { it.name.contains(search, true) }.take(30).forEach { v ->
                            SheetAction(v.name) { sheet = SheetType.NONE; selected = v; playVideo(v) }
                        }
                        Spacer(Modifier.height(28.dp))
                    }
                    SheetType.VIDEO_ACTIONS -> SheetContent(selected?.name ?: "视频操作") {
                        SheetAction("立即播放") { selected?.let { playVideo(it) }; sheet = SheetType.NONE }
                        SheetAction(if (selected?.isFav == true) "取消收藏" else "加入收藏") {
                            selected?.let { repository.favorite(it.id) }; refresh(); sheet = SheetType.NONE
                        }
                        SheetAction("移动到分类") { sheet = SheetType.MOVE }
                        SheetAction("重命名") { editName = selected?.name.orEmpty(); sheet = SheetType.RENAME }
                        SheetAction("更换封面") { coverPicker.launch("image/*") }
                        SheetAction("移出片库", danger = true) { selected?.let { repository.delete(it.id) }; refresh(); sheet = SheetType.NONE }
                    }
                    SheetType.RENAME -> Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        SheetHeading("重命名")
                        BasicTextField(editName, { editName = it }, Modifier.fillMaxWidth().onFocusChanged { sheetKeyboardUp = it.isFocused }.background(Color.White, RoundedCornerShape(12.dp)).padding(14.dp), singleLine = true)
                        SheetAction("保存") {
                            val newName = editName.trim()
                            if (newName.isEmpty()) notify("视频名称不能为空")
                            else { selected?.let { repository.rename(it.id, newName) }; refresh(); sheet = SheetType.NONE }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                    SheetType.MOVE -> SheetContent("移动到分类") {
                        categories.filter { it != "全部分类" }.forEach { cat ->
                            SheetAction(cat) { selected?.let { repository.moveCategory(it.id, cat) }; refresh(); sheet = SheetType.NONE }
                        }
                    }
                    SheetType.NOTE -> Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        SheetHeading("观影记录")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..5).forEach { n ->
                                Text(if (n <= editRating) "★" else "☆", color = Color(0xFFFFB800), fontSize = 28.sp, modifier = Modifier.clickable { editRating = n })
                            }
                        }
                        BasicTextField(editReview, { editReview = it }, Modifier.fillMaxWidth().onFocusChanged { sheetKeyboardUp = it.isFocused }.heightIn(min = 110.dp).background(Color.White, RoundedCornerShape(14.dp)).padding(14.dp), decorationBox = { inner -> if (editReview.isEmpty()) Text("写下你的观影感想…", color = MutedText); inner() })
                        SheetAction("保存记录") { selected?.let { repository.update(it.copy(rating = editRating, review = editReview)) }; refresh(); sheet = SheetType.NONE }
                        Spacer(Modifier.height(24.dp))
                    }
                    SheetType.CLEAR_RECENT -> SheetContent("清空播放记录？") {
                        Text("只清除观看进度，不会删除片库中的视频。", color = MutedText, modifier = Modifier.padding(bottom = 12.dp))
                        SheetAction("确认清空", danger = true) { repository.clearRecent(); refresh(); sheet = SheetType.NONE }
                    }
                    SheetType.CLEAR_DATA -> SheetContent("清空全部数据？") {
                        Text("此操作会删除片库条目、分类、封面和本地记录。", color = MutedText, modifier = Modifier.padding(bottom = 12.dp))
                        SheetAction("确认清空全部数据", danger = true) { repository.clearAll(); refresh(); sheet = SheetType.NONE }
                    }
                    SheetType.DELETE_RECENT -> SheetContent("删除这条播放记录？") {
                        SheetAction("删除播放记录", danger = true) { selected?.let { repository.deleteRecent(it.id) }; refresh(); sheet = SheetType.NONE }
                    }
                    SheetType.NONE -> Unit
                }
                }
            }
        }
        if (toast.isNotBlank()) {
            Surface(Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp), shape = CircleShape, color = Color(0xDD222222)) {
                Text(toast, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp))
            }
        }
    }
}

@Composable
private fun BottomPillNavigation(current: MainPage, onSelect: (MainPage) -> Unit) {
    val entries = listOf(
        MainPage.HOME to FrameHomeIcon,
        MainPage.LIBRARY to FrameLibraryIcon,
        MainPage.RECENT to FrameRecentIcon,
        MainPage.RECORDS to FrameRecordsIcon
    )
    BoxWithConstraints(Modifier.fillMaxWidth().padding(start = 16.dp, end = 78.dp, bottom = 16.dp).height(56.dp).clip(RoundedCornerShape(28.dp)).background(Glass).padding(4.dp)) {
        val slot = maxWidth / 4
        val target = slot * entries.indexOfFirst { it.first == current }
        val offset by animateDpAsState(target, tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing), label = "nav-indicator")
        Box(Modifier.offset(x = offset).width(slot).fillMaxHeight().clip(CircleShape).background(Color(0xFFE9E9EB)))
        Row(Modifier.fillMaxSize()) {
            entries.forEach { (item, icon) ->
                Column(Modifier.weight(1f).fillMaxHeight().clickable { onSelect(item) }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(icon, item.label, Modifier.size(20.dp), tint = MainText)
                    Text(item.label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MainText)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomePage(videos: List<VideoItem>, weeklyWatchTimeMs: Long, onSettings: () -> Unit, onVideo: (VideoItem) -> Unit) {
    val continuing = videos.filter { it.progress >= 1f && it.progress < 100f }.take(5)
    var continueExpanded by remember { mutableStateOf(true) }
    var recentExpanded by remember { mutableStateOf(true) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        item {
            Header("首页", onSettings)
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(Glass).padding(horizontal = 10.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Stat("视频总数", videos.size.toString(), "个")
                Box(Modifier.width(1.dp).height(30.dp).background(Color.Black.copy(alpha = .05f)))
                Stat("已看完", videos.count { it.progress >= 100f || it.status == "watched" }.toString(), "个")
                Box(Modifier.width(1.dp).height(30.dp).background(Color.Black.copy(alpha = .05f)))
                Stat("本周时长", (weeklyWatchTimeMs / 3600000L).toString(), "h")
            }
            Spacer(Modifier.height(15.dp))
        }
        stickyHeader {
            Box(Modifier.fillMaxWidth().background(PageBg.copy(alpha = .95f))) {
                SectionHeading("继续观看", continueExpanded) { continueExpanded = !continueExpanded }
            }
        }
        if (continueExpanded) {
            if (continuing.isEmpty()) item { EmptyHint("暂无观看记录") }
            else item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    continuing.forEach { video ->
                        Box(
                            Modifier.width(280.dp).height(160.dp)
                                .shadow(8.dp, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(PosterBrush(video.bgIndex))
                                .clickable { onVideo(video) }
                        ) {
                            Thumbnail(video, Modifier.fillMaxSize())
                            Box(
                                Modifier.fillMaxSize().background(
                                    Brush.verticalGradient(
                                        colorStops = arrayOf(
                                            0f to Color.Transparent,
                                            .4f to Color.Black.copy(alpha = .2f),
                                            1f to Color.Black.copy(alpha = .8f)
                                        )
                                    )
                                )
                            )
                            Column(Modifier.align(Alignment.BottomStart).padding(15.dp)) {
                                Text(displayVideoName(video.name), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("已看 ${video.progress.toInt()}%", color = Color.White.copy(alpha = .8f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                                Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = .2f))) {
                                    Box(Modifier.fillMaxWidth((video.progress / 100f).coerceIn(0f, 1f)).fillMaxHeight().background(ThemeTeal))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
        stickyHeader {
            Box(Modifier.fillMaxWidth().background(PageBg.copy(alpha = .95f))) {
                SectionHeading("最近添加", recentExpanded) { recentExpanded = !recentExpanded }
            }
        }
        if (recentExpanded) {
            if (videos.isEmpty()) item { EmptyHint("暂无视频，点击右下角添加") }
            else items(videos.sortedByDescending { it.addedAt }.take(4), key = { it.id }) { video ->
                VideoRow(video, "${video.date} 添加", { onVideo(video) })
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LibraryPage(
    videos: List<VideoItem>, categories: List<String>, status: String, category: String, compact: Boolean,
    onStatus: (String) -> Unit, onCategory: (String) -> Unit, onGrid: () -> Unit, onSort: () -> Unit,
    onCategories: () -> Unit, onLongVideo: (VideoItem) -> Unit, onPlay: (VideoItem) -> Unit
) {
    val filtered = videos.filter {
        (status == "全部" || (status == "在看" && it.progress in .01f..99.99f) || (status == "已看" && it.progress >= 100f)) &&
            (category == "全部分类" || (category == "我的最爱" && it.isFav) || it.category == category)
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(if (compact) 3 else 2),
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 15.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 15.dp)
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Row(
                Modifier.fillMaxWidth().padding(top = 60.dp, bottom = 15.dp),
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
            Column(
                Modifier.fillMaxWidth().background(PageBg.copy(alpha = .95f))
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(25.dp)
                ) {
                    listOf("全部", "在看", "已看").forEach { value ->
                        Column(Modifier.clickable { onStatus(value) }, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(value, color = if (status == value) MainText else MutedText, fontWeight = if (status == value) FontWeight.Bold else FontWeight.Medium, fontSize = 16.sp)
                            if (status == value) Spacer(Modifier.padding(top = 5.dp).width(20.dp).height(3.dp).clip(CircleShape).background(ThemeTeal))
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 10.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categories.forEach { value ->
                        Surface(
                            Modifier.clickable { onCategory(value) },
                            shape = CircleShape,
                            color = if (category == value) ThemeTeal else Color.White,
                            shadowElevation = if (category == value) 3.dp else 1.dp
                        ) {
                            Text(value, color = if (category == value) Color.White else Color(0xFF666666), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                    }
                }
            }
        }
        if (filtered.isEmpty()) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                EmptyHint(if (videos.isEmpty()) "片库还是空的，点击右下角导入视频" else "没有符合条件的视频")
            }
        } else {
            items(filtered, key = { it.id }) { video ->
                PosterCard(video, compact, onPlay) { onLongVideo(video) }
            }
        }
    }
}@Composable
private fun RecentPage(videos: List<VideoItem>, onVideo: (VideoItem) -> Unit, onClear: () -> Unit, onMore: (VideoItem) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 60.dp, bottom = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("最近", Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
            ActionIcon(FrameTrashIcon, "清空播放记录", onClear)
        }
        val recentItems = videos.sortedByDescending { it.addedAt }
        if (recentItems.isEmpty()) EmptyHint("暂无播放记录")
        else LazyColumn {
            val today = SimpleDateFormat("M/d/yyyy", Locale.getDefault()).format(Date())
            val yesterday = SimpleDateFormat("M/d/yyyy", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000L))
            val groups = recentItems.groupBy { item ->
                when (item.date) { today -> "今天"; yesterday -> "昨天"; else -> "更早" }
            }
            groups.forEach { (label, list) ->
                stickyHeader {
                    Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF666666),
                        modifier = Modifier.fillMaxWidth().background(Color(0xF2F5F5F7))
                            .padding(top = 15.dp, bottom = 5.dp))
                }
                items(list, key = { it.id }) { video ->
                    RecentHistoryCard(video, onClick = { onVideo(video) }, onMore = { onMore(video) })
                }
            }
        }
    }
}


@Composable
private fun RecentHistoryCard(video: VideoItem, onClick: () -> Unit, onMore: () -> Unit) {
    val playedMs = (video.durationMs * video.progress.coerceIn(0f, 100f) / 100f).toLong()
    Row(
        Modifier.fillMaxWidth().padding(bottom = 12.dp)
            .shadow(3.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp))
            .background(Color.White).clickable(onClick = onClick).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp), verticalAlignment = Alignment.Top
    ) {
        Box(Modifier.size(120.dp, 84.dp).clip(RoundedCornerShape(10.dp)).background(PosterBrush(video.bgIndex))) {
            Thumbnail(video, Modifier.fillMaxSize())
        }
        Box(Modifier.weight(1f).height(84.dp)) {
            Column(Modifier.fillMaxSize().padding(end = 24.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Text(displayVideoName(video.name), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF222222),
                    lineHeight = 21.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(FrameRecentIcon, null, tint = Color(0xFFB0B0B0), modifier = Modifier.size(12.dp))
                        Text(formatClock(video.durationMs), fontSize = 11.sp, color = Color(0xFF999999), maxLines = 1)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(Icons.Outlined.PlayArrow, null, tint = Color(0xFFB0B0B0), modifier = Modifier.size(12.dp))
                        Text("${formatClock(playedMs)} · ${video.date}", fontSize = 11.sp, color = Color(0xFF999999), maxLines = 1)
                    }
                }
            }
            Box(Modifier.align(Alignment.BottomEnd).size(24.dp).clickable(onClick = onMore), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.MoreVert, "更多", tint = Color(0xFFD0D0D0), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun RecordsPage(
    videos: List<VideoItem>,
    onSearch: () -> Unit,
    onSort: () -> Unit,
    onEdit: (VideoItem) -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(top = 60.dp, bottom = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("记录", Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionIcon(FrameSearchIcon, "搜索观影感想", onSearch)
                    ActionIcon(FrameSortIcon, "记录排序", onSort)
                }
            }
        }
        stickyHeader {
            Row(
                Modifier.fillMaxWidth()
                    .background(PageBg.copy(alpha = .95f))
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Glass)
                    .padding(horizontal = 10.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Stat("已看完", videos.size.toString(), "部")
                Box(Modifier.width(1.dp).height(30.dp).background(Color.Black.copy(alpha = .05f)))
                Stat("累计时长", (videos.sumOf { it.durationMs } / 3600000).toString(), "h")
                Box(Modifier.width(1.dp).height(30.dp).background(Color.Black.copy(alpha = .05f)))
                Stat("写下感想", videos.count { it.review.isNotBlank() }.toString(), "篇")
            }
        }
        if (videos.isEmpty()) {
            item { EmptyHint("暂无已看完的视频") }
        } else {
            items(videos, key = { it.id }) { video ->
                Column(
                    Modifier.fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .clickable { onEdit(video) }
                        .padding(15.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(75.dp, 100.dp)
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
                            .clip(RoundedCornerShape(12.dp))
                            .height(androidx.compose.foundation.layout.IntrinsicSize.Min)
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
    var current by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(item.durationMs) }
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
            override fun onIsPlayingChanged(value: Boolean) { isPlaying = value }
            override fun onPlaybackStateChanged(state: Int) { if (state == Player.STATE_ENDED) onEnded() }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }
    LaunchedEffect(player) {
        var lastPersistedAt = 0L
        var previousTick = SystemClock.elapsedRealtime()
        var wasPlaying = player.isPlaying
        while (true) {
            current = player.currentPosition.coerceAtLeast(0L)
            if (player.duration > 0L) duration = player.duration
            val now = System.currentTimeMillis()
            val elapsedNow = SystemClock.elapsedRealtime()
            val elapsed = (elapsedNow - previousTick).coerceAtLeast(0L)
            val playingNow = player.isPlaying
            if (playingNow && wasPlaying) onWatchTime(elapsed)
            wasPlaying = playingNow
            previousTick = elapsedNow
            if (now - lastPersistedAt >= 5000L) {
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
                                start.x < size.width / 3f -> "brightness"
                                start.x > size.width * 2f / 3f -> "volume"
                                else -> "progress"
                            }
                        }
                    },
                    onDragEnd = {
                        if (dragMode == "progress") player.seekTo(current)
                        dragMode = "none"
                    },
                    onDragCancel = { dragMode = "none" },
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
            factory = { ctx -> PlayerView(ctx).apply {
                this.player = player
                useController = false
                layoutParams = ViewGroup.LayoutParams(-1, -1)
            } },
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
                            onSeek = { fraction ->
                                current = (duration * fraction).toLong().coerceIn(0L, duration.coerceAtLeast(0L))
                                player.seekTo(current)
                            }
                        )
                        Text(formatTime(duration), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (player.isPlaying) player.pause() else player.play() }, modifier = Modifier.size(48.dp)) {
                            Icon(
                                if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                contentDescription = if (isPlaying) "暂停" else "播放",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Box(
                            Modifier.clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = .2f))
                                .clickable { onSpeed() }
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
                Modifier.align(Alignment.Center).size(60.dp).clip(CircleShape)
                    .background(Color.Black.copy(alpha = .5f))
                    .border(1.dp, Color.White.copy(alpha = .2f), CircleShape)
                    .clickable { unlockVisible = false; onToggleLock() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.LockOpen, contentDescription = "解锁", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
        AnimatedVisibility(
            visible = centerMessage.isNotEmpty() && !locked,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            Box(
                Modifier.align(Alignment.Center).clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = .6f)).padding(horizontal = 25.dp, vertical = 15.dp)
            ) {
                Text(centerMessage, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PlayerIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(CircleShape)
            .background(Color.White.copy(alpha = .2f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun PlayerProgressBar(progress: Float, modifier: Modifier = Modifier, onSeek: (Float) -> Unit) {
    BoxWithConstraints(
        modifier.height(30.dp).pointerInput(Unit) {
            detectTapGestures { offset -> onSeek((offset.x / size.width).coerceIn(0f, 1f)) }
        }.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { onSeek((it.x / size.width).coerceIn(0f, 1f)) },
                onDrag = { change, _ -> change.consume(); onSeek((change.position.x / size.width).coerceIn(0f, 1f)) }
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
    return String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60)
}

private fun displayVideoName(name: String): String = name.replace(
    Regex("\\.(mp4|mkv|avi|mov|wmv|flv|webm)$", RegexOption.IGNORE_CASE), ""
)

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
        modifier = Modifier.size(36.dp).clickable(onClick = onClick),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 1.dp
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
        Surface(Modifier.size(36.dp).clickable { onSettings() }, shape = CircleShape, color = Glass) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Settings, "设置", Modifier.size(20.dp), tint = MainText) }
        }
    }
}

@Composable
private fun SectionHeading(text: String, expanded: Boolean? = null, onToggle: (() -> Unit)? = null) {
    val rotation by animateFloatAsState(if (expanded == false) -90f else 0f, tween(220), label = "section-chevron")
    Row(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp).then(if (onToggle != null) Modifier.clickable { onToggle() } else Modifier), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(4.dp).height(16.dp).clip(CircleShape).background(ThemeTeal))
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MainText, modifier = Modifier.padding(start = 8.dp).weight(1f))
        if (expanded != null) Icon(Icons.Outlined.ExpandMore, null, tint = MutedText, modifier = Modifier.rotate(rotation).size(20.dp))
    }
}

@Composable
private fun Stat(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = MutedText)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111111))
            Text(unit, fontSize = 12.sp, color = MutedText, modifier = Modifier.padding(start = 2.dp, bottom = 3.dp))
        }
    }
}

@Composable
private fun VideoRow(video: VideoItem, subtitle: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp).shadow(4.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(Color.White).clickable(onClick = onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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
private fun PosterCard(video: VideoItem, compact: Boolean, onPlay: (VideoItem) -> Unit, onLong: () -> Unit) {
    Column(Modifier.combinedClickable(onClick = { onPlay(video) }, onLongClick = onLong)) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f / 1.4f).clip(RoundedCornerShape(if (compact) 12.dp else 16.dp)).background(PosterBrush(video.bgIndex))) {
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
private fun EmptyHint(text: String) {
    Text(text, color = MutedText, fontSize = 14.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp))
}

@Composable
private fun FabAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(shape = CircleShape, color = ThemeTeal, shadowElevation = 5.dp, modifier = Modifier.size(48.dp).clickable(onClick = onClick)) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, label, tint = Color.White) }
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
        Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
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
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MainText, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
