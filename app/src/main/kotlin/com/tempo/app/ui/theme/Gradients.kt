package com.tempo.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Bold gradient identities used for full-bleed screen backgrounds and header bands, echoing the
 * "hero gradient" look of the redesign reference (each major section gets its own punchy
 * gradient rather than a flat Material surface).
 */
object TempoGradients {
    val home = Brush.linearGradient(
        colors = listOf(Color(0xFF6B4CE0), Color(0xFFA64FC7), Color(0xFFE85D8A)),
    )

    val reflect = Brush.linearGradient(
        colors = listOf(Color(0xFFE2472A), Color(0xFFE8794A), Color(0xFF6B3FA0)),
    )

    val detail = Brush.linearGradient(
        colors = listOf(Color(0xFF3A6FE0), Color(0xFF274B9E), Color(0xFF14213D)),
    )

    val calm = Brush.linearGradient(
        colors = listOf(Color(0xFF4C5FD5), Color(0xFF7A4FC7), Color(0xFF3A2E6E)),
    )
}

/** Translucent-white surface tones for content that sits on top of a full-bleed gradient. */
object OnGradient {
    val textPrimary = Color.White
    val textSecondary = Color.White.copy(alpha = 0.75f)
    val surface = Color.White.copy(alpha = 0.14f)
    val surfaceStrong = Color.White.copy(alpha = 0.22f)
    val outline = Color.White.copy(alpha = 0.35f)
}
