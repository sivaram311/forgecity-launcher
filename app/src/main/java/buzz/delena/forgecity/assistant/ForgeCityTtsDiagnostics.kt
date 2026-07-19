package buzz.delena.forgecity.assistant

import android.util.Log

/**
 * Safe diagnostic events for `adb logcat -s ForgeCityTTS`.
 * Never pass API keys, notification bodies, titles, or translated text.
 */
object ForgeCityTtsDiagnostics {
    const val TAG = "ForgeCityTTS"

    fun info(event: String, details: String = "") {
        Log.i(TAG, if (details.isBlank()) event else "$event $details")
    }

    fun warn(event: String, details: String = "") {
        Log.w(TAG, if (details.isBlank()) event else "$event $details")
    }
}
