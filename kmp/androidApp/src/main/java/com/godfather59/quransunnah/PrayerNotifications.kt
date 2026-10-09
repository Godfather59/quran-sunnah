package com.godfather59.quransunnah

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.godfather59.quransunnah.notifications.upcomingAlarms
import java.util.Calendar
import java.util.Locale

private const val PRAYER_ALARM_ACTION = "com.godfather59.quransunnah.PRAYER_ALARM"
private const val EXTRA_ALARM_ID = "alarm_id"
private const val EXTRA_PRAYER_KEY = "prayer_key"

// Local prayer reminders. Mirrors lib/data/services/prayer_notifications.dart:
// inexact allow-while-idle alarms, stable ids 100+i, sunrise excluded, past
// prayers skipped. Best-effort like Flutter — failures never break the UI.
object PrayerNotifications {
    const val CHANNEL_ID = "prayer_times"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Prayer times",
                    NotificationManager.IMPORTANCE_HIGH,
                ),
            )
        }
    }

    /** Schedules today's remaining prayers from saved prefs. */
    fun scheduleDaily(context: Context) {
        try {
            val app = context.applicationContext
            cancelScheduled(app)
            val prefs = PrayerPrefs.load(app)
            val now = System.currentTimeMillis()
            val snap = prayerSnapshot(
                prefs.methodName,
                prefs.city,
                prefs.latitude,
                prefs.longitude,
                prefs.tzOffsetHours,
                now,
            )
            var alarms = upcomingAlarms(snap.times, snap.nowMinutes)
            // After Isha there is nothing left today: keep exactly one
            // alarm for tomorrow Fajr so reminders never go silent until
            // the next toggle. Stable id 100 = fajr.
            var tomorrowOffsetDays = 0
            if (alarms.isEmpty()) {
                alarms = listOf(
                    com.godfather59.quransunnah.notifications.PrayerAlarm(
                        id = 100,
                        prayerKey = "fajr",
                        minutes = snap.times.fajr,
                    ),
                )
                tomorrowOffsetDays = 1
            }
            val manager = app.getSystemService(AlarmManager::class.java) ?: return
            ensureChannel(app)
            // Place-tz midnight: GPS tz may differ from device tz (travel,
            // Morocco Ramadan UTC+1->UTC+0). Device midnight would shift
            // triggers by hours; compute the place day-start instead.
            val midnight = midnightMillisForOffset(snap.place.tzOffsetHours)
            for (alarm in alarms) {
                val triggerAt = midnight + (tomorrowOffsetDays * 1440L + alarm.minutes) * 60_000L
                if (triggerAt <= now) continue
                val intent = Intent(app, PrayerAlarmReceiver::class.java).apply {
                    action = PRAYER_ALARM_ACTION
                    putExtra(EXTRA_ALARM_ID, alarm.id)
                    putExtra(EXTRA_PRAYER_KEY, alarm.prayerKey)
                }
                val pending = PendingIntent.getBroadcast(
                    app,
                    alarm.id,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
                try {
                    // Exact first (adhan ±10 min drift under Doze is
                    // unacceptable); fall back to inexact when the exact-alarm
                    // permission is withheld. Needs SCHEDULE_EXACT_ALARM.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        manager.canScheduleExactAlarms()
                    ) {
                        manager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAt,
                            pending,
                        )
                    } else {
                        manager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAt,
                            pending,
                        )
                    }
                } catch (_: SecurityException) {
                    // Doze/permission edge: best-effort, never crash UI.
                    try {
                        manager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAt,
                            pending,
                        )
                    } catch (_: Exception) {
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    /** Cancels scheduled alarms; optionally clears shown prayer notifications. */
    fun cancelAll(context: Context, clearShown: Boolean = true) {
        try {
            val app = context.applicationContext
            cancelScheduled(app)
            if (clearShown) {
                // Never cancelAll(): that would also kill the foreground
                // audio service notification while playing. Cancel only
                // our stable prayer ids.
                val manager = app.getSystemService(NotificationManager::class.java)
                if (manager != null) {
                    for (id in 100..109) {
                        try {
                            manager.cancel(id)
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun cancelScheduled(context: Context) {
        try {
            val manager = context.getSystemService(AlarmManager::class.java) ?: return
            for (id in 100..109) {
                val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    action = PRAYER_ALARM_ACTION
                }
                val pending = PendingIntent.getBroadcast(
                    context,
                    id,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
                ) ?: continue
                manager.cancel(pending)
                pending.cancel()
            }
        } catch (_: Exception) {
        }
    }

    private fun midnightMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * Day-start in millis for a place at [tzOffsetHours] (may differ from
     * device tz). Derived from the UTC day-start so travel / DST shifts
     * can't move triggers by hours.
     */
    private fun midnightMillisForOffset(tzOffsetHours: Double): Long {
        return try {
            val now = System.currentTimeMillis()
            val utc = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
            utc.timeInMillis = now
            utc.set(Calendar.HOUR_OF_DAY, 0)
            utc.set(Calendar.MINUTE, 0)
            utc.set(Calendar.SECOND, 0)
            utc.set(Calendar.MILLISECOND, 0)
            utc.timeInMillis + (tzOffsetHours * 3_600_000L).toLong()
        } catch (_: Exception) {
            midnightMillis()
        }
    }
}

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (intent.action != PRAYER_ALARM_ACTION) return
            val id = intent.getIntExtra(EXTRA_ALARM_ID, 0)
            if (id == 0) return
            val key = intent.getStringExtra(EXTRA_PRAYER_KEY) ?: return
            val app = context.applicationContext
            PrayerNotifications.ensureChannel(app)
            val lang = Locale.getDefault().language
            val title = when (lang) {
                "ar" -> "حان وقت الصلاة"
                "fr" -> "Heure de la prière"
                else -> "Prayer time"
            }
            val open = PendingIntent.getActivity(
                app,
                0,
                Intent(app, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val notification = NotificationCompat.Builder(app, PrayerNotifications.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(prayerNotificationLabel(key, lang))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(open)
                .build()
            app.getSystemService(NotificationManager::class.java)?.notify(id, notification)
        } catch (_: Exception) {
        }
    }

    private fun prayerNotificationLabel(key: String, lang: String): String {
        if (lang == "ar") {
            return when (key) {
                "fajr" -> "الفجر"
                "dhuhr" -> "الظهر"
                "asr" -> "العصر"
                "maghrib" -> "المغرب"
                "isha" -> "العشاء"
                else -> key
            }
        }
        return key.replaceFirstChar { it.uppercase() }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        // Recompute wall-clock alarms on boot, clock/timezone changes and
        // updates: stored trigger times are wall-clock millis.
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != Intent.ACTION_TIME_CHANGED &&
            action != Intent.ACTION_TIMEZONE_CHANGED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }
        try {
            val app = context.applicationContext
            if (PrayerPrefs.load(app).notificationsEnabled) {
                PrayerNotifications.scheduleDaily(app)
            }
        } catch (_: Exception) {
        }
    }
}
