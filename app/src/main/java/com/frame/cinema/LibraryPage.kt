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



@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LibraryPage(
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
    Box(
        Modifier.fillMaxSize().clipToBounds()
            .onGloballyPositioned { onViewportBoundsChanged(it.boundsInRoot()) }
            
    ) {
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
                            if (listState.firstVisibleItemIndex == 0) {
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
                                    .background(if (video.id in selectedVideoIds) ActiveAccent else Color.Black.copy(alpha = .42f))
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
                    Text("取消", color = ActiveAccent, fontSize = 16.sp)
                }
                Text("已选 $selectedCount 项", Modifier.weight(1f), fontSize = 19.sp, fontWeight = FontWeight.Bold, color = MainText, textAlign = TextAlign.Center, maxLines = 1)
                TextButton(onClick = {
                    val visibleIds = filtered.map { it.id }.toSet()
                    selectedVideoIds = if (visibleIds.isNotEmpty() && visibleIds.all { it in selectedVideoIds }) selectedVideoIds - visibleIds else selectedVideoIds + visibleIds
                }, contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Text(if (filtered.isNotEmpty() && filtered.all { it.id in selectedVideoIds }) "取消全选" else "全选", color = ActiveAccent, fontSize = 16.sp)
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
                        hazeState = hazeState,
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        offset = androidx.compose.ui.unit.IntOffset.Zero,
                        modifier = Modifier.width(192.dp),
                        animateFromAnchor = true
                    ) {
                        CustomSortMenuItem("2列大图", !compact) { onCompactChange(false); showSortMenu = false }
                        CustomSortMenuItem("3列小图", compact) { onCompactChange(true); showSortMenu = false }
                        HorizontalDivider(color = LocalFramePalette.current.divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))
                        CustomSortMenuItem("按添加时间", sort == SortMode.ADDED) { onSortModeChange(SortMode.ADDED); showSortMenu = false }
                        CustomSortMenuItem("按名称", sort == SortMode.NAME) { onSortModeChange(SortMode.NAME); showSortMenu = false }
                        HorizontalDivider(color = LocalFramePalette.current.divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))
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
                    .clip(RoundedCornerShape(90.dp)).background(MaterialTheme.colorScheme.surface),
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
                        hazeState = hazeState,
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
                        hazeState = hazeState,
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
                    Icon(Icons.Outlined.DeleteOutline, null, tint = LocalFramePalette.current.danger, modifier = Modifier.size(19.dp))
                    Text("批量移除", fontSize = 11.sp, color = LocalFramePalette.current.danger, modifier = Modifier.padding(top = 1.dp))
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
internal fun LibraryFilterHeader(
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
            .noRippleClickable { }
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
                    if (status == value) Spacer(Modifier.padding(top = 5.dp).width(20.dp).height(3.dp).clip(CircleShape).background(ActiveAccent))
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
                        color = if (category == value) ActiveAccent else MaterialTheme.colorScheme.surface,
                        shadowElevation = 0.dp,
                    ) {
                        val label = when (value) { "全部" -> "全部"; "全部分类" -> "未分类"; "我的最爱" -> "收藏"; else -> value }
                        Text(label, color = if (category == value) Color.White else MutedText, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                }
            }
        }
    }
}

