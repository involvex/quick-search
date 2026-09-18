package com.tk.quicksearch.tools.hashGenerator

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object HashGeneratorUtils {
    enum class Algorithm(val algorithmName: String, val displayName: String, val aliasPrefix: String) {
        MD5("MD5", "MD5", "md5 "),
        SHA1("SHA-1", "SHA-1", "sha1 "),
        SHA256("SHA-256", "SHA-256", "sha256 "),
        SHA512("SHA-512", "SHA-512", "sha512 "),
    }

    private val hashPrefixPattern = Regex("^(md5|sha1|sha256|sha512)\\s+", RegexOption.IGNORE_CASE)

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

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
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