package com.tk.quicksearch.tools.hashGenerator

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object HashGeneratorUtils {
    /**
     * Supported hash algorithms.
     * 
     * @note MD5 and SHA-1 are cryptographically broken and MUST NOT be used for
     * security-sensitive purposes (passwords, tokens, signatures, integrity verification).
     * They are provided only for compatibility/legacy use cases.
     * For cryptographic use, prefer SHA-256 or SHA-512.
     */
    enum class Algorithm(val algorithmName: String, val displayName: String, val aliasPrefix: String) {
        MD5("MD5", "MD5", "md5 "),
        SHA1("SHA-1", "SHA-1", "sha1 "),
        SHA256("SHA-256", "SHA-256", "sha256 "),
        SHA512("SHA-512", "SHA-512", "sha512 "),
    }

    private val hashPrefixPattern = Regex("^(md5|sha1|sha256|sha512)\\s+", RegexOption.IGNORE_CASE)

    /** Hex character lookup table for fast byte-to-hex conversion. */
    private val HEX_CHARS = "0123456789abcdef".toCharArray()

    fun isCandidate(query: String): Boolean {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return false
        val lower = trimmed.lowercase()
        return lower.startsWith("md5 ") || lower.startsWith("sha1 ") ||
            lower.startsWith("sha256 ") || lower.startsWith("sha512 ") ||
            lower.startsWith("hash ")
    }

    fun computeHash(input: String, algorithm: Algorithm): String {
        try {
            val md = MessageDigest.getInstance(algorithm.algorithmName)
            val bytes = md.digest(input.toByteArray(StandardCharsets.UTF_8))
            return bytesToHex(bytes)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Algorithm ${algorithm.algorithmName} not available", e)
        }
    }

    /**
     * Converts a byte array to a lowercase hex string using a lookup table.
     * Avoids String.format allocations for better performance.
     */
    private fun bytesToHex(bytes: ByteArray): String {
        val chars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val b = bytes[i].toInt() and 0xFF
            chars[i * 2] = HEX_CHARS[b ushr 4]
            chars[i * 2 + 1] = HEX_CHARS[b and 0xF]
        }
        return String(chars)
    }

    fun detectAndProcess(query: String): Triple<String, String, Algorithm>? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null
        val lower = trimmed.lowercase()

        return when {
            lower.startsWith("hash ") -> {
                val payload = trimmed.substringAfter(' ').trim()
                if (payload.isEmpty()) return null
                val results = Algorithm.values().map { algo ->
                    algo to computeHash(payload, algo)
                }
                val output = results.map { (algo, hash) -> "${algo.displayName}: $hash" }.joinToString("\n")
                Triple(payload, output, Algorithm.MD5)
            }
            lower.startsWith("md5 ") -> {
                val payload = trimmed.substringAfter(' ').trim()
                if (payload.isEmpty()) return null
                Triple(payload, computeHash(payload, Algorithm.MD5), Algorithm.MD5)
            }
            lower.startsWith("sha1 ") -> {
                val payload = trimmed.substringAfter(' ').trim()
                if (payload.isEmpty()) return null
                Triple(payload, computeHash(payload, Algorithm.SHA1), Algorithm.SHA1)
            }
            lower.startsWith("sha256 ") -> {
                val payload = trimmed.substringAfter(' ').trim()
                if (payload.isEmpty()) return null
                Triple(payload, computeHash(payload, Algorithm.SHA256), Algorithm.SHA256)
            }
            lower.startsWith("sha512 ") -> {
                val payload = trimmed.substringAfter(' ').trim()
                if (payload.isEmpty()) return null
                Triple(payload, computeHash(payload, Algorithm.SHA512), Algorithm.SHA512)
            }
            else -> null
        }
    }
}