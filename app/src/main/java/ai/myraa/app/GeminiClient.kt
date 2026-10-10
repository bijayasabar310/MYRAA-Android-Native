package ai.myraa.app

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Minimal, configurable AI client. The API key is never hardcoded — it comes from Settings. */
object GeminiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    fun generate(apiKey: String, model: String, system: String, prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
        val body = JSONObject()
            .put("system_instruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system))))
            .put(
                "contents", JSONArray().put(
                    JSONObject().put("role", "user")
                        .put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                )
            )
        val req = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", apiKey)
            .post(body.toString().toRequestBody(JSON))
            .build()
        client.newCall(req).execute().use { resp ->
            val s = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) return "AI request failed (${resp.code}). Check your key or model name in Settings."
            val j = try { JSONObject(s) } catch (_: Exception) { return "Unexpected AI response." }
            val cands = j.optJSONArray("candidates") ?: return "No answer from the model."
            if (cands.length() == 0) return "No answer from the model."
            val parts = cands.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts") ?: return "Empty answer."
            val sb = StringBuilder()
            for (i in 0 until parts.length()) sb.append(parts.getJSONObject(i).optString("text"))
            return sb.toString().trim().ifBlank { "…" }
        }
    }
}
