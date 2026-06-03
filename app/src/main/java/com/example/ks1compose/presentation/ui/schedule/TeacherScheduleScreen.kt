// com.example.ks1compose.presentation.ui.schedule.TeacherScheduleScreen.kt
package com.example.ks1compose.presentation.ui.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalCard
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalLoadingIndicator
import com.example.ks1compose.domain.models.LessonUIModel
import com.example.ks1compose.presentation.ui.profile.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScheduleScreen(
    lessonViewModel: LessonViewModel,
    userViewModel: UserViewModel
) {
    val userInfo by userViewModel.userInfo.collectAsStateWithLifecycle()
    val teacherLessons by lessonViewModel.teacherLessons.collectAsStateWithLifecycle()
    val isLoading by lessonViewModel.isLoading.collectAsStateWithLifecycle()
    val error by lessonViewModel.error.collectAsStateWithLifecycle()

    var selectedDay by remember { mutableStateOf<String?>(null) }
    var showDaySelector by remember { mutableStateOf(false) }

    // Маппинг дней недели с правильным порядком
    val daysOfWeek = listOf(
        "monday" to "Понедельник",
        "tuesday" to "Вторник",
        "wednesday" to "Среда",
        "thursday" to "Четверг",
        "friday" to "Пятница",
        "saturday" to "Суббота"
    )

    // Порядок дней для сортировки
    val dayOrder = mapOf(
        "monday" to 1,
        "tuesday" to 2,
        "wednesday" to 3,
        "thursday" to 4,
        "friday" to 5,
        "saturday" to 6,
        "sunday" to 7
    )

    // Загружаем расписание учителя
    LaunchedEffect(userInfo?.userId, selectedDay) {
        if (userInfo?.role == "teacher" && userInfo?.userId != null) {
            lessonViewModel.loadTeacherLessons(userInfo!!.userId, selectedDay)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDaySelector = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .size(56.dp)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = "Выбрать день",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && teacherLessons.isEmpty()) {
                PersonalLoadingIndicator()
            } else if (error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Ошибка загрузки",
                        color = Color.Red,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error!!,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        if (userInfo?.userId != null) {
                            lessonViewModel.loadTeacherLessons(userInfo!!.userId, selectedDay)
                        }
                    }) {
                        Text("Повторить")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Заголовок
                    Text(
                        text = "Расписание учителя",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = userInfo?.let { "${it.name} ${it.sName}" } ?: "",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (selectedDay != null) {
                        // Показываем выбранный день
                        DayHeader(
                            dayName = daysOfWeek.find { it.first == selectedDay }?.second ?: selectedDay!!,
                            onClearFilter = { selectedDay = null }
                        )
                    }

                    if (teacherLessons.isEmpty()) {
                        EmptyScheduleMessage(selectedDay)
                    } else {
                        // Фильтруем уроки по выбранному дню
                        val filteredLessons = if (selectedDay != null) {
                            teacherLessons.filter { it.dayOfWeek == selectedDay }
                        } else {
                            teacherLessons
                        }

                        // Группируем уроки по дням и сортируем дни
                        val lessonsByDay = filteredLessons
                            .groupBy { it.dayOfWeek ?: "unknown" }
                            .toList()
                            .sortedBy { (day, _) -> dayOrder[day] ?: 7 } // Сортировка дней

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            lessonsByDay.forEach { (day, lessons) ->
                                item {
                                    DayScheduleCard(
                                        dayName = daysOfWeek.find { it.first == day }?.second ?: day,
                                        lessons = lessons.sortedBy { it.lessonNumber } // Сортировка уроков по номеру
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог выбора дня
    if (showDaySelector) {
        AlertDialog(
            onDismissRequest = { showDaySelector = false },
            title = { Text("Выберите день") },
            text = {
                Column {
                    daysOfWeek.forEach { (day, dayName) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedDay = if (selectedDay == day) null else day
                                    showDaySelector = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDay == day,
                                onClick = {
                                    selectedDay = if (selectedDay == day) null else day
                                    showDaySelector = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = Color.Gray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dayName,
                                fontSize = 14.sp,
                                color = if (selectedDay == day)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            selectedDay = null
                            showDaySelector = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Показать все дни")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDaySelector = false }) {
                    Text("Готово")
                }
            }
        )
    }
}

// Остальные функции (DayHeader, DayScheduleCard, TeacherLessonItem, EmptyScheduleMessage) остаются без изменений
@Composable
fun DayHeader(
    dayName: String,
    onClearFilter: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dayName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(onClick = onClearFilter) {
                Text("Сбросить")
            }
        }
    }
}

@Composable
fun DayScheduleCard(
    dayName: String,
    lessons: List<LessonUIModel>
) {
    PersonalCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = dayName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            lessons.sortedBy { it.lessonNumber }.forEach { lesson ->
                TeacherLessonItem(lesson = lesson)
                if (lesson != lessons.last()) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.Gray.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun TeacherLessonItem(
    lesson: LessonUIModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Номер урока
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = lesson.lessonNumber.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Информация об уроке
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Используем className из модели
            val className = lesson.className ?: "Не указан"
            Text(
                text = "${lesson.subjectName} • $className",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.padding(top = 2.dp)
            ) {
                if (lesson.room != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Room,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = lesson.room,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (lesson.startTime != null && lesson.endTime != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${lesson.startTime} - ${lesson.endTime}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyScheduleMessage(
    selectedDay: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (selectedDay != null) "На этот день нет уроков" else "У вас пока нет уроков",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
    }
}