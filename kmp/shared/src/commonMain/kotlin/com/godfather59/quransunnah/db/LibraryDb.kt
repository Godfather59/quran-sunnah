package com.godfather59.quransunnah.db

import app.cash.sqldelight.db.SqlDriver

// FTS5 external-content table + sync triggers as raw SQL (NOT in .sq, so
// codegen never references search_fts). Mirrors Dart beforeOpen
// (_createIndexesAndFts) exactly. Raw SQL keeps a missing FTS5 module from
// ever breaking schema creation: some framework SQLite builds ship without
// FTS5 entirely ("no such module: fts5"), which once bricked the app into
// a crash loop on first open.
private val SEARCH_FTS_DDL = listOf(
    "CREATE VIRTUAL TABLE IF NOT EXISTS search_fts USING fts5(" +
        "normalized_body, normalized_title, " +
        "content='search_documents', content_rowid='rowid', " +
        "tokenize='unicode61')",
    "CREATE TRIGGER IF NOT EXISTS search_documents_ai " +
        "AFTER INSERT ON search_documents BEGIN " +
        "INSERT INTO search_fts(rowid, normalized_body, normalized_title) " +
        "VALUES (new.rowid, new.normalized_body, new.normalized_title); " +
        "END",
    "CREATE TRIGGER IF NOT EXISTS search_documents_ad " +
        "AFTER DELETE ON search_documents BEGIN " +
        "INSERT INTO search_fts(search_fts, rowid, normalized_body, normalized_title) " +
        "VALUES ('delete', old.rowid, old.normalized_body, old.normalized_title); " +
        "END",
    "CREATE TRIGGER IF NOT EXISTS search_documents_au " +
        "AFTER UPDATE ON search_documents BEGIN " +
        "INSERT INTO search_fts(search_fts, rowid, normalized_body, normalized_title) " +
        "VALUES ('delete', old.rowid, old.normalized_body, old.normalized_title); " +
        "INSERT INTO search_fts(rowid, normalized_body, normalized_title) " +
        "VALUES (new.rowid, new.normalized_body, new.normalized_title); " +
        "END",
)

fun ensureSearchFts(driver: SqlDriver) {
    for (sql in SEARCH_FTS_DDL) {
        try {
            driver.execute(null, sql, 0)
        } catch (_: Exception) {
            // No FTS5/unicode61 on this SQLite build: skip loud. The
            // library itself must keep working; only the index is lost.
        }
    }
}

/** Open handle: raw driver + generated queries. */
data class LibraryDatabase(val driver: SqlDriver, val db: Library)

/**
 * Opens the v1 library schema on [driver]. Schema creation stays
 * fail-soft (all statements are IF NOT EXISTS) so a partial pre-existing
 * database converges instead of crashing.
 */
fun openLibrary(driver: SqlDriver): LibraryDatabase {
    try {
        Library.Schema.create(driver)
    } catch (_: Exception) {
        // Partial pre-existing database or a device-specific DDL failure.
    }
    ensureSearchFts(driver)
    return LibraryDatabase(driver, Library(driver))
}
