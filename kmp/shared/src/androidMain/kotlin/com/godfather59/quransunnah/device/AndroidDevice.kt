package com.godfather59.quransunnah.device

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

// GPS with graceful manual fallback (mirrors requestGpsLocation).
// Permissions (ACCESS_FINE/COARSE_LOCATION) are requested app-side.
class AndroidLocator(private val context: Context) : Locator {
    override suspend fun currentFix(timeoutMs: Long): GpsFix? =
        withContext(Dispatchers.IO) {
            try {
                val manager = context.getSystemService(LocationManager::class.java)
                    ?: return@withContext null
                if (manager.getProviders(true).isEmpty()) {
                    return@withContext null
                }
                val freshest = freshestLastKnown(manager)
                if (freshest != null &&
                    System.currentTimeMillis() - freshest.time < 5 * 60 * 1000
                ) {
                    return@withContext fixOf(freshest)
                }
                suspendCancellableCoroutine { cont ->
                    val handler = Handler(Looper.getMainLooper())
                    var timeout: Runnable? = null
                    var listener: LocationListener? = null
                    fun finish(value: GpsFix?) {
                        try {
                            timeout?.let { handler.removeCallbacks(it) }
                        } catch (_: Exception) {
                        }
                        try {
                            listener?.let { manager.removeUpdates(it) }
                        } catch (_: Exception) {
                        }
                        if (cont.isActive) cont.resume(value)
                    }
                    listener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            finish(fixOf(location))
                        }

                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                        override fun onStatusChanged(
                            provider: String?,
                            status: Int,
                            extras: Bundle?,
                        ) {
                        }
                    }
                    val activeListener = listener!!
                    try {
                        manager.requestSingleUpdate(
                            LocationManager.GPS_PROVIDER,
                            activeListener,
                            Looper.getMainLooper(),
                        )
                    } catch (_: Exception) {
                        try {
                            manager.requestSingleUpdate(
                                LocationManager.NETWORK_PROVIDER,
                                activeListener,
                                Looper.getMainLooper(),
                            )
                        } catch (_: Exception) {
                            finish(freshest?.let(::fixOf))
                            return@suspendCancellableCoroutine
                        }
                    }
                    timeout = Runnable {
                        finish(freshest?.let(::fixOf))
                    }
                    handler.postDelayed(timeout!!, timeoutMs)
                    cont.invokeOnCancellation {
                        try {
                            timeout?.let { handler.removeCallbacks(it) }
                        } catch (_: Exception) {
                        }
                        try {
                            manager.removeUpdates(activeListener)
                        } catch (_: Exception) {
                        }
                    }
                }
            } catch (_: Exception) {
                null
            }
        }

    private fun freshestLastKnown(manager: LocationManager): Location? {
        var best: Location? = null
        for (provider in manager.getProviders(true)) {
            val location = try {
                manager.getLastKnownLocation(provider)
            } catch (_: SecurityException) {
                // One denied provider must not drop the others.
                continue
            } catch (_: Exception) {
                continue
            } ?: continue
            if (best == null || location.time > best.time) best = location
        }
        return best
    }

    private fun fixOf(location: Location): GpsFix = GpsFix(
        latitude = location.latitude,
        longitude = location.longitude,
        city = "GPS ${"%.2f".format(java.util.Locale.US, location.latitude)}, " +
            "%.2f".format(java.util.Locale.US, location.longitude),
    )
}

// Rotation-vector compass, degrees from true north (mirrors LiveQiblaCompass).
class AndroidCompass(context: Context) : Compass {
    private val manager =
        context.applicationContext.getSystemService(SensorManager::class.java)
    private var listener: ((Float) -> Unit)? = null
    private val rotation = FloatArray(9)
    private val orientation = FloatArray(3)

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type != Sensor.TYPE_ROTATION_VECTOR) return
            SensorManager.getRotationMatrixFromVector(rotation, event.values)
            SensorManager.getOrientation(rotation, orientation)
            val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            listener?.invoke((azimuth + 360f) % 360f)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun start(listener: (Float) -> Unit) {
        this.listener = listener
        val sensor = manager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: return
        manager?.registerListener(
            sensorListener, sensor, SensorManager.SENSOR_DELAY_UI,
        )
    }

    override fun stop() {
        manager?.unregisterListener(sensorListener)
        listener = null
    }
}

class AndroidClipboard(private val context: Context) : Clipboard {
    override fun copy(text: String) {
        val manager =
            context.getSystemService(ClipboardManager::class.java) ?: return
        manager.setPrimaryClip(ClipData.newPlainText("quran-sunnah", text))
    }
}

class AndroidSharer(private val context: Context) : Sharer {
    private fun launchSafely(intent: Intent) {
        try {
            // NEW_TASK breaks the back stack when we already have an
            // Activity context; only add it for application context.
            val chooser = Intent.createChooser(intent, intent.getStringExtra(Intent.EXTRA_SUBJECT))
            if (context !is android.app.Activity) {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: android.content.ActivityNotFoundException) {
            // No share target installed: best-effort, never crash.
        } catch (_: Exception) {
        }
    }

    override fun shareText(text: String, subject: String?) {
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, text)
        if (subject != null) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        launchSafely(intent)
    }

    /**
     * Shares a local file via FileProvider. The host app manifest must
     * declare it (Phase 5):
     * `<provider android:name="androidx.core.content.FileProvider"
     *   android:authorities="com.godfather59.quransunnah.fileprovider" ...>`
     * with `res/xml/filepaths.xml` granting cache access.
     */
    override fun shareFile(path: String, mimeType: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                File(path),
            )
            val intent = Intent(Intent.ACTION_SEND)
                .setType(mimeType)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            launchSafely(intent)
        } catch (_: IllegalArgumentException) {
            // File outside FileProvider roots: never crash the sheet.
        } catch (_: Exception) {
        }
    }
}

/** Backup import helpers (intent built app-side, bytes read here). */
object AndroidBackupPicker {
    fun createPickJsonIntent(): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT)
            .setType("application/json")
            .addCategory(Intent.CATEGORY_OPENABLE)
            .putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf("application/json", "text/plain"),
            )

    /**
     * Reads picked backup bytes with the 2MB guard (Flutter import parity).
     * Null when cancelled, oversized, or unreadable.
     */
    fun readPickedBytes(
        resolver: ContentResolver,
        uri: Uri,
        maxBytes: Long = 2L * 1024 * 1024,
    ): ByteArray? {
        return try {
            resolver.openInputStream(uri)?.use { input ->
                // ByteArrayOutputStream avoids boxing ~2M Bytes in a List.
                val out = java.io.ByteArrayOutputStream(8192)
                val buffer = ByteArray(8192)
                var total = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    if (read == 0) continue
                    total += read
                    if (total > maxBytes) return null
                    out.write(buffer, 0, read)
                }
                out.toByteArray()
            }
        } catch (_: Exception) {
            null
        }
    }
}
