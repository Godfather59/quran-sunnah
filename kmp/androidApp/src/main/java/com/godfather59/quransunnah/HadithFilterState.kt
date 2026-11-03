package com.godfather59.quransunnah

import android.content.Context
import com.godfather59.quransunnah.data.AndroidAssetReader
import com.godfather59.quransunnah.hadith.Hadith
import com.godfather59.quransunnah.search.SearchEngine
import com.godfather59.quransunnah.search.SearchKind
import com.godfather59.quransunnah.search.SearchOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pure predicate mirroring the filter sheet's post-search narrowing.
 * Extracted from [HadithFilterState.applyFilters] so it is unit-testable
 * without a SearchEngine / Android context.
 */
internal fun hadithFilterMatches(
    hadith: Hadith,
    bookQuery: String,
    numberQuery: String,
    grades: Set<String>,
): Boolean {
    val book = bookQuery.trim()
    val number = numberQuery.trim()
    return (book.isBlank() || hadith.book.contains(book, ignoreCase = true)) &&
        (number.isBlank() || hadith.hadithNumber == number) &&
        (grades.isEmpty() ||
            grades.any { g ->
                hadith.grade?.contains(g, ignoreCase = true) == true
            })
}

/**
 * UI state for [HadithFilterSheet], now a [ViewModel].
 *
 * Filter fields live in [SavedStateHandle] (process-death safe); hits and
 * flags are transient flows. `viewModelScope` cancels the index warm-up and
 * in-flight search when the sheet is permanently dismissed.
 */
class HadithFilterState(
    private val reader: AndroidAssetReader,
    private val engine: SearchEngine,
    private val appContext: Context,
    private val collectionId: String,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val query: StateFlow<String> =
        savedStateHandle.getStateFlow("query", "")

    val bookQuery: StateFlow<String> =
        savedStateHandle.getStateFlow("bookQuery", "")

    val numberQuery: StateFlow<String> =
        savedStateHandle.getStateFlow("numberQuery", "")

    val gradeFilter: StateFlow<Set<String>> =
        savedStateHandle.getStateFlow("gradeFilter", emptySet())

    private val _indexReady = MutableStateFlow(engine.hasDocuments(SearchKind.HADITH))
    val indexReady: StateFlow<Boolean> = _indexReady.asStateFlow()

    private val _hits = MutableStateFlow<List<Hadith>>(emptyList())
    val hits: StateFlow<List<Hadith>> = _hits.asStateFlow()

    private val _searched = MutableStateFlow(false)
    val searched: StateFlow<Boolean> = _searched.asStateFlow()

    val gradeOptions: List<String> = listOf("Sahih", "Hasan", "Daif")

    init {
        viewModelScope.launch {
            ensureHadithIndex(reader, engine, appContext)
            _indexReady.value = true
        }
    }

    fun onQueryChange(value: String) {
        savedStateHandle["query"] = value
    }

    fun onBookQueryChange(value: String) {
        savedStateHandle["bookQuery"] = value
    }

    fun onNumberQueryChange(value: String) {
        savedStateHandle["numberQuery"] = value
    }

    fun toggleGrade(grade: String) {
        val current: Set<String> = savedStateHandle["gradeFilter"] ?: emptySet()
        savedStateHandle["gradeFilter"] = if (grade in current) {
            current - grade
        } else {
            current + grade
        }
    }

    fun clearAll() {
        savedStateHandle["query"] = ""
        savedStateHandle["bookQuery"] = ""
        savedStateHandle["numberQuery"] = ""
        savedStateHandle["gradeFilter"] = emptySet<String>()
        _hits.value = emptyList()
        _searched.value = false
    }

    fun applyFilters() {
        val q = query.value
        val bookQ = bookQuery.value.trim()
        val numberQ = numberQuery.value.trim()
        val grades = gradeFilter.value
        if (q.isBlank()) return
        viewModelScope.launch {
            val found = withContext(Dispatchers.Default) {
                engine.search(
                    q,
                    SearchOptions(
                        collectionIds = if (collectionId == "all") {
                            null
                        } else {
                            setOf(collectionId)
                        },
                        limitPerCategory = 50,
                    ),
                ).hadith
                    .mapNotNull { it.hadith }
                    .filter { h ->
                        hadithFilterMatches(h, bookQ, numberQ, grades)
                    }
            }
            _hits.value = found
            _searched.value = true
        }
    }
}

@Composable
fun rememberHadithFilterState(
    reader: AndroidAssetReader,
    engine: SearchEngine,
    collectionId: String,
): HadithFilterState {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val factory = remember(reader, engine, appContext, collectionId) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: androidx.lifecycle.viewmodel.CreationExtras,
            ): T {
                return HadithFilterState(
                    reader,
                    engine,
                    appContext,
                    collectionId,
                    extras.createSavedStateHandle(),
                ) as T
            }
        }
    }
    return viewModel(key = "hadithFilter:$collectionId", factory = factory)
}
