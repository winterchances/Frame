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



internal val FrameHomeIcon: ImageVector = ImageVector.Builder("FrameHome", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(3f, 9f); lineTo(12f, 2f); lineTo(21f, 9f); lineTo(21f, 20f); quadTo(21f, 22f, 19f, 22f); lineTo(5f, 22f); quadTo(3f, 22f, 3f, 20f); close()
        moveTo(9f, 22f); lineTo(9f, 12f); lineTo(15f, 12f); lineTo(15f, 22f)
    }
}.build()
internal val FrameLibraryIcon: ImageVector = ImageVector.Builder("FrameLibrary", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(4.5f, 2f); lineTo(19.5f, 2f); quadTo(22f, 2f, 22f, 4.5f); lineTo(22f, 19.5f); quadTo(22f, 22f, 19.5f, 22f); lineTo(4.5f, 22f); quadTo(2f, 22f, 2f, 19.5f); lineTo(2f, 4.5f); quadTo(2f, 2f, 4.5f, 2f); close()
        moveTo(7f, 2f); lineTo(7f, 22f); moveTo(17f, 2f); lineTo(17f, 22f); moveTo(2f, 12f); lineTo(22f, 12f)
        moveTo(2f, 7f); lineTo(7f, 7f); moveTo(2f, 17f); lineTo(7f, 17f); moveTo(17f, 7f); lineTo(22f, 7f); moveTo(17f, 17f); lineTo(22f, 17f)
    }
}.build()
internal val FrameRecentIcon: ImageVector = ImageVector.Builder("FrameRecent", 24.dp, 24.dp, 24f, 24f).apply {
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
internal val FrameTimelineClockIcon: ImageVector = ImageVector.Builder("FrameTimelineClock", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color(0xFF888888)), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(12f, 3f); curveTo(16.97f, 3f, 21f, 7.03f, 21f, 12f)
        curveTo(21f, 16.97f, 16.97f, 21f, 12f, 21f)
        curveTo(7.03f, 21f, 3f, 16.97f, 3f, 12f)
        curveTo(3f, 7.03f, 7.03f, 3f, 12f, 3f); close()
        moveTo(12f, 7f); lineTo(12f, 12f); lineTo(15f, 15f)
    }
}.build()
internal val FrameTimelinePlayIcon: ImageVector = ImageVector.Builder("FrameTimelinePlay", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color(0xFF888888)), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(5f, 3f); lineTo(19f, 12f); lineTo(5f, 21f); close()
    }
}.build()
internal val FrameFeatherIcon: ImageVector = ImageVector.Builder("FrameFeather", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.25f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(20.5f, 3.5f)
        curveTo(15.7f, 2.8f, 9.1f, 5.2f, 6.1f, 10.1f)
        curveTo(3.9f, 13.7f, 4.1f, 18.1f, 6.8f, 20.5f)
        curveTo(10.4f, 20.1f, 14.1f, 16.8f, 16.8f, 11.8f)
        lineTo(20.5f, 3.5f)
        moveTo(6.2f, 19.2f)
        curveTo(9.1f, 14.4f, 13.4f, 9.6f, 19.1f, 4.7f)
        moveTo(9.1f, 15.1f); lineTo(6.8f, 13.7f)
        moveTo(12.1f, 11.8f); lineTo(9.7f, 10.2f)
        moveTo(15.1f, 8.5f); lineTo(12.9f, 7.1f)
        moveTo(8.4f, 17.3f); lineTo(10.9f, 17.8f)
        moveTo(11.4f, 13.9f); lineTo(14f, 14.4f)
    }
}.build()

internal val FrameRecordsIcon: ImageVector = ImageVector.Builder("FrameRecords", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(4f, 19.5f); quadTo(4f, 17f, 6.5f, 17f); lineTo(20f, 17f); moveTo(6.5f, 2f); lineTo(20f, 2f); lineTo(20f, 22f); lineTo(6.5f, 22f); quadTo(4f, 22f, 4f, 19.5f); lineTo(4f, 4.5f); quadTo(4f, 2f, 6.5f, 2f); close()
    }
}.build()
internal val FrameTagIcon: ImageVector = ImageVector.Builder("FrameTag", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(20.59f, 13.41f); lineTo(13.42f, 20.58f); quadTo(12.25f, 21.75f, 11.01f, 20.58f); lineTo(2f, 12f); lineTo(2f, 2f); lineTo(12f, 2f); lineTo(20.59f, 10.59f); quadTo(21.76f, 11.76f, 20.59f, 13.41f); close()
        moveTo(7f, 7f); lineTo(7.01f, 7f)
    }
}.build()
internal val FrameSortIcon: ImageVector = ImageVector.Builder("FrameSort", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(7f, 3f); lineTo(7f, 21f)
        moveTo(3f, 7f); lineTo(7f, 3f); lineTo(11f, 7f)
        moveTo(17f, 21f); lineTo(17f, 3f)
        moveTo(13f, 17f); lineTo(17f, 21f); lineTo(21f, 17f)
    }
}.build()

internal val FrameSortDirectionIcon: ImageVector = ImageVector.Builder("FrameSortDirection", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(12f, 20f); lineTo(12f, 4f)
        moveTo(6f, 10f); lineTo(12f, 4f); lineTo(18f, 10f)
    }
}.build()
internal val FrameGridIcon: ImageVector = ImageVector.Builder("FrameGrid", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(3f, 3f); lineTo(10f, 3f); lineTo(10f, 10f); lineTo(3f, 10f); close()
        moveTo(14f, 3f); lineTo(21f, 3f); lineTo(21f, 10f); lineTo(14f, 10f); close()
        moveTo(14f, 14f); lineTo(21f, 14f); lineTo(21f, 21f); lineTo(14f, 21f); close()
        moveTo(3f, 14f); lineTo(10f, 14f); lineTo(10f, 21f); lineTo(3f, 21f); close()
    }
}.build()

internal val FrameSearchIcon: ImageVector = ImageVector.Builder("FrameSearch", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(11f, 3f); curveTo(15.42f, 3f, 19f, 6.58f, 19f, 11f); curveTo(19f, 15.42f, 15.42f, 19f, 11f, 19f); curveTo(6.58f, 19f, 3f, 15.42f, 3f, 11f); curveTo(3f, 6.58f, 6.58f, 3f, 11f, 3f); close()
        moveTo(21f, 21f); lineTo(16.65f, 16.65f)
    }
}.build()
internal val FrameTrashIcon: ImageVector = ImageVector.Builder("FrameTrash", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(3f, 6f); lineTo(21f, 6f); moveTo(19f, 6f); lineTo(19f, 20f); quadTo(19f, 22f, 17f, 22f); lineTo(7f, 22f); quadTo(5f, 22f, 5f, 20f); lineTo(5f, 6f)
        moveTo(8f, 6f); lineTo(8f, 4f); quadTo(8f, 2f, 10f, 2f); lineTo(14f, 2f); quadTo(16f, 2f, 16f, 4f); lineTo(16f, 6f)
    }
}.build()

internal val FrameHamburgerIcon: ImageVector = ImageVector.Builder("FrameMenu", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = null, stroke = SolidColor(Color(0xFF333333)), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(5f, 8f); lineTo(19f, 8f); moveTo(5f, 16f); lineTo(19f, 16f)
    }
}.build()
