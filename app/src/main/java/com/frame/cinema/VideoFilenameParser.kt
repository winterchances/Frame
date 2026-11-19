package com.frame.cinema

import java.util.Locale

data class ParsedFilenameMetadata(
    val chineseTitle: String? = null,
    val englishTitle: String? = null,
    val season: Int? = null,
    val episodeStart: Int? = null,
    val episodeEnd: Int? = null,
    val languageTags: List<String> = emptyList(),
    val subtitleTags: List<String> = emptyList(),
    val technicalInfo: Map<String, List<String>> = emptyMap()
) {
    val displayTitle: String
        get() = listOfNotNull(chineseTitle, englishTitle).joinToString(" ").ifBlank { "" }
}

data class ParsedVideoFilename(val displayTitle: String, val metadata: ParsedFilenameMetadata)

/** Conservative parser: only returns metadata when the remaining title is unambiguous. */
object VideoFilenameParser {
    private val extensionRegex = Regex("""\.[A-Za-z0-9]{1,8}$""")
    private val separatorRegex = Regex("""[\s._\-–—()[\]{}]+""")
    private val cleanupRegex = Regex("""^[\s._\-–—()[\]{}]+|[\s._\-–—()[\]{}]+$""")

    private data class TagRule(val canonical: String, val pattern: Regex)

    private fun rules(vararg entries: Pair<String, String>): List<TagRule> =
        entries.map { (canonical, expression) ->
            TagRule(canonical, Regex("""(?i)(?<![A-Za-z0-9])(?:$expression)(?![A-Za-z0-9])"""))
        }.sortedByDescending { it.pattern.pattern.length }

    private val subtitleRules = rules(
        "中英双字" to "中英双字|中英字幕|中英双语字幕|双语字幕",
        "中文字幕" to "中文字幕|内嵌中字|内封中字|简体中文字幕|繁体中文字幕",
        "英文字幕" to "英文字幕|内嵌英字|English[ ._-]*Sub(?:title)?s?",
        "简中" to "简中|简体中字",
        "繁中" to "繁中|繁体中字",
        "中字" to "中字",
        "英字" to "英字",
        "内嵌字幕" to "内嵌字幕|内封字幕",
        "外挂字幕" to "外挂字幕|外置字幕",
        "硬字幕" to "硬字幕",
        "软字幕" to "软字幕",
        "ASS" to "ASS|SSA",
        "SRT" to "SRT",
        "VTT" to "VTT"
    )

    private val languageRules = rules(
        "繁体中文" to "繁体中文|繁中",
        "普通话" to "普通话|国语|国配",
        "粤语" to "粤语|粤语音轨",
        "中文" to "中文|汉语|华语",
        "英语" to "英语|英文|English|EN",
        "日语" to "日语|日文|Japanese|JPN|JP",
        "韩语" to "韩语|韩文|Korean|KOR|KR",
        "法语" to "法语|法文|French|FRA|FR",
        "德语" to "德语|德文|German|DEU|DE",
        "西班牙语" to "西班牙语|西语|Spanish|SPA|ES",
        "泰语" to "泰语|泰文|Thai|THA|TH",
        "双语" to "双语|多语|多语言|Multi[ ._-]*Audio"
    )

    private val technicalRules = listOf(
        "resolution" to listOf("2160P", "1080P", "720P", "576P", "480P", "4320P", "8K", "4K"),
        "videoCodec" to listOf("H.264", "H264", "AVC", "X264", "H.265", "H265", "HEVC", "X265", "AV1"),
        "audioCodec" to listOf("TRUEHD", "DTS-HD", "DTS", "EAC3", "DDP", "AC3", "AAC", "FLAC", "OPUS", "MP3"),
        "source" to listOf("WEB-DL", "WEBDL", "WEBRIP", "WEB-RIP", "BLURAY", "BDRIP", "BRRIP", "HDTV", "DVDRIP", "REMUX"),
        "hdr" to listOf("DOLBY VISION", "DOVI", "HDR10+", "HDR10", "HDR", "HLG"),
        "other" to listOf("PROPER", "REPACK", "再版", "修复版")
    )

