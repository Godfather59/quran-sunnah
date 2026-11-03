package com.godfather59.quransunnah.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/** File database. Wired from the Application class in Phase 5. */
fun createAndroidDatabase(
    context: Context,
    name: String = "quran_sunnah.db",
): SearchDatabase {
    val driver: SqlDriver = AndroidSqliteDriver(Search.Schema, context, name)
    return openSearch(driver)
}
