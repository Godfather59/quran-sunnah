package com.godfather59.quransunnah.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

/** In-memory database (tests, desktop). */
fun createJvmMemoryDatabase(): LibraryDatabase {
    val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    return openLibrary(driver)
}
