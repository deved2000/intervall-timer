package app.intervalltimer.data

/** Alle Zeiten in Sekunden. */
data class Preset(
    val id: Long = System.currentTimeMillis(),
    val name: String = "",
    val prepare: Int = 5,
    val sets: Int = 3,
    val work: Int = 90,
    val rest: Int = 30,
    val cooldown: Int = 0,
) {
    /** Gesamtdauer in Sekunden (berücksichtigt "Letzte Pause überspringen"). */
    fun totalSeconds(skipLastRest: Boolean): Int {
        val rests = if (skipLastRest) (sets - 1).coerceAtLeast(0) else sets
        return prepare + sets * work + rests * rest + cooldown
    }
}

/** Werte der Schnellstart-Karte. */
data class QuickStart(
    val sets: Int = 19,
    val work: Int = 30,
    val rest: Int = 15,
) {
    fun toPreset(name: String = "Schnellstart") =
        Preset(name = name, prepare = QUICKSTART_PREPARE, sets = sets, work = work, rest = rest, cooldown = 0)

    companion object {
        const val QUICKSTART_PREPARE = 5
    }
}

enum class Phase { PREPARE, WORK, REST, COOLDOWN, FINISH }

data class Settings(
    /** Erster Gong N Sekunden vor Ende, dann jede Sekunde, plus Gong beim Ende. */
    val finalCount: Int = 2,
    /** Lautstärke des Gongs (0..1). */
    val volume: Float = 0.2f,
    val skipLastRest: Boolean = true,
    val colorPrepare: Long = 0xFFC75000,
    val colorWork: Long = 0xFF00883F,
    val colorRest: Long = 0xFF1B7CC0,
    val colorCooldown: Long = 0xFF1B7CC0,
    val colorFinish: Long = 0xFF008792,
) {
    fun colorOf(phase: Phase): Long = when (phase) {
        Phase.PREPARE -> colorPrepare
        Phase.WORK -> colorWork
        Phase.REST -> colorRest
        Phase.COOLDOWN -> colorCooldown
        Phase.FINISH -> colorFinish
    }

    fun withColor(phase: Phase, color: Long): Settings = when (phase) {
        Phase.PREPARE -> copy(colorPrepare = color)
        Phase.WORK -> copy(colorWork = color)
        Phase.REST -> copy(colorRest = color)
        Phase.COOLDOWN -> copy(colorCooldown = color)
        Phase.FINISH -> copy(colorFinish = color)
    }

    companion object {
        const val MAX_FINAL_COUNT = 10
    }
}

fun formatTime(totalSeconds: Int): String {
    val s = totalSeconds.coerceAtLeast(0)
    return "%02d:%02d".format(s / 60, s % 60)
}
