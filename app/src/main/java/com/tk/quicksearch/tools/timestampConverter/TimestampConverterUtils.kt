package com.tk.quicksearch.tools.timestampConverter

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQueries

object TimestampConverterUtils {
    private val epochPattern = Regex("^\\d{10,13}$")
    private val isoPattern = Regex("^\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,3})?(Z|[+-]\\d{2}:?\\d{2})?$")

    private val isoFormatter = DateTimeFormatter.ISO_INSTANT
    private val isoWithMillisFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private val isoWithOffsetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
    private val isoBasicFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")
    private val localDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val localDateTimeWithMillisFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
    private val outputIsoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"))
    private val outputLocalFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun isCandidate(query: String): Boolean {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return false
        val lower = trimmed.lowercase()
        return lower.startsWith("epoch ") || lower.startsWith("timestamp ") ||
            lower.startsWith("iso ") || lower.startsWith("date ") ||
            epochPattern.matches(trimmed) || isoPattern.matches(trimmed)
    }

    fun epochToIso(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.of("UTC")).format(outputIsoFormatter)
    }

    fun epochToLocal(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(outputLocalFormatter)
    }

    private fun parseAsInstant(iso: String, formatter: DateTimeFormatter): Long? {
        try {
            return formatter.parse(iso, Instant::from).toEpochMilli()
        } catch (e: DateTimeParseException) {
            return null
        }
    }

    private fun parseAsLocalDateTime(iso: String, formatter: DateTimeFormatter): Long? {
        try {
            val temporal: TemporalAccessor = formatter.parse(iso)
            val localDateTime = LocalDateTime.from(temporal)
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) {
            return null
        }
    }

    fun isoToEpoch(iso: String): Long? {
        // Try ISO_INSTANT first (handles Z and offset)
        parseAsInstant(iso, isoFormatter)?.let { return it }

        // Try with milliseconds and offset (e.g., 2024-01-01T00:00:00.000+05:00)
        parseAsInstant(iso, isoWithMillisFormatter)?.let { return it }

        // Try with offset only (e.g., 2024-01-01T00:00:00+05:00)
        parseAsInstant(iso, isoWithOffsetFormatter)?.let { return it }

        // Try basic ISO with offset (e.g., 2024-01-01T00:00:00Z)
        parseAsInstant(iso, isoBasicFormatter)?.let { return it }

        // Try local date-time (assume system default zone)
        parseAsLocalDateTime(iso, localDateTimeFormatter)?.let { return it }

        // Try local date-time with millis
        parseAsLocalDateTime(iso, localDateTimeWithMillisFormatter)?.let { return it }

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