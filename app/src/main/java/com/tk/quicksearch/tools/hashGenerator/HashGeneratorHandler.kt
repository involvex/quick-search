package com.tk.quicksearch.tools.hashGenerator

import com.tk.quicksearch.search.core.CalculatorState
import com.tk.quicksearch.search.core.SearchToolType
import com.tk.quicksearch.search.data.UserAppPreferences

class HashGeneratorHandler(
    private val userPreferences: UserAppPreferences,
) {
    fun processQuery(
        query: String,
        forceHashMode: Boolean = false,
    ): CalculatorState {
        if (!forceHashMode && !userPreferences.isHashGeneratorEnabled()) {
            return CalculatorState()
        }

        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return if (forceHashMode) {
                CalculatorState(
                    isHashMode = true,
                    toolType = SearchToolType.HASH_GENERATOR,
                )
            } else {
                CalculatorState()
            }
        }

        val processed = HashGeneratorUtils.detectAndProcess(trimmedQuery)
        if (processed != null) {
            return CalculatorState(
                result = processed.second,
                expression = processed.first,
                isHashMode = forceHashMode,
                toolType = SearchToolType.HASH_GENERATOR,
            )
        }

        return if (forceHashMode) {
            CalculatorState(
                expression = trimmedQuery,
                isHashMode = true,
                toolType = SearchToolType.HASH_GENERATOR,
                showInvalidExpression = true,
            )
        } else {
            CalculatorState()
        }
    }
}