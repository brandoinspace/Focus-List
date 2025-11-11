package com.brandoinspace.focuslist.data

import android.content.Context
import androidx.room.Room
import com.brandoinspace.focuslist.data.tasks.TasksDatabase
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
    fun providesAppDatabase(@ApplicationContext context: Context): TasksDatabase =
        Room.databaseBuilder(
            context,
            TasksDatabase::class.java,
            "tasks_database"
        ).fallbackToDestructiveMigration(true)
            .build()

    @Provides
    fun providesDao(database: TasksDatabase) = database.tasksDao()
}