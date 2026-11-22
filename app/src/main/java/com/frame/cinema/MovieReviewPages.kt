package com.frame.cinema

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun MovieReviewDetailPage(
    video: VideoItem,
    entry: MovieReviewEntry?,
    initialContent: String,
    onBack: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onCopy: () -> Unit
) {
    var content by rememberSaveable(video.id, entry?.id) { mutableStateOf(entry?.content ?: initialContent) }
    var isEditing by rememberSaveable(video.id, entry?.id) { mutableStateOf(entry == null) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    fun save() {
        if (content.isNotBlank()) {
            onSave(content)
            isEditing = false
        }
    }

    BackHandler(enabled = isEditing) { save() }

    Column(Modifier.fillMaxSize().background(PageBg).imePadding()) {
        Box(
            Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("影评", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, color = MainText)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Box(Modifier.size(40.dp).clickable { if (isEditing) save() else onBack() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "返回", tint = MainText)
                }
                Box(Modifier.size(40.dp).clickable {
                    if (isEditing) save() else showMenu = true
                }, contentAlignment = Alignment.Center) {
                    if (isEditing) {
                        Icon(Icons.Outlined.Check, contentDescription = "完成编辑", tint = ActiveAccent)
                    } else {
                        Icon(Icons.Outlined.MoreHoriz, contentDescription = "更多", tint = MainText)
                    }
                }
                AnimatedDropdownPopup(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    CustomSortMenuItem("导出", false) { showMenu = false; onExport() }
                    CustomSortMenuItem("复制", false) { showMenu = false; onCopy() }
                    CustomSortMenuItem("删除", false) { showMenu = false; showDeleteConfirm = true }
                }
            }
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 48.dp)
        ) {
            MovieReviewWorkHeader(video)
            Text(
                formatMovieReviewDate(entry?.createdAt ?: System.currentTimeMillis()),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = Color(0xFFAAAAAA)
            )
            Spacer(Modifier.height(18.dp))
            if (isEditing) {
                BasicTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 260.dp),
                    textStyle = TextStyle(fontSize = 16.sp, lineHeight = 28.sp, color = MainText),
                    decorationBox = { inner ->
                        Box(Modifier.fillMaxWidth()) {
                            if (content.isEmpty()) {
                                Text("写下你对这部作品的整体感受……", fontSize = 16.sp, lineHeight = 28.sp, color = MutedText)
                            }
                            inner()
                        }
                    }
                )
            } else {
                Text(
                    content,
                    modifier = Modifier.fillMaxWidth().clickable { isEditing = true },
                    fontSize = 16.sp,
                    lineHeight = 28.sp,
                    color = MainText
                )
            }
            Spacer(Modifier.height(32.dp))
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
private fun MovieReviewWorkHeader(video: VideoItem) {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.fillMaxWidth().defaultMinSize(minHeight = 68.dp),
            contentAlignment = Alignment.Center
        ) {
            VideoCardTitle(
                name = video.name,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                fontSize = 16.sp,
                color = if (displayVideoName(video.name) == "未命名") MutedText else MainText,
                englishFontSize = 14.sp,
                englishColor = MutedText,
                lineHeight = 22.sp,
                englishSpacing = 2.dp,
                maxLines = 2,
                showEnglish = true,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(5.dp))
        RatingStars(
            video.rating,
            14.dp,
            onRate = null,
            spacing = 1.dp,
            modifier = Modifier.graphicsLayer { translationX = -4.dp.toPx() }
        )
    }
}

private fun formatMovieReviewDate(timestamp: Long): String =
    SimpleDateFormat("yyyy/M/d", Locale.getDefault()).format(Date(timestamp))
