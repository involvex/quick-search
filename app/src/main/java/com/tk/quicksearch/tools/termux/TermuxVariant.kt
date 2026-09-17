package com.tk.quicksearch.tools.termux

import android.content.Context
import android.content.pm.PackageManager

/**
 * A Termux-compatible app installation that accepts `RUN_COMMAND` intents.
 *
 * Stock Termux and API-compatible forks (such as the custom
 * `com.involvex.termux_app` build) namespace their service, intent action,
 * permission and intent extras differently, so every integration point must go
 * through the selected variant instead of hardcoded `com.termux` constants.
 */
enum class TermuxVariant(
    val packageName: String,
    val serviceClassName: String,
    val runCommandAction: String,
    val runCommandPermission: String,
    /** Prefix for `RUN_COMMAND_*` intent extras, e.g. `com.termux`. */
    val intentExtraPrefix: String,
    val displayName: String,
) {
    STOCK(
        packageName = "com.termux",
        serviceClassName = "com.termux.app.RunCommandService",
        runCommandAction = "com.termux.RUN_COMMAND",
        runCommandPermission = "com.termux.permission.RUN_COMMAND",
        intentExtraPrefix = "com.termux",
        displayName = "Termux",
    ),
    INVOLVEX(
        packageName = "com.involvex.termux_app",
        serviceClassName = "com.invapp.app.RunCommandService",
        runCommandAction = "com.involvex.termux_app.RUN_COMMAND",
        runCommandPermission = "com.involvex.termux_app.permission.RUN_COMMAND",
        intentExtraPrefix = "com.involvex.termux_app",
        displayName = "Involvex Termux",
    ),
    ;

    fun extraKey(suffix: String): String = "$intentExtraPrefix.$suffix"

    fun isInstalled(context: Context): Boolean =
        runCatching {
            context.packageManager.getPackageInfo(packageName, 0)
        }.isSuccess

    fun hasRunCommandPermission(context: Context): Boolean =
        context.packageManager.checkPermission(runCommandPermission, context.packageName) ==
            PackageManager.PERMISSION_GRANTED

    /** `<dataDir>/files/usr/bin/bash`, resolved from the variant's real data dir. */
    fun bashPath(context: Context): String = "${appDataDir(context)}/files/usr/bin/bash"

    /** `<dataDir>/files/home`, resolved from the variant's real data dir. */
    fun homePath(context: Context): String = "${appDataDir(context)}/files/home"

    private fun appDataDir(context: Context): String =
        runCatching {
            context.packageManager.getApplicationInfo(packageName, 0).dataDir
        }.getOrNull() ?: "/data/data/$packageName"

    companion object {
        fun installedVariants(context: Context): List<TermuxVariant> =
            entries.filter { it.isInstalled(context) }

        /**
         * Resolve the variant to use: an explicit package override wins when that
         * app is installed, otherwise prefer Involvex Termux when installed and
         * fall back to stock Termux (also used as the status sentinel when
         * nothing is installed).
         */
        fun resolve(context: Context, packageOverride: String): TermuxVariant {
            val override = entries.firstOrNull { it.packageName == packageOverride }
            if (override != null && override.isInstalled(context)) return override
            return listOf(INVOLVEX, STOCK).firstOrNull { it.isInstalled(context) } ?: STOCK
        }
    }
}
