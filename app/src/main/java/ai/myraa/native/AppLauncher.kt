package ai.myraa.native

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/** Opens any installed app by spoken name. */
object AppLauncher {
    fun open(ctx: Context, name: String): String? {
        val q = name.lowercase().trim()
        if (q.isEmpty()) return null
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        val hit = apps.firstOrNull { it.loadLabel(pm).toString().lowercase() == q }
            ?: apps.firstOrNull { it.loadLabel(pm).toString().lowercase().contains(q) }
            ?: apps.firstOrNull { it.activityInfo.packageName.lowercase().contains(q) }
            ?: return null
        val launch = pm.getLaunchIntentForPackage(hit.activityInfo.packageName) ?: return null
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(launch)
        return hit.loadLabel(pm).toString()
    }
}
