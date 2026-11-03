package com.godfather59.quransunnah

import android.content.Context
import com.godfather59.quransunnah.data.AndroidAssetReader
import com.godfather59.quransunnah.hadith.Hadith
import com.godfather59.quransunnah.search.SearchEngine
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
 * UI-scoped search holder for [SunnahSearchAndBooks], now a [ViewModel].
 *
 * Previously `submitWordSearch()` was a local fun inside composition, then a
 * plain `remember(resetToken)` holder. As a ViewModel it survives rotation
 * independent of composition, `viewModelScope` cancels in-flight search on
 * permanent leave, and the text input lives in [SavedStateHandle] so it
 * survives process death too. Scoped per collection via `viewModel(key)` so
 * "all" vs single-collection searches stay independent.
 */
class SunnahSearchState(
    private val reader: AndroidAssetReader,
    private val engine: SearchEngine,
    private val appContext: Context,
    private val collectionIds: Set<String>?,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _hits = MutableStateFlow<List<Hadith>>(emptyList())
    val hits: StateFlow<List<Hadith>> = _hits.asStateFlow()

    private val _searching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = _searching.asStateFlow()

    private val _searched = MutableStateFlow(false)
    val searched: StateFlow<Boolean> = _searched.asStateFlow()

    /** Search input, process-death safe. */
    val input: StateFlow<String> =
        savedStateHandle.getStateFlow("wordInput", "")

    fun onInputChange(value: String) {
        savedStateHandle["wordInput"] = value
        if (_searched.value) _searched.value = false
    }

    fun submit(rawQuery: String = input.value) {
        val query = rawQuery.trim()
        if (query.isEmpty() || _searching.value) return
        _searching.value = true
        viewModelScope.launch {
            try {
                ensureHadithIndex(reader, engine, appContext)
                val found = withContext(Dispatchers.Default) {
                    engine.search(
                        query,
                        SearchOptions(
                            collectionIds = collectionIds,
                            limitPerCategory = 20,
                        ),
                    ).hadith.mapNotNull { it.hadith }
                }
                _hits.value = found
            } catch (_: Exception) {
                _hits.value = emptyList()
            } finally {
                _searching.value = false
                _searched.value = true
            }
        }
    }

    fun onInputChanged() {
        if (_searched.value) _searched.value = false
    }

    fun clear() {
        _hits.value = emptyList()
        _searched.value = false
    }
}

@Composable
fun rememberSunnahSearchState(
    reader: AndroidAssetReader,
    engine: SearchEngine,
    collectionIds: Set<String>?,
    resetToken: Any?,
): SunnahSearchState {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val factory = remember(reader, engine, appContext, collectionIds) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: androidx.lifecycle.viewmodel.CreationExtras,
            ): T {
                return SunnahSearchState(
                    reader,
                    engine,
                    appContext,
                    collectionIds,
                    extras.createSavedStateHandle(),
                ) as T
            }
        }
    }
    return viewModel(key = "sunnahSearch:$resetToken", factory = factory)
}
