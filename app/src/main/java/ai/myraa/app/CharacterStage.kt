package ai.myraa.app

import android.content.Context
import android.view.ViewGroup
import android.widget.VideoView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File

/**
 * The preserved character "Evelyn". The PMX model + textures stay bundled under
 * assets/public/characters/evelyn, and the three animation clips (idle, talking,
 * thinking) are played here so the character reacts to MYRAA's state.
 */
object CharacterAssets {
    val VIDEOS = mapOf(
        Anim.IDLE to "idle.mp4",
        Anim.TALKING to "talking.mp4",
        Anim.THINKING to "thinking.mp4"
    )

    fun prepare(ctx: Context, asset: String): String {
        val out = File(ctx.filesDir, asset)
        if (!out.exists() || out.length() == 0L) {
            ctx.assets.open("public/$asset").use { input ->
                out.outputStream().use { input.copyTo(it) }
            }
        }
        return out.absolutePath
    }
}

@Composable
fun CharacterStage(anim: Anim, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val path = remember(anim) { CharacterAssets.prepare(ctx, CharacterAssets.VIDEOS[anim] ?: "idle.mp4") }
    AndroidView(
        modifier = modifier,
        factory = { c ->
            VideoView(c).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setOnPreparedListener { mp -> mp.isLooping = true; mp.start() }
                tag = path
                setVideoPath(path)
            }
        },
        update = { v ->
            if (v.tag != path) {
                v.tag = path
                v.setVideoPath(path)
                v.start()
            }
        }
    )
}
