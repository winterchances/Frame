package com.frame.cinema

import android.app.Activity
import android.content.Intent
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
    var selected by remember { mutableStateOf<VideoItem?>(null) }
    var videos by remember { mutableStateOf(repository.videos()) }
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

    fun refresh() { videos = repository.videos(); categories = repository.categories() }
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

    Box(Modifier.fillMaxSize().background(PageBg)) {
        if (playing == null) {
            Scaffold(
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
                    Column(
                        Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = 18.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnimatedVisibility(showFab) {
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                FabAction(Icons.Outlined.Search, "搜索") { sheet = SheetType.SEARCH; showFab = false }
                                FabAction(Icons.Outlined.VideoLibrary, "导入视频") { sheet = SheetType.IMPORT; showFab = false }
                            }
                        }
                        Surface(
                            modifier = Modifier.size(56.dp).clickable { showFab = !showFab },
                            shape = CircleShape, color = Glass, shadowElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Add, null, tint = MainText, modifier = Modifier.size(25.dp))
                            }
                        }
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
                onToggleLock = { locked = !locked },
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
                }
            )
        }

        if (sheet != SheetType.NONE) {
            ModalBottomSheet(
                onDismissRequest = { sheet = SheetType.NONE },
                containerColor = PageBg,
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                scrimColor = Color.Black.copy(alpha = .4f)
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
                            BasicTextField(newCategory, { newCategory = it }, Modifier.weight(1f).background(Color.White, RoundedCornerShape(12.dp)).padding(14.dp), singleLine = true, decorationBox = { inner -> if (newCategory.isEmpty()) Text("新分类名称", color = MutedText); inner() })
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
                        BasicTextField(recordSearch, { recordSearch = it }, Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp)).padding(15.dp), singleLine = true, decorationBox = { inner -> if (recordSearch.isEmpty()) Text("搜索视频名称或观影感想", color = MutedText); inner() })
                        videos.filter { (it.status == "watched" || it.progress >= 100f) && (recordSearch.isBlank() || it.name.contains(recordSearch, true) || it.review.contains(recordSearch, true)) }
                            .take(30).forEach { video ->
                                SheetAction(video.name) {
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
                        BasicTextField(search, { search = it }, Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp)).padding(15.dp), singleLine = true, decorationBox = { inner -> if (search.isEmpty()) Text("输入视频名称", color = MutedText); inner() })
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
                        BasicTextField(editName, { editName = it }, Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(14.dp), singleLine = true)
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
                        BasicTextField(editReview, { editReview = it }, Modifier.fillMaxWidth().heightIn(min = 110.dp).background(Color.White, RoundedCornerShape(14.dp)).padding(14.dp), decorationBox = { inner -> if (editReview.isEmpty()) Text("写下你的观影感想…", color = MutedText); inner() })
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
        MainPage.HOME to Icons.Outlined.Home,
        MainPage.LIBRARY to Icons.Outlined.VideoLibrary,
        MainPage.RECENT to Icons.Outlined.History,
        MainPage.RECORDS to Icons.Outlined.Bookmarks
    )
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp).height(56.dp).clip(CircleShape).background(Glass).padding(4.dp)) {
        val slot = maxWidth / 4
        val target = slot * entries.indexOfFirst { it.first == current }
        val offset by animateDpAsState(target, tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing), label = "nav-indicator")
        Box(Modifier.offset(x = offset).width(slot).fillMaxHeight().clip(CircleShape).background(Color(0xFFE9E9EB)))
        Row(Modifier.fillMaxSize()) {
            entries.forEach { (item, icon) ->
                Column(Modifier.weight(1f).fillMaxHeight().clickable { onSelect(item) }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(icon, item.label, Modifier.size(20.dp), tint = MainText)
                    Text(item.label, fontSize = 10.sp, color = MainText)
                }
            }
        }
    }
}

