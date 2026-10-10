package ai.myraa.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen() {
    val ctx = LocalContext.current
    val anim by AppState.anim.collectAsState()
    val status by AppState.status.collectAsState()
    val log by AppState.log.collectAsState()
    val listening by AppState.listening.collectAsState()

    val voice = remember { mutableStateOf<VoiceEngine?>(null) }
    var pendingCall by remember { mutableStateOf<Pair<String, String>?>(null) }

    DisposableEffect(Unit) {
        val v = VoiceEngine(
            ctx,
            onResult = { text ->
                Assistant.handle(
                    ctx, text,
                    speak = { reply ->
                        AppState.add("Myraa: $reply")
                        voice.value?.speak(reply, Prefs.get(ctx, Prefs.LANG_TAG, "hi-IN"))
                    },
                    confirmCall = { name, number -> pendingCall = name to number }
                )
            },
            onError = { AppState.add(it) }
        )
        v.init()
        voice.value = v
        onDispose { v.release() }
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.fillMaxWidth().weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0B0B14))
        ) {
            CharacterStage(anim, Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(12.dp))
        Text(status, color = Color(0xFFB8B8D0), fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                val v = voice.value
                if (listening) v?.stop() else v?.listen(Prefs.get(ctx, Prefs.LANG_TAG, "hi-IN"))
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text(if (listening) "Stop listening" else "Talk to MYRAA") }
        Spacer(Modifier.height(12.dp))
        LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
            items(log) { line ->
                Text(
                    line,
                    color = Color(0xFF9A9AB8),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }

    val call = pendingCall
    if (call != null) {
        AlertDialog(
            onDismissRequest = { pendingCall = null },
            title = { Text("Confirm call") },
            text = { Text("Call ${call.first}?\n${call.second}") },
            confirmButton = {
                TextButton(onClick = {
                    CallAgent.dial(ctx, call.second, Prefs.getBool(ctx, Prefs.AUTO_CALL, false))
                    pendingCall = null
                }) { Text("Call") }
            },
            dismissButton = {
                TextButton(onClick = { pendingCall = null }) { Text("Cancel") }
            }
        )
    }
}
