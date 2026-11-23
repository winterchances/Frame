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
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
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
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.ui.graphics.drawscope.clipRect
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
import androidx.compose.ui.unit.IntSize
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



private val thumbnailBitmapCache = object : LruCache<String, Bitmap>(
    (Runtime.getRuntime().maxMemory() / 1024L / 4L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
) {
    override fun sizeOf(key: String, value: Bitmap): Int = (value.byteCount / 1024).coerceAtLeast(1)
}

private fun buildFrameStarIcon(name: String, filled: Boolean): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        path(
            fill = if (filled) SolidColor(ThemeTeal) else null,
            stroke = SolidColor(ThemeTeal),
            strokeLineWidth = 1.35f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 2.6f)
            curveTo(12.35f, 2.6f, 12.58f, 2.82f, 12.74f, 3.16f)
            lineTo(15.05f, 8.12f)
            curveTo(15.18f, 8.38f, 15.42f, 8.56f, 15.71f, 8.6f)
            lineTo(21.18f, 9.39f)
            curveTo(21.58f, 9.45f, 21.74f, 9.94f, 21.45f, 10.23f)
            lineTo(17.49f, 14.09f)
            curveTo(17.28f, 14.29f, 17.19f, 14.57f, 17.24f, 14.85f)
            lineTo(18.18f, 20.28f)
            curveTo(18.25f, 20.68f, 17.83f, 20.98f, 17.47f, 20.79f)
            lineTo(12.58f, 18.22f)
            curveTo(12.32f, 18.08f, 12.02f, 18.08f, 11.76f, 18.22f)
            lineTo(6.87f, 20.79f)
            curveTo(6.51f, 20.98f, 6.09f, 20.68f, 6.16f, 20.28f)
            lineTo(7.1f, 14.85f)
            curveTo(7.15f, 14.57f, 7.06f, 14.29f, 6.85f, 14.09f)
            lineTo(2.89f, 10.23f)
            curveTo(2.6f, 9.94f, 2.76f, 9.45f, 3.16f, 9.39f)
            lineTo(8.63f, 8.6f)
            curveTo(8.92f, 8.56f, 9.16f, 8.38f, 9.29f, 8.12f)
            lineTo(11.6f, 3.16f)
            curveTo(11.76f, 2.82f, 11.99f, 2.6f, 12f, 2.6f)
            close()
        }
    }.build()

private val FrameStarOutlineIcon = buildFrameStarIcon("FrameStarOutline", false)
private val FrameStarFilledIcon = buildFrameStarIcon("FrameStarFilled", true)


@Composable
internal fun Header(title: String, onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText, letterSpacing = .5.sp)
        Box(Modifier.size(36.dp).shadow(2.dp, CircleShape, ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(CircleShape).background(MaterialTheme.colorScheme.surface).noRippleClickable { onSettings() }, contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Settings, "设置", Modifier.size(22.dp), tint = MainText)
        }
    }
}

@Composable
internal fun SectionHeading(
    text: String,
    expanded: Boolean? = null,
    onToggle: (() -> Unit)? = null,
    chevronExpandDurationMs: Int = 300,
    chevronCollapseDurationMs: Int = 250,
    chevronExpandEasing: androidx.compose.animation.core.Easing = androidx.compose.animation.core.CubicBezierEasing(.34f, 1.12f, .64f, 1f),
    chevronCollapseEasing: androidx.compose.animation.core.Easing = androidx.compose.animation.core.CubicBezierEasing(.4f, 0f, 1f, 1f),
    horizontalPadding: androidx.compose.ui.unit.Dp = 0.dp
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
        Modifier.fillMaxWidth().padding(horizontal = horizontalPadding).padding(top = 10.dp, bottom = 12.dp)
            .then(if (onToggle != null) Modifier.clickable(interactionSource = interactionSource, indication = null) { onToggle() } else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(4.dp).height(16.dp).clip(CircleShape).background(ActiveAccent))
        Text(
            text, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = MainText,
            modifier = Modifier.padding(start = 8.dp).weight(1f).graphicsLayer { scaleX = titleScale; scaleY = titleScale }
        )
        if (expanded != null) Icon(Icons.Outlined.ExpandMore, null, tint = MutedText, modifier = Modifier.rotate(rotation).size(20.dp))
    }
}

