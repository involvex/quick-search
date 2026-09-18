package com.tk.quicksearch.tools.timestampConverter

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimestampConverterUtils {
    private val epochPattern = Regex("^\\d{10,13}$")
    private val isoPattern = Regex("^\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,3})?(Z|[+-]\\d{2}:?\\d{2})?$")

    fun isCandidate(query: String): Boolean {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return false
        val lower = trimmed.lowercase()
        return lower.startsWith("epoch ") || lower.startsWith("timestamp ") ||
            lower.startsWith("iso ") || lower.startsWith("date ") ||
            epochPattern.matches(trimmed) || isoPattern.matches(trimmed)
    }

    fun epochToIso(epochMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(epochMillis))
    }

    fun epochToLocal(epochMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }

    fun isoToEpoch(iso: String): Long? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS"
        )
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                if (format.endsWith("Z") || format.contains("ZZ")) {
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                }
                return sdf.parse(iso)?.time
            } catch (e: Exception) {
                // Try next format
            }
        }
        return null
    }

    fun detectAndProcess(query: String): Pair<String, String>? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null
        val lower = trimmed.lowercase()

        return when {
            lower.startsWith("epoch ") -> {
                val payload = trimmed.substringAfter(" ").trim()
                val epoch = payload.toLongOrNull() ?: return null
                val iso = epochToIso(epoch)
                val local = epochToLocal(epoch)
                payload to "UTC: $iso\nLocal: $local"
            }
            lower.startsWith("timestamp ") -> {
                val payload = trimmed.substringAfter(" ").trim()
                val epoch = payload.toLongOrNull() ?: return null
                val iso = epochToIso(epoch)
                val local = epochToLocal(epoch)
                payload to "UTC: $iso\nLocal: $local"
            }
            lower.startsWith("iso ") -> {
                val payload = trimmed.substringAfter(" ").trim()
                val epoch = isoToEpoch(payload) ?: return null
                val iso = epochToIso(epoch)
                val local = epochToLocal(epoch)
                payload to "Epoch: $epoch\nUTC: $iso\nLocal: $local"
            }
            lower.startsWith("date ") -> {
                val payload = trimmed.substringAfter(" ").trim()
                val epoch = isoToEpoch(payload) ?: return null
                val iso = epochToIso(epoch)
                val local = epochToLocal(epoch)
                payload to "Epoch: $epoch\nUTC: $iso\nLocal: $local"
            }
            epochPattern.matches(trimmed) -> {
                val epoch = trimmed.toLongOrNull() ?: return null
                val iso = epochToIso(epoch)
                val local = epochToLocal(epoch)
                trimmed to "UTC: $iso\nLocal: $local"
            }
            isoPattern.matches(trimmed) -> {
                val epoch = isoToEpoch(trimmed) ?: return null
                val iso = epochToIso(epoch)
                val local = epochToLocal(epoch)
                trimmed to "Epoch: $epoch\nUTC: $iso\nLocal: $local"
            }
            else -> null
        }
    }
}