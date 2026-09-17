package com.tk.quicksearch.tools.aiSearch

import android.content.Context

object KiloAiSearchLlmProvider : AiSearchLlmProvider {
    override val id: AiSearchLlmProviderId = AiSearchLlmProviderId.KILO
    override val displayName: String = "Kilo"
    override val defaultModelId: String = KiloModelCatalog.DEFAULT_MODEL_ID
    override val defaultGroundingEnabled: Boolean = KiloModelCatalog.DEFAULT_GROUNDING_ENABLED
    override val fallbackTextModels: List<LlmTextModel> = KiloModelCatalog.FALLBACK_TEXT_MODELS

    override suspend fun fetchAvailableTextModels(
        apiKey: String,
        context: Context,
    ): Result<List<LlmTextModel>> = KiloClient.fetchAvailableTextModels(apiKey, context)

    override suspend fun fetchAnswer(
        apiKey: String,
        context: Context,
        request: LlmRequest,
    ): Result<LlmResponse> {
        val client = KiloClient(apiKey = apiKey, context = context)
        return client.fetchAnswer(
            query = request.query,
            personalContext = request.personalContext,
            modelId = request.modelId,
            useSystemInstruction = request.useSystemInstruction,
            systemInstruction = request.systemInstruction,
            advancedPayloadJson = request.advancedPayloadJson,
        ).map(::LlmResponse)
    }
}