@Composable
internal fun Stat(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = MutedText)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value, fontSize = 22.sp, lineHeight = 22.sp, fontWeight = FontWeight.ExtraBold, color = MainText,
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
internal fun VideoRow(video: VideoItem, subtitle: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 16.dp).shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).noRippleClickable(onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(100.dp, 65.dp).clip(RoundedCornerShape(10.dp)).background(PosterBrush(video.bgIndex)), contentAlignment = Alignment.Center) {
            Thumbnail(video, Modifier.fillMaxSize())
            if (video.progress > 0f) Box(Modifier.align(Alignment.BottomStart).fillMaxWidth((video.progress / 100f).coerceIn(0f, 1f)).height(3.dp).background(ActiveAccent))
        }
        Column(Modifier.weight(1f).padding(start = 15.dp)) {
            VideoCardTitle(video.name, fontSize = 15.sp, color = MainText, englishFontSize = 13.sp, englishColor = MutedText, fontWeight = FontWeight.Medium, maxLines = 1, showEnglish = true, englishSpacing = 2.dp)
            Text(subtitle, fontSize = 12.sp, color = MutedText, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PosterCard(
    video: VideoItem, compact: Boolean, onPlay: (VideoItem) -> Unit, onLong: (Rect) -> Unit,
    modifier: Modifier = Modifier, highlighted: Boolean = false, alpha: Float = 1f,
    onBoundsChanged: ((Rect) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val cardBounds = remember { arrayOf(Rect.Zero) }
    Column(
        modifier.graphicsLayer { this.alpha = alpha }
            .onGloballyPositioned {
                cardBounds[0] = it.boundsInRoot()
                onBoundsChanged?.invoke(cardBounds[0])
            }
            .noRippleCombinedClickable(onClick = { onPlay(video) }, onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onLong(cardBounds[0]) })
    ) {
        PosterCardVisual(video, compact, highlighted)
    }
}

@Composable
internal fun PosterCardVisual(video: VideoItem, compact: Boolean, highlighted: Boolean = false) {
    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f / 1.4f).shadow(if (highlighted) 6.dp else 4.dp, RoundedCornerShape(if (compact) 12.dp else 16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f)).clip(RoundedCornerShape(if (compact) 12.dp else 16.dp)).background(PosterBrush(video.bgIndex))) {
            Thumbnail(video, Modifier.fillMaxSize())
            if (video.progress > 0f) {
                Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().height(3.dp).background(Color.White.copy(alpha = .22f)))
                Box(Modifier.align(Alignment.BottomStart).fillMaxWidth((video.progress / 100f).coerceIn(0f, 1f)).height(3.dp).background(ActiveAccent))
            }
            if (video.isFav) Text("♥", color = Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(7.dp))
        }
        VideoCardTitle(
            video.name,
            fontSize = 13.sp,
            color = MainText,
            englishColor = MutedText,
            fontWeight = FontWeight.Medium,
            maxLines = if (compact) 2 else 1,
            showEnglish = !compact,
            combineEnglish = compact,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
internal fun Thumbnail(video: VideoItem, modifier: Modifier = Modifier) {
    val bitmap = produceState<ImageBitmap?>(
        initialValue = video.thumbPath?.let { thumbnailBitmapCache.get(it)?.asImageBitmap() },
        key1 = video.thumbPath
    ) {
        val path = video.thumbPath
        value = if (path.isNullOrBlank()) {
            null
        } else {
            val decoded = withContext(Dispatchers.IO) {
                thumbnailBitmapCache.get(path) ?: runCatching {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(path, bounds)
                    val maxDimension = maxOf(bounds.outWidth, bounds.outHeight)
                    val sampleSize = generateSequence(1) { it * 2 }
                        .takeWhile { maxDimension / it > 600 }
                        .lastOrNull() ?: 1
                    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                    BitmapFactory.decodeFile(path, options)?.also { thumbnailBitmapCache.put(path, it) }
                }.getOrNull()
            }
            decoded?.asImageBitmap()
        }
    }.value
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

internal fun PosterBrush(index: Int): Brush {
    val colors = when (((index - 1).coerceAtLeast(0)) % 4) {
        0 -> listOf(Color(0xFF667EEA), Color(0xFF764BA2))
        1 -> listOf(Color(0xFFF093FB), Color(0xFFF5576C))
        2 -> listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))
        else -> listOf(Color(0xFF43E97B), Color(0xFF38F9D7))
    }
    return Brush.linearGradient(colors)
}

