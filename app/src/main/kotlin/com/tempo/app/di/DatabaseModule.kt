package com.tempo.app.di

import android.content.Context
import androidx.room.Room
import com.tempo.app.data.local.TempoDatabase
import com.tempo.app.data.local.dao.HabitCompletionDao
import com.tempo.app.data.local.dao.HabitDao
import com.tempo.app.data.local.dao.RoutineDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTempoDatabase(@ApplicationContext context: Context): TempoDatabase =
        Room.databaseBuilder(context, TempoDatabase::class.java, TempoDatabase.DATABASE_NAME)
            // Pre-release app with no shipped users yet; simplest path through schema churn.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideHabitDao(database: TempoDatabase): HabitDao = database.habitDao()

    @Provides
    fun provideHabitCompletionDao(database: TempoDatabase): HabitCompletionDao = database.habitCompletionDao()

    @Provides
    fun provideRoutineDao(database: TempoDatabase): RoutineDao = database.routineDao()
}
