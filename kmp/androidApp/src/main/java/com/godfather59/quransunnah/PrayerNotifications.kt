package com.godfather59.quransunnah

import android.Manifest
import android.content.pm.PackageManager
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit

private const val PRAYER_ALARM_ACTION = "com.godfather59.quransunnah.PRAYER_ALARM"
private const val EXTRA_ALARM_ID = "alarm_id"
private const val EXTRA_PRAYER_KEY = "prayer_key"

internal enum class PrayerDeliveryStatus { DISABLED, BLOCKED, FAILED, APPROXIMATE, EXACT }

/** A rolling window of the next five prayers, renewed after every delivery. */
object PrayerNotifications {
    const val CHANNEL_ID = "prayer_times"
    private const val PREF_FILE = "quran_sunnah_kmp"
    private var scheduledSignature: String? = null
    private var nextRefreshAt: Long = 0L

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID, context.getString(R.string.prayerChannelName),
                    NotificationManager.IMPORTANCE_HIGH,
                ),
            )
        }
    }

    private fun notificationsAllowed(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = context.getSystemService(NotificationManager::class.java)
                ?.getNotificationChannel(CHANNEL_ID)
            if (channel?.importance == NotificationManager.IMPORTANCE_NONE) return false
        }
        return true
    }

    private fun exactAlarmsAllowed(manager: AlarmManager): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()

    internal fun deliveryStatus(context: Context): PrayerDeliveryStatus {
        if (!PrayerPrefs.load(context).notificationsEnabled) return PrayerDeliveryStatus.DISABLED
        if (!notificationsAllowed(context)) return PrayerDeliveryStatus.BLOCKED
        if (!context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                .getBoolean("prayer.scheduleOk", true)
        ) return PrayerDeliveryStatus.FAILED
        val manager = context.getSystemService(AlarmManager::class.java)
            ?: return PrayerDeliveryStatus.FAILED
        return if (exactAlarmsAllowed(manager)) PrayerDeliveryStatus.EXACT
            else PrayerDeliveryStatus.APPROXIMATE
    }

    /** Safe to call on foreground; unchanged schedules are reused in-process. */
    @Synchronized
    fun scheduleDaily(context: Context, force: Boolean = false) {
        val app = context.applicationContext
        try {
            val prefs = PrayerPrefs.load(app)
            if (!prefs.notificationsEnabled) {
                cancelScheduled(app)
                scheduledSignature = null
                return
            }
            ensureChannel(app)
            if (!notificationsAllowed(app)) {
                cancelScheduled(app)
                scheduledSignature = null
                return
            }
            val manager = app.getSystemService(AlarmManager::class.java)
                ?: error("Alarm service unavailable")
            val now = System.currentTimeMillis()
            val place = resolvePrayerPlace(prefs)
            val localDate = prayerCalendar(place, now)
            val exact = exactAlarmsAllowed(manager)
            val signature = "$prefs|${localDate.get(java.util.Calendar.YEAR)}|" +
                "${localDate.get(java.util.Calendar.DAY_OF_YEAR)}|$exact"
            if (!force && scheduledSignature == signature && now < nextRefreshAt) return
            val alarms = nextPrayerAlarms(prefs, now)
            for (alarm in alarms) {
                val pending = PendingIntent.getBroadcast(
                    app, alarm.id,
                    Intent(app, PrayerAlarmReceiver::class.java).apply {
                        action = PRAYER_ALARM_ACTION
                        putExtra(EXTRA_ALARM_ID, alarm.id)
                        putExtra(EXTRA_PRAYER_KEY, alarm.key)
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
                try {
                    if (exact) {
                        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarm.atMillis, pending)
                    } else {
                        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarm.atMillis, pending)
                    }
                } catch (_: SecurityException) {
                    manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarm.atMillis, pending)
                }
            }
            scheduledSignature = signature
            nextRefreshAt = alarms.firstOrNull()?.atMillis ?: 0L
            app.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit { putBoolean("prayer.scheduleOk", true) }
        } catch (_: Exception) {
            scheduledSignature = null
            app.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit { putBoolean("prayer.scheduleOk", false) }
        }
    }

    /** Only prayer alarms/notifications are cancelled; audio keeps playing. */
    @Synchronized
    fun cancelAll(context: Context, clearShown: Boolean = true) {
        val app = context.applicationContext
        cancelScheduled(app)
        scheduledSignature = null
        if (clearShown) {
            val manager = app.getSystemService(NotificationManager::class.java)
            for (id in 100..109) runCatching { manager?.cancel(id) }
        }
    }

    private fun cancelScheduled(context: Context) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        for (id in 100..109) {
            val pending = PendingIntent.getBroadcast(
                context, id,
                Intent(context, PrayerAlarmReceiver::class.java).apply { action = PRAYER_ALARM_ACTION },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
            ) ?: continue
            runCatching {
                manager.cancel(pending)
                pending.cancel()
            }
        }
    }
}

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != PRAYER_ALARM_ACTION) return
        val id = intent.getIntExtra(EXTRA_ALARM_ID, 0)
        val key = intent.getStringExtra(EXTRA_PRAYER_KEY) ?: return
        if (id == 0) return
        val app = context.applicationContext
        try {
            if (!PrayerPrefs.load(app).notificationsEnabled) return
            PrayerNotifications.ensureChannel(app)
            val open = PendingIntent.getActivity(
                app, 0, Intent(app, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val notification = NotificationCompat.Builder(app, PrayerNotifications.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(app.getString(R.string.prayerTimeTitle))
                .setContentText(prayerNotificationLabel(app, key))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(open)
                .build()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    NotificationManagerCompat.from(app).notify(id, notification)
                } catch (_: SecurityException) {
                    // Permission can be revoked between the check and delivery.
                }
            }
        } catch (_: Exception) {
            // Revoked permissions or unavailable notification services must not
            // prevent renewal of the remaining prayer alarms.
        } finally {
            // Each prayer also schedules its next day's occurrence, so reminders
            // continue without opening the app or relying on a midnight timer.
            PrayerNotifications.scheduleDaily(app, force = true)
        }
    }

    private fun prayerNotificationLabel(context: Context, key: String): String = context.getString(
        when (key) {
            "fajr" -> R.string.prayerNameFajr
            "dhuhr" -> R.string.prayerNameDhuhr
            "asr" -> R.string.prayerNameAsr
            "maghrib" -> R.string.prayerNameMaghrib
            else -> R.string.prayerNameIsha
        },
    )
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in setOf(
                Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_LOCKED_BOOT_COMPLETED,
                Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED,
                Intent.ACTION_MY_PACKAGE_REPLACED,
                "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED",
            )
        ) {
            PrayerNotifications.scheduleDaily(context, force = true)
        }
    }
}
