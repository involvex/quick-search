package com.tk.quicksearch.tools.termux

import com.tk.quicksearch.searchEngines.AliasValidator.isValidGeneralAliasCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TermuxCommandPresetsTest {
    @Test
    fun presetsAreCompleteAndValid() {
        assertTrue(TERMUX_COMMAND_PRESETS.isNotEmpty())
        TERMUX_COMMAND_PRESETS.forEach { preset ->
            assertTrue("blank name", preset.name.isNotBlank())
            assertTrue("blank command: ${preset.name}", preset.command.isNotBlank())
            assertTrue("invalid alias: ${preset.alias}", isValidGeneralAliasCode(preset.alias))
        }
    }

    @Test
    fun presetAliasesAndCommandsAreUnique() {
        assertEquals(
            TERMUX_COMMAND_PRESETS.size,
            TERMUX_COMMAND_PRESETS.map { it.alias.lowercase() }.distinct().size,
        )
        assertEquals(
            TERMUX_COMMAND_PRESETS.size,
            TERMUX_COMMAND_PRESETS.map { it.command }.distinct().size,
        )
    }
}
