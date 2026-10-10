package com.godfather59.quransunnah

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.godfather59.quransunnah.audio.SharedExoPlayer

// Background playback + lock-screen metadata (channel `com.quran_sunnah.audio`,
// ongoing notification with transport controls).
// Wraps the shared ExoPlayer so the reader UI, MiniPlayer, notification, and
// lock-screen controls all follow one queue with the verified title/artist
// metadata already set on each ayah MediaItem.
//
// Notification ownership: this service posts and refreshes its own media
// notification (same id Media3 would use) instead of relying on Media3's
// async provider, which proved racy on device (loading notice stuck forever,
// no transport buttons). `onUpdateNotification` is overridden WITHOUT calling
// super so Media3 never posts a duplicate. Lock-screen + Bluetooth metadata
// keep flowing through the MediaSession independently.
// Media3 marks the session APIs unstable; opting in here (the service is
// internal — nothing else touches these calls).
@UnstableApi
class QuranAudioService : MediaSessionService() {
    private var session: MediaSession? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            postNotification()
        }

        override fun onMediaItemTransition(
            mediaItem: androidx.media3.common.MediaItem?,
            reason: Int,
        ) {
            postNotification()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            postNotification()
        }
    }

    override fun onCreate() {
        super.onCreate()
        QuranAudio.ensureChannel(this)
        val player = SharedExoPlayer.get(this)
        player.addListener(playerListener)
        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        session = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivity)
            .build()
        // Foreground IMMEDIATELY (same id our updates reuse): a slow first
        // buffer must never exceed the ~10s startForegroundService deadline
        // (ForegroundServiceDidNotStartInTimeException, seen on device).
        // Type comes from the manifest (mediaPlayback).
        postNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        try {
            val player = SharedExoPlayer.get(this)
            when (intent?.action) {
                QuranAudio.ACTION_TOGGLE -> if (player.isPlaying) player.pause() else player.play()
                QuranAudio.ACTION_NEXT -> if (player.hasNextMediaItem()) player.seekToNextMediaItem()
                QuranAudio.ACTION_PREV -> if (player.hasPreviousMediaItem()) player.seekToPreviousMediaItem()
                QuranAudio.ACTION_STOP -> {
                    try {
                        player.stop()
                    } catch (_: Exception) {
                    }
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
        } catch (_: Exception) {
        }
        return START_NOT_STICKY
    }

    /**
     * Fully owned: intentionally does NOT call super, so Media3 never posts
     * its own (duplicate/stale) notification next to ours.
     */
    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        postNotification()
    }

    private fun postNotification() {
        try {
            val player = SharedExoPlayer.get(this)
            val meta = player.currentMediaItem?.mediaMetadata
            val title = meta?.title?.toString()?.takeIf { it.isNotBlank() }
                ?: getString(R.string.quranAudio)
            val artist = meta?.artist?.toString()
            val buffering = player.playbackState == Player.STATE_BUFFERING
            val text = when {
                buffering -> getString(R.string.downloadingContent)
                artist.isNullOrBlank() -> getString(R.string.quranAudio)
                else -> artist
            }
            val playing = player.isPlaying
            val contentIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val builder = NotificationCompat.Builder(this, QuranAudio.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(
                    android.R.drawable.ic_media_previous,
                    getString(R.string.previous),
                    serviceAction(QuranAudio.ACTION_PREV, 1),
                )
                .addAction(
                    if (playing) {
                        android.R.drawable.ic_media_pause
                    } else {
                        android.R.drawable.ic_media_play
                    },
                    getString(if (playing) R.string.pause else R.string.play),
                    serviceAction(QuranAudio.ACTION_TOGGLE, 2),
                )
                .addAction(
                    android.R.drawable.ic_media_next,
                    getString(R.string.next),
                    serviceAction(QuranAudio.ACTION_NEXT, 3),
                )
                .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    getString(R.string.stop),
                    serviceAction(QuranAudio.ACTION_STOP, 4),
                )
            // Session-linked media style (compact transport row); plain
            // fallback when the session isn't built yet (first foreground).
            val currentSession = session
            if (currentSession != null) {
                builder.setStyle(
                    MediaStyleNotificationHelper.MediaStyle(currentSession)
                        .setShowActionsInCompactView(0, 1, 2),
                )
            }
            startForeground(QuranAudio.NOTIF_ID, builder.build())
        } catch (_: Exception) {
        }
    }

    private fun serviceAction(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, QuranAudioService::class.java).setAction(action)
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        session

    override fun onDestroy() {
        try {
            SharedExoPlayer.get(this).removeListener(playerListener)
        } catch (_: Exception) {
        }
        session?.release()
        session = null
        super.onDestroy()
    }
}

/**
 * Stable entry points (channel id, channel creation, service start/stop).
 * Kept outside the [@UnstableApi] service so UI callers need no opt-in:
 * none of these touch unstable Media3 APIs.
 */
object QuranAudio {
    const val CHANNEL_ID = "com.quran_sunnah.audio"

    /** Same id Media3's default provider uses: single notification, replaced on update. */
    const val NOTIF_ID = 1001
    const val ACTION_TOGGLE = "com.godfather59.quransunnah.audio.TOGGLE"
    const val ACTION_NEXT = "com.godfather59.quransunnah.audio.NEXT"
    const val ACTION_PREV = "com.godfather59.quransunnah.audio.PREV"
    const val ACTION_STOP = "com.godfather59.quransunnah.audio.STOP"

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

    /** Stop playback and dismiss the notification (MiniPlayer close, player Stop). */
    fun stopAll(context: Context) {
        try {
            SharedExoPlayer.get(context).stop()
        } catch (_: Exception) {
        }
        try {
            context.stopService(
                Intent().setClassName(
                    context,
                    "com.godfather59.quransunnah.QuranAudioService",
                ),
            )
        } catch (_: Exception) {
        }
    }
}