    private val episodePatterns = listOf(
        Regex("""(?i)(?<![A-Za-z0-9])S(\d{1,2})\s*E(\d{1,3})(?:\s*[-~至]\s*(?:S\d{1,2}\s*E)?(\d{1,3}))?(?!\d)"""),
        Regex("""(?i)(?<![A-Za-z0-9])(\d{1,2})x(\d{1,3})(?:\s*[-~至]\s*(\d{1,3}))?(?!\d)"""),
        Regex("""(?i)(?<![A-Za-z0-9])EP\.?\s*(\d{1,3})(?:\s*[-~至]\s*(\d{1,3}))?(?!\d)"""),
        Regex("""(?i)(?<![A-Za-z0-9])E(\d{1,3})(?:\s*[-~至]\s*(\d{1,3}))?(?!\d)"""),
        Regex("""第\s*(\d{1,3})(?:\s*[-~至]\s*(\d{1,3}))?\s*集"""),
        Regex("""(?<!\d)(\d{1,3})(?:\s*[-~至]\s*(\d{1,3}))?\s*集""")
    )

    fun parse(fileName: String): ParsedVideoFilename? {
        val baseName = extensionRegex.replace(fileName.trim(), "")
        if (baseName.isBlank()) return null
        var remaining = baseName

        val subtitleTags = mutableListOf<String>()
        for (rule in subtitleRules) {
            if (rule.pattern.containsMatchIn(remaining)) {
                subtitleTags += rule.canonical
                remaining = rule.pattern.replace(remaining, " ")
            }
        }

        val languageTags = mutableListOf<String>()
        for (rule in languageRules) {
            if (rule.pattern.containsMatchIn(remaining)) {
                languageTags += rule.canonical
                remaining = rule.pattern.replace(remaining, " ")
            }
        }

        var season: Int? = null
        var episodeStart: Int? = null
        var episodeEnd: Int? = null
        for ((index, pattern) in episodePatterns.withIndex()) {
            val match = pattern.find(remaining) ?: continue
            val groups = match.groupValues
            when (index) {
                0, 1 -> {
                    season = groups[1].toIntOrNull()
                    episodeStart = groups[2].toIntOrNull()
                    episodeEnd = groups.getOrNull(3)?.toIntOrNull() ?: episodeStart
                }
                else -> {
                    episodeStart = groups[1].toIntOrNull()
                    episodeEnd = groups.getOrNull(2)?.toIntOrNull() ?: episodeStart
                }
            }
            remaining = pattern.replace(remaining, " ")
            break
        }

        val technicalInfo = linkedMapOf<String, MutableList<String>>()
        for ((category, values) in technicalRules) {
            for (value in values.sortedByDescending { it.length }) {
                val pattern = Regex("""(?i)(?<![A-Za-z0-9])${Regex.escape(value)}(?![A-Za-z0-9])""")
                if (pattern.containsMatchIn(remaining)) {
                    technicalInfo.getOrPut(category) { mutableListOf() }.add(value.uppercase(Locale.ROOT))
                    remaining = pattern.replace(remaining, " ")
                }
            }
        }

        remaining = cleanupRegex.replace(remaining, "").replace(separatorRegex, " ").trim()
        if (remaining.isBlank()) return null
        val titleParts = splitTitle(remaining) ?: return null
        val metadata = ParsedFilenameMetadata(
            chineseTitle = titleParts.first,
            englishTitle = titleParts.second,
            season = season,
            episodeStart = episodeStart,
            episodeEnd = episodeEnd,
            languageTags = languageTags.distinct(),
            subtitleTags = subtitleTags.distinct(),
            technicalInfo = technicalInfo.mapValues { it.value.distinct() }
        )
        return ParsedVideoFilename(metadata.displayTitle, metadata)
    }

    private fun splitTitle(value: String): Pair<String?, String?>? {
        val title = value.trim()
        val hasHan = title.any { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.HAN }
        val hasLatin = title.any { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.LATIN }
        if (hasHan && !hasLatin) return title to null
        if (hasLatin && !hasHan) return null to title
        if (!hasHan || !hasLatin) return null

        val delimiters = Regex("""\s+|\s*[-_–—]\s*""")
        for (delimiter in delimiters.findAll(title)) {
            val left = title.substring(0, delimiter.range.first).trim()
            val right = title.substring(delimiter.range.last + 1).trim()
            if (left.isBlank() || right.isBlank()) continue
            val leftHan = left.any { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.HAN }
            val leftLatin = left.any { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.LATIN }
            val rightHan = right.any { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.HAN }
            val rightLatin = right.any { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.LATIN }
            if (leftHan && !leftLatin && rightLatin && !rightHan) return left to right
            if (leftLatin && !leftHan && rightHan && !rightLatin) return right to left
        }
        return null
    }
}
