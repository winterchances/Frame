package com.frame.cinema

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

private val PageBg = Color(0xFFF5F5F7)
private val MainText = Color(0xFF1A1A1A)
private val ThemeTeal = Color(0xFF7FB5B1)
private val MutedText = Color(0xFF888888)
private val PosterPalette = listOf(Color(0xFF667EEA), Color(0xFFF093FB), Color(0xFF4FACFE), Color(0xFF43E97B))

private data class LocalVideo(
    val uri: Uri,
    val title: String,
    val addedAt: String,
    val sizeLabel: String = "",
    val progress: Int = 0,
    val watched: Boolean = false,
    val favorite: Boolean = false,
    val category: String = "全部分类",
    val rating: Int = 0,
    val review: String = ""
)

private enum class MainPage(val label: String) {
    HOME("首页"), LIBRARY("片库"), RECENT("最近"), RECORDS("记录")
}
private enum class SheetType { NONE, IMPORT, SETTINGS, SORT, GRID, CATEGORIES, SEARCH, VIDEO_ACTIONS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                background = PageBg, surface = PageBg, primary = ThemeTeal, onBackground = MainText
            )) {
                CinemaApp(
                    readVideoName = { uri -> queryVideoName(uri) },
                    readVideoSize = { uri -> queryVideoSize(uri) },
                    persistUri = { uri ->
                        runCatching {
                            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    }
                )
            }
        }
    }

    private fun queryVideoName(uri: Uri): String =
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) it.getString(0) else null
        } ?: uri.lastPathSegment ?: "未命名视频"

    private fun queryVideoSize(uri: Uri): String {
        val bytes = contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use {
            if (it.moveToFirst()) it.getLong(0) else 0L
        } ?: 0L
        return if (bytes > 0) String.format(Locale.getDefault(), "%.1f MB", bytes / 1048576f) else ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CinemaApp(
    readVideoName: (Uri) -> String,
    readVideoSize: (Uri) -> String,
    persistUri: (Uri) -> Unit
) {
    var page by remember { mutableStateOf(MainPage.HOME) }
    var sheet by remember { mutableStateOf(SheetType.NONE) }
    var selectedVideo by remember { mutableStateOf<LocalVideo?>(null) }
    var compactGrid by remember { mutableStateOf(true) }
    var statusFilter by remember { mutableStateOf("全部") }
    var categoryFilter by remember { mutableStateOf("全部分类") }
    var allowBackground by remember { mutableStateOf(true) }
    var keepAwake by remember { mutableStateOf(true) }
    var showFabActions by remember { mutableStateOf(false) }
    val videos = remember { mutableStateListOf<LocalVideo>() }
    val categories = remember { mutableStateListOf("全部分类", "我的最爱") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            persistUri(uri)
            if (videos.none { it.uri == uri }) {
                videos.add(0, LocalVideo(
                    uri = uri,
                    title = readVideoName(uri),
                    addedAt = SimpleDateFormat("yyyy/M/d", Locale.getDefault()).format(Date()),
                    sizeLabel = readVideoSize(uri)
                ))
            }
        }
        sheet = SheetType.NONE
    }

    Box(Modifier.fillMaxSize().background(PageBg)) {
        Scaffold(
            containerColor = PageBg,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = { BottomPillNavigation(page) { page = it } }
        ) { padding ->
            AnimatedContent(
                targetState = page,
                modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                transitionSpec = {
                    (slideInHorizontally { it / 5 } + fadeIn()).togetherWith(
                        slideOutHorizontally { -it / 5 } + fadeOut()
                    )
                },
                label = "main-page-transition"
            ) { target ->
                when (target) {
                    MainPage.HOME -> HomePage(videos, { sheet = SheetType.SETTINGS }, { selectedVideo = it; sheet = SheetType.VIDEO_ACTIONS })
                    MainPage.LIBRARY -> LibraryPage(
                        videos, categories, statusFilter, categoryFilter, compactGrid,
                        { statusFilter = it }, { categoryFilter = it },
                        { sheet = SheetType.GRID }, { sheet = SheetType.SORT },
                        { sheet = SheetType.CATEGORIES }, { selectedVideo = it; sheet = SheetType.VIDEO_ACTIONS }
                    )
                    MainPage.RECENT -> RecentPage(videos) {
                        videos.indices.forEach { i -> videos[i] = videos[i].copy(progress = 0, watched = false) }
                    }
                    MainPage.RECORDS -> RecordsPage(videos.filter { it.watched }) {
                        selectedVideo = it; sheet = SheetType.VIDEO_ACTIONS
                    }
                }
            }
        }

        Column(
            Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = 86.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (showFabActions) {
                FabAction(Icons.Outlined.Search, "搜索") { sheet = SheetType.SEARCH; showFabActions = false }
                FabAction(Icons.Outlined.Add, "导入") { sheet = SheetType.IMPORT; showFabActions = false }
            }
            Surface(
                modifier = Modifier.size(56.dp).clickable { showFabActions = !showFabActions },
                shape = CircleShape, color = Color.White.copy(alpha = .88f), shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Menu, null, tint = MainText, modifier = Modifier.size(22.dp)) }
            }
        }

        if (sheet != SheetType.NONE) {
            ModalBottomSheet(onDismissRequest = { sheet = SheetType.NONE }, containerColor = PageBg,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
                when (sheet) {
                    SheetType.IMPORT -> SheetContent("导入本地视频") {
                        SheetAction("从相册选择视频") { picker.launch(arrayOf("video/*")) }
                        SheetAction("从文件夹导入") { picker.launch(arrayOf("video/*")) }
                        SheetAction("取消") { sheet = SheetType.NONE }
                    }
                    SheetType.SETTINGS -> Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 8.dp)) {
                        SheetHeading("设置")
                        Text("播放偏好", color = MutedText, fontSize = 13.sp, modifier = Modifier.padding(vertical = 10.dp))
                        SettingRow("允许后台播放", allowBackground) { allowBackground = it }
                        SettingRow("保持屏幕常亮", keepAwake) { keepAwake = it }
                        Spacer(Modifier.height(26.dp))
                    }
                    SheetType.SORT -> SheetContent("视频排序") {
                        SheetAction("按添加时间排序") { videos.sortByDescending { it.addedAt }; sheet = SheetType.NONE }
                        SheetAction("按文件名排序") { videos.sortBy { it.title.lowercase(Locale.getDefault()) }; sheet = SheetType.NONE }
                    }
                    SheetType.GRID -> SheetContent("视图模式") {
                        SheetAction("2列大图（舒适）") { compactGrid = false; sheet = SheetType.NONE }
                        SheetAction("3列小图（紧凑）") { compactGrid = true; sheet = SheetType.NONE }
                    }
                    SheetType.CATEGORIES -> SheetContent("分类管理") {
                        categories.forEach { cat -> SheetAction(cat) { categoryFilter = cat; sheet = SheetType.NONE } }
                    }
                    SheetType.SEARCH -> SheetContent("搜索") {
                        Text("搜索结果与筛选逻辑将在后续功能阶段接入。", color = MutedText, modifier = Modifier.padding(16.dp))
                    }
                    SheetType.VIDEO_ACTIONS -> SheetContent(selectedVideo?.title ?: "视频操作") {
                        SheetAction("立即播放") { sheet = SheetType.NONE }
                        SheetAction(if (selectedVideo?.favorite == true) "取消收藏" else "加入收藏") {
                            selectedVideo?.let { v ->
                                val i = videos.indexOfFirst { it.uri == v.uri }
                                if (i >= 0) videos[i] = v.copy(favorite = !v.favorite)
                            }
                            sheet = SheetType.NONE
                        }
                        SheetAction("移出片库", danger = true) {
                            selectedVideo?.let { v -> videos.removeAll { it.uri == v.uri } }
                            sheet = SheetType.NONE
                        }
                    }
                    SheetType.NONE -> Unit
                }
            }
        }
    }
}

