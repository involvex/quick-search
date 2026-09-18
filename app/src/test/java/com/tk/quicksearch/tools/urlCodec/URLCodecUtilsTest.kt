package com.tk.quicksearch.tools.urlCodec

import org.junit.Assert.*
import org.junit.Test

class URLCodecUtilsTest {

    @Test
    fun testEncode() {
        val input = "hello world"
        val encoded = URLCodecUtils.encode(input)
        // Should contain %20 for space (implementation replaces + with %20)
        assertTrue(encoded.contains("%20"))
    }

    @Test
    fun testEncodeEmptyString() {
        val input = ""
        val expected = ""
        assertEquals(expected, URLCodecUtils.encode(input))
    }

    @Test
    fun testEncodeSpecialChars() {
        val input = "hello!@#$%^&*()"
        val encoded = URLCodecUtils.encode(input)
        // All special chars should be encoded
        assertTrue(encoded.contains("%21")) // !
        assertTrue(encoded.contains("%40")) // @
        assertTrue(encoded.contains("%23")) // #
        assertTrue(encoded.contains("%24")) // $
        assertTrue(encoded.contains("%25")) // %
        assertTrue(encoded.contains("%5E")) // ^
        assertTrue(encoded.contains("%26")) // &
        assertTrue(encoded.contains("%2A")) // *
        assertTrue(encoded.contains("%28")) // (
        assertTrue(encoded.contains("%29")) // )
    }

    @Test
    fun testEncodeUnicode() {
        val input = "hello 世界"
        val encoded = URLCodecUtils.encode(input)
        // Unicode should be encoded as UTF-8 bytes
        assertTrue(encoded.contains("%E4") && encoded.contains("%B8") && encoded.contains("%96")) // 世
        assertTrue(encoded.contains("%E7") && encoded.contains("%95") && encoded.contains("%8C")) // 界
    }

    @Test
    fun testEncodePreservesTilde() {
        val input = "~home"
        val expected = "~home"
        assertEquals(expected, URLCodecUtils.encode(input))
    }

    @Test
    fun testDecode() {
        val input = "hello%20world"
        val expected = "hello world"
        assertEquals(expected, URLCodecUtils.decode(input))
    }

    @Test
    fun testDecodeEmptyString() {
        val input = ""
        val expected = ""
        assertEquals(expected, URLCodecUtils.decode(input))
    }

    @Test
    fun testDecodePlusAsSpace() {
        val input = "hello+world"
        val expected = "hello world"
        assertEquals(expected, URLCodecUtils.decode(input))
    }

    @Test
    fun testDecodeUnicode() {
        val input = "hello%20%E4%B8%96%E7%95%8C"
        val expected = "hello 世界"
        assertEquals(expected, URLCodecUtils.decode(input))
    }

    @Test
    fun testDecodePreservesTilde() {
        val input = "~home"
        val expected = "~home"
        assertEquals(expected, URLCodecUtils.decode(input))
    }

    @Test
    fun testDecodeInvalidReturnsNull() {
        assertNull(URLCodecUtils.decode("%ZZ"))
        assertNull(URLCodecUtils.decode("%"))
    }

    @Test
    fun testIsCandidate() {
        assertTrue(URLCodecUtils.isCandidate("url encode hello"))
        assertTrue(URLCodecUtils.isCandidate("encode url hello"))
        assertTrue(URLCodecUtils.isCandidate("url decode hello%20world"))
        assertTrue(URLCodecUtils.isCandidate("decode url hello%20world"))
        assertTrue(URLCodecUtils.isCandidate("URL ENCODE HELLO"))
        assertTrue(URLCodecUtils.isCandidate("ENCODE URL HELLO"))
    }

    @Test
    fun testIsCandidateRejects() {
        assertFalse(URLCodecUtils.isCandidate(""))
        assertFalse(URLCodecUtils.isCandidate("hello"))
        assertFalse(URLCodecUtils.isCandidate("url"))
        assertFalse(URLCodecUtils.isCandidate("encode"))
        assertFalse(URLCodecUtils.isCandidate("decode"))
    }

    @Test
    fun testDetectAndProcessUrlEncode() {
        val result = URLCodecUtils.detectAndProcess("url encode hello world")
        assertNotNull(result)
        assertEquals("hello world", result?.first)
        assertTrue(result?.second?.contains("%20") == true)
    }

    @Test
    fun testDetectAndProcessEncodeUrl() {
        val result = URLCodecUtils.detectAndProcess("encode url hello world")
        assertNotNull(result)
        assertEquals("hello world", result?.first)
        assertTrue(result?.second?.contains("%20") == true)
    }

    @Test
    fun testDetectAndProcessUrlDecode() {
        val result = URLCodecUtils.detectAndProcess("url decode hello%20world")
        assertNotNull(result)
        assertEquals("hello%20world", result?.first)
        assertEquals("hello world", result?.second)
    }

    @Test
    fun testDetectAndProcessDecodeUrl() {
        val result = URLCodecUtils.detectAndProcess("decode url hello%20world")
        assertNotNull(result)
        assertEquals("hello%20world", result?.first)
        assertEquals("hello world", result?.second)
    }

    @Test
    fun testDetectAndProcessEmptyPayload() {
        assertNull(URLCodecUtils.detectAndProcess("url encode"))
        assertNull(URLCodecUtils.detectAndProcess("encode url"))
        assertNull(URLCodecUtils.detectAndProcess("url decode"))
        assertNull(URLCodecUtils.detectAndProcess("decode url"))
    }

    @Test
    fun testDetectAndProcessInvalidDecode() {
        assertNull(URLCodecUtils.detectAndProcess("url decode %ZZ"))
    }

    @Test
    fun testDetectAndProcessEmptyString() {
        assertNull(URLCodecUtils.detectAndProcess(""))
        assertNull(URLCodecUtils.detectAndProcess("   "))
    }

    @Test
    fun testEncodeThenDecodeRoundTrip() {
        val original = "hello world!@#$%^&*()"
        val encoded = URLCodecUtils.encode(original)
        val decoded = URLCodecUtils.decode(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun testEncodeWithSpaces() {
        val input = "a b c"
        val encoded = URLCodecUtils.encode(input)
        assertTrue(encoded.contains("%20"))
    }
}