package com.godfather59.quransunnah.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

// Native driver (SQLDelight native-driver, already in the version catalog).
// openLibrary stays fail-soft for schema convergence; FTS5 lives in raw SQL
// (LibraryDb.ensureSearchFts) so a missing module can never brick startup.
fun createIosDatabase(name: String = "quran_sunnah.db"): LibraryDatabase {
    val driver: SqlDriver = NativeSqliteDriver(Library.Schema, name)
    return openLibrary(driver)
}

/** In-memory database (iOS tests/previews). */
fun createIosMemoryDatabase(): LibraryDatabase {
    val driver: SqlDriver = NativeSqliteDriver(Library.Schema, ":memory:")
    return openLibrary(driver)
}
