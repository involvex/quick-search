package com.tk.quicksearch.tools.aiSearch

/** Shared Kilo Gateway model configuration defaults. */
object KiloModelCatalog {
    const val DEFAULT_MODEL_ID = "kilo-auto/free"
    const val DEFAULT_GROUNDING_ENABLED = false

    /**
     * Fallback list used when the model catalog cannot be fetched from the API.
     * Kilo Gateway does not support grounding/web search natively.
     */
    val FALLBACK_TEXT_MODELS: List<LlmTextModel> =
        listOf(
            LlmTextModel(id = "kilo-auto/free", displayName = "Kilo Auto Free", supportsGrounding = false),
            LlmTextModel(id = "kilo-auto/efficient", displayName = "Kilo Auto Efficient", supportsGrounding = false),
            LlmTextModel(id = "anthropic/claude-sonnet-4.5", displayName = "Claude Sonnet 4.5", supportsGrounding = false),
            LlmTextModel(id = "openai/gpt-5.4-mini", displayName = "GPT 5.4 Mini", supportsGrounding = false),
            LlmTextModel(id = "google/gemini-2.5-flash", displayName = "Gemini 2.5 Flash", supportsGrounding = false),
        )

    /** Heuristic filter: keep only chat/text generation models served by the Kilo Gateway. */
    fun isLikelyTextModel(modelId: String): Boolean {
        val lower = modelId.lowercase()
        if (lower.contains("whisper") || lower.contains("tts") || lower.contains("embed")) return false
        if (lower.contains("image") || lower.contains("vision") && lower.contains("embed")) return false
        if (lower.contains("codestral") || lower.contains("fim")) return false
        if (lower.isBlank()) return false
        return true
    }
}
