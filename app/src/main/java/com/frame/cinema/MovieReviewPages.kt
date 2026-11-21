package com.frame.cinema

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun MovieReviewListPage(
    video: VideoItem,
    reviews: List<MovieReviewEntry>,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onOpen: (MovieReviewEntry) -> Unit
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val showTopBar by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 20 }
    }
    val density = LocalDensity.current
    val statusBarBottomPx = WindowInsets.statusBars.getTop(density).toFloat()
    val titleSafetyGapPx = with(density) { 4.dp.toPx() }
    val titleTopPx = remember { mutableFloatStateOf(0f) }
    val titleAlpha by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 0f
            else titleFadeAlpha(listState.firstVisibleItemScrollOffset.toFloat(), titleTopPx.floatValue, statusBarBottomPx, titleSafetyGapPx)
        }
    }
    val showStickyTitle = showTopBar && titleAlpha <= 0f
    Box(Modifier.fillMaxSize().background(PageBg)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 104.dp)
        ) {
            item(key = "movie-reviews-title") {
                Row(
                    Modifier.fillMaxWidth()
                        .onGloballyPositioned {
                            titleTopPx.floatValue = it.positionInWindow().y + listState.firstVisibleItemScrollOffset + with(density) { 40.dp.toPx() }
                        }
                        .graphicsLayer { alpha = titleAlpha }
                        .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Icon(
                        Icons.Outlined.ArrowBack,
                        contentDescription = "返回",
                        tint = MainText,
                        modifier = Modifier.size(24.dp).clickable(onClick = onBack)
                    )
                    Spacer(Modifier.width(14.dp))
                    Text("影评", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText, modifier = Modifier.weight(1f))
                    androidx.compose.material3.Icon(
                        Icons.Outlined.Add,
                        contentDescription = "新增影评",
                        tint = MainText,
                        modifier = Modifier.size(24.dp).clickable(onClick = onCreate)
                    )
                }
            }
            item(key = "movie-reviews-work") {
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 18.dp)) {
                    Text(video.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MutedText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(5.dp))
                    Text("${reviews.size} 条影评", fontSize = 12.sp, color = MutedText)
                }
            }
            if (reviews.isEmpty()) {
                item(key = "movie-reviews-empty") {
                    Text("还没有影评，写下第一篇吧", fontSize = 14.sp, color = MutedText, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 28.dp))
                }
            } else {
                items(reviews, key = { it.id }) { entry ->
                    Column(
                        Modifier.fillMaxWidth().clickable { onOpen(entry) }.padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                entry.title.ifBlank { "未命名影评" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (entry.title.isBlank()) MutedText else MainText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(formatMovieReviewDate(entry.createdAt), fontSize = 11.sp, color = MutedText)
                        }
                        Spacer(Modifier.height(7.dp))
                        Text(entry.content, fontSize = 13.sp, lineHeight = 20.sp, color = MutedText, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(0.5.dp).background(LocalFramePalette.current.divider))
                }
            }
        }
        AnimatedVisibility(
            visible = showStickyTitle,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                Modifier.fillMaxWidth().background(PageBg).padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("影评", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MainText)
            }
        }
    }
}

@Composable
internal fun MovieReviewDetailPage(
    entry: MovieReviewEntry,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember(entry.id) { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val statusBarBottomPx = WindowInsets.statusBars.getTop(density).toFloat()
    val titleSafetyGapPx = with(density) { 4.dp.toPx() }
    val titleTopPx = remember { mutableFloatStateOf(0f) }
    val titleAlpha by remember {
        derivedStateOf { titleFadeAlpha(scrollState.value.toFloat(), titleTopPx.floatValue, statusBarBottomPx, titleSafetyGapPx) }
    }
    val showTopBar by remember { derivedStateOf { scrollState.value > 20 && titleAlpha <= 0f } }
    Box(Modifier.fillMaxSize().background(PageBg)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 20.dp).padding(bottom = 48.dp)
        ) {
            MovieReviewLargeHeader(
                "影评详情", onBack, action = "编辑", onAction = onEdit,
                modifier = Modifier.onGloballyPositioned { titleTopPx.floatValue = it.positionInWindow().y + scrollState.value }
                    .graphicsLayer { alpha = titleAlpha }
            )
            Text(entry.title.ifBlank { "未命名影评" }, fontSize = 22.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold, color = if (entry.title.isBlank()) MutedText else MainText)
            Spacer(Modifier.height(10.dp))
            Text(formatMovieReviewDate(entry.createdAt), fontSize = 11.sp, color = MutedText)
            Spacer(Modifier.height(22.dp))
            Text(entry.content, fontSize = 15.sp, lineHeight = 26.sp, color = MainText)
            Spacer(Modifier.height(32.dp))
            Text("删除影评", fontSize = 13.sp, color = MutedText, modifier = Modifier.clickable { showDeleteConfirm = true }.padding(vertical = 10.dp))
        }
        AnimatedVisibility(visible = showTopBar, enter = fadeIn(tween(200)), exit = fadeOut(tween(200)), modifier = Modifier.align(Alignment.TopCenter)) {
            Box(Modifier.fillMaxWidth().background(PageBg).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                Text("影评详情", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MainText)
            }
        }
    }
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除这篇影评？", color = MainText) },
            text = { Text("删除后无法恢复，但不会影响作品评分和随记。", color = MutedText) },
            confirmButton = { TextButton(onClick = { showDeleteConfirm = false; onDelete() }) { Text("删除", color = ActiveAccent) } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("取消", color = MutedText) } },
            containerColor = PageBg,
            shape = RoundedCornerShape(30.dp)
        )
    }
}

