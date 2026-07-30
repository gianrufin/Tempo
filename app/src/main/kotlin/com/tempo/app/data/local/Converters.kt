package com.tempo.app.data.local

import androidx.room.TypeConverter
import com.tempo.app.domain.model.HabitCompletionStatus
import com.tempo.app.domain.model.RecurrenceRule
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toInstant(millis: Long?): Instant? = millis?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): Int? = time?.toSecondOfDay()

    @TypeConverter
    fun toLocalTime(secondOfDay: Int?): LocalTime? = secondOfDay?.let(LocalTime::ofSecondOfDay)

    @TypeConverter
    fun fromLocalTimeList(times: List<LocalTime>): String =
        json.encodeToString(times.map { it.toSecondOfDay() })

    @TypeConverter
    fun toLocalTimeList(value: String): List<LocalTime> =
        json.decodeFromString<List<Int>>(value).map(LocalTime::ofSecondOfDay)

    @TypeConverter
    fun fromRecurrenceRule(rule: RecurrenceRule): String = json.encodeToString(rule)

    @TypeConverter
    fun toRecurrenceRule(value: String): RecurrenceRule = json.decodeFromString(value)

    @TypeConverter
    fun fromCompletionStatus(status: HabitCompletionStatus): String = status.name

    @TypeConverter
    fun toCompletionStatus(value: String): HabitCompletionStatus = HabitCompletionStatus.valueOf(value)
}
