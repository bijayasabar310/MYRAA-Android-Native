package ai.myraa.native

import android.content.Context

/**
 * Routes a recognised utterance to the right capability (call / open app / reminder /
 * general AI) so each action stays modular.
 */
object Assistant {

    private val CALL_WORDS = listOf("call", "phone", "dial", "ring", "कॉल", "फोन")
    private val REMIND_WORDS = listOf("remind", "reminder", "yaad", "याद")
    private val OPEN_WORDS = listOf("open", "launch", "khol", "kholo", "खोल")

    fun handle(
        ctx: Context,
        raw: String,
        speak: (String) -> Unit,
        confirmCall: (String, String) -> Unit
    ) {
        val text = raw.trim()
        if (text.isEmpty()) return
        AppState.add("You: $text")
        val t = text.lowercase()

        if (hasAny(t, CALL_WORDS)) {
            val target = extractCallTarget(text)
            if (target.isBlank()) {
                speak("Who should I call?")
                return
            }
            AppState.status.value = "Finding contact…"
            Thread {
                val number = CallAgent.resolve(ctx, target)
                if (number == null) {
                    speak("I couldn't find $target in your contacts.")
                } else {
                    confirmCall(target, number)
                }
            }.start()
            return
        }

        if (hasAny(t, REMIND_WORDS)) {
            val (msg, delay) = parseReminder(text)
            Reminders.schedule(ctx, msg, System.currentTimeMillis() + delay)
            speak("Okay, I'll remind you.")
            return
        }

        if (t.startsWith("open ") || hasAny(t, OPEN_WORDS)) {
            val app = extractTarget(text, OPEN_WORDS)
            if (app.isNotBlank()) {
                val label = AppLauncher.open(ctx, app)
                speak(if (label != null) "Opening $label" else "I couldn't find an app called $app")
                return
            }
        }

        askAi(ctx, text, speak)
    }

    private fun askAi(ctx: Context, prompt: String, speak: (String) -> Unit) {
        val key = Prefs.get(ctx, Prefs.KEY, "").trim()
        if (key.isEmpty()) {
            speak("Please add your AI API key in Settings first.")
            return
        }
        AppState.anim.value = Anim.THINKING
        AppState.status.value = "Thinking…"
        val model = Prefs.get(ctx, Prefs.MODEL, Prefs.DEFAULT_MODEL)
        Thread {
            val reply = try {
                GeminiClient.generate(key, model, Persona.prompt(ctx), prompt)
            } catch (e: Exception) {
                "Sorry, something went wrong: ${e.message}"
            }
            AppState.add("Myraa: $reply")
            speak(reply)
        }.start()
    }

    private fun hasAny(t: String, words: List<String>) = words.any { t.contains(it) }

    private fun extractCallTarget(text: String): String {
        var s = text
        for (w in CALL_WORDS) s = s.replace(w, " ", ignoreCase = true)
        for (w in listOf("please", "to", "ko", "se", "kar", "karo", "do", "my", "mera", "meri", "call")) {
            s = s.replace(Regex("(?i)\\b$w\\b"), " ")
        }
        return s.trim().trim('.', ',', '!', '?', ' ').trim()
    }

    private fun extractTarget(text: String, keys: List<String>): String {
        var s = text
        for (k in keys) s = s.replace(Regex("(?i)\\b$k\\b"), " ")
        return s.trim().trim('.', ',', '!', '?', ' ').trim()
    }

    private fun parseReminder(text: String): Pair<String, Long> {
        val t = text.lowercase()
        val m = Regex("in (\\d+)\\s?(second|seconds|sec|minute|minutes|min|hour|hours|hr)").find(t)
        var delay = 60_000L
        var msg = text
        if (m != null) {
            val n = m.groupValues[1].toLongOrNull() ?: 1L
            delay = when {
                m.groupValues[2].startsWith("sec") -> n * 1000
                m.groupValues[2].startsWith("h") -> n * 3_600_000
                else -> n * 60_000
            }
            msg = text.replace(m.value, " ", ignoreCase = true)
        }
        for (w in REMIND_WORDS + listOf("me", "to", "that", "my", "please")) {
            msg = msg.replace(Regex("(?i)\\b$w\\b"), " ")
        }
        val clean = msg.trim().trim('.', ',', '!', '?', ' ').trim().ifBlank { "Reminder" }
        return clean to delay
    }
}
