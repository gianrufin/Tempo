package com.tempo.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tempo.app.data.local.dao.HabitCompletionDao
import com.tempo.app.data.local.dao.HabitDao
import com.tempo.app.data.local.dao.RoutineDao
import com.tempo.app.data.local.entity.HabitCompletionEntity
import com.tempo.app.data.local.entity.HabitEntity
import com.tempo.app.data.local.entity.RoutineEntity

@Database(
    entities = [HabitEntity::class, HabitCompletionEntity::class, RoutineEntity::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class TempoDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun routineDao(): RoutineDao

    companion object {
        const val DATABASE_NAME = "tempo.db"
    }
}
