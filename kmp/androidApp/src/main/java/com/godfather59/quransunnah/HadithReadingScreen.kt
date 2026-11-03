package com.godfather59.quransunnah

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.godfather59.quransunnah.hadith.Hadith
import com.godfather59.quransunnah.hadith.HadithCollection

/** Sustained reading preserves exact bundled text and source information. */
@Composable
internal fun HadithReadingScreen(hadith: Hadith, collection: HadithCollection, sequence: List<Hadith>,
    isActive: Boolean, onBack: () -> Unit, onSelect: (Hadith) -> Unit,
    listState: LazyListState) {
    BackHandler(enabled = isActive, onBack = onBack)
    val language = currentLanguage()
    val context = LocalContext.current
    val font = quranFontFamily(context, "naskh")
    val sourceName = when (language) { "ar" -> collection.nameAr; "fr" -> collection.nameFr; else -> collection.nameEn }
    val index = sequence.indexOfFirst { it.id == hadith.id }
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text("$sourceName · ${hadith.hadithNumber}", style = MaterialTheme.typography.titleMedium)
        }
        LazyColumn(state = listState, modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            if (hadith.book.isNotBlank()) item {
                Text(hadith.book, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right,
                    style = MaterialTheme.typography.titleMedium.copy(textDirection = TextDirection.Rtl))
            }
            item {
                Text(hadith.matnAr.ifBlank { context.getString(R.string.arabicMatnPlaceholderHint) },
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = if (hadith.matnAr.isNotBlank()) TextAlign.Right else TextAlign.Start,
                    fontFamily = font,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp, lineHeight = 40.sp,
                        textDirection = if (hadith.matnAr.isNotBlank()) TextDirection.Rtl else TextDirection.Content))
            }
            item {
                Text("${stringResource(R.string.gradeLabel)}: ${hadith.grade?.takeIf { it.isNotBlank() } ?: stringResource(R.string.gradeUnavailable)}" +
                    (hadith.gradingAuthority?.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${stringResource(R.string.source)}: ${collection.source}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (index >= 0 && sequence.size > 1) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(enabled = index > 0, onClick = { sequence.getOrNull(index - 1)?.let(onSelect) }) {
                Text(stringResource(R.string.previous))
            }
            Spacer(Modifier.weight(1f))
            Button(enabled = index < sequence.lastIndex, onClick = { sequence.getOrNull(index + 1)?.let(onSelect) }) {
                Text(stringResource(R.string.next))
            }
        }
    }
}
