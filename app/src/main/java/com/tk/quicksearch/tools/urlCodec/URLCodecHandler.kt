package com.tk.quicksearch.tools.urlCodec

import com.tk.quicksearch.search.core.CalculatorState
import com.tk.quicksearch.search.core.SearchToolType
import com.tk.quicksearch.search.data.UserAppPreferences

class URLCodecHandler(
    private val userPreferences: UserAppPreferences,
) {
    fun processQuery(
        query: String,
        forceUrlCodecMode: Boolean = false,
    ): CalculatorState {
        if (!forceUrlCodecMode && !userPreferences.isUrlCodecEnabled()) {
            return CalculatorState()
        }

        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return if (forceUrlCodecMode) {
                CalculatorState(
                    isUrlCodecMode = true,
                    toolType = SearchToolType.URL_CODEC,
                )
            } else {
                CalculatorState()
            }
        }

        val processed = URLCodecUtils.detectAndProcess(trimmedQuery)
        if (processed != null) {
            return CalculatorState(
                result = processed.second,
                expression = processed.first,
                isUrlCodecMode = forceUrlCodecMode,
                toolType = SearchToolType.URL_CODEC,
            )
        }

        return if (forceUrlCodecMode) {
            CalculatorState(
                expression = trimmedQuery,
                isUrlCodecMode = true,
                toolType = SearchToolType.URL_CODEC,
                showInvalidExpression = true,
            )
        } else {
            CalculatorState()
        }
    }
}