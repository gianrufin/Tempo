package com.tempo.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

/** Called after anything that changes habit data or completion state, so the widget stays live. */
object WidgetRefresher {
    suspend fun refresh(context: Context) {
        TempoWidget().updateAll(context)
    }
}
