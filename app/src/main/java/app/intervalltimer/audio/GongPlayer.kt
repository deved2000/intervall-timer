package app.intervalltimer.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import app.intervalltimer.R

/**
 * Spielt den tiefen Gong (res/raw/gong.wav).
 * SoundPool erlaubt mehrere gleichzeitige Gongs – der Nachhall eines Gongs
 * läuft weiter, während eine Sekunde später schon der nächste erklingt.
 */
class GongPlayer(context: Context) {
    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    @Volatile private var loaded = false
    private val soundId: Int

    init {
        pool.setOnLoadCompleteListener { _, _, status -> if (status == 0) loaded = true }
        soundId = pool.load(context.applicationContext, R.raw.gong, 1)
    }

    fun play(volume: Float) {
        val v = volume.coerceIn(0f, 1f)
        if (!loaded || v <= 0f) return
        pool.play(soundId, v, v, 1, 0, 1f)
    }

    fun release() = pool.release()
}
