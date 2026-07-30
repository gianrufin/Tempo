package com.tempo.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.defaultWeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.material3.GlanceTheme
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.tempo.app.di.WidgetEntryPoint
import com.tempo.app.domain.model.HabitCompletionStatus
import com.tempo.app.domain.model.HabitWithTodayStatus
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class TempoWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        ).habitRepository()

        val habits = repository.observeHabitsForDate(LocalDate.now()).first()

        provideContent {
            GlanceTheme {
                TempoWidgetContent(habits)
            }
        }
    }
}

@Composable
private fun TempoWidgetContent(habits: List<HabitWithTodayStatus>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp),
    ) {
        Text(
            text = "Today",
            style = TextStyle(fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onBackground),
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        if (habits.isEmpty()) {
            Text(
                text = "No habits today",
                style = TextStyle(color = GlanceTheme.colors.onBackground),
            )
        } else {
            habits.take(6).forEach { item ->
                HabitWidgetRow(item)
                Spacer(modifier = GlanceModifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun HabitWidgetRow(item: HabitWithTodayStatus) {
    val done = item.status == HabitCompletionStatus.DONE
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(if (done) GlanceTheme.colors.primaryContainer else GlanceTheme.colors.surfaceVariant)
            .cornerRadius(20.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(actionRunCallback<ToggleHabitAction>(actionParametersOf(habitIdKey to item.habit.id))),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${item.habit.icon} ${item.habit.name}",
            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
            modifier = GlanceModifier.defaultWeight(),
        )
        Text(
            text = if (done) "✓" else "○",
            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontWeight = FontWeight.Bold),
        )
    }
}
