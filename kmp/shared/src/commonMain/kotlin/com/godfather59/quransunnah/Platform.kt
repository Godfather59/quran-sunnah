package com.godfather59.quransunnah

// Phase 0 scaffold placeholder. Real domain code (models, verified loaders,
// prayer math, search) lands in Phase 1 per TODO_KMP.md.
expect object PlatformInfo {
    val name: String
}

fun appTag(): String = "QuranSunnah/${PlatformInfo.name}"