@Composable
private fun HomePage(videos: List<VideoItem>, onSettings: () -> Unit, onVideo: (VideoItem) -> Unit) {
    val continuing = videos.filter { it.progress > 0f && it.progress < 100f }.take(5)
    var continueExpanded by remember { mutableStateOf(true) }
    var recentExpanded by remember { mutableStateOf(true) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        item {
            Header("首页", onSettings)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Glass).padding(horizontal = 15.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Stat("视频总数", videos.size.toString(), "个")
                Stat("已看完", videos.count { it.progress >= 100f || it.status == "watched" }.toString(), "个")
                val hours = videos.sumOf { it.durationMs * it.progress / 100.0 }.toLong() / 3600000
                Stat("累计时长", hours.toString(), "h")
            }
            SectionHeading("继续观看", continueExpanded) { continueExpanded = !continueExpanded }
        }
        if (continueExpanded) {
            if (continuing.isEmpty()) item { EmptyHint("暂无观看记录") }
            else item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    continuing.forEach { video ->
                        Box(Modifier.width(280.dp).height(160.dp).clip(RoundedCornerShape(20.dp)).background(PosterColor(video.bgIndex)).clickable { onVideo(video) }) {
                            Thumbnail(video, Modifier.fillMaxSize())
                            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .28f)))
                            Column(Modifier.align(Alignment.BottomStart).padding(15.dp)) {
                                Text(video.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Spacer(Modifier.height(8.dp))
                                Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = .35f))) {
                                    Box(Modifier.fillMaxWidth((video.progress / 100f).coerceIn(0f, 1f)).fillMaxHeight().background(ThemeTeal))
                                }
                            }
                        }
                    }
                }
            }
        }
        item { SectionHeading("最近添加", recentExpanded) { recentExpanded = !recentExpanded } }
        if (recentExpanded) items(videos.sortedByDescending { it.addedAt }.take(4), key = { it.id }) { video ->
            VideoRow(video, "${video.date} 添加", { onVideo(video) })
        }
        if (videos.isEmpty()) item { EmptyHint("暂无视频，点击右下角添加") }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

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
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("片库", Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
            IconButton(onClick = onCategories) { Icon(Icons.Outlined.MoreHoriz, "分类管理") }
            IconButton(onClick = onSort) { Icon(Icons.Outlined.Sort, "排序") }
            IconButton(onClick = onGrid) { Icon(Icons.Outlined.GridView, "布局管理") }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            listOf("全部", "在看", "已看").forEach { value ->
                Column(Modifier.clickable { onStatus(value) }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value, color = if (status == value) MainText else MutedText, fontWeight = if (status == value) FontWeight.Bold else FontWeight.Medium, fontSize = 16.sp)
                    if (status == value) Spacer(Modifier.padding(top = 5.dp).width(20.dp).height(3.dp).clip(CircleShape).background(ThemeTeal))
                }
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            categories.forEach { value ->
                Surface(Modifier.clickable { onCategory(value) }, shape = CircleShape, color = if (category == value) ThemeTeal else Color.White) {
                    Text(value, color = if (category == value) Color.White else Color(0xFF666666), fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }
        }
        if (filtered.isEmpty()) EmptyHint(if (videos.isEmpty()) "片库还是空的，点击右下角导入视频" else "没有符合条件的视频")
        else LazyVerticalGrid(
            columns = GridCells.Fixed(if (compact) 3 else 2), modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 15.dp), verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filtered, key = { it.id }) { video ->
                PosterCard(video, compact, onPlay) { onLongVideo(video) }
            }
        }
    }
}

