package com.godfather59.quransunnah

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.godfather59.quransunnah.search.findHighlightRanges

/**
 * Search-hit text with matched terms marked. Ranges come from the shared
 * [findHighlightRanges] (diacritic-insensitive like the index); display text
 * is never mutated, only spanned. Empty query renders plain text.
 */
@Composable
fun highlightedText(text: String, query: String): AnnotatedString {
    val ranges = remember(text, query) { findHighlightRanges(text, query) }
    val mark = MaterialTheme.colorScheme.primaryContainer
    return remember(text, ranges, mark) {
        buildAnnotatedString {
            append(text)
            for (r in ranges) {
                addStyle(
                    SpanStyle(background = mark),
                    r.first,
                    (r.last + 1).coerceAtMost(text.length),
                )
            }
        }
    }
}
