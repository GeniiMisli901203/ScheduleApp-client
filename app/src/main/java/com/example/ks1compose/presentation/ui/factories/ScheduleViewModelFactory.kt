package com.example.ks1compose.presentation.ui.factories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.ks1compose.presentation.ui.grades.GradeViewModel
import com.example.ks1compose.presentation.ui.schedule.LessonViewModel
import com.example.ks1compose.presentation.ui.news.NewsViewModel
import com.example.ks1compose.presentation.ui.schedule.ScheduleViewModel
import com.example.ks1compose.presentation.ui.profile.UserViewModel



class ScheduleViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class NewsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsViewModel(application.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LessonViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(LessonViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LessonViewModel(application.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class UserViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(application.applicationContext) as T  // Передаем context
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class GradeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(GradeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GradeViewModel(application.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}