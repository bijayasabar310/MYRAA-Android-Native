package ai.myraa.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

private val SECTIONS = listOf(
    "Appearance", "Voice", "AI Models", "Permissions", "Call Agent", "Integrations", "About"
)

@Composable
fun SettingsScreen() {
    val ctx = LocalContext.current
    var open by remember { mutableStateOf<String?>(null) }
    val current = open
    if (current != null) {
        SectionDetail(ctx, current) { open = null }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Text("Settings", fontSize = 24.sp, color = Color.White, modifier = Modifier.padding(6.dp))
        }
        items(SECTIONS) { s ->
            Card(
                modifier = Modifier.fillMaxWidth().height(112.dp).clickable { open = s },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14141F))
            ) {
                Column(
                    Modifier.fillMaxSize().padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(s, color = Color(0xFF8B7BFF), fontSize = 15.sp)
                    Text(descFor(s), color = Color(0xFF9A9AB8), fontSize = 11.sp)
                }
            }
        }
    }
}

private fun descFor(s: String) = when (s) {
    "Appearance" -> "Character, theme, desktop mode"
    "Voice" -> "Language, speech and voice"
    "AI Models" -> "API key and model"
    "Permissions" -> "Mic, contacts, calls"
    "Call Agent" -> "How MYRAA places calls"
    "Integrations" -> "Contacts, apps, reminders"
    else -> "Version and credits"
}

@Composable
private fun SectionDetail(ctx: Context, section: String, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("< Back") }
            Spacer(Modifier.width(8.dp))
            Text(section, fontSize = 20.sp, color = Color.White)
        }
        when (section) {
            "Appearance" -> AppearanceSection()
            "Voice" -> VoiceSection(ctx)
            "AI Models" -> AiSection(ctx)
            "Permissions" -> PermissionsSection(ctx)
            "Call Agent" -> CallSection(ctx)
            "Integrations" -> IntegrationsSection()
            else -> AboutSection()
        }
    }
}

@Composable
private fun AppearanceSection() {
    InfoCard(
        "Character",
        "Evelyn — the original 3D character. Her PMX model, textures and the idle / talking / thinking animations are bundled and preserved, and she reacts to MYRAA's state."
    )
    InfoCard("Theme", "Dark theme with the MYRAA violet accent (unchanged).")
    InfoCard(
        "Desktop mode",
        "Removed. MYRAA is now a pure Android-native app — the desktop control agent and every desktop-only option are gone."
    )
}

@Composable
private fun VoiceSection(ctx: Context) {
    var tag by remember { mutableStateOf(Prefs.get(ctx, Prefs.LANG_TAG, "hi-IN")) }
    Text("Recognition & reply language", color = Color(0xFF9A9AB8), fontSize = 13.sp)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LangButton("English", "en-IN", tag) { tag = it; Prefs.put(ctx, Prefs.LANG_TAG, it) }
        LangButton("Hindi / Hinglish", "hi-IN", tag) { tag = it; Prefs.put(ctx, Prefs.LANG_TAG, it) }
    }
    var voice by remember { mutableStateOf(Prefs.get(ctx, Prefs.VOICE, "Aoede")) }
    Field("Voice name", voice) { voice = it; Prefs.put(ctx, Prefs.VOICE, it) }
    InfoCard(
        "Speech",
        "Uses the phone's on-device speech recognition and text-to-speech. Hindi, Hinglish and English are supported."
    )
}

@Composable
private fun LangButton(label: String, value: String, current: String, onPick: (String) -> Unit) {
    Button(
        onClick = { onPick(value) },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (current == value) Color(0xFF7C5CFF) else Color(0xFF2A2A3A)
        )
    ) { Text(label) }
}

@Composable
private fun AiSection(ctx: Context) {
    var key by remember { mutableStateOf(Prefs.get(ctx, Prefs.KEY, "")) }
    var model by remember { mutableStateOf(Prefs.get(ctx, Prefs.MODEL, Prefs.DEFAULT_MODEL)) }
    var name by remember { mutableStateOf(Prefs.get(ctx, Prefs.NAME, "")) }
    var show by remember { mutableStateOf(false) }

    Field("Your name", name) { name = it; Prefs.put(ctx, Prefs.NAME, it) }
    OutlinedTextField(
        value = key,
        onValueChange = { key = it; Prefs.put(ctx, Prefs.KEY, it.trim()) },
        label = { Text("AI API key") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation()
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { show = !show }) { Text(if (show) "Hide" else "Show") }
        TextButton(onClick = {
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey")))
        }) { Text("Get a free key") }
    }
    Field("Model name", model) { model = it; Prefs.put(ctx, Prefs.MODEL, it.trim()) }
    InfoCard(
        "Security",
        "The API key is stored only in this device's private storage and is never hardcoded in the source. It is sent only to the AI provider you configure."
    )
}

@Composable
private fun PermissionsSection(ctx: Context) {
    val perms = listOf(
        Manifest.permission.RECORD_AUDIO to "Microphone (voice commands)",
        Manifest.permission.READ_CONTACTS to "Contacts (find who to call)",
        Manifest.permission.CALL_PHONE to "Phone (place calls)",
        Manifest.permission.POST_NOTIFICATIONS to "Notifications (reminders)"
    )
    perms.forEach { (p, label) ->
        val ok = ContextCompat.checkSelfPermission(ctx, p) == PackageManager.PERMISSION_GRANTED
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(if (ok) "[x] " else "[ ] ", color = if (ok) Color(0xFF7CE38B) else Color(0xFFFFC46B))
            Text(label, Modifier.weight(1f), fontSize = 14.sp, color = Color.White)
            if (!ok) {
                TextButton(onClick = {
                    ctx.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + ctx.packageName)
                        )
                    )
                }) { Text("Open") }
            }
        }
    }
}

@Composable
private fun CallSection(ctx: Context) {
    var auto by remember { mutableStateOf(Prefs.getBool(ctx, Prefs.AUTO_CALL, false)) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Switch(checked = auto, onCheckedChange = { auto = it; Prefs.putBool(ctx, Prefs.AUTO_CALL, it) })
        Spacer(Modifier.width(8.dp))
        Text(
            "Place the call directly (off = open the dialer instead)",
            fontSize = 13.sp,
            color = Color.White
        )
    }
    InfoCard(
        "How calling works",
        "Say \"call Rina\". MYRAA finds the contact, shows the name and number for confirmation, then calls. MYRAA always confirms the recipient before dialling."
    )
}

@Composable
private fun IntegrationsSection() {
    InfoCard("Contacts", "Used to resolve a spoken name to a phone number.")
    InfoCard("Apps", "MYRAA can open any installed app by name.")
    InfoCard("Reminders", "Scheduled with Android alarms and delivered as notifications.")
    InfoCard("AI provider", "A configurable Gemini-compatible text API. No key is bundled with the app.")
}

@Composable
private fun AboutSection() {
    InfoCard("MYRAA", "Android-native voice assistant, v1.0.2.")
    InfoCard(
        "Character",
        "Evelyn — the original 3D character and her idle, talking and thinking animations, preserved."
    )
    InfoCard(
        "Notes",
        "A native Android rewrite of the original app. The desktop control agent and all desktop-only options were removed."
    )
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14141F))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = Color(0xFF8B7BFF), fontSize = 14.sp)
            Text(body, color = Color(0xFF9A9AB8), fontSize = 12.sp)
        }
    }
}
