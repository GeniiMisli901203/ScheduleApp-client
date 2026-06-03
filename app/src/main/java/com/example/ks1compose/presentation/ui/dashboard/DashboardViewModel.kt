package com.example.ks1compose.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ks1compose.domain.models.DashboardData
import com.example.ks1compose.presentation.ui.grades.GradeViewModel
import com.example.ks1compose.presentation.ui.schedule.LessonViewModel
import com.example.ks1compose.presentation.ui.news.NewsViewModel
import com.example.ks1compose.presentation.ui.profile.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val userViewModel: UserViewModel,
    private val gradeViewModel: GradeViewModel,
    private val lessonViewModel: LessonViewModel,
    private val newsViewModel: NewsViewModel
) : ViewModel() {

    private val _dashboardData = MutableStateFlow<DashboardData?>(null)
    val dashboardData: StateFlow<DashboardData?> = _dashboardData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true

            // Загружаем информацию о пользователе
            userViewModel.loadUserInfo()

            // Ждем немного для загрузки данных
            delay(500)

            val userInfo = userViewModel.userInfo.value

            if (userInfo != null) {
                // Для ученика загружаем оценки и расписание
                if (userInfo.role == "student") {
                    gradeViewModel.loadMyGrades()
                    lessonViewModel.loadTodayLessons(userInfo.uClass)
                }

                // Всегда загружаем новости
                newsViewModel.loadAllNews()

                // Собираем данные для дашборда
                val data = DashboardData(
                    userName = "${userInfo.name} ${userInfo.sName}",
                    userRole = when (userInfo.role) {
                        "student" -> "Ученик"
                        "teacher" -> "Учитель"
                        "admin" -> "Администратор"
                        else -> userInfo.role
                    },
                    className = if (userInfo.role == "student") userInfo.uClass else null,
                    todayLessons = lessonViewModel.todayLessons.value,
                    recentGrades = gradeViewModel.myGrades.value.take(5),
                    averageGrade = gradeViewModel.averageGrade.value,
                    newsCount = newsViewModel.allNews.value.size
                )

                _dashboardData.value = data
            }

            _isLoading.value = false
        }
    }
}