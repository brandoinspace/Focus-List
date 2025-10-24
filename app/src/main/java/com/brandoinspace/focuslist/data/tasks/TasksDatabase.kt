package com.brandoinspace.focuslist.data.tasks

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TaskEntity::class], version = 2, exportSchema = false)
abstract class TasksDatabase : RoomDatabase() {
    abstract fun tasksDao(): TasksDao

    companion object {
        @Volatile
        private var Instance: TasksDatabase? = null

        fun getDatabase(context: Context): TasksDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    TasksDatabase::class.java,
                    "tasks_database"
                ).fallbackToDestructiveMigration(true)
                    .build()
                    .also {
                        Instance = it
                    }
            }
        }
    }
}