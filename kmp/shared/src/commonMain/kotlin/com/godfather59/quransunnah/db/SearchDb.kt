package com.godfather59.quransunnah.db

import app.cash.sqldelight.db.SqlDriver

// FTS5 external-content table + sync triggers as raw SQL (NOT in .sq, so
// codegen never references search_fts). Mirrors Dart beforeOpen
// (_createIndexesAndFts) exactly. Raw SQL keeps a missing FTS5 module from
// ever breaking schema creation: some framework SQLite builds ship without
// FTS5 entirely ("no such module: fts5"), which once bricked the app into
// a crash loop on first open.
private const val SEARCH_HADITH_DETAILS_DDL =
    "CREATE TABLE IF NOT EXISTS search_hadith_details (" +
        "id TEXT NOT NULL PRIMARY KEY, payload TEXT NOT NULL)"

private const val SEARCH_FTS_TABLE_DDL =
    "CREATE VIRTUAL TABLE IF NOT EXISTS search_fts USING fts5(" +
        "normalized_body, normalized_title, " +
        "content='search_documents', content_rowid='rowid', " +
        "tokenize='unicode61')"

private val SEARCH_FTS_TRIGGER_DDL = listOf(
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
    try {
        driver.execute(null, SEARCH_HADITH_DETAILS_DDL, 0)
    } catch (_: Exception) {
        // Hadith detail persistence is optional; indexed documents still work.
    }

    val ftsAvailable = try {
        driver.execute(null, SEARCH_FTS_TABLE_DDL, 0)
        true
    } catch (_: Exception) {
        false
    }

    if (ftsAvailable) {
        for (sql in SEARCH_FTS_TRIGGER_DDL) {
            try {
                driver.execute(null, sql, 0)
            } catch (_: Exception) {
                // FTS search can be unavailable while document persistence remains usable.
            }
        }
    } else {
        // Older builds could leave triggers behind even when FTS5 creation
        // failed. Those triggers break ordinary document inserts/deletes.
        for (trigger in listOf(
            "search_documents_ai",
            "search_documents_ad",
            "search_documents_au",
        )) {
            try {
                driver.execute(null, "DROP TRIGGER IF EXISTS $trigger", 0)
            } catch (_: Exception) {
                // Keep the app usable even if optional FTS cleanup fails.
            }
        }
    }
}

/** Open handle: raw driver + generated queries. */
data class SearchDatabase(val driver: SqlDriver, val db: Search)

/**
 * Opens the search index schema on [driver]. Schema creation stays
 * fail-soft so a partial pre-existing database can converge without
 * preventing the app from opening.
 */
fun openSearch(driver: SqlDriver): SearchDatabase {
    try {
        Search.Schema.create(driver)
    } catch (_: Exception) {
        // Partial pre-existing database or a device-specific DDL failure.
    }
    ensureSearchFts(driver)
    return SearchDatabase(driver, Search(driver))
}
