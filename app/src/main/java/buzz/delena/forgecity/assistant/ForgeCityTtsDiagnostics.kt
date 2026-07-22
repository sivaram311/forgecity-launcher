package buzz.delena.forgecity.assistant

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Safe diagnostic events for `adb logcat -s ForgeCityTTS` **and** an in-app
 * append-only ring buffer the user can copy-paste to an agent.
 *
 * Never pass API keys, notification bodies, titles, or translated text.
 */
object ForgeCityTtsDiagnostics {
    const val TAG = "ForgeCityTTS"
    const val MAX_LINES = 200

    private val lock = Any()
    private val lines = ArrayList<String>(MAX_LINES + 8)
    private val _snapshot = MutableStateFlow("")
    val snapshot: StateFlow<String> = _snapshot.asStateFlow()

    private val timeFormat =
        ThreadLocal.withInitial {
            SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
        }

    fun info(event: String, details: String = "") {
        val line = formatLine("I", event, details)
        Log.i(TAG, if (details.isBlank()) event else "$event $details")
        append(line)
    }

    fun warn(event: String, details: String = "") {
        val line = formatLine("W", event, details)
        Log.w(TAG, if (details.isBlank()) event else "$event $details")
        append(line)
    }

    /** Append a UI status string (already scrubbed by callers). */
    fun uiStatus(message: String) {
        val safe = message.trim().take(160)
        if (safe.isEmpty()) return
        info("ui_status", safe)
    }

    fun clear() {
        synchronized(lock) {
            lines.clear()
            _snapshot.value = ""
        }
    }

    fun lineCount(): Int = synchronized(lock) { lines.size }

    private fun formatLine(level: String, event: String, details: String): String {
        val ts = timeFormat.get()!!.format(Date())
        val body = if (details.isBlank()) event else "$event $details"
        return "$ts $level $body"
    }

    private fun append(line: String) {
        synchronized(lock) {
            lines.add(line)
            while (lines.size > MAX_LINES) {
                lines.removeAt(0)
            }
            _snapshot.value = lines.joinToString("\n")
        }
    }
}
