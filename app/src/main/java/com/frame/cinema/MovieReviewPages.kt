package com.frame.cinema

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    Column(Modifier.fillMaxSize().background(PageBg)) {
        MovieReviewTopBar(
            title = "影评",
            onBack = onBack,
            onAction = onCreate,
            showAddIcon = true
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item(key = "movie-reviews-work") {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier.fillMaxWidth().defaultMinSize(minHeight = 68.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MovieReviewVideoTitle(video.name)
                    }
                    Spacer(Modifier.height(5.dp))
                    Text("${reviews.size} 条影评", fontSize = 12.sp, color = MutedText)
                }
            }
            if (reviews.isEmpty()) {
                item(key = "movie-reviews-empty") {
                    Text(
                        "还没有影评，写下第一篇吧",
                        fontSize = 14.sp,
                        color = MutedText,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 28.dp)
                    )
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
    }
}

@Composable
internal fun MovieReviewDetailPage(
    video: VideoItem,
    entry: MovieReviewEntry,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember(entry.id) { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(PageBg)) {
        MovieReviewTopBar(
            title = "影评详情",
            onBack = onBack,
            actionLabel = "编辑",
            onAction = onEdit
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 48.dp)
        ) {
            Box(
                Modifier.fillMaxWidth().defaultMinSize(minHeight = 84.dp),
                contentAlignment = Alignment.Center
            ) {
                MovieReviewVideoTitle(video.name)
            }
            Text(
                entry.title.ifBlank { "未命名影评" },
                fontSize = 22.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (entry.title.isBlank()) MutedText else MainText
            )
            Spacer(Modifier.height(10.dp))
            Text(formatMovieReviewDate(entry.createdAt), fontSize = 11.sp, color = MutedText)
            Spacer(Modifier.height(22.dp))
            Text(entry.content, fontSize = 15.sp, lineHeight = 26.sp, color = MainText)
            Spacer(Modifier.height(32.dp))
            Text(
                "删除影评",
                fontSize = 13.sp,
                color = MutedText,
                modifier = Modifier.clickable { showDeleteConfirm = true }.padding(vertical = 10.dp)
            )
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
    val canSave = content.isNotBlank()
    Column(Modifier.fillMaxSize().background(PageBg).imePadding()) {
        MovieReviewTopBar(
            title = if (isEditing) "编辑影评" else "写影评",
            onBack = onBack,
            actionLabel = "保存",
            actionEnabled = canSave,
            onAction = { if (canSave) onSave(title, content) }
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 32.dp)
        ) {
            Box(
                Modifier.fillMaxWidth().defaultMinSize(minHeight = 84.dp),
                contentAlignment = Alignment.Center
            ) {
                MovieReviewVideoTitle(video.name)
            }
            Spacer(Modifier.height(8.dp))
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 42.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                    .clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface).padding(11.dp),
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, color = MainText),
                decorationBox = { inner ->
                    Box {
                        if (title.isEmpty()) Text("输入标题（选填）", fontSize = 14.sp, color = MutedText)
                        inner()
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
            BasicTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 260.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                    .clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(15.dp),
                textStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, color = MainText),
                decorationBox = { inner ->
                    Box {
                        if (content.isEmpty()) Text("写下你对这部作品的整体感受……", fontSize = 16.sp, lineHeight = 24.sp, color = MutedText)
                        inner()
                    }
                }
            )
        }
    }
}

@Composable
private fun MovieReviewTopBar(
    title: String,
    onBack: () -> Unit,
    actionLabel: String? = null,
    onAction: () -> Unit,
    actionEnabled: Boolean = true,
    showAddIcon: Boolean = false
) {
    Box(
        Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MainText)
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                Modifier.size(40.dp).clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "返回", tint = MainText)
            }
            Box(
                Modifier.size(40.dp).clickable(enabled = actionEnabled, onClick = onAction),
                contentAlignment = Alignment.Center
            ) {
                if (showAddIcon) {
                    Icon(Icons.Outlined.Add, contentDescription = "新增影评", tint = MainText)
                } else if (actionLabel != null) {
                    Text(
                        actionLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (actionEnabled) ActiveAccent else MutedText.copy(alpha = .45f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MovieReviewVideoTitle(name: String) {
    VideoCardTitle(
        name = name,
        modifier = Modifier.fillMaxWidth(),
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = if (displayVideoName(name) == "未命名") MutedText else MainText,
        englishFontSize = 14.sp,
        englishColor = MutedText,
        lineHeight = 22.sp,
        englishSpacing = 2.dp,
        maxLines = 2,
        showEnglish = true,
        textAlign = TextAlign.Center
    )
}

private fun formatMovieReviewDate(timestamp: Long): String =
    SimpleDateFormat("yyyy/M/d", Locale.getDefault()).format(Date(timestamp))
