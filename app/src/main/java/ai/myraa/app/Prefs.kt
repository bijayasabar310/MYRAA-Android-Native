package ai.myraa.app

import android.content.Context

object Prefs {
    const val KEY = "api_key"
    const val MODEL = "model"
    const val LANG_TAG = "lang_tag"
    const val NAME = "name"
    const val VOICE = "voice"
    const val AUTO_CALL = "auto_call"
    const val DEFAULT_MODEL = "gemini-2.0-flash"

    private fun sp(c: Context) = c.getSharedPreferences("myraa_native", Context.MODE_PRIVATE)

    fun get(c: Context, k: String, d: String): String = sp(c).getString(k, d) ?: d
    fun put(c: Context, k: String, v: String) = sp(c).edit().putString(k, v).apply()
    fun getBool(c: Context, k: String, d: Boolean): Boolean = sp(c).getBoolean(k, d)
    fun putBool(c: Context, k: String, v: Boolean) = sp(c).edit().putBoolean(k, v).apply()
}
