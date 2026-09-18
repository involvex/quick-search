package com.tk.quicksearch.tools.base64Codec

import com.tk.quicksearch.search.core.CalculatorState
import com.tk.quicksearch.search.core.SearchToolType
import com.tk.quicksearch.search.data.UserAppPreferences

class Base64CodecHandler(
    private val userPreferences: UserAppPreferences,
) {
    fun processQuery(
        query: String,
        forceBase64Mode: Boolean = false,
    ): CalculatorState {
        if (!forceBase64Mode && !userPreferences.isBase64CodecEnabled()) {
            return CalculatorState()
        }

        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return if (forceBase64Mode) {
                CalculatorState(
                    isBase64Mode = true,
                    toolType = SearchToolType.BASE64_CODEC,
                )
            } else {
                CalculatorState()
            }
        }

        val processed = Base64CodecUtils.detectAndProcess(trimmedQuery)
        if (processed != null) {
            return CalculatorState(
                result = processed.second,
                expression = processed.first,
                isBase64Mode = forceBase64Mode,
                toolType = SearchToolType.BASE64_CODEC,
            )
        }

        return if (forceBase64Mode) {
            CalculatorState(
                expression = trimmedQuery,
                isBase64Mode = true,
                toolType = SearchToolType.BASE64_CODEC,
                showInvalidExpression = true,
            )
        } else {
            CalculatorState()
        }
    }
}
