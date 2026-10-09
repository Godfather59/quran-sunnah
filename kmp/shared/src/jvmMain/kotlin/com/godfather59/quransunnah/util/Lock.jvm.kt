package com.godfather59.quransunnah.util

actual class CommonLock actual constructor() {
    actual inline fun <T> withLock(action: () -> T): T =
        synchronized(this, action)
}
