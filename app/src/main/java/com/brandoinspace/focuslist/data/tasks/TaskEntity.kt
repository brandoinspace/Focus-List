package com.brandoinspace.focuslist.data.tasks

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String = "My Task",
    var completed: Boolean = false,
    var listOrder: Int = 0,
)