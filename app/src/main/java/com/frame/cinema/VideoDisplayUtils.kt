package com.frame.cinema

import java.math.BigInteger
import java.text.Collator
import java.util.Locale

private val ChineseNameCollator: Collator by lazy {
    Collator.getInstance(Locale.CHINA).apply { strength = Collator.PRIMARY }
}

internal fun compareNaturalVideoNames(left: String, right: String): Int {
    val leftName = displayVideoName(left)
    val rightName = displayVideoName(right)
    val tokenRegex = Regex("\\d+|\\D+")
    val leftTokens = tokenRegex.findAll(leftName).map { it.value }.toList()
    val rightTokens = tokenRegex.findAll(rightName).map { it.value }.toList()
    for (index in 0 until minOf(leftTokens.size, rightTokens.size)) {
        val a = leftTokens[index]
        val b = rightTokens[index]
        val aNumeric = a.all { it.isDigit() }
        val bNumeric = b.all { it.isDigit() }
        val comparison = when {
            aNumeric && bNumeric -> {
                val numberComparison = BigInteger(a).compareTo(BigInteger(b))
                if (numberComparison != 0) numberComparison else a.length.compareTo(b.length)
            }
            aNumeric != bNumeric -> if (aNumeric) -1 else 1
            else -> ChineseNameCollator.compare(a, b)
        }
        if (comparison != 0) return comparison
    }
    return leftTokens.size.compareTo(rightTokens.size)
}

internal fun displayVideoName(name: String): String {
    val displayName = name.replace(
        Regex("\\.(mp4|m4v|mkv|avi|mov|wmv|flv|webm|3gp|3g2|ts|mts|m2ts|mpg|mpeg|mpe|mpv|ogv|ogg|asf|rm|rmvb|vob|divx|f4v|mxf|nsv|wtv|dvr-ms|amv|hevc|h264|h265)$", RegexOption.IGNORE_CASE), ""
    )
    return displayName.ifBlank { "未命名" }
}

private val ChineseEnglishTitleRegex = Regex(
    "^(.+?)\\s+([\\p{IsLatin}\\p{M}\\p{N}\\[（(「『“‘][\\p{IsLatin}\\p{M}\\p{N}\\s&＆'’.,:;!?()\\[\\]{}+_\\-/]*)$"
)

internal fun splitChineseEnglishTitle(title: String): Pair<String, String?> {
    val cleanTitle = title.trim()
    val match = ChineseEnglishTitleRegex.matchEntire(cleanTitle)
    return if (match != null) {
        match.groupValues[1].trim() to match.groupValues[2].trim().takeIf { it.isNotEmpty() }
    } else {
        cleanTitle to null
    }
}

internal fun splitDisplayVideoTitle(title: String): Pair<String, String?> {
    val cleanTitle = displayVideoName(title).trim()
    return splitChineseEnglishTitle(cleanTitle)
}