@Composable
internal fun EmptyHint(
    text: String,
    horizontalPadding: androidx.compose.ui.unit.Dp = 20.dp,
    topPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    Text(text, color = MutedText, fontSize = 14.sp, modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding).padding(top = topPadding, bottom = 20.dp))
}


@Composable
internal fun RatingStars(
    rating: Float,
    size: androidx.compose.ui.unit.Dp,
    onRate: ((Float) -> Unit)? = null,
    spacing: androidx.compose.ui.unit.Dp = 2.dp,
    modifier: Modifier = Modifier
) {
    val normalizedRating = rating.coerceIn(0f, 5f)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(spacing), verticalAlignment = Alignment.CenterVertically) {
        (1..5).forEach { value ->
            val fullStar = normalizedRating >= value.toFloat()
            val halfStar = !fullStar && normalizedRating >= value - 0.5f
            Box(
                Modifier.size(size + 8.dp).then(
                    if (onRate != null) Modifier.pointerInput(onRate, value) {
                        detectTapGestures(
                            onTap = { onRate(value.toFloat()) },
                            onDoubleTap = { onRate(value - 0.5f) }
                        )
                    } else Modifier
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (fullStar) FrameStarFilledIcon else FrameStarOutlineIcon,
                    contentDescription = "评分 ${value - if (halfStar) 0.5f else 0f} 星",
                    tint = ActiveAccent,
                    modifier = Modifier.size(size)
                )
                if (halfStar) {
                    Box(
                        Modifier.width(size / 2f).height(size).clipToBounds(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Icon(
                            FrameStarFilledIcon,
                            contentDescription = null,
                            tint = ActiveAccent,
                            modifier = Modifier.size(size)
                        )
                    }
                }
            }
        }
    }
}


