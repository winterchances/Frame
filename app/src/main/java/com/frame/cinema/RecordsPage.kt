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
internal fun RecordsPage(
    hazeState: HazeState,
    listState: LazyListState,
    videos: List<VideoItem>,
    reviews: List<ReviewEntry>,
    movieReviews: List<MovieReviewEntry>,
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
    onOpenMovieReviews: (VideoItem) -> Unit,
    onOpenStandaloneMovieReview: (ReviewEntry) -> Unit,
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
    val movieReviewsByVideo = remember(movieReviews) {
        movieReviews.filter { it.standaloneReviewId == null }.groupBy { it.videoId }
    }
    val movieReviewsByStandalone = remember(movieReviews) {
        movieReviews.filter { it.standaloneReviewId != null }.associateBy { it.standaloneReviewId!! }
    }
    val movieReviewCounts = remember(movieReviewsByVideo) {
        movieReviewsByVideo.mapValues { (_, entries) -> entries.size }
    }
    val sortedReviewsByVideo = remember(reviewsByVideo) {
        reviewsByVideo.mapValues { (_, entries) -> entries.sortedByDescending { it.createdAt } }
    }
    val displayVideos = remember(videos, reviewsByVideo, movieReviewsByVideo, sortMode, sortDirection) {
        videos.filter { video ->
            val completed = video.status == "watched" || video.progress >= 100f
            val hasReviews = reviewsByVideo[video.id].orEmpty().isNotEmpty()
            val hasMovieReviews = movieReviewsByVideo[video.id].orEmpty().isNotEmpty()
            completed || hasReviews || hasMovieReviews
        }.sortedWith { a, b ->
            val primary = when (sortMode) {
                RecordSortMode.ADDED -> a.addedAt.compareTo(b.addedAt)
                RecordSortMode.RECORD_TIME -> {
                    val aRecordTime = maxOf(
                        reviewsByVideo[a.id].orEmpty().maxOfOrNull { it.createdAt } ?: 0L,
                        movieReviewsByVideo[a.id].orEmpty().maxOfOrNull { it.createdAt } ?: 0L,
                        a.lastPlayedAt
                    )
                    val bRecordTime = maxOf(
                        reviewsByVideo[b.id].orEmpty().maxOfOrNull { it.createdAt } ?: 0L,
                        movieReviewsByVideo[b.id].orEmpty().maxOfOrNull { it.createdAt } ?: 0L,
                        b.lastPlayedAt
                    )
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
        roots.sortedWith { a, b ->
            val primary = when (sortMode) {
                RecordSortMode.NAME -> compareNaturalVideoNames(a.title, b.title)
                RecordSortMode.RATING -> a.rating.compareTo(b.rating)
                RecordSortMode.ADDED, RecordSortMode.RECORD_TIME, RecordSortMode.WATCHED -> a.createdAt.compareTo(b.createdAt)
            }
            val directed = if (sortMode == RecordSortMode.RATING && (a.rating == 0f || b.rating == 0f)) {
                when {
                    a.rating == 0f && b.rating > 0f -> 1
                    a.rating > 0f && b.rating == 0f -> -1
                    else -> 0
                }
            } else if (sortDirection == SortDirection.ASCENDING) primary else -primary
            if (directed != 0) directed else a.createdAt.compareTo(b.createdAt)
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
                        if (listState.firstVisibleItemIndex == 0) {
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
                            .background(MaterialTheme.colorScheme.surface)
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
                                    color = if (displayVideoName(video.name) == "未命名") MutedText else MainText,
                                    englishFontSize = 14.sp,
                                    englishColor = MutedText,
                                    lineHeight = 22.sp,
                                    englishSpacing = 2.dp,
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        if (completed) video.date + " · 已看完" else "尚未看完 · ${video.progress.toInt()}%",
                                        color = MutedText,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                    )
                                    val movieReviewCount = movieReviewCounts[video.id] ?: 0
                                    if (completed || movieReviewCount > 0) {
                                        Text(
                                            " · ",
                                            color = MutedText,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                        )
                                        Text(
                                            if (movieReviewCount > 0) "影评" else "写影评",
                                            color = ActiveAccent,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            maxLines = 1,
                                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                            modifier = Modifier.noRippleClickable { onOpenMovieReviews(video) }
                                        )
                                    }
                                }
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Box(Modifier.fillMaxWidth().height(0.5.dp).background(LocalFramePalette.current.divider))
                        Spacer(Modifier.height(16.dp))
                        Row(
                            Modifier.fillMaxWidth().padding(end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "随记",
                                color = MutedText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${entries.size} 条",
                                modifier = Modifier,
                                color = MutedText,
                                fontSize = 13.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Spacer(Modifier.weight(1f))
                            val standaloneMovieReview = movieReviewsByStandalone[entry.id]
                            Text(
                                if (standaloneMovieReview != null) "影评" else "写影评",
                                modifier = Modifier.noRippleClickable { onOpenStandaloneMovieReview(entry) },
                                color = ActiveAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Text(" 〡 ", color = MutedText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
                                color = MutedText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Text(" 〡 ", color = MutedText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "添加",
                                modifier = Modifier.noRippleClickable { onCreate(video) },
                                color = MutedText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Text(" 〡 ", color = MutedText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(
                                if (entries.isEmpty()) "展开" else if (expanded) "关闭" else "展开",
                                modifier = Modifier.then(
                                    if (entries.isNotEmpty()) Modifier.noRippleClickable {
                                        setVideoReviewsExpanded(video.id, !expanded)
                                    } else Modifier
                                ),
                                color = MutedText,
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
                                if (entryHighlight) ActiveAccent else MainText.copy(alpha = 0.78f),
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
                                val timelineAccent = ActiveAccent
                                Row(
                                    Modifier.fillMaxWidth().drawBehind {
                                        val axisX = 7.dp.toPx()
                                        val dotCenterY = 8.dp.toPx()
                                        val lineWidth = 0.8.dp.toPx()
                                        val lineColor = timelineAccent.copy(alpha = 0.32f)
                                        val dotColor = timelineAccent.copy(alpha = 0.48f)
                                        if (entries.size > 1) {
                                            val dotRadius = 3.dp.toPx()
                                            if (index > 0) {
                                                drawLine(
                                                    lineColor,
                                                    Offset(axisX, 0f),
                                                    Offset(axisX, dotCenterY - dotRadius),
                                                    strokeWidth = lineWidth
                                                )
                                            }
                                            if (index < entries.lastIndex) {
                                                drawLine(
                                                    lineColor,
                                                    Offset(axisX, dotCenterY + dotRadius),
                                                    Offset(axisX, size.height),
                                                    strokeWidth = lineWidth
                                                )
                                            }
                                        }
                                        drawCircle(
                                            color = dotColor,
                                            radius = 3.dp.toPx(),
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
                                                color = MutedText.copy(alpha = 0.8f),
                                                fontSize = 10.sp,
                                                lineHeight = 14.sp,
                                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                            )
                                            entry.playbackPositionMs?.let { positionMs ->
                                                Text(
                                                    "|",
                                                    color = MutedText,
                                                    fontSize = 10.sp,
                                                    lineHeight = 14.sp,
                                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                                )
                                                Text(
                                                    formatTime(positionMs),
                                                    modifier = Modifier.noRippleClickable { onSeekToTimestamp(video, positionMs) },
                                                    color = ActiveAccent,
                                                    fontSize = 10.sp,
                                                    lineHeight = 14.sp,
                                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                                )
                                            }
                                        }
                                        Text(entry.content, color = entryTextColor, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 7.dp))
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
                        if (entryHighlight) ActiveAccent else MainText.copy(alpha = 0.78f),
                        animationSpec = tween(420),
                        label = "standalone-review-text-highlight-${entry.id}"
                    )
                    Column(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 16.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
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
                                    .background(LocalFramePalette.current.secondarySurface),
                                contentAlignment = Alignment.Center
                            ) {
                                if (coverBitmap != null) {
                                    Image(coverBitmap.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else {
                                    Icon(Icons.Outlined.Image, null, tint = LocalFramePalette.current.tertiaryText, modifier = Modifier.size(25.dp))
                                }
                            }
                            Column(Modifier.weight(1f).height(106.dp)) {
                                VideoCardTitle(
                                    standaloneTitle,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = if (entry.title.isBlank()) MutedText else MainText,
                                    englishFontSize = 14.sp,
                                    englishColor = MutedText,
                                    lineHeight = 22.sp,
                                    maxLines = 2,
                                    showEnglish = true
                                )
                                Spacer(Modifier.weight(1f))
                                RatingStars(
                                    entry.rating,
                                    14.dp,
                                    onRate = null,
                                    spacing = 1.dp,
                                    modifier = Modifier.graphicsLayer { translationX = -4.dp.toPx() }
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    SimpleDateFormat("M/d/yyyy HH:mm", Locale.getDefault()).format(Date(entry.createdAt)),
                                    color = MutedText,
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
                        Box(Modifier.fillMaxWidth().height(0.5.dp).background(LocalFramePalette.current.divider))
                        Spacer(Modifier.height(16.dp))
                        Row(
                            Modifier.fillMaxWidth().padding(end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "随记",
                                color = MutedText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${1 + reviews.count { it.standaloneGroupId == entry.id }} 条",
                                modifier = Modifier,
                                color = MutedText,
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
                                color = MutedText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Text(" 〡 ", color = MutedText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "添加",
                                modifier = Modifier.noRippleClickable { onCreateStandalone(entry.id) },
                                color = MutedText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Text(" 〡 ", color = MutedText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(
                                if (expanded) "关闭" else "展开",
                                modifier = Modifier.noRippleClickable {
                                    setStandaloneReviewExpanded(entry.id, !expanded)
                                },
                                color = MutedText,
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
                                        val timelineAccent = ActiveAccent
                                        Row(
                                            Modifier.fillMaxWidth()
                                                .drawBehind {
                                                    val axisX = 7.dp.toPx()
                                                    val dotCenterY = 8.dp.toPx()
                                                    drawCircle(
                                                        color = timelineAccent.copy(alpha = 0.48f),
                                                        radius = 3.dp.toPx(),
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
                                                    color = MutedText.copy(alpha = 0.8f),
                                                    fontSize = 10.sp,
                                                    lineHeight = 14.sp,
                                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                                )
                                                Text(
                                                    timelineEntry.content,
                                                    color = entryTextColor,
                                                    fontSize = 14.sp,
                                                    lineHeight = 21.sp,
                                                    modifier = Modifier.padding(top = 7.dp)
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
                            color = LocalFramePalette.current.divider,
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
                .noRippleClickable { }
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
