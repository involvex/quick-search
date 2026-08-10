package com.tk.quicksearch.tools.termux

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TermuxResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val resultBundle = intent.getBundleExtra("result") ?: return
        val errCode = resultBundle.getInt("err", -1)

        if (errCode != RESULT_OK) {
            TermuxResultState.dispatchError(
                resultBundle.getInt("exit_code", -1),
                resultBundle.getString("errmsg"),
                errCode,
            )
            return
        }

        val stdout = resultBundle.getString("stdout") ?: ""
        val stderr = resultBundle.getString("stderr")
        val exitCode = resultBundle.getInt("exit_code", -1)

        TermuxResultState.dispatchSuccess(stdout, stderr, exitCode)
    }

    companion object {
        private const val RESULT_OK = -1
        private var nextRequestCode = 1000

        fun createPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, TermuxResultReceiver::class.java)
            val code = nextRequestCode++
            if (nextRequestCode > 2000) nextRequestCode = 1000
            return PendingIntent.getBroadcast(
                context,
                code,
                intent,
                PendingIntent.FLAG_IMMUTABLE,
            )
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