@Composable
internal fun MovieReviewEditorPage(
    video: VideoItem,
    initialTitle: String,
    initialContent: String,
    isEditing: Boolean,
    sessionKey: Int,
    onBack: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by rememberSaveable(sessionKey, video.id, isEditing, initialTitle) { mutableStateOf(initialTitle) }
    var content by rememberSaveable(sessionKey, video.id, isEditing, initialContent) { mutableStateOf(initialContent) }
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val statusBarBottomPx = WindowInsets.statusBars.getTop(density).toFloat()
    val titleSafetyGapPx = with(density) { 4.dp.toPx() }
    val titleTopPx = remember { mutableFloatStateOf(0f) }
    val titleAlpha by remember {
        derivedStateOf { titleFadeAlpha(scrollState.value.toFloat(), titleTopPx.floatValue, statusBarBottomPx, titleSafetyGapPx) }
    }
    val showTopBar by remember { derivedStateOf { scrollState.value > 20 && titleAlpha <= 0f } }
    val canSave = content.isNotBlank()
    Box(Modifier.fillMaxSize().background(PageBg).imePadding()) {
        Column(Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            MovieReviewLargeHeader(
                if (isEditing) "编辑影评" else "写影评", onBack, action = "保存",
                onAction = { if (canSave) onSave(title, content) }, actionEnabled = canSave,
                modifier = Modifier.onGloballyPositioned { titleTopPx.floatValue = it.positionInWindow().y + scrollState.value }
                    .graphicsLayer { alpha = titleAlpha }
            )
            Text(video.name, fontSize = 13.sp, color = MutedText, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(24.dp))
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = TextStyle(fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.Medium, color = MainText),
                decorationBox = { inner ->
                    Box {
                        if (title.isEmpty()) Text("标题（选填）", fontSize = 18.sp, color = MutedText)
                        inner()
                    }
                }
            )
            Spacer(Modifier.height(18.dp))
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(LocalFramePalette.current.divider))
            Spacer(Modifier.height(18.dp))
            BasicTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 260.dp),
                textStyle = TextStyle(fontSize = 15.sp, lineHeight = 26.sp, color = MainText),
                decorationBox = { inner ->
                    Box {
                        if (content.isEmpty()) Text("写下你对这部作品的整体感受……", fontSize = 15.sp, lineHeight = 26.sp, color = MutedText)
                        inner()
                    }
                }
            )
        }
        AnimatedVisibility(visible = showTopBar, enter = fadeIn(tween(200)), exit = fadeOut(tween(200)), modifier = Modifier.align(Alignment.TopCenter)) {
            Box(Modifier.fillMaxWidth().background(PageBg).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                Text(if (isEditing) "编辑影评" else "写影评", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MainText)
            }
        }
    }
}

@Composable
private fun MovieReviewLargeHeader(
    title: String,
    onBack: () -> Unit,
    action: String,
    onAction: () -> Unit,
    actionEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth().padding(top = 40.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Icon(
            Icons.Outlined.ArrowBack,
            contentDescription = "返回",
            tint = MainText,
            modifier = Modifier.size(24.dp).clickable(onClick = onBack)
        )
        Spacer(Modifier.width(14.dp))
        Text(title, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MainText, modifier = Modifier.weight(1f))
        Text(
            action,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (actionEnabled) ActiveAccent else MutedText.copy(alpha = .45f),
            modifier = Modifier.clickable(enabled = actionEnabled, onClick = onAction).padding(6.dp)
        )
    }
}

private fun formatMovieReviewDate(timestamp: Long): String =
    SimpleDateFormat("yyyy/M/d", Locale.getDefault()).format(Date(timestamp))