@Composable
private fun BottomPillNavigation(current: MainPage, onSelect: (MainPage) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(30.dp)).background(Color.White.copy(alpha = .88f)).padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        val entries = listOf(
            MainPage.HOME to Icons.Outlined.Home,
            MainPage.LIBRARY to Icons.Outlined.VideoLibrary,
            MainPage.RECENT to Icons.Outlined.History,
            MainPage.RECORDS to Icons.Outlined.Bookmarks
        )
        entries.forEach { (item, icon) ->
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(24.dp))
                    .background(if (current == item) Color(0xFFECECEE) else Color.Transparent)
                    .clickable { onSelect(item) }.padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(icon, item.label, Modifier.size(20.dp), tint = MainText)
                Text(item.label, fontSize = 10.sp, color = MainText, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun HomePage(videos: List<LocalVideo>, onSettings: () -> Unit, onVideo: (LocalVideo) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        item {
            Header("首页", onSettings)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = .88f)).padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Stat("视频总数", videos.size.toString(), "个")
                Stat("已看完", videos.count { it.watched }.toString(), "个")
                Stat("本周时长", "0", "h")
            }
            SectionHeading("继续观看")
        }
        val continuing = videos.filter { it.progress in 1..99 }.take(5)
        if (continuing.isEmpty()) item { EmptyHint("暂无观看记录") }
        items(continuing) { VideoRow(it, "已看 ${it.progress}%", onClick = { onVideo(it) }) }
        item { SectionHeading("最近添加") }
        items(videos.take(4)) { VideoRow(it, "${it.addedAt} 添加") { onVideo(it) } }
        if (videos.isEmpty()) item { EmptyHint("暂无视频，点击右下角导入") }
        item { Spacer(Modifier.height(18.dp)) }
    }
}

