package com.tk.quicksearch.tools.termux

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.tk.quicksearch.search.core.TermuxCommandState
import com.tk.quicksearch.search.core.TermuxCommandStatus
import com.tk.quicksearch.search.core.TermuxExecutionMode
import com.tk.quicksearch.search.data.UserAppPreferences

class TermuxCommandHandler(
    private val context: Context,
    private val userPreferences: UserAppPreferences,
) {
    /** Variant currently selected via preferences/auto-detect. Never cached. */
    fun selectedVariant(): TermuxVariant =
        TermuxVariant.resolve(context, userPreferences.getTermuxVariantPackage())

    fun installedVariants(): List<TermuxVariant> = TermuxVariant.installedVariants(context)

    fun isTermuxInstalled(): Boolean = selectedVariant().isInstalled(context)

    fun hasRunCommandPermission(): Boolean = selectedVariant().hasRunCommandPermission(context)

    /** Dangerous permission of the selected variant; must be granted at runtime. */
    fun runCommandPermission(): String = selectedVariant().runCommandPermission

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
        executionMode: TermuxExecutionMode = userPreferences.getTermuxDefaultExecutionMode(),
        onResult: (TermuxCommandState) -> Unit,
    ): TermuxCommandState {
        val variant = selectedVariant()

        if (!variant.isInstalled(context)) {
            return TermuxCommandState(
                status = TermuxCommandStatus.NotInstalled,
                command = command,
                executionMode = executionMode,
            )
        }

        if (!variant.hasRunCommandPermission(context)) {
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

        val trimmedCommand = command.trim()
        val intent = Intent().apply {
            setClassName(variant.packageName, variant.serviceClassName)
            action = variant.runCommandAction
            putExtra(variant.extraKey("RUN_COMMAND_PATH"), variant.bashPath(context))
            putExtra(variant.extraKey("RUN_COMMAND_ARGUMENTS"), arrayOf("-c", trimmedCommand))
            putExtra(variant.extraKey("RUN_COMMAND_WORKDIR"), variant.homePath(context))
            putExtra(variant.extraKey("RUN_COMMAND_BACKGROUND"), isBackground)
            putExtra(variant.extraKey("RUN_COMMAND_SESSION_ACTION"), "0")
            putExtra(variant.extraKey("RUN_COMMAND_COMMAND_LABEL"), "Quick Search")
            putExtra(
                variant.extraKey("RUN_COMMAND_COMMAND_DESCRIPTION"),
                trimmedCommand.take(COMMAND_DESCRIPTION_MAX_LENGTH),
            )
            if (isBackground) {
                putExtra(
                    variant.extraKey("RUN_COMMAND_PENDING_INTENT"),
                    TermuxResultReceiver.createPendingIntent(context),
                )
            }
        }

        return try {
            // RunCommandService runs as a foreground service on API 26+; use the
            // compat helper so the start also works from background surfaces.
            ContextCompat.startForegroundService(context, intent)
            if (!isBackground) {
                openTermuxApp(variant)
            }
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

    /**
     * Brings the Termux app to the foreground so a foreground-mode command is
     * actually visible. The RUN_COMMAND intent alone only starts the session;
     * without this the handoff looks like nothing happened.
     */
    private fun openTermuxApp(variant: TermuxVariant) {
        runCatching {
            val launch =
                context.packageManager.getLaunchIntentForPackage(variant.packageName) ?: return
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launch)
        }
    }

    companion object {
        private const val COMMAND_DESCRIPTION_MAX_LENGTH = 200
    }
}
