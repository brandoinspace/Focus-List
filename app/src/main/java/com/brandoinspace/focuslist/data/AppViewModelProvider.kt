package com.brandoinspace.focuslist.data

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.brandoinspace.focuslist.MainApplication
import com.brandoinspace.focuslist.data.tasks.TasksViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            checkNotNull(this)
            val repository = MainApplication.container
            TasksViewModel(repository)
        }
    }
}