package com.forgelegends.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.forgelegends.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val tapSoundId: Int = soundPool.load(context, R.raw.sfx_tap, 1)
    private val purchaseSoundId: Int = soundPool.load(context, R.raw.sfx_purchase, 1)
    private val phaseCompleteSoundId: Int = soundPool.load(context, R.raw.sfx_phase_complete, 1)
    private val buttonClickSoundId: Int = soundPool.load(context, R.raw.sfx_button_click, 1)
    private val errorSoundId: Int = soundPool.load(context, R.raw.sfx_error, 1)
    private val passiveTickSoundId: Int = soundPool.load(context, R.raw.sfx_passive_tick, 1)

    fun playTap() {
        soundPool.play(tapSoundId, 0.5f, 0.5f, 1, 0, 1.0f)
    }

    fun playPurchase() {
        soundPool.play(purchaseSoundId, 0.8f, 0.8f, 1, 0, 1.0f)
    }

    fun playPhaseComplete() {
        soundPool.play(phaseCompleteSoundId, 1.0f, 1.0f, 2, 0, 1.0f)
    }

    fun playButtonClick() {
        soundPool.play(buttonClickSoundId, 0.4f, 0.4f, 0, 0, 1.0f)
    }

    fun playError() {
        soundPool.play(errorSoundId, 0.6f, 0.6f, 1, 0, 1.0f)
    }

    fun playPassiveTick() {
        soundPool.play(passiveTickSoundId, 0.15f, 0.15f, 0, 0, 1.0f)
    }

    fun release() {
        soundPool.release()
    }
}
