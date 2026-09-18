package com.tk.quicksearch.tools.urlCodec

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object URLCodecUtils {
    private val urlEncodePattern = Regex("^(url ?encode|encode ?url)\\s+", RegexOption.IGNORE_CASE)
    private val urlDecodePattern = Regex("^(url ?decode|decode ?url)\\s+", RegexOption.IGNORE_CASE)

    fun isCandidate(query: String): Boolean {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return false
        val lower = trimmed.lowercase()
        return lower.startsWith("url encode ") || lower.startsWith("encode url ") ||
            lower.startsWith("url decode ") || lower.startsWith("decode url ")
    }

    fun encode(input: String): String {
        return URLEncoder.encode(input, StandardCharsets.UTF_8)
            .replace("+", "%20")
            .replace("%7E", "~")
    }

    fun decode(input: String): String? {
        return try {
            URLDecoder.decode(input, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    fun detectAndProcess(query: String): Pair<String, String>? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null
        val lower = trimmed.lowercase()

        return when {
            urlEncodePattern.matches(lower) -> {
                val matchResult = urlEncodePattern.find(lower)!!
                val prefixEnd = matchResult.range.last + 1
                val payload = trimmed.substring(prefixEnd).trim()
                if (payload.isEmpty()) return null
                payload to encode(payload)
            }
            urlDecodePattern.matches(lower) -> {
                val matchResult = urlDecodePattern.find(lower)!!
                val prefixEnd = matchResult.range.last + 1
                val payload = trimmed.substring(prefixEnd).trim()
                val decoded = decode(payload) ?: return null
                payload to decoded
            }
            else -> null
        }
    }
}