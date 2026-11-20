package app.intervalltimer

import app.intervalltimer.data.Phase
import app.intervalltimer.data.Preset

/** Ein Abschnitt des Ablaufs, z. B. "Training, 30 s, noch 19 Sets". */
data class Segment(
    val phase: Phase,
    val durationMs: Long,
    /** Verbleibende Sets inkl. aktuellem (wird groß oben angezeigt), null = keine Anzeige. */
    val setsLeft: Int?,
)

data class TimerState(
    val segments: List<Segment>,
    val index: Int,
    val remainingMs: Long,
    val paused: Boolean = false,
) {
    val current: Segment get() = segments[index]
    val finished: Boolean get() = current.phase == Phase.FINISH

    /** Angezeigte Sekunden (aufgerundet: 29,4 s → "00:30" … erst bei ≤ 29,0 s → "00:29"). */
    val displaySeconds: Int get() = ((remainingMs + 999) / 1000).toInt()
}

fun buildSegments(p: Preset, skipLastRest: Boolean): List<Segment> {
    val list = mutableListOf<Segment>()
    if (p.prepare > 0) list += Segment(Phase.PREPARE, p.prepare * 1000L, null)
    for (i in 1..p.sets) {
        val left = p.sets - i + 1
        if (p.work > 0) list += Segment(Phase.WORK, p.work * 1000L, left)
        val isLastSet = i == p.sets
        if (p.rest > 0 && !(isLastSet && skipLastRest)) {
            list += Segment(Phase.REST, p.rest * 1000L, left)
        }
    }
    if (p.cooldown > 0) list += Segment(Phase.COOLDOWN, p.cooldown * 1000L, null)
    list += Segment(Phase.FINISH, 0L, null)
    return list
}

/**
 * Reine Timer-Logik ohne Android-Abhängigkeiten.
 *
 * Gong-Regel (nur bei TRAINING und PAUSE):
 * finalCount = N → Gong bei N, N-1, …, 1 Sekunde(n) vor Ende und ein Gong beim Ende (0).
 * Beispiel N = 2: Gong bei 2 s, 1 s und 0 s.
 */
class TimerEngine(
    private val finalCount: Int,
    private val onGong: () -> Unit,
) {
    private var gongIndex = 0

    fun start(segments: List<Segment>): TimerState = enter(TimerState(segments, 0, 0L), 0)

    private fun thresholds(seg: Segment): List<Long> {
        if (seg.phase != Phase.WORK && seg.phase != Phase.REST) return emptyList()
        val before = (finalCount downTo 1).map { it * 1000L }.filter { it < seg.durationMs }
        return before + 0L
    }

    private fun enter(s: TimerState, index: Int): TimerState {
        gongIndex = 0
        val i = index.coerceIn(0, s.segments.lastIndex)
        return s.copy(index = i, remainingMs = s.segments[i].durationMs)
    }

    /** Lässt [dtMs] Millisekunden vergehen. */
    fun advance(state: TimerState, dtMs: Long): TimerState {
        if (state.paused) return state
        var s = state
        var dt = dtMs
        while (dt > 0 && !s.finished) {
            val th = thresholds(s.current)
            val newRemaining = s.remainingMs - dt
            while (gongIndex < th.size && newRemaining <= th[gongIndex]) {
                onGong()
                gongIndex++
            }
            if (newRemaining > 0) {
                s = s.copy(remainingMs = newRemaining)
                dt = 0
            } else {
                dt = -newRemaining
                s = enter(s, s.index + 1)
            }
        }
        return s
    }

    /** Zum nächsten Abschnitt springen (ohne Gong). */
    fun next(s: TimerState): TimerState =
        if (s.finished) s else enter(s, s.index + 1)

    /** Abschnitt neu starten bzw. – wenn gerade erst begonnen – zum vorherigen springen. */
    fun previous(s: TimerState): TimerState {
        val elapsed = s.current.durationMs - s.remainingMs
        return if (elapsed > 2000 || s.index == 0) enter(s, s.index) else enter(s, s.index - 1)
    }
}
