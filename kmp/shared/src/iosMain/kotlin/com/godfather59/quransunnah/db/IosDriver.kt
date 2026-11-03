package com.godfather59.quransunnah.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

// Native driver (SQLDelight native-driver, already in the version catalog).
// openSearch stays fail-soft for schema convergence; FTS5 lives in raw SQL
// (SearchDb.ensureSearchFts) so a missing module can never brick startup.
fun createIosDatabase(name: String = "quran_sunnah.db"): SearchDatabase {
    val driver: SqlDriver = NativeSqliteDriver(Search.Schema, name)
    return openSearch(driver)
}

/** In-memory database (iOS tests/previews). */
fun createIosMemoryDatabase(): SearchDatabase {
    val driver: SqlDriver = NativeSqliteDriver(Search.Schema, ":memory:")
    return openSearch(driver)
}
