package com.frame.cinema

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    stateKey: String = video.id.toString(),
    initialContent: String,
    onBack: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
    onRate: (Float) -> Unit,
    onExport: () -> Unit,
    onCopy: () -> Unit
) {
    var content by rememberSaveable(stateKey, entry?.id) { mutableStateOf(entry?.content ?: initialContent) }
    var isEditing by rememberSaveable(stateKey, entry?.id) { mutableStateOf(entry == null) }
    var showMenu by remember { mutableStateOf(false) }

    fun save(): Boolean {
        if (content.isBlank()) return false
        onSave(content)
        isEditing = false
        return true
    }

    BackHandler(enabled = isEditing) { if (!save()) onBack() }

    Column(Modifier.fillMaxSize().background(PageBg).imePadding()) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(top = 40.dp).height(40.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
                Box(
                    Modifier.size(36.dp)
                        .shadow(2.dp, CircleShape, ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { if (isEditing) { if (!save()) onBack() } else onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "返回", tint = MainText, modifier = Modifier.size(20.dp))
                }
                Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.fillMaxSize()
                            .shadow(2.dp, CircleShape, ambientColor = Color.Black.copy(alpha = .03f), spotColor = Color.Black.copy(alpha = .03f))
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .noRippleClickable { if (isEditing) save() else showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isEditing) {
                            Icon(Icons.Outlined.Check, contentDescription = "完成编辑", tint = MainText, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Outlined.MoreHoriz, contentDescription = "更多", tint = MainText, modifier = Modifier.size(20.dp))
                        }
                    }
                    AnimatedDropdownPopup(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.width(152.dp),
                        animateFromAnchor = true
                    ) {
                        CustomSortMenuItem("导出", false) { showMenu = false; onExport() }
                        CustomSortMenuItem("复制", false) { showMenu = false; onCopy() }
                        CustomSortMenuItem("删除", false) { showMenu = false; onDelete() }
                    }
                }
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(top = 24.dp, bottom = 48.dp)
        ) {
            MovieReviewWorkHeader(video, onRate)
            Text(
                formatMovieReviewDate(entry?.createdAt ?: System.currentTimeMillis()),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                fontSize = 12.sp,
                color = Color(0xFFAAAAAA)
            )
            Spacer(Modifier.height(18.dp))
            if (isEditing) {
                BasicTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 260.dp),
                    textStyle = TextStyle.Default.copy(
                        fontSize = 16.sp,
                        lineHeight = 28.sp,
                        letterSpacing = 0.sp,
                        color = MainText,
                    ),
                    decorationBox = { inner ->
                        Box(Modifier.fillMaxWidth()) {
                            if (content.isEmpty()) {
                                Text(
                                    "写下你对这部作品的整体感受……",
                                    fontSize = 16.sp,
                                    letterSpacing = 0.sp,
                                    color = MutedText,
                                    style = TextStyle.Default.copy(
                                        fontSize = 16.sp,
                                        lineHeight = 28.sp,
                                        letterSpacing = 0.sp,
                                        color = MutedText,
                                    )
                                )
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
                    letterSpacing = 0.sp,
                    color = MainText,
                    style = TextStyle.Default.copy(
                        fontSize = 16.sp,
                        lineHeight = 28.sp,
                        letterSpacing = 0.sp,
                        color = MainText,
                    )
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MovieReviewWorkHeader(video: VideoItem, onRate: (Float) -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 18.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            Modifier.fillMaxWidth().defaultMinSize(minHeight = 68.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            VideoCardTitle(
                name = video.name,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                fontSize = 18.sp,
                color = if (displayVideoName(video.name) == "未命名") MutedText else MainText,
                englishFontSize = 15.sp,
                englishColor = MutedText,
                lineHeight = 24.sp,
                englishSpacing = 2.dp,
                maxLines = 2,
                showEnglish = true,
                textAlign = TextAlign.Start
            )
        }
        Spacer(Modifier.height(5.dp))
        RatingStars(
            video.rating,
            14.dp,
            onRate = onRate,
            spacing = 1.dp,
            modifier = Modifier.offset(x = (-4).dp)
        )
    }
}

private fun formatMovieReviewDate(timestamp: Long): String =
    SimpleDateFormat("yyyy/M/d", Locale.getDefault()).format(Date(timestamp))
