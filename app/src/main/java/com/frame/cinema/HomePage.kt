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
internal fun HomePage(
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
    val homePageBg = PageBg
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
                            if (listState.firstVisibleItemIndex == 0) {
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
                            drawRect(homePageBg, topLeft = Offset(-bleed, 0f), size = Size(size.width + bleed * 2f, size.height))

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
                                            Box(Modifier.fillMaxWidth((video.progress / 100f).coerceIn(0f, 1f)).fillMaxHeight().background(ActiveAccent))
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
                        drawRect(homePageBg, topLeft = Offset(-bleed, 0f), size = Size(size.width + bleed * 2f, size.height))
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
                Text("首页", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MainText)
            }
        }
    }
}

