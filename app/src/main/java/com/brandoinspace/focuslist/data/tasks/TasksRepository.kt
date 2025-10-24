package com.brandoinspace.focuslist.data.tasks

import kotlinx.coroutines.flow.Flow

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

class TasksRepository(private val tasksDao: TasksDao) : TasksRepo {
    override fun getAllTasksStream(): Flow<List<TaskEntity>> = tasksDao.queryAllTasksStream()

    override fun getTaskStream(id: Int): Flow<TaskEntity?> = tasksDao.queryTask(id)

    override suspend fun insertTask(task: TaskEntity) = tasksDao.insert(task)

    override suspend fun deleteTask(task: TaskEntity) = tasksDao.delete(task)

    override suspend fun updateTask(task: TaskEntity) = tasksDao.update(task)
    override suspend fun numberOfIncompleteTasks(): Int = tasksDao.numberOfIncompleteTasks()
    override suspend fun numberOfTasks(): Int = tasksDao.numberOfTasks()
    override suspend fun dropTasks() = tasksDao.dropTasks()
    override suspend fun updateAllTasks(tasks: List<TaskEntity>) = tasksDao.updateAllTasks(tasks)
}