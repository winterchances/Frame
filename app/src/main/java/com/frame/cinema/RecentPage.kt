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
internal fun RecentPage(hazeState: HazeState, listState: LazyListState, videos: List<VideoItem>, onVideo: (VideoItem) -> Unit, onClear: () -> Unit, onMore: (VideoItem) -> Unit) {
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
                            if (listState.firstVisibleItemIndex == 0) {
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
                Modifier.fillMaxWidth()
                    .offset {
                    val nextOffset = nextHeaderOffsetState.value
                    val pushOffset = if (activeGroupIndex >= 0 && nextOffset != Int.MAX_VALUE) {
                        minOf(0, nextOffset - headerHeightPx)
                    } else 0
                    androidx.compose.ui.unit.IntOffset(0, pushOffset)
                }.zIndex(3f)
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
        Modifier.fillMaxWidth().noRippleClickable { }.graphicsLayer { this.alpha = alpha }
            .onGloballyPositioned { onHeightChanged(it.size.height) }
            .background(PageBg)
            .statusBarsPadding()
            .padding(top = 9.dp, bottom = 10.dp)
    ) {
        Text(
            label,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = MainText,
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
            .background(MaterialTheme.colorScheme.surface).noRippleClickable(onClick).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp), verticalAlignment = Alignment.Top
    ) {
        Box(Modifier.size(120.dp, 84.dp).clip(RoundedCornerShape(10.dp)).background(PosterBrush(video.bgIndex))) {
            Thumbnail(video, Modifier.fillMaxSize())
        }
        Box(Modifier.weight(1f).height(84.dp)) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {                VideoCardTitle(
                    video.name,
                    fontSize = 15.sp,
                    color = MainText,
                    englishFontSize = 13.sp,
                    englishColor = MutedText,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    showEnglish = true,
                    lineHeight = 21.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("总时长${formatClock(video.durationMs)}", fontSize = 11.sp, lineHeight = 13.sp, color = MutedText, maxLines = 1, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
                    Text("播放至${formatClock(playedMs)} · ${watchedDate}", fontSize = 11.sp, lineHeight = 13.sp, color = MutedText, maxLines = 1, overflow = TextOverflow.Clip, softWrap = false, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
                }
            }
            Box(Modifier.align(Alignment.BottomEnd).offset(x = 4.dp).size(24.dp).noRippleClickable(onClick = onMore), contentAlignment = Alignment.BottomEnd) {
                Icon(Icons.Outlined.MoreVert, "更多", tint = Color(0xFFDDDDDD), modifier = Modifier.size(14.dp).offset(x = 4.dp))
            }
        }
    }
}
