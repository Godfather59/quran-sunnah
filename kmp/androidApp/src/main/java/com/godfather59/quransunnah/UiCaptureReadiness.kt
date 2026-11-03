package com.godfather59.quransunnah

import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

/** Test-only semantic metadata; not announced as user-facing accessibility copy. */
internal val UiContentReady = SemanticsPropertyKey<Boolean>("UiContentReady")
internal var SemanticsPropertyReceiver.uiContentReady by UiContentReady
