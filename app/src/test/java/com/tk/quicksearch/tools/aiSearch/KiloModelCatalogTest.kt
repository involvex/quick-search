package com.tk.quicksearch.tools.aiSearch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KiloModelCatalogTest {
    @Test
    fun defaultModelIsFreeAutoRouter() {
        assertEquals("kilo-auto/free", KiloModelCatalog.DEFAULT_MODEL_ID)
    }

    @Test
    fun keepsChatTextModels() {
        assertTrue(KiloModelCatalog.isLikelyTextModel("kilo-auto/free"))
        assertTrue(KiloModelCatalog.isLikelyTextModel("anthropic/claude-sonnet-4.5"))
        assertTrue(KiloModelCatalog.isLikelyTextModel("openai/gpt-5.4-mini"))
        assertTrue(KiloModelCatalog.isLikelyTextModel("KILO-AUTO/EFFICIENT"))
    }

    @Test
    fun filtersNonTextModels() {
        assertFalse(KiloModelCatalog.isLikelyTextModel("openai/whisper-large-v3"))
        assertFalse(KiloModelCatalog.isLikelyTextModel("openai/tts-1"))
        assertFalse(KiloModelCatalog.isLikelyTextModel("openai/text-embedding-3-small"))
        assertFalse(KiloModelCatalog.isLikelyTextModel("mistralai/codestral-2508"))
        assertFalse(KiloModelCatalog.isLikelyTextModel("  "))
    }

    @Test
    fun fallbackModelsDisableGrounding() {
        assertTrue(KiloModelCatalog.FALLBACK_TEXT_MODELS.isNotEmpty())
        assertTrue(KiloModelCatalog.FALLBACK_TEXT_MODELS.all { !it.supportsGrounding })
        assertTrue(
            KiloModelCatalog.FALLBACK_TEXT_MODELS.any { it.id == KiloModelCatalog.DEFAULT_MODEL_ID },
        )
    }
}
