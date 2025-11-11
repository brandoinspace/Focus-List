package com.brandoinspace.focuslist.data.tasks

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.brandoinspace.focuslist.widget.TaskWidget
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface TasksRepo {
    fun getAllTasksStream(): Flow<List<TaskEntity>>

    fun getTaskStream(id: Int): Flow<TaskEntity?>

    suspend fun insertTask(task: TaskEntity)

    suspend fun deleteTask(task: TaskEntity)

    suspend fun updateTask(task: TaskEntity)
    suspend fun numberOfIncompleteTasks(): Int
    suspend fun numberOfTasks(): Int
    suspend fun dropTasks()
    suspend fun updateAllTasks(tasks: List<TaskEntity>)
}

// https://youtu.be/CliaZtp2i9k?si=qfTYRHmGaNdOROWP
class TasksRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val tasksDao: TasksDao
) : TasksRepo {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TasksRepositoryEntryPoint {
        fun tasksRepository(): TasksRepository
    }

    companion object {
        fun get(applicationContext: Context): TasksRepository {
            val entryPoint: TasksRepositoryEntryPoint = EntryPoints.get(
                applicationContext,
                TasksRepositoryEntryPoint::class.java
            )
            return entryPoint.tasksRepository()
        }
    }

    override fun getAllTasksStream(): Flow<List<TaskEntity>> = tasksDao.queryAllTasksStream()

    override fun getTaskStream(id: Int): Flow<TaskEntity?> = tasksDao.queryTask(id)

    override suspend fun insertTask(task: TaskEntity) = tasksDao.insert(task)

    override suspend fun deleteTask(task: TaskEntity) = tasksDao.delete(task)

    override suspend fun updateTask(task: TaskEntity) = tasksDao.update(task)
    override suspend fun numberOfIncompleteTasks(): Int = tasksDao.numberOfIncompleteTasks()
    override suspend fun numberOfTasks(): Int = tasksDao.numberOfTasks()
    override suspend fun dropTasks() = tasksDao.dropTasks()
    override suspend fun updateAllTasks(tasks: List<TaskEntity>) = tasksDao.updateAllTasks(tasks)

    suspend fun updateWidget() {
        TaskWidget().updateAll(appContext)
    }
}