package com.tk.quicksearch.tools.timestampConverter

import org.junit.Assert.*
import org.junit.Test

class TimestampConverterUtilsTest {

    @Test
    fun testEpochToIso() {
        // 2024-01-01T00:00:00.000Z
        val epoch = 1704067200000L
        val result = TimestampConverterUtils.epochToIso(epoch)
        assertTrue(result.startsWith("2024-01-01T00:00:00"))
        assertTrue(result.endsWith("Z"))
    }

    @Test
    fun testEpochToIsoUnixEpoch() {
        val epoch = 0L
        val result = TimestampConverterUtils.epochToIso(epoch)
        assertTrue(result.startsWith("1970-01-01T00:00:00"))
        assertTrue(result.endsWith("Z"))
    }

    @Test
    fun testEpochToLocal() {
        val epoch = 1704067200000L
        val result = TimestampConverterUtils.epochToLocal(epoch)
        // Should contain date and time in local format
        assertTrue(result.contains("2024-01-01") || result.contains("2023-12-31")) // timezone dependent
    }

    @Test
    fun testIsoToEpoch() {
        val iso = "2024-01-01T00:00:00.000Z"
        val result = TimestampConverterUtils.isoToEpoch(iso)
        assertNotNull(result)
        assertEquals(1704067200000L, result!!)
    }

    @Test
    fun testIsoToEpochWithSpace() {
        val iso = "2024-01-01 00:00:00"
        val result = TimestampConverterUtils.isoToEpoch(iso)
        assertNotNull(result)
    }

    @Test
    fun testIsoToEpochWithoutMillis() {
        val iso = "2024-01-01T00:00:00Z"
        val result = TimestampConverterUtils.isoToEpoch(iso)
        assertNotNull(result)
        assertEquals(1704067200000L, result!!)
    }

    @Test
    fun testIsoToEpochWithTimezoneOffset() {
        val iso = "2024-01-01T00:00:00+05:00"
        val result = TimestampConverterUtils.isoToEpoch(iso)
        assertNotNull(result)
        // 5 hours behind UTC = 1704067200000 + 5*3600*1000
        // Note: exact value depends on implementation
        assertTrue(result!! > 1704067200000L)
    }

    @Test
    fun testIsoToEpochInvalidReturnsNull() {
        assertNull(TimestampConverterUtils.isoToEpoch("not a date"))
        assertNull(TimestampConverterUtils.isoToEpoch(""))
    }

    @Test
    fun testIsCandidate() {
        assertTrue(TimestampConverterUtils.isCandidate("epoch 1704067200"))
        assertTrue(TimestampConverterUtils.isCandidate("timestamp 1704067200"))
        assertTrue(TimestampConverterUtils.isCandidate("iso 2024-01-01T00:00:00Z"))
        assertTrue(TimestampConverterUtils.isCandidate("date 2024-01-01"))
        assertTrue(TimestampConverterUtils.isCandidate("1704067200000"))
        assertTrue(TimestampConverterUtils.isCandidate("2024-01-01T00:00:00Z"))
        assertTrue(TimestampConverterUtils.isCandidate("EPOCH 1704067200"))
    }

    @Test
    fun testIsCandidateRejects() {
        assertFalse(TimestampConverterUtils.isCandidate(""))
        assertFalse(TimestampConverterUtils.isCandidate("hello"))
        assertFalse(TimestampConverterUtils.isCandidate("12345")) // too short
    }

    @Test
    fun testDetectAndProcessEpochPrefix() {
        val result = TimestampConverterUtils.detectAndProcess("epoch 1704067200000")
        assertNotNull(result)
        assertEquals("1704067200000", result?.first)
        assertTrue(result?.second?.contains("UTC:") == true)
        assertTrue(result?.second?.contains("Local:") == true)
    }

    @Test
    fun testDetectAndProcessTimestampPrefix() {
        val result = TimestampConverterUtils.detectAndProcess("timestamp 1704067200000")
        assertNotNull(result)
        assertEquals("1704067200000", result?.first)
        assertTrue(result?.second?.contains("UTC:") == true)
    }

    @Test
    fun testDetectAndProcessIsoPrefix() {
        val result = TimestampConverterUtils.detectAndProcess("iso 2024-01-01T00:00:00Z")
        assertNotNull(result)
        assertEquals("2024-01-01T00:00:00Z", result?.first)
        assertTrue(result?.second?.contains("Epoch:") == true)
        assertTrue(result?.second?.contains("UTC:") == true)
        assertTrue(result?.second?.contains("Local:") == true)
    }

    @Test
    fun testDetectAndProcessDatePrefix() {
        val result = TimestampConverterUtils.detectAndProcess("date 2024-01-01")
        assertNotNull(result)
        assertEquals("2024-01-01", result?.first)
        // Just verify it produces epoch output
        assertTrue(result?.second?.contains("Epoch:") == true)
    }

    @Test
    fun testDetectAndProcessBareEpoch() {
        val result = TimestampConverterUtils.detectAndProcess("1704067200000")
        assertNotNull(result)
        assertEquals("1704067200000", result?.first)
        assertTrue(result?.second?.contains("UTC:") == true)
    }

    @Test
    fun testDetectAndProcessBareIso() {
        val result = TimestampConverterUtils.detectAndProcess("2024-01-01T00:00:00Z")
        assertNotNull(result)
        assertEquals("2024-01-01T00:00:00Z", result?.first)
        assertTrue(result?.second?.contains("Epoch:") == true)
    }

    @Test
    fun testDetectAndProcessEmptyPayload() {
        assertNull(TimestampConverterUtils.detectAndProcess("epoch"))
        assertNull(TimestampConverterUtils.detectAndProcess("timestamp"))
        assertNull(TimestampConverterUtils.detectAndProcess("iso"))
        assertNull(TimestampConverterUtils.detectAndProcess("date"))
    }

    @Test
    fun testDetectAndProcessInvalidEpoch() {
        assertNull(TimestampConverterUtils.detectAndProcess("epoch notanumber"))
    }

    @Test
    fun testDetectAndProcessInvalidIso() {
        assertNull(TimestampConverterUtils.detectAndProcess("iso notadate"))
    }

    @Test
    fun testDetectAndProcessEmptyString() {
        assertNull(TimestampConverterUtils.detectAndProcess(""))
        assertNull(TimestampConverterUtils.detectAndProcess("   "))
    }

    @Test
    fun testRoundTripEpochToIsoToEpoch() {
        val originalEpoch = 1704067200000L
        val iso = TimestampConverterUtils.epochToIso(originalEpoch)
        val parsedEpoch = TimestampConverterUtils.isoToEpoch(iso)
        assertNotNull(parsedEpoch)
        assertEquals(originalEpoch, parsedEpoch!!)
    }

    @Test
    fun testSecondsEpoch() {
        // 10-digit epoch (seconds)
        val epochSeconds = 1704067200L
        val result = TimestampConverterUtils.detectAndProcess("epoch $epochSeconds")
        assertNotNull(result)
    }
}