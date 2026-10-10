package ai.myraa.native

import kotlinx.coroutines.flow.MutableStateFlow

/** The three character animation states preserved from the original app. */
enum class Anim { IDLE, TALKING, THINKING }

object AppState {
    val status = MutableStateFlow("Ready")
    val running = MutableStateFlow(false)
    val anim = MutableStateFlow(Anim.IDLE)
    val listening = MutableStateFlow(false)
    val log = MutableStateFlow<List<String>>(emptyList())

    fun add(line: String) {
        log.value = (log.value + line).takeLast(60)
    }
}
