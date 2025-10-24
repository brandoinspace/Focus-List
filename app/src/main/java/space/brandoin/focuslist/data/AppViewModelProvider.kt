package space.brandoin.focuslist.data

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import space.brandoin.focuslist.MainApplication
import space.brandoin.focuslist.data.tasks.TasksViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            checkNotNull(this)
            val repository = MainApplication.container
            TasksViewModel(repository)
        }
    }
}