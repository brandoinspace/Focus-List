package com.brandoinspace.focuslist.data.tasks

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// TODO: https://developer.android.com/training/data-storage/room/async-queries#observable
@Dao
interface TasksDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("SELECT * from tasks WHERE id = :id")
    fun queryTask(id: Int): Flow<TaskEntity>

    @Query("SELECT * from tasks")
    fun queryAllTasksStream(): Flow<List<TaskEntity>>

    @Query("SELECT count() from tasks WHERE completed = 0")
    suspend fun numberOfIncompleteTasks(): Int

    @Query("SELECT count() from tasks")
    suspend fun numberOfTasks(): Int

    @Query("DELETE from tasks")
    suspend fun dropTasks()

    @Update
    suspend fun updateAllTasks(tasks: List<TaskEntity>)
}