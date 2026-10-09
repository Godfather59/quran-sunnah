package com.godfather59.quransunnah.ui

enum class LayoutDirection {
    LTR,
    RTL,
}

enum class HorizontalIconDirection {
    LEFT,
    RIGHT,
}

/**
 * Direction-aware trailing chevron semantics.
 *
 * Mirrors Flutter's AdaptiveChevron: a trailing disclosure points forward in
 * LTR layouts and backward in RTL layouts.
 */
fun trailingChevronDirection(layoutDirection: LayoutDirection): HorizontalIconDirection =
    when (layoutDirection) {
        LayoutDirection.LTR -> HorizontalIconDirection.RIGHT
        LayoutDirection.RTL -> HorizontalIconDirection.LEFT
    }

fun layoutDirectionForLanguage(languageCode: String): LayoutDirection =
    when (languageCode.lowercase()) {
        "ar", "fa", "he", "ur" -> LayoutDirection.RTL
        else -> LayoutDirection.LTR
    }
