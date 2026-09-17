package com.tk.quicksearch.tools.termux

import com.tk.quicksearch.search.core.TermuxSavedCommand

/**
 * Fuzzy matching for saved Termux commands in global search.
 * Pure logic so it stays unit-testable; the query coordinator owns gating
 * (integration enabled, blank query, locked alias modes).
 */
object TermuxSuggestionMatcher {
    const val MAX_SUGGESTIONS = 5

    fun rankMatches(
        query: String,
        commands: List<TermuxSavedCommand>,
        limit: Int = MAX_SUGGESTIONS,
    ): List<TermuxSavedCommand> {
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty() || commands.isEmpty()) return emptyList()
        return commands
            .mapNotNull { command ->
                val tier = matchTier(normalized, command) ?: return@mapNotNull null
                tier to command
            }
            .sortedWith(compareBy({ it.first }, { it.second.name.lowercase() }))
            .take(limit.coerceAtLeast(0))
            .map { it.second }
    }

    private fun matchTier(normalizedQuery: String, command: TermuxSavedCommand): Int? {
        val alias = command.aliasCode.trim().lowercase()
        val name = command.name.trim().lowercase()
        val body = command.command.trim().lowercase()
        return when {
            alias.isNotEmpty() && alias == normalizedQuery -> 0
            alias.isNotEmpty() && alias.startsWith(normalizedQuery) -> 1
            name.isNotEmpty() && name.contains(normalizedQuery) -> 2
            alias.isNotEmpty() && alias.contains(normalizedQuery) -> 3
            body.isNotEmpty() && body.contains(normalizedQuery) -> 4
            else -> null
        }
    }
}