@Composable
internal fun AnimatedDropdownPopup(
    hazeState: HazeState,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: androidx.compose.ui.unit.IntOffset = androidx.compose.ui.unit.IntOffset.Zero,
    alignment: Alignment = Alignment.TopEnd,
    animateFromAnchor: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val visibilityState = remember { MutableTransitionState(false) }
    visibilityState.targetState = expanded

    if (visibilityState.currentState || visibilityState.targetState) {
        Popup(
            alignment = alignment,
            offset = offset,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true)
        ) {
            AnimatedVisibility(
                visibleState = visibilityState,
                enter = fadeIn(animationSpec = tween(if (animateFromAnchor) 320 else 280, easing = androidx.compose.animation.core.FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = tween(if (animateFromAnchor) 280 else 180, easing = androidx.compose.animation.core.FastOutSlowInEasing))
            ) {
                val revealProgress by transition.animateFloat(
                    transitionSpec = { tween(if (animateFromAnchor) 320 else 280, easing = androidx.compose.animation.core.FastOutSlowInEasing) },
                    label = "dropdownRevealProgress"
                ) { state ->
                    if (state == androidx.compose.animation.EnterExitState.Visible) 1f else 0f
                }
                val cornerRadius by transition.animateDp(
                    transitionSpec = { tween(if (animateFromAnchor) 320 else 280, easing = androidx.compose.animation.core.FastOutSlowInEasing) },
                    label = "sortMenuCornerRadius"
                ) { state ->
                    if (state == androidx.compose.animation.EnterExitState.Visible) 16.dp else 180.dp
                }
                val menuShape = if (animateFromAnchor) RoundedCornerShape(16.dp) else RoundedCornerShape(cornerRadius)

                Box(
                    modifier = modifier
                        .shadow(12.dp, menuShape, ambientColor = Color.Black.copy(alpha = .18f), spotColor = Color.Black.copy(alpha = .28f))
                        .drawWithContent {
                        if (animateFromAnchor) {
                            val visibleWidth = size.width * revealProgress
                            val visibleHeight = size.height * revealProgress
                            clipRect(
                                left = size.width - visibleWidth,
                                top = 0f,
                                right = size.width,
                                bottom = visibleHeight
                            ) {
                                this@drawWithContent.drawContent()
                            }
                        } else {
                            this@drawWithContent.drawContent()
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .hazeEffect(
                                state = hazeState,
                                style = HazeStyle(
                                    backgroundColor = PageBg,
                                    tints = emptyList(),
                                    blurRadius = 20.dp,
                                    noiseFactor = 0f
                                )
                            )
                            .clip(menuShape)
                            .background(Glass.copy(alpha = Glass.alpha - 0.05f))
                            .border(0.3.dp, LocalFramePalette.current.divider, menuShape),
                        content = content
                    )
                }
            }
        }
    }
}

@Composable
internal fun CustomSortMenuItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().height(42.dp)
            .noRippleClickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text,
            color = if (isSelected) ActiveAccent else MainText,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
        if (isSelected) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = ActiveAccent,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Spacer(Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SortOptionRow(
    title: String,
    isSelected: Boolean,
    direction: SortDirection,
    onAscending: () -> Unit,
    onDescending: () -> Unit
) {
    val ascendingSelected = isSelected && direction == SortDirection.ASCENDING
    val indicatorOffset by animateDpAsState(
        targetValue = if (ascendingSelected) 0.dp else 50.dp,
        animationSpec = tween(
            300,
            easing = androidx.compose.animation.core.CubicBezierEasing(.25f, 1f, .5f, 1f)
        ),
        label = "sort-direction-indicator"
    )
    val indicatorColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) ActiveAccent else Color.Transparent,
        animationSpec = tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "sort-direction-indicator-color"
    )
    val ascendingText by androidx.compose.animation.animateColorAsState(
        targetValue = if (ascendingSelected) Color.White else Color(0xFFAAAAAA),
        animationSpec = tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "sort-ascending-text"
    )
    val descendingText by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected && direction == SortDirection.DESCENDING) Color.White else Color(0xFFAAAAAA),
        animationSpec = tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "sort-descending-text"
    )
    Row(
        Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            Modifier.weight(1f),
            color = if (isSelected) MainText else MutedText,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
        Box(
            Modifier.width(96.dp).height(28.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                Modifier.offset(x = indicatorOffset)
                    .size(width = 46.dp, height = 28.dp)
                    .clip(RoundedCornerShape(90.dp))
                    .background(indicatorColor)
            )
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(width = 46.dp, height = 28.dp).noRippleClickable { onAscending() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("正序", color = ascendingText, fontSize = 13.sp, fontWeight = if (ascendingSelected) FontWeight.Bold else FontWeight.Normal)
                }
                Spacer(Modifier.width(4.dp))
                Box(
                    Modifier.size(width = 46.dp, height = 28.dp).noRippleClickable { onDescending() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("倒序", color = descendingText, fontSize = 13.sp, fontWeight = if (isSelected && direction == SortDirection.DESCENDING) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}
