package com.godfather59.quransunnah.util

import platform.Foundation.NSRecursiveLock

actual class CommonLock actual constructor() {
    @PublishedApi
    internal val nsLock = NSRecursiveLock()

    actual inline fun <T> withLock(action: () -> T): T {
        nsLock.lock()
        try {
            return action()
        } finally {
            nsLock.unlock()
        }
    }
}