@Composable
private fun RecentPage(videos: List<VideoItem>, onVideo: (VideoItem) -> Unit, onClear: () -> Unit, onMore: (VideoItem) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("最近", Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
            IconButton(onClick = onClear) { Icon(Icons.Outlined.DeleteOutline, "清空播放记录") }
        }
        val history = videos.filter { it.progress > 0f || it.lastPlayedAt > 0L }.sortedByDescending { it.lastPlayedAt }
        if (history.isEmpty()) EmptyHint("暂无播放记录")
        else LazyColumn {
            val groups = history.groupBy { item ->
                val today = SimpleDateFormat("yyyy/M/d", Locale.getDefault()).format(Date())
                val yesterday = SimpleDateFormat("yyyy/M/d", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000))
                val playedDate = SimpleDateFormat("yyyy/M/d", Locale.getDefault()).format(Date(item.lastPlayedAt))
                when (playedDate) { today -> "今天"; yesterday -> "昨天"; else -> "更早" }
            }
            groups.forEach { (label, list) ->
                item { Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MainText, modifier = Modifier.padding(vertical = 10.dp)) }
                items(list, key = { it.id }) { video ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) {
                            val playedMs = (video.durationMs * video.progress.coerceIn(0f, 100f) / 100f).toLong()
                            VideoRow(
                                video,
                                "时长 ${formatClock(video.durationMs)} · 已播放 ${formatClock(playedMs)}",
                                { onVideo(video) }
                            )
                        }
                        IconButton(onClick = { onMore(video) }) { Icon(Icons.Outlined.MoreVert, "更多") }
                    }
                }
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
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("记录", Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
            IconButton(onClick = onSearch) { Icon(Icons.Outlined.Search, "搜索观影感想") }
            IconButton(onClick = onSort) { Icon(Icons.Outlined.Sort, "记录排序") }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 14.dp).clip(RoundedCornerShape(20.dp)).background(Glass).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Stat("已看完", videos.size.toString(), "部")
            Stat("累计时长", (videos.sumOf { it.durationMs } / 3600000).toString(), "h")
            Stat("写下感想", videos.count { it.review.isNotBlank() }.toString(), "篇")
        }
        if (videos.isEmpty()) EmptyHint("暂无符合条件的观影记录")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(videos, key = { it.id }) { video ->
                Card(Modifier.fillMaxWidth().clickable { onEdit(video) }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
                    Row(Modifier.padding(15.dp), verticalAlignment = Alignment.Top) {
                        Box(Modifier.size(75.dp, 100.dp).clip(RoundedCornerShape(10.dp)).background(PosterColor(video.bgIndex))) { Thumbnail(video, Modifier.fillMaxSize()) }
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(video.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MainText, maxLines = 2)
                            Text("★".repeat(video.rating.coerceIn(0, 5)) + "☆".repeat(5 - video.rating.coerceIn(0, 5)), color = Color(0xFFFFB800), modifier = Modifier.padding(top = 5.dp))
                            Text(video.review.ifBlank { "点击添加你的观影感想…" }, color = if (video.review.isBlank()) Color.LightGray else Color(0xFF555555), fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp), maxLines = 3)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NativePlayerOverlay(
    item: VideoItem, player: Player, speeds: List<Float>, speedIndex: Int, locked: Boolean, controlsVisible: Boolean,
    onClose: () -> Unit, onToggleControls: () -> Unit, onAutoHideControls: () -> Unit, onToggleLock: () -> Unit,
    onSpeed: () -> Unit, onOrientation: () -> Unit, onEnded: () -> Unit,
    onProgress: (Long, Long) -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var current by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(item.durationMs) }
    var brightness by remember { mutableFloatStateOf(.5f) }
    var volume by remember { mutableFloatStateOf(.5f) }
    var seekDelta by remember { mutableFloatStateOf(0f) }

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
        while (true) {
            current = player.currentPosition
            if (player.duration > 0) duration = player.duration
            val now = System.currentTimeMillis()
            if (now - lastPersistedAt >= 5000L) {
                onProgress(current, duration)
                lastPersistedAt = now
            }
            delay(500)
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
            .pointerInput(locked, speedIndex) {
                detectTapGestures(
                    onPress = {
                        tryAwaitRelease()
                        if (!locked) player.setPlaybackParameters(PlaybackParameters(speeds[speedIndex]))
                    },
                    onTap = { if (!locked) { if (player.isPlaying) player.pause() else player.play(); onToggleControls() } },
                    onDoubleTap = { offset ->
                        if (!locked) {
                            val forward = offset.x > size.width / 2
                            player.seekTo((player.currentPosition + if (forward) 10000 else -10000).coerceAtLeast(0))
                        }
                    },
                    onLongPress = { if (!locked) player.setPlaybackParameters(PlaybackParameters(2f)) }
                )
            }
            .pointerInput(locked) {
                detectDragGestures(
                    onDragEnd = { seekDelta = 0f },
                    onDrag = { change, drag ->
                        if (!locked) {
                            change.consume()
                            if (abs(drag.x) > abs(drag.y)) {
                                seekDelta += drag.x / size.width * (duration.coerceAtLeast(60000))
                                player.seekTo((player.currentPosition + seekDelta.toLong()).coerceIn(0, duration.coerceAtLeast(0)))
                                seekDelta = 0f
                            } else if (change.position.x < size.width * .33f) {
                                brightness = (brightness - drag.y / size.height).coerceIn(.02f, 1f)
                                (context as? Activity)?.window?.let { window ->
                                    val params = window.attributes
                                    params.screenBrightness = brightness
                                    window.attributes = params
                                }
                            } else if (change.position.x > size.width * .67f) {
                                volume = (volume - drag.y / size.height).coerceIn(0f, 1f)
                                player.volume = volume
                            }
                        }
                    }
                )
            }
    ) {
        AndroidView(
            factory = { ctx -> PlayerView(ctx).apply { this.player = player; useController = false; layoutParams = ViewGroup.LayoutParams(-1, -1) } },
            modifier = Modifier.fillMaxSize(),
            update = { it.player = player }
        )
        if (controlsVisible) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .22f)))
            Row(Modifier.fillMaxWidth().align(Alignment.TopStart).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("‹", color = Color.White, fontSize = 32.sp, modifier = Modifier.clickable { onClose() }.padding(end = 18.dp))
                Text(item.name, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1)
                Text(if (locked) "已锁定" else "锁屏", color = Color.White, modifier = Modifier.clickable { onToggleLock() }.padding(10.dp))
                Text("⛶", color = Color.White, modifier = Modifier.clickable { onOrientation() }.padding(10.dp))
            }
            Row(Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(46.dp)) {
                Text("↶10", color = Color.White, fontSize = 18.sp, modifier = Modifier.clickable { player.seekTo((player.currentPosition - 10000).coerceAtLeast(0)) })
                Text(if (isPlaying) "Ⅱ" else "▶", color = Color.White, fontSize = 36.sp, modifier = Modifier.clickable { if (player.isPlaying) player.pause() else player.play() })
                Text("10↷", color = Color.White, fontSize = 18.sp, modifier = Modifier.clickable { player.seekTo(player.currentPosition + 10000) })
            }
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 18.dp, vertical = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(formatTime(current), color = Color.White, fontSize = 12.sp)
                    Slider(value = if (duration > 0) current.toFloat().coerceIn(0f, duration.toFloat()) else 0f, onValueChange = { player.seekTo(it.toLong()) }, valueRange = 0f..duration.coerceAtLeast(1).toFloat(), modifier = Modifier.weight(1f))
                    Text(formatTime(duration), color = Color.White, fontSize = 12.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text("${speeds[speedIndex]}×", color = Color.White, modifier = Modifier.clickable { onSpeed() }.padding(10.dp))
                }
            }
        }
        if (locked) Text("点击锁图标解锁", color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp))
    }
}

