package com.tk.quicksearch.tools.termux

import com.tk.quicksearch.search.core.TermuxExecutionMode

/** One-tap starter command for the Termux settings screen. */
data class TermuxCommandPreset(
    val name: String,
    val alias: String,
    val command: String,
    val executionMode: TermuxExecutionMode = TermuxExecutionMode.BACKGROUND,
    /** True when the command needs the separate Termux:API app installed. */
    val requiresApiApp: Boolean = false,
)

/** Curated starter commands. Aliases must stay unique and conflict-free. */
val TERMUX_COMMAND_PRESETS: List<TermuxCommandPreset> =
    listOf(
        TermuxCommandPreset(
            name = "Update packages",
            alias = "upd",
            command = "pkg update -y",
        ),
        TermuxCommandPreset(
            name = "Upgrade packages",
            alias = "upg",
            command = "pkg upgrade -y",
        ),
        TermuxCommandPreset(
            name = "Enable storage access",
            alias = "storage",
            command = "termux-setup-storage",
            executionMode = TermuxExecutionMode.FOREGROUND,
        ),
        TermuxCommandPreset(
            name = "Device info",
            alias = "sysinfo",
            command = "uname -a && uptime",
        ),
        TermuxCommandPreset(
            name = "Disk usage",
            alias = "disk",
            command = "df -h ~",
        ),
        TermuxCommandPreset(
            name = "List home folder",
            alias = "lshome",
            command = "ls -la ~",
        ),
        TermuxCommandPreset(
            name = "Battery status",
            alias = "battery",
            command = "termux-battery-status",
            requiresApiApp = true,
        ),
        TermuxCommandPreset(
            name = "Show clipboard",
            alias = "clip",
            command = "termux-clipboard-get",
            requiresApiApp = true,
        ),
        TermuxCommandPreset(
            name = "Public IP address",
            alias = "myip",
            command = "curl -s ifconfig.me; echo",
        ),
    )
