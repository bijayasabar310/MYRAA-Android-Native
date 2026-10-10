package ai.myraa.native

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract

/** Resolves a spoken name or number, and places the call only after confirmation. */
object CallAgent {

    fun looksLikeNumber(q: String): Boolean {
        val t = q.trim()
        if (t.isEmpty()) return false
        val digits = t.count { it.isDigit() || it == '+' }
        val allowed = t.count { it.isDigit() || it == '+' || it == ' ' || it == '-' || it == '(' || it == ')' }
        return digits >= 3 && allowed >= t.length - 1
    }

    /** Returns a phone number for the query, or null if it cannot be resolved. */
    fun resolve(ctx: Context, query: String): String? {
        val q = query.trim()
        if (q.isEmpty()) return null
        if (looksLikeNumber(q)) return q.filter { it.isDigit() || it == '+' }
        if (ctx.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return null
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val proj = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val needle = q.lowercase()
        try {
            ctx.contentResolver.query(uri, proj, null, null, null)?.use { c ->
                val nameIdx = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (c.moveToNext()) {
                    val name = c.getString(nameIdx) ?: continue
                    val num = c.getString(numIdx) ?: continue
                    if (name.lowercase().contains(needle)) return num
                }
            }
        } catch (_: Exception) {}
        return null
    }

    fun dial(ctx: Context, number: String, direct: Boolean) {
        val uri = Uri.parse("tel:" + Uri.encode(number))
        val canCall = ctx.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        val action = if (direct && canCall) Intent.ACTION_CALL else Intent.ACTION_DIAL
        ctx.startActivity(Intent(action, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