private fun formatTime(ms: Long): String {
    val seconds = (ms / 1000).coerceAtLeast(0)
    return String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60)
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
    Row(Modifier.fillMaxWidth().padding(top = 25.dp, bottom = 12.dp).then(if (onToggle != null) Modifier.clickable { onToggle() } else Modifier), verticalAlignment = Alignment.CenterVertically) {
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
    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(RoundedCornerShape(16.dp)).background(Glass).clickable(onClick = onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(100.dp, 65.dp).clip(RoundedCornerShape(10.dp)).background(PosterColor(video.bgIndex)), contentAlignment = Alignment.Center) {
            Thumbnail(video, Modifier.fillMaxSize())
            if (video.progress > 0f) Box(Modifier.align(Alignment.BottomStart).fillMaxWidth((video.progress / 100f).coerceIn(0f, 1f)).height(3.dp).background(ThemeTeal))
        }
        Column(Modifier.weight(1f).padding(start = 15.dp)) {
            Text(video.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF222222), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(subtitle, fontSize = 12.sp, color = MutedText, modifier = Modifier.padding(top = 5.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PosterCard(video: VideoItem, compact: Boolean, onPlay: (VideoItem) -> Unit, onLong: () -> Unit) {
    val height = if (compact) 166.dp else 235.dp
    Column(Modifier.combinedClickable(onClick = { onPlay(video) }, onLongClick = onLong)) {
        Box(Modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(12.dp)).background(PosterColor(video.bgIndex))) {
            Thumbnail(video, Modifier.fillMaxSize())
            if (video.progress > 0f) {
                Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().height(3.dp).background(Color.White.copy(alpha = .22f)))
                Box(Modifier.align(Alignment.BottomStart).fillMaxWidth((video.progress / 100f).coerceIn(0f, 1f)).height(3.dp).background(ThemeTeal))
            }
            if (video.isFav) Text("♥", color = Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(7.dp))
        }
        Text(video.name, fontSize = if (compact) 12.sp else 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF222222), maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 7.dp))
    }
}

@Composable
private fun Thumbnail(video: VideoItem, modifier: Modifier = Modifier) {
    val bitmap = remember(video.thumbPath) {
        video.thumbPath?.let { path -> runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull() }
    }
    if (bitmap != null) Image(bitmap, contentDescription = video.name, modifier = modifier)
    else Box(modifier, contentAlignment = Alignment.Center) { Text("▶", color = Color.White.copy(alpha = .7f), fontSize = 28.sp) }
}

private fun PosterColor(index: Int): Color = listOf(Color(0xFF667EEA), Color(0xFF6D9C9A), Color(0xFF4FACFE), Color(0xFF8E7CC3))[((index - 1).coerceAtLeast(0)) % 4]

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
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
        SheetHeading(title); content()
    }
}

@Composable
private fun SheetHeading(title: String) {
    Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MainText, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
}

@Composable
private fun SheetAction(text: String, danger: Boolean = false, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp) {
        Text(text, color = if (danger) Color(0xFFFF3B30) else MainText, fontSize = 15.sp, modifier = Modifier.padding(17.dp))
    }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MainText, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
