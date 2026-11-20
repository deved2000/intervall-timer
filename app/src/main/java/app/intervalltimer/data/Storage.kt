package app.intervalltimer.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Speichert alles lokal auf dem Gerät (SharedPreferences). Kein Konto, keine Cloud. */
class Storage(context: Context) {
    private val prefs = context.getSharedPreferences("intervalltimer", Context.MODE_PRIVATE)

    // ---------- Voreinstellungen ----------
    fun loadPresets(): List<Preset> {
        val raw = prefs.getString(KEY_PRESETS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Preset(
                    id = o.getLong("id"),
                    name = o.optString("name"),
                    prepare = o.optInt("prepare", 5),
                    sets = o.optInt("sets", 3),
                    work = o.optInt("work", 90),
                    rest = o.optInt("rest", 30),
                    cooldown = o.optInt("cooldown", 0),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun savePresets(list: List<Preset>) {
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(
                JSONObject()
                    .put("id", p.id)
                    .put("name", p.name)
                    .put("prepare", p.prepare)
                    .put("sets", p.sets)
                    .put("work", p.work)
                    .put("rest", p.rest)
                    .put("cooldown", p.cooldown)
            )
        }
        prefs.edit().putString(KEY_PRESETS, arr.toString()).apply()
    }

    // ---------- Schnellstart ----------
    fun loadQuickStart(): QuickStart {
        val d = QuickStart()
        return QuickStart(
            sets = prefs.getInt("qs_sets", d.sets),
            work = prefs.getInt("qs_work", d.work),
            rest = prefs.getInt("qs_rest", d.rest),
        )
    }

    fun saveQuickStart(q: QuickStart) {
        prefs.edit()
            .putInt("qs_sets", q.sets)
            .putInt("qs_work", q.work)
            .putInt("qs_rest", q.rest)
            .apply()
    }

    // ---------- Einstellungen ----------
    fun loadSettings(): Settings {
        val d = Settings()
        return Settings(
            finalCount = prefs.getInt("final_count", d.finalCount),
            volume = prefs.getFloat("volume", d.volume),
            skipLastRest = prefs.getBoolean("skip_last_rest", d.skipLastRest),
            colorPrepare = prefs.getLong("color_prepare", d.colorPrepare),
            colorWork = prefs.getLong("color_work", d.colorWork),
            colorRest = prefs.getLong("color_rest", d.colorRest),
            colorCooldown = prefs.getLong("color_cooldown", d.colorCooldown),
            colorFinish = prefs.getLong("color_finish", d.colorFinish),
        )
    }

    fun saveSettings(s: Settings) {
        prefs.edit()
            .putInt("final_count", s.finalCount)
            .putFloat("volume", s.volume)
            .putBoolean("skip_last_rest", s.skipLastRest)
            .putLong("color_prepare", s.colorPrepare)
            .putLong("color_work", s.colorWork)
            .putLong("color_rest", s.colorRest)
            .putLong("color_cooldown", s.colorCooldown)
            .putLong("color_finish", s.colorFinish)
            .apply()
    }

    companion object {
        private const val KEY_PRESETS = "presets"
    }
}
