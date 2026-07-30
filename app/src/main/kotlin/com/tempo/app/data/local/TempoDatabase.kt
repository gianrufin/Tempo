package com.tempo.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tempo.app.data.local.dao.HabitCompletionDao
import com.tempo.app.data.local.dao.HabitDao
import com.tempo.app.data.local.entity.HabitCompletionEntity
import com.tempo.app.data.local.entity.HabitEntity

@Database(
    entities = [HabitEntity::class, HabitCompletionEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class TempoDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao

    companion object {
        const val DATABASE_NAME = "tempo.db"
    }
}
