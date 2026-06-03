// com.example.ks1compose.presentation.ui.schedule.ScheduleScreen.kt
package com.example.ks1compose.presentation.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Room
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.ks1compose.data.DTOs.ScheduleResponse
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalButton
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalCard
import com.example.ks1compose.presentation.ui.profile.UserViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    scheduleViewModel: ScheduleViewModel,
    userViewModel: UserViewModel,
    userLogin: String,
    navController: NavController
) {
    val userInfo by userViewModel.userInfo.collectAsStateWithLifecycle()
    val scheduleResponse by scheduleViewModel.scheduleResponse.collectAsStateWithLifecycle()
    val isLoading by scheduleViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by scheduleViewModel.errorMessage.collectAsStateWithLifecycle()

    var selectedDay by remember { mutableStateOf(getCurrentDayOfWeek()) }
    var showDaySelector by remember { mutableStateOf(false) }

    val daysOfWeek = listOf(
        "Понедельник" to "Понедельник",
        "Вторник" to "Вторник",
        "Среда" to "Среда",
        "Четверг" to "Четверг",
        "Пятница" to "Пятница",
        "Суббота" to "Суббота"
    )

    // Загружаем информацию о пользователе
    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo()
    }

    // Загружаем расписание для ученика
    LaunchedEffect(userInfo?.uClass, selectedDay) {
        if (userInfo?.role == "student" && !userInfo?.uClass.isNullOrBlank()) {
            val formattedDay = selectedDay.replaceFirstChar { it.titlecase() }
            println("📱 Запрос расписания: класс=${userInfo?.uClass}, день='$formattedDay'")
            scheduleViewModel.getSchedule(userInfo!!.uClass, formattedDay)
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
            when (userInfo?.role) {
                "student" -> {
                    if (userInfo?.uClass.isNullOrBlank()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Класс не указан",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Обратитесь к администратору",
                                    color = Color.Gray.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        StudentScheduleContent(
                            userClass = userInfo!!.uClass,
                            selectedDay = selectedDay,
                            daysOfWeek = daysOfWeek,
                            scheduleResponse = scheduleResponse,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            onRefresh = {
                                val formattedDay = selectedDay.replaceFirstChar { it.titlecase() }
                                scheduleViewModel.getSchedule(userInfo!!.uClass, formattedDay)
                            }
                        )
                    }
                }
                "teacher" -> {
                    // Улучшенный экран для учителя
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Иконка
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = RoundedCornerShape(50.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Заголовок
                        Text(
                            text = "Расписание для учителей",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Имя учителя
                        Text(
                            text = userInfo?.let { "${it.name} ${it.sName}" } ?: "",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Информационное сообщение
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Здесь вы можете просмотреть ваше расписание уроков по дням недели",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Кнопка перехода
                        Button(
                            onClick = {
                                println("📱 Navigating to teacher_schedule")
                                navController.navigate("teacher_schedule")
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Открыть расписание",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Расписание недоступно",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
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
                                    selectedDay = day
                                    showDaySelector = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDay == day,
                                onClick = {
                                    selectedDay = day
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

@Composable
fun StudentScheduleContent(
    userClass: String,
    selectedDay: String,
    daysOfWeek: List<Pair<String, String>>,
    scheduleResponse: ScheduleResponse?,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Текущий день
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedDay,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = userClass,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Обновить",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        val schedules = scheduleResponse?.schedules ?: emptyList()
        val schedule = schedules.firstOrNull()

        if (errorMessage != null && schedule == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ошибка загрузки",
                    color = Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                PersonalButton(
                    text = "Повторить",
                    onClick = onRefresh,
                    widthFactor = 0.5f
                )
            }
        } else if (schedule == null && !isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                    text = "На этот день нет расписания",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Text(
                    text = selectedDay,
                    fontSize = 14.sp,
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            }
        } else if (schedule != null) {
            // Отображаем расписание
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(schedule.lessons.size) { index ->
                    ScheduleLessonCard(
                        lessonNumber = index + 1,
                        subject = schedule.lessons[index],
                        office = schedule.office.getOrNull(index) ?: "—"
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleLessonCard(
    lessonNumber: Int,
    subject: String,
    office: String
) {
    PersonalCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Номер урока
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = lessonNumber.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Информация об уроке
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = subject,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Кабинет
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Room,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = office,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun TeacherScheduleContent(
    teacherId: String,
    scheduleViewModel: ScheduleViewModel,
    userViewModel: UserViewModel
) {
    // Для учителя - показываем заглушку
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Расписание учителя",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Функция в разработке",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

fun getCurrentDayOfWeek(): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("ru"))
    return LocalDate.now().format(formatter).replaceFirstChar { it.titlecase() }
}