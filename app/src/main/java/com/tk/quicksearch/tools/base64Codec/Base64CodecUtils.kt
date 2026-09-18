package com.tk.quicksearch.tools.base64Codec

import android.util.Base64

object Base64CodecUtils {
    private val base64Pattern = Regex("^[A-Za-z0-9+/=_\\-\\s]+$")

    fun isCandidate(query: String): Boolean {
        val trimmed = query.trim()
        if (trimmed.length < 4) return false
        if (trimmed.length > 10000) return false
        return trimmed.startsWith("b64 ", ignoreCase = true) ||
            trimmed.startsWith("base64 ", ignoreCase = true) ||
            trimmed.startsWith("encode ", ignoreCase = true) ||
            trimmed.startsWith("decode ", ignoreCase = true) ||
            (base64Pattern.matches(trimmed) && trimmed.length % 4 == 0 && trimmed.length >= 8)
    }

    fun encode(input: String): String =
        Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

    fun decode(input: String): String? =
        try {
            val normalized = input.trim().replace("\\s".toRegex(), "")
            val padded = normalizePadding(normalized)
            val bytes = Base64.decode(padded, Base64.DEFAULT)
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }

    private fun normalizePadding(input: String): String {
        val urlSafe = input.replace('-', '+').replace('_', '/')
        val remainder = urlSafe.length % 4
        return if (remainder == 0) urlSafe else urlSafe + "=".repeat(4 - remainder)
    }

    fun detectAndProcess(query: String): Pair<String, String>? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null
        val lower = trimmed.lowercase()
        return when {
            lower.startsWith("b64 ") || lower.startsWith("base64 ") -> {
                val payload = trimmed.substringAfter(' ').trim()
                if (payload.isEmpty()) return null
                val decoded = decode(payload)
                if (decoded != null) {
                    payload to decoded
                } else {
                    payload to encode(payload)
                }
            }
            lower.startsWith("encode ") -> {
                val payload = trimmed.substringAfter(' ').trim()
                if (payload.isEmpty()) return null
                payload to encode(payload)
            }
            lower.startsWith("decode ") -> {
                val payload = trimmed.substringAfter(' ').trim()
                val decoded = decode(payload) ?: return null
                payload to decoded
            }
            else -> {
                if (!isCandidate(trimmed)) return null
                val decoded = decode(trimmed) ?: return null
                if (!isPlausiblyDecoded(decoded)) return null
                trimmed to decoded
            }
        }
    }

    private fun isPlausiblyDecoded(decoded: String): Boolean {
        if (decoded.isEmpty() || decoded.length > 5000) return false
        val printable = decoded.count { it in ' '..'~' || it == '\n' || it == '\t' }
        return printable.toDouble() / decoded.length >= 0.8
    }
}
