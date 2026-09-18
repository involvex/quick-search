package com.tk.quicksearch.tools.timestampConverter

import com.tk.quicksearch.search.core.CalculatorState
import com.tk.quicksearch.search.core.SearchToolType
import com.tk.quicksearch.search.data.UserAppPreferences

class TimestampConverterHandler(
    private val userPreferences: UserAppPreferences,
) {
    fun processQuery(
        query: String,
        forceTimestampMode: Boolean = false,
    ): CalculatorState {
        if (!forceTimestampMode && !userPreferences.isTimestampConverterEnabled()) {
            return CalculatorState()
        }

        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return if (forceTimestampMode) {
                CalculatorState(
                    isTimestampMode = true,
                    toolType = SearchToolType.TIMESTAMP_CONVERTER,
                )
            } else {
                CalculatorState()
            }
        }

        val processed = TimestampConverterUtils.detectAndProcess(trimmedQuery)
        if (processed != null) {
            return CalculatorState(
                result = processed.second,
                expression = processed.first,
                isTimestampMode = forceTimestampMode,
                toolType = SearchToolType.TIMESTAMP_CONVERTER,
            )
        }

        return if (forceTimestampMode) {
            CalculatorState(
                expression = trimmedQuery,
                isTimestampMode = true,
                toolType = SearchToolType.TIMESTAMP_CONVERTER,
                showInvalidExpression = true,
            )
        } else {
            CalculatorState()
        }
    }
}