@Composable
private fun LibraryPage(
    videos: List<LocalVideo>, categories: List<String>, status: String, category: String, compact: Boolean,
    onStatus: (String) -> Unit, onCategory: (String) -> Unit, onGrid: () -> Unit, onSort: () -> Unit,
    onCategories: () -> Unit, onVideo: (LocalVideo) -> Unit
) {
    val filtered = videos.filter {
        (status == "全部" || (status == "在看" && it.progress > 0 && !it.watched) || (status == "已看" && it.watched)) &&
            (category == "全部分类" || (category == "我的最爱" && it.favorite) || it.category == category)
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("片库", Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
            IconButton(onClick = onCategories) { Icon(Icons.Outlined.Label, "分类") }
            IconButton(onClick = onSort) { Icon(Icons.Outlined.Sort, "排序") }
            IconButton(onClick = onGrid) { Icon(Icons.Outlined.GridView, "视图") }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            listOf("全部", "在看", "已看").forEach { value ->
                Column(Modifier.clickable { onStatus(value) }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value, color = if (status == value) MainText else MutedText,
                        fontWeight = if (status == value) FontWeight.Bold else FontWeight.Medium, fontSize = 16.sp)
                    if (status == value) Spacer(Modifier.padding(top = 5.dp).width(20.dp).height(3.dp).clip(CircleShape).background(ThemeTeal))
                }
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            categories.forEach { value ->
                val active = category == value
                Surface(Modifier.clickable { onCategory(value) }, shape = CircleShape, color = if (active) ThemeTeal else Color.White) {
                    Text(value, color = if (active) Color.White else Color(0xFF666666), fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }
        }
        if (filtered.isEmpty()) EmptyHint(if (videos.isEmpty()) "片库还是空的，点击右下角导入视频" else "没有符合条件的视频")
        else LazyVerticalGrid(
            columns = GridCells.Fixed(if (compact) 3 else 2), modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 15.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) { items(filtered, key = { it.uri.toString() }) { PosterCard(it) { onVideo(it) } } }
    }
}

@Composable
private fun RecentPage(videos: List<LocalVideo>, onClear: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("最近", Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
            IconButton(onClick = onClear) { Icon(Icons.Outlined.DeleteOutline, "清空播放记录") }
        }
        val history = videos.filter { it.progress > 0 || it.watched }
        if (history.isEmpty()) EmptyHint("暂无播放记录")
        else LazyColumn { items(history) { VideoRow(it, "${it.progress}% · ${it.addedAt}") {} } }
    }
}

@Composable
private fun RecordsPage(videos: List<LocalVideo>, onEdit: (LocalVideo) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Text("记录", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
        Row(Modifier.fillMaxWidth().padding(vertical = 14.dp).clip(RoundedCornerShape(20.dp)).background(Color.White).padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Stat("已看完", videos.size.toString(), "部")
            Stat("累计时长", "0", "h")
            Stat("写下感想", videos.count { it.review.isNotBlank() }.toString(), "篇")
        }
        if (videos.isEmpty()) EmptyHint("暂无已看完的视频")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(videos) { video ->
                Card(Modifier.fillMaxWidth().clickable { onEdit(video) }, shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(15.dp)) {
                        Text(video.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MainText)
                        Text("★".repeat(video.rating) + "☆".repeat(5 - video.rating), color = Color(0xFFFFB800), modifier = Modifier.padding(top = 5.dp))
                        Text(video.review.ifBlank { "点击添加你的观影感想..." },
                            color = if (video.review.isBlank()) Color.LightGray else Color(0xFF555555),
                            fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(title: String, onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 15.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText)
        IconButton(onClick = onSettings) {
            Surface(shape = CircleShape, color = Color.White, shadowElevation = 2.dp) {
                Icon(Icons.Outlined.Settings, "设置", Modifier.padding(9.dp).size(20.dp), tint = Color(0xFF333333))
            }
        }
    }
}

@Composable
private fun SectionHeading(text: String) {
    Row(Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(4.dp).height(16.dp).clip(CircleShape).background(ThemeTeal))
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MainText, modifier = Modifier.padding(start = 8.dp))
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
private fun VideoRow(video: LocalVideo, subtitle: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(RoundedCornerShape(16.dp))
        .background(Color.White).clickable(onClick = onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(100.dp, 65.dp).clip(RoundedCornerShape(10.dp))
            .background(PosterPalette[video.title.hashCode().absoluteValue % PosterPalette.size]), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.VideoLibrary, null, tint = Color.White.copy(alpha = .8f), modifier = Modifier.size(25.dp))
        }
        Column(Modifier.weight(1f).padding(start = 15.dp)) {
            Text(video.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF222222), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(subtitle, fontSize = 12.sp, color = MutedText, modifier = Modifier.padding(top = 5.dp))
        }
    }
}

@Composable
private fun PosterCard(video: LocalVideo, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick)) {
        Box(Modifier.fillMaxWidth().height(166.dp).clip(RoundedCornerShape(12.dp))
            .background(PosterPalette[video.title.hashCode().absoluteValue % PosterPalette.size]), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.VideoLibrary, null, tint = Color.White.copy(alpha = .8f), modifier = Modifier.size(30.dp))
            Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().height(3.dp).background(Color.White.copy(alpha = .2f)))
            Box(Modifier.align(Alignment.BottomStart).fillMaxWidth(video.progress.coerceIn(0, 100) / 100f).height(3.dp).background(ThemeTeal))
        }
        Text(video.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF222222),
            maxLines = if (video.title.isEmpty()) 1 else 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 7.dp))
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(text, color = MutedText, fontSize = 14.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp))
}

@Composable
private fun FabAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(shape = CircleShape, color = ThemeTeal, shadowElevation = 5.dp,
        modifier = Modifier.size(48.dp).clickable(onClick = onClick)) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, label, tint = Color.White) }
    }
}

@Composable
private fun SheetContent(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
        SheetHeading(title)
        content()
    }
}

@Composable
private fun SheetHeading(title: String) {
    Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MainText,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
}

@Composable
private fun SheetAction(text: String, danger: Boolean = false, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp) {
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
