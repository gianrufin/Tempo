package com.tempo.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * AMOLED-friendly gradient identities: mostly true black with a faint tonal shift, rather than a
 * fully saturated hero gradient. Keeps a distinct "vibe" per section without fighting an AMOLED
 * panel's power/contrast benefits or reading as a bright/loud background.
 */
object TempoGradients {
    val home = Brush.linearGradient(
        colors = listOf(Color(0xFF060005), Color(0xFF120A14), Color(0xFF0A0610)),
    )

    val reflect = Brush.linearGradient(
        colors = listOf(Color(0xFF070000), Color(0xFF150A06), Color(0xFF0A0508)),
    )

    val detail = Brush.linearGradient(
        colors = listOf(Color(0xFF000103), Color(0xFF0A1018), Color(0xFF000000)),
    )

    val calm = Brush.linearGradient(
        colors = listOf(Color(0xFF08050F), Color(0xFF120A1C), Color(0xFF050308)),
    )
}

/** Translucent-white surface tones for content that sits on top of a full-bleed gradient. */
object OnGradient {
    val textPrimary = Color.White
    val textSecondary = Color.White.copy(alpha = 0.7f)
    val surface = Color.White.copy(alpha = 0.08f)
    val surfaceStrong = Color.White.copy(alpha = 0.16f)
    val outline = Color.White.copy(alpha = 0.25f)
}
