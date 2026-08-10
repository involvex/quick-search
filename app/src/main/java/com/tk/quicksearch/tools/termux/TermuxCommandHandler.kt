package com.tk.quicksearch.tools.termux

import android.content.Context
import android.content.Intent
import com.tk.quicksearch.search.core.TermuxCommandState
import com.tk.quicksearch.search.core.TermuxCommandStatus
import com.tk.quicksearch.search.core.TermuxExecutionMode
import com.tk.quicksearch.search.data.UserAppPreferences

class TermuxCommandHandler(
    private val context: Context,
    private val userPreferences: UserAppPreferences,
) {
    companion object {
        const val TERMUX_PACKAGE = "com.termux"
        private const val RUN_COMMAND_SERVICE = "com.termux.app.RunCommandService"
        private const val RUN_COMMAND_ACTION = "com.termux.RUN_COMMAND"
        private const val EXTRA_RUN_COMMAND_PATH = "com.termux.RUN_COMMAND_PATH"
        private const val EXTRA_RUN_COMMAND_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"
        private const val EXTRA_RUN_COMMAND_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
        private const val EXTRA_RUN_COMMAND_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"
        private const val EXTRA_RUN_COMMAND_SESSION_ACTION = "com.termux.RUN_COMMAND_SESSION_ACTION"
        private const val EXTRA_PENDING_INTENT = "com.termux.RUN_COMMAND_PENDING_INTENT"
        private const val TERMUX_HOME = "/data/data/com.termux/files/home"
        private const val BASH_PATH = "/data/data/com.termux/files/usr/bin/bash"
    }

    fun isTermuxInstalled(): Boolean =
        runCatching {
            context.packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
        }.isSuccess

    fun hasRunCommandPermission(): Boolean =
        context.packageManager.checkPermission(
            "com.termux.permission.RUN_COMMAND",
            context.packageName,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    fun buildIdleState(
        command: String,
        executionMode: TermuxExecutionMode = userPreferences.getTermuxDefaultExecutionMode(),
    ): TermuxCommandState =
        TermuxCommandState(
            status = TermuxCommandStatus.Idle,
            command = command,
            executionMode = executionMode,
        )

    fun executeCommand(
        command: String,
        onResult: (TermuxCommandState) -> Unit,
    ): TermuxCommandState {
        val executionMode = userPreferences.getTermuxDefaultExecutionMode()

        if (!isTermuxInstalled()) {
            return TermuxCommandState(
                status = TermuxCommandStatus.NotInstalled,
                command = command,
                executionMode = executionMode,
            )
        }

        if (!hasRunCommandPermission()) {
            return TermuxCommandState(
                status = TermuxCommandStatus.PermissionError,
                command = command,
                executionMode = executionMode,
            )
        }

        val isBackground = executionMode == TermuxExecutionMode.BACKGROUND

        if (isBackground) {
            TermuxResultState.prepare(command) { resultState ->
                if (resultState.errorMessage != null) {
                    onResult(
                        TermuxCommandState(
                            status = TermuxCommandStatus.Error,
                            command = resultState.lastCommand,
                            executionMode = TermuxExecutionMode.BACKGROUND,
                            errorMessage = resultState.errorMessage,
                            exitCode = resultState.exitCode,
                        ),
                    )
                } else {
                    onResult(
                        TermuxCommandState(
                            status = TermuxCommandStatus.Success,
                            command = resultState.lastCommand,
                            executionMode = TermuxExecutionMode.BACKGROUND,
                            stdout = resultState.stdout,
                            stderr = resultState.stderr,
                            exitCode = resultState.exitCode,
                        ),
                    )
                }
            }
        }

        val intent = Intent().apply {
            setClassName(TERMUX_PACKAGE, RUN_COMMAND_SERVICE)
            action = RUN_COMMAND_ACTION
            putExtra(EXTRA_RUN_COMMAND_PATH, BASH_PATH)
            putExtra(EXTRA_RUN_COMMAND_ARGUMENTS, arrayOf("-c", command.trim()))
            putExtra(EXTRA_RUN_COMMAND_WORKDIR, TERMUX_HOME)
            putExtra(EXTRA_RUN_COMMAND_BACKGROUND, isBackground)
            putExtra(EXTRA_RUN_COMMAND_SESSION_ACTION, "0")
            if (isBackground) {
                putExtra(
                    EXTRA_PENDING_INTENT,
                    TermuxResultReceiver.createPendingIntent(context),
                )
            }
        }

        return try {
            context.startService(intent)
            TermuxCommandState(
                status = TermuxCommandStatus.Loading,
                command = command,
                executionMode = executionMode,
            )
        } catch (e: Exception) {
            TermuxCommandState(
                status = TermuxCommandStatus.Error,
                command = command,
                executionMode = executionMode,
                errorMessage = e.message ?: "Failed to start Termux service",
            )
        }
    }
}
