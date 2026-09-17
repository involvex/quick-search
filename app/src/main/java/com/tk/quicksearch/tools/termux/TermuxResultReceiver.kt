package com.tk.quicksearch.tools.termux

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TermuxResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Key confirmed against TermuxConstants.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE.
        val resultBundle = intent.getBundleExtra(RESULT_BUNDLE_KEY) ?: return
        val errCode = resultBundle.getInt(RESULT_ERR_KEY, RESULT_OK)

        if (errCode != RESULT_OK) {
            TermuxResultState.dispatchError(
                readExitCode(resultBundle),
                resultBundle.getString(RESULT_ERRMSG_KEY),
                errCode,
            )
            return
        }

        val stdout = resultBundle.getString(RESULT_STDOUT_KEY) ?: ""
        val stderr = resultBundle.getString(RESULT_STDERR_KEY)
        val exitCode = readExitCode(resultBundle)

        TermuxResultState.dispatchSuccess(stdout, stderr, exitCode)
    }

    companion object {
        private const val RESULT_BUNDLE_KEY = "result"
        private const val RESULT_STDOUT_KEY = "stdout"
        private const val RESULT_STDERR_KEY = "stderr"
        private const val RESULT_EXIT_CODE_KEY = "exitCode"
        private const val RESULT_EXIT_CODE_LEGACY_KEY = "exit_code"
        private const val RESULT_ERR_KEY = "err"
        private const val RESULT_ERRMSG_KEY = "errmsg"
        private const val RESULT_OK = -1

        private val nextRequestCode = java.util.concurrent.atomic.AtomicInteger(1000)

        fun createPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, TermuxResultReceiver::class.java)
            // Termux attaches the result bundle to this PendingIntent when sending
            // the result, so it MUST be mutable (plus update-current so repeated
            // executions don't reuse a stale one). FLAG_IMMUTABLE silently drops
            // the result and leaves the UI stuck on "Executing...".
            return PendingIntent.getBroadcast(
                context,
                nextRequestCode.getAndIncrement(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
            )
        }

        private fun readExitCode(resultBundle: android.os.Bundle): Int =
            when {
                resultBundle.containsKey(RESULT_EXIT_CODE_KEY) ->
                    resultBundle.getInt(RESULT_EXIT_CODE_KEY, -1)
                else -> resultBundle.getInt(RESULT_EXIT_CODE_LEGACY_KEY, -1)
            }
    }
}

object TermuxResultState {
    var lastCommand: String? = null
        private set
    var stdout: String? = null
        private set
    var stderr: String? = null
        private set
    var exitCode: Int? = null
        private set
    var pending: Boolean = false
        private set
    var errorMessage: String? = null
        private set

    private var listener: ((TermuxResultState) -> Unit)? = null

    fun prepare(command: String, callback: (TermuxResultState) -> Unit) {
        lastCommand = command
        stdout = null
        stderr = null
        exitCode = null
        errorMessage = null
        pending = true
        listener = callback
    }

    fun dispatchSuccess(sout: String, serr: String?, code: Int) {
        stdout = sout
        stderr = serr
        exitCode = code
        errorMessage = null
        pending = false
        listener?.invoke(this)
    }

    fun dispatchError(code: Int, errmsg: String?, errCode: Int) {
        exitCode = code
        pending = false
        errorMessage = errmsg ?: "Termux error code: $errCode"
        listener?.invoke(this)
    }
}
