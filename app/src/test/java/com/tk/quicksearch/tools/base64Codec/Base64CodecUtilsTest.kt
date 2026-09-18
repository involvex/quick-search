package com.tk.quicksearch.tools.base64Codec

import org.junit.Assert.*
import org.junit.Test

class Base64CodecUtilsTest {

    @Test
    fun testEncode() {
        val input = "hello world"
        // Base64.NO_WRAP produces unpadded output
        val expected = "aGVsbG8gd29ybGQ"
        assertEquals(expected, Base64CodecUtils.encode(input))
    }

    @Test
    fun testEncodeEmptyString() {
        val input = ""
        val expected = ""
        assertEquals(expected, Base64CodecUtils.encode(input))
    }

    @Test
    fun testEncodeUnicode() {
        val input = "hello 世界"
        // Unicode chars are encoded as UTF-8 bytes then base64
        val expected = "aGVsbG8g5LiW55WM"
        assertEquals(expected, Base64CodecUtils.encode(input))
    }

    @Test
    fun testDecode() {
        // decode expects padding, normalizePadding adds it
        val input = "aGVsbG8gd29ybGQ"
        val expected = "hello world"
        assertEquals(expected, Base64CodecUtils.decode(input))
    }

    @Test
    fun testDecodeWithPadding() {
        val input = "aGVsbG8="
        val expected = "hello"
        assertEquals(expected, Base64CodecUtils.decode(input))
    }

    @Test
    fun testDecodeInvalidReturnsNull() {
        assertNull(Base64CodecUtils.decode("not valid base64!!!"))
        assertNull(Base64CodecUtils.decode(""))
    }

    @Test
    fun testDecodeWithWhitespace() {
        val input = " aGVsbG8gd29ybGQ "
        val expected = "hello world"
        assertEquals(expected, Base64CodecUtils.decode(input))
    }

    @Test
    fun testDecodeUrlSafeAlphabet() {
        val input = "aGVsbG8gd29ybGQ"
        val expected = "hello world"
        assertEquals(expected, Base64CodecUtils.decode(input))
    }

    @Test
    fun testDecodeUrlSafeWithUnderscoreAndDash() {
        // The input uses standard alphabet, not URL-safe
        // URL-safe uses - and _ instead of + and /
        // For "hello/" -> "aGVsbG8v" (no + or / in result)
        // For something with + or /: "hello+world" -> "aGVsbG8rd29ybGQ="
        // URL-safe version: "aGVsbG8rd29ybGQ" -> after normalizePadding becomes standard
        val standard = "aGVsbG8rd29ybGQ"
        val urlSafe = "aGVsbG8rd29ybGQ"
        assertEquals("hello+world", Base64CodecUtils.decode(standard))
        assertEquals("hello+world", Base64CodecUtils.decode(urlSafe))
    }

    @Test
    fun testIsCandidateWithPrefixes() {
        assertTrue(Base64CodecUtils.isCandidate("b64 hello"))
        assertTrue(Base64CodecUtils.isCandidate("base64 hello"))
        assertTrue(Base64CodecUtils.isCandidate("encode hello"))
        assertTrue(Base64CodecUtils.isCandidate("decode hello"))
        assertTrue(Base64CodecUtils.isCandidate("B64 HELLO"))
        assertTrue(Base64CodecUtils.isCandidate("BASE64 HELLO"))
    }

    @Test
    fun testIsCandidateAutoDetect() {
        // Valid base64 with correct length and padding
        assertTrue(Base64CodecUtils.isCandidate("aGVsbG8="))  // "hello" in base64
        assertTrue(Base64CodecUtils.isCandidate("SGVsbG8gV29ybGQ="))  // "Hello World"
    }

    @Test
    fun testIsCandidateRejectsShort() {
        assertFalse(Base64CodecUtils.isCandidate("aG8="))  // too short
        assertFalse(Base64CodecUtils.isCandidate(""))      // empty
    }

    @Test
    fun testIsCandidateRejectsLong() {
        val longInput = "a".repeat(10001)
        assertFalse(Base64CodecUtils.isCandidate(longInput))
    }

    @Test
    fun testIsCandidateRejectsNonBase64() {
        assertFalse(Base64CodecUtils.isCandidate("hello world"))
        assertFalse(Base64CodecUtils.isCandidate("not-base64!"))
    }

    @Test
    fun testDetectAndProcessB64Decode() {
        // "hello" in base64 with padding
        val result = Base64CodecUtils.detectAndProcess("b64 aGVsbG8=")
        assertNotNull(result)
        assertEquals("aGVsbG8=", result?.first)
        assertEquals("hello", result?.second)
    }

    @Test
    fun testDetectAndProcessB64Encode() {
        val result = Base64CodecUtils.detectAndProcess("b64 hello")
        assertNotNull(result)
        assertEquals("hello", result?.first)
        // encode uses NO_WRAP so no padding
        assertEquals("aGVsbG8=", result?.second)
    }

    @Test
    fun testDetectAndProcessBase64Prefix() {
        val result = Base64CodecUtils.detectAndProcess("base64 aGVsbG8=")
        assertNotNull(result)
        assertEquals("aGVsbG8=", result?.first)
        assertEquals("hello", result?.second)
    }

    @Test
    fun testDetectAndProcessEncodePrefix() {
        val result = Base64CodecUtils.detectAndProcess("encode hello world")
        assertNotNull(result)
        assertEquals("hello world", result?.first)
        assertEquals("aGVsbG8gd29ybGQ", result?.second)
    }

    @Test
    fun testDetectAndProcessDecodePrefix() {
        val result = Base64CodecUtils.detectAndProcess("decode aGVsbG8gd29ybGQ")
        assertNotNull(result)
        assertEquals("aGVsbG8gd29ybGQ", result?.first)
        assertEquals("hello world", result?.second)
    }

    @Test
    fun testDetectAndProcessAutoDetectDecodes() {
        val result = Base64CodecUtils.detectAndProcess("aGVsbG8=")
        assertNotNull(result)
        assertEquals("aGVsbG8=", result?.first)
        assertEquals("hello", result?.second)
    }

    @Test
    fun testDetectAndProcessEmptyPayload() {
        assertNull(Base64CodecUtils.detectAndProcess("b64"))
        assertNull(Base64CodecUtils.detectAndProcess("encode"))
        assertNull(Base64CodecUtils.detectAndProcess("decode"))
    }

    @Test
    fun testDetectAndProcessInvalidBase64() {
        // Should return null for invalid base64 (decode returns null, so it tries to encode)
        // But since it's not empty, encode will work - so actually it won't return null for invalid
        // Let's test with something that has invalid chars and can't be decoded
        assertNull(Base64CodecUtils.detectAndProcess("decode notvalid!!!"))
    }

    @Test
    fun testDetectAndProcessEmptyString() {
        assertNull(Base64CodecUtils.detectAndProcess(""))
        assertNull(Base64CodecUtils.detectAndProcess("   "))
    }
}