package com.tk.quicksearch.tools.hashGenerator

import org.junit.Assert.*
import org.junit.Test

class HashGeneratorUtilsTest {

    @Test
    fun testMd5() {
        val input = "hello"
        val expected = "5d41402abc4b2a76b9719d911017c592"
        assertEquals(expected, HashGeneratorUtils.computeHash(input, HashGeneratorUtils.Algorithm.MD5))
    }

    @Test
    fun testSha1() {
        val input = "hello"
        val expected = "aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d"
        assertEquals(expected, HashGeneratorUtils.computeHash(input, HashGeneratorUtils.Algorithm.SHA1))
    }

    @Test
    fun testSha256() {
        val input = "hello"
        val expected = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"
        assertEquals(expected, HashGeneratorUtils.computeHash(input, HashGeneratorUtils.Algorithm.SHA256))
    }

    @Test
    fun testSha512() {
        val input = "hello"
        val expected = "9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca72323c3d99ba5c11d7c7acc6e14b8c5da0c4663475c2e5c3adef46f73bcdec043"
        assertEquals(expected, HashGeneratorUtils.computeHash(input, HashGeneratorUtils.Algorithm.SHA512))
    }

    @Test
    fun testIsCandidate() {
        assertTrue(HashGeneratorUtils.isCandidate("md5 hello"))
        assertTrue(HashGeneratorUtils.isCandidate("sha1 hello"))
        assertTrue(HashGeneratorUtils.isCandidate("sha256 hello"))
        assertTrue(HashGeneratorUtils.isCandidate("sha512 hello"))
        assertTrue(HashGeneratorUtils.isCandidate("hash hello"))
        assertTrue(HashGeneratorUtils.isCandidate("MD5 HELLO"))
        assertTrue(HashGeneratorUtils.isCandidate("SHA256 HELLO"))

        assertFalse(HashGeneratorUtils.isCandidate(""))
        assertFalse(HashGeneratorUtils.isCandidate("hello"))
        assertFalse(HashGeneratorUtils.isCandidate("md5"))
        assertFalse(HashGeneratorUtils.isCandidate("sha256"))
    }

    @Test
    fun testDetectAndProcessMd5() {
        val result = HashGeneratorUtils.detectAndProcess("md5 hello")
        assertNotNull(result)
        assertEquals("hello", result?.first)
        assertEquals("5d41402abc4b2a76b9719d911017c592", result?.second)
        assertEquals(HashGeneratorUtils.Algorithm.MD5, result?.third)
    }

    @Test
    fun testDetectAndProcessSha256() {
        val result = HashGeneratorUtils.detectAndProcess("sha256 hello")
        assertNotNull(result)
        assertEquals("hello", result?.first)
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", result?.second)
        assertEquals(HashGeneratorUtils.Algorithm.SHA256, result?.third)
    }

    @Test
    fun testDetectAndProcessHashAll() {
        val result = HashGeneratorUtils.detectAndProcess("hash hello")
        assertNotNull(result)
        assertEquals("hello", result?.first)
        assertTrue(result?.second?.contains("MD5: 5d41402abc4b2a76b9719d911017c592") == true)
        assertTrue(result?.second?.contains("SHA-1: aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d") == true)
        assertTrue(result?.second?.contains("SHA-256: 2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824") == true)
        assertTrue(result?.second?.contains("SHA-512: 9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca72323c3d99ba5c11d7c7acc6e14b8c5da0c4663475c2e5c3adef46f73bcdec043") == true)
        assertEquals(HashGeneratorUtils.Algorithm.MD5, result?.third)
    }

    @Test
    fun testDetectAndProcessEmpty() {
        assertNull(HashGeneratorUtils.detectAndProcess(""))
        assertNull(HashGeneratorUtils.detectAndProcess("md5"))
        assertNull(HashGeneratorUtils.detectAndProcess("sha256"))
    }
}