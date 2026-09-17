package com.tk.quicksearch.tools.termux

import com.tk.quicksearch.search.core.TermuxExecutionMode
import com.tk.quicksearch.search.core.TermuxSavedCommand
import org.junit.Assert.assertEquals
import org.junit.Test

class TermuxSuggestionMatcherTest {
    private val commands =
        listOf(
            TermuxSavedCommand(
                id = "termux_cmd:1",
                name = "Update system",
                command = "pkg update && pkg upgrade -y",
                aliasCode = "upd",
            ),
            TermuxSavedCommand(
                id = "termux_cmd:2",
                name = "SSH home server",
                command = "ssh user@home",
                aliasCode = "home",
                executionMode = TermuxExecutionMode.FOREGROUND,
            ),
            TermuxSavedCommand(
                id = "termux_cmd:3",
                name = "Git sync notes",
                command = "git -C ~/notes pull",
                aliasCode = "sync",
            ),
        )

    @Test
    fun blankQueryReturnsNothing() {
        assertEquals(emptyList<TermuxSavedCommand>(), TermuxSuggestionMatcher.rankMatches("  ", commands))
    }

    @Test
    fun exactAliasRanksFirst() {
        val result = TermuxSuggestionMatcher.rankMatches("home", commands)
        assertEquals(listOf("termux_cmd:2"), result.map { it.id })
    }

    @Test
    fun aliasPrefixBeatsNameMatch() {
        val result = TermuxSuggestionMatcher.rankMatches("sy", commands)
        assertEquals(listOf("termux_cmd:3", "termux_cmd:1"), result.map { it.id })
    }

    @Test
    fun matchesNameAndCommandBody() {
        assertEquals(listOf("termux_cmd:1"), TermuxSuggestionMatcher.rankMatches("system", commands).map { it.id })
        assertEquals(listOf("termux_cmd:2"), TermuxSuggestionMatcher.rankMatches("ssh", commands).map { it.id })
    }

    @Test
    fun respectsLimit() {
        val result = TermuxSuggestionMatcher.rankMatches("s", commands, limit = 1)
        assertEquals(1, result.size)
    }
}
