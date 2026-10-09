package com.godfather59.quransunnah

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.godfather59.quransunnah.audio.SharedExoPlayer

// Background playback + lock-screen metadata (Flutter just_audio_background
// parity: channel `com.quran_sunnah.audio`, ongoing notification).
// Wraps the shared ExoPlayer so the reader UI, MiniPlayer, notification, and
// lock-screen controls all follow one queue with the verified title/artist
// metadata already set on each ayah MediaItem.
// Media3 marks the session/notification APIs unstable; opting in here (the
// service is internal — nothing else touches these calls).
@UnstableApi
class QuranAudioService : MediaSessionService() {
    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        QuranAudio.ensureChannel(this)
        val player = SharedExoPlayer.get(this)
        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        session = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivity)
            .build()
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setChannelId(QuranAudio.CHANNEL_ID)
                .setChannelName(R.string.quranAudio)
                .build(),
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        session

    override fun onDestroy() {
        session?.release()
        session = null
        super.onDestroy()
    }
}

/**
 * Stable entry points (channel id, channel creation, service start).
 * Kept outside the [@UnstableApi] service so UI callers need no opt-in:
 * none of these touch unstable Media3 APIs.
 */
object QuranAudio {
    const val CHANNEL_ID = "com.quran_sunnah.audio"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.quranAudio),
                    NotificationManager.IMPORTANCE_LOW,
                ),
            )
        }
    }

    /** Start the service so playback survives backgrounding. */
    fun ensureStarted(context: Context) {
        ensureChannel(context)
        // String component name: referencing QuranAudioService::class here
        // would pull the @UnstableApi marker into stable callers.
        val intent = Intent().setClassName(
            context,
            "com.godfather59.quransunnah.QuranAudioService",
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                context.startForegroundService(intent)
            } catch (_: Exception) {
                // ForegroundServiceStartNotAllowedException on Android 12+
                // when started from the background: best-effort.
            }
        } else {
            try {
                context.startService(intent)
            } catch (_: Exception) {
            }
        }
    }
}